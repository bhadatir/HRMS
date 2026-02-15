import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useLogin } from "../hooks/hook";

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({ email: "", password: "" });

  const loginMutation = useLogin();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    loginMutation.mutate(formData, {
      onSuccess: (data) => {
        login(data.accessToken, formData.email);
        navigate("/dashboard");
      },
      onError: (error: any) => {
        alert("Login Error: " + (error.response?.data || error.message));
      }
    });
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-slate-100 p-4 w-318">
      <Card className="w-full max-w-md shadow-xl border-t-4 border-blue-600">
        <CardHeader className="space-y-1">
          <CardTitle className="text-3xl font-bold text-center">Login</CardTitle>
          <CardDescription className="text-center">
            Enter your email and password to access your account
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleLogin} className="space-y-4">
            <div className="space-y-2">
              <Input 
                type="email"
                placeholder="Email (e.g. name@company.com)" 
                required
                className="h-11"
                onChange={(e) => setFormData({...formData, email: e.target.value})} 
              />
            </div>
            <div className="space-y-2">
              <Input 
                type="password" 
                placeholder="Password" 
                required
                className="h-11"
                onChange={(e) => setFormData({...formData, password: e.target.value})} 
              />
            </div>
            
            <Button 
              type="submit"
              className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-black font-medium transition-colors" 
              disabled={loginMutation.isPending}
            >
              {loginMutation.isPending ? "Authenticating..." : "Sign In"}
            </Button>

            <div className="text-sm text-center space-y-4 pt-2">
               <Link 
                 to="/reset-password" 
                 className="text-blue-600 hover:underline block transition-all"
               >
                 Forgot Password?
               </Link>
               
               <div className="relative">
                  <div className="absolute inset-0 flex items-center"><span className="w-full border-t" /></div>
                  <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-white px-2 text-muted-foreground font-semibold">Or</span>
                  </div>
               </div>

               <p className="text-gray-600">
                 Don't have an account?{" "}
                 <Link to="/signup" className="text-blue-600 font-bold hover:underline">
                   Sign Up
                 </Link>
               </p>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

import { useState } from "react";
import { Link } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { apiService } from "../api/apiService";
import { MailCheck, ShieldCheck } from "lucide-react"; 

export default function ResetPassword() {
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState("");
  const [details, setDetails] = useState({ token: "", newPassword: "" });

  const forgotMutation = useMutation({
    mutationFn: () => apiService.forgotPassword(email),
    onSuccess: () => setStep(2),
    onError: (error: any) => alert("Error: " + (error.response?.data || "Failed to send email")),
  });

  const resetMutation = useMutation({
    mutationFn: () => apiService.resetPassword(details),
    onSuccess: () => {
      alert("Password updated successfully!");
      setStep(3);
    },
    onError: (error: any) => alert("Update failed: " + (error.response?.data || "Invalid Token")),
  });

  return (
    <div className="flex items-center justify-center min-h-screen bg-slate-100 p-4 w-318">
      <Card className="w-full max-w-md shadow-2xl border-t-4 border-blue-600 transition-all duration-300">
        <CardHeader className="text-center">
          <div className="mx-auto bg-blue-50 w-12 h-12 rounded-full flex items-center justify-center mb-2">
            {step === 1 && <MailCheck className="text-blue-600" />}
            {step === 2 && <ShieldCheck className="text-blue-600" />}
            {step === 3 && <ShieldCheck className="text-green-600" />}
          </div>
          <CardTitle className="text-2xl font-bold">
            {step === 1 && "Forgot Password"}
            {step === 2 && "Reset Password"}
            {step === 3 && "All Set!"}
          </CardTitle>
          <CardDescription>
            {step === 1 && "Enter your email to receive a secure reset token."}
            {step === 2 && "Check your email and enter the token below."}
            {step === 3 && "Your password has been successfully updated."}
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-4">
          {step === 1 && (
            <div className="space-y-4">
              <Input 
                type="email" 
                placeholder="work-email@company.com" 
                className="h-11"
                value={email}
                onChange={(e) => setEmail(e.target.value)} 
              />
              <Button 
                className="w-full h-11 text-black font-semibold"
                disabled={forgotMutation.isPending || !email}
                onClick={() => forgotMutation.mutate()}
              >
                {forgotMutation.isPending ? "Sending..." : "Send Reset Link"}
              </Button>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-4">
              <Input 
                placeholder="Enter Token" 
                className="h-11 text-center tracking-widest font-bold"
                onChange={(e) => setDetails({ ...details, token: e.target.value })} 
              />
              <Input 
                type="password" 
                placeholder="New Password" 
                className="h-11"
                onChange={(e) => setDetails({ ...details, newPassword: e.target.value })} 
              />
              <Button 
                className="w-full h-11 text-black font-semibold"
                disabled={resetMutation.isPending || !details.token || !details.newPassword}
                onClick={() => resetMutation.mutate()}
              >
                {resetMutation.isPending ? "Updating..." : "Confirm New Password"}
              </Button>
              <Button variant="ghost" className="w-full text-xs" onClick={() => setStep(1)}>
                Didn't get a code? Resend
              </Button>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-4">
              <Link to="/login">
                <Button className="w-full h-11 bg-green-600 hover:bg-green-700 font-semibold">
                  Back to Login
                </Button>
              </Link>
            </div>
          )}

        </CardContent>
      </Card>
    </div>
  );
}
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useRegister } from "../hooks/hook";

export default function Signup() {
  const navigate = useNavigate();
  const registerMutation = useRegister();

  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    dob: "",
    gender: "male",
    hireDate: "",
    salary: 0,
    departmentId: 1,
    positionId: 1,
    roleId: 1
  });

  const handleSignup = (e: React.FormEvent) => {
    e.preventDefault();
    registerMutation.mutate(form, {
      onSuccess: () => {
        alert("Registration Successful! Please login.");
        navigate("/login");
      },
      onError: (error: any) => {
        alert("Registration failed: " + (error.response?.data || error.message));
      }
    });
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-slate-100 p-4 md:p-8 w-314">
      <Card className="w-full max-w-3xl shadow-2xl border-t-4 border-blue-600">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-bold">Create Employee Account</CardTitle>
          <CardDescription>Enter the details to register a new member in the system.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSignup} className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            
            <div className="space-y-4 md:col-span-2 border-b pb-2">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Personal Information</h3>
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">First Name</label>
              <Input required placeholder="John" onChange={e => setForm({...form, firstName: e.target.value})} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Last Name</label>
              <Input required placeholder="Doe" onChange={e => setForm({...form, lastName: e.target.value})} />
            </div>
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">Email Address</label>
              <Input required type="email" placeholder="john.doe@company.com" onChange={e => setForm({...form, email: e.target.value})} />
            </div>
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">Password</label>
              <Input required type="password" placeholder="••••••••" onChange={e => setForm({...form, password: e.target.value})} />
            </div>

            <div className="space-y-4 md:col-span-2 border-b pb-2 pt-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Employment Details</h3>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Date of Birth</label>
              <Input required type="date" max={new Date().toISOString().split("T")[0]} onChange={e => setForm({...form, dob: e.target.value})} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Hire Date</label>
              <Input required type="date" onChange={e => setForm({...form, hireDate: e.target.value})} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Gender</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                onChange={e => setForm({...form, gender: e.target.value})}
              >
                <option value="male">Male</option>
                <option value="female">Female</option>
                <option value="other">Other</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Annual Salary</label>
              <Input required type="number" placeholder="0.00" onChange={e => setForm({...form, salary: parseFloat(e.target.value)})} />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Department</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm"
                onChange={e => setForm({...form, departmentId: parseInt(e.target.value)})}
              >
                <option value={1}>Engineering</option>
                <option value={2}>Data Science</option>
                <option value={3}>AI/ML</option>
                <option value={4}>Design</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Role</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm"
                onChange={e => setForm({...form, roleId: parseInt(e.target.value)})}
              >
                <option value={1}>Employee</option>
                <option value={2}>Manager</option>
                <option value={3}>HR</option>
                <option value={4}>Admin</option>
              </select>
            </div>

            <Button 
              type="submit" 
              className="md:col-span-2 w-full h-11 bg-blue-600 hover:bg-blue-700 text-black font-bold mt-4"
              disabled={registerMutation.isPending}
            >
              {registerMutation.isPending ? "Registering Members..." : "Complete Registration"}
            </Button>

            <p className="md:col-span-2 text-center text-sm text-gray-600">
              Already have an account?{" "}
              <Link to="/login" className="text-blue-600 font-bold hover:underline">Log In</Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

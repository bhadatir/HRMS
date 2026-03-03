import { useAuth } from "../context/AuthContext";
import { useRef, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Mail, Calendar, Building, X, Bell, IndianRupee, PenIcon, Pencil } from "lucide-react";
import Notifications from "../components/Notifications.tsx";
import { Button } from "@/components/ui/button";
import {
  SidebarInset,
  SidebarProvider,
} from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { Input } from "@/components/ui/input.tsx";
import { apiService } from "@/api/apiService.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export default function Dashboard() {
  const { setIsFirstLogin, token, isFirstLogin, user, unreadNotifications } = useAuth();
  const [showNotification, setShowNotification] = useState(false);
  const [newPassword, setNewPassword] = useState(""); 
  const fileInputRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <p className="text-gray-500">Failed to load user profile.</p>
      </div>
    );
  }

  const profilePicMutation = useMutation({
    mutationFn: async (file: File) => {
    
      const formData = new FormData();
      formData.append("file", file);
  
      return apiService.updateProfileImage(user.id, formData, token || "");
    },
    onSuccess: () => {
      alert("Profile picture updated!");
      queryClient.invalidateQueries({ queryKey: ["user"] });
    },
    onError: (err: any) => alert(err.message)
  });

  const updatePasswordMutation = useMutation({
    mutationFn: () => apiService.updatePassword(user.id, newPassword, token || ""),
    onSuccess: () => {
      alert("Password updated successfully!");
      setIsFirstLogin("no");
    },
    onError: (error: any) => {
      alert("Failed to update password: " + (error.response?.data || error.message));
    }
  });

  return (
    isFirstLogin === "yes" ? (
      <div className="w-314">
        <Card className="w-full max-w-md shadow-xl border-t-4 border-blue-600 mx-auto">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold text-center">Update Your Password!</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-center text-gray-600">Since this is your first login, please set a new password to secure your account.</p>
            <p>email : {user.employeeEmail}</p>
            <Input 
              type="password" 
              placeholder="New Password" 
              className="h-11"
              onChange={(e) => setNewPassword(e.target.value)} 
            />
            <Button 
              className="w-full h-11 text-black font-semibold"
              disabled={updatePasswordMutation.isPending || !newPassword}
              onClick={() => updatePasswordMutation.mutate()}
            >
              {updatePasswordMutation.isPending ? "Updating..." : "Confirm New Password"}
            </Button>
          </CardContent>
        </Card>
      </div>
    ) : (
      <SidebarProvider>
        <AppSidebar />
        <SidebarInset className="bg-slate-50">
          <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
            
            <div className="flex items-center gap-2">
              {/* <SidebarTrigger /> */}
              <h3 className="text-lg font-bold text-slate-800">My Dashboard</h3>
            </div>
            <div className="relative inline-block">
              <Bell 
                size={25} 
                onClick={() => setShowNotification(true)} 
                className="text-gray-600 cursor-pointer hover:text-blue-600 transition-colors"
              />
              {unreadNotifications > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center">
                  {unreadNotifications}
                </span>
              )}
            </div>
          </header>
          <main className="p-6 max-w-7xl mx-auto space-y-6 w-250">
            
            {/* Notifications */}
            {showNotification && (
              <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-xl max-w-lg w-full relative h-150 overflow-y-auto">
                  <Button variant="ghost" className="absolute right-2 top-2" onClick={() => {
                    setShowNotification(false);
                  }}><X /></Button>
                  <Notifications />
                </div>
              </div>
            )}

            <div className="rounded p-8 text-black">
              <div className="flex flex-col md:flex-row items-center gap-6">
                <Avatar title="edit profile image" className="h-16 w-16 cursor-pointer" onClick={() => fileInputRef.current?.click()}>
                  <AvatarImage src={user.employeeProfileUrl} />
                  <AvatarFallback className="bg-blue-600 text-white">
                    {user.employeeFirstName.charAt(0)}{user.employeeLastName.charAt(0)}
                  </AvatarFallback>
                  <Input type="file" className="hidden" onChange={(e) => e.target.files && profilePicMutation.mutate(e.target.files[0])} ref={fileInputRef} />
                  <Pencil className="absolute bottom-0 right-0 bg-white rounded-full p-1 text-gray-600" />                  
                </Avatar>
                <div className="text-center md:text-left">
                  <h1 className="text-3xl font-bold">{user.employeeFirstName} {user.employeeLastName}</h1>
                  <div className="flex flex-wrap justify-center md:justify-start gap-2 mt-2">
                      <Badge variant="outline" className="text-slate-500 border-slate-700">
                      {user.roleName || "Employee"}
                    </Badge>
                  </div>
                </div>
              </div>
              
              <div className="absolute top-[-10%] right-[-5%] w-64 h-64 bg-blue-500/10 rounded-full blur-3xl"></div>
            </div>


            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              
              
              <Card className="shadow-sm border-slate-200">
                <CardHeader className="flex flex-row items-center space-x-2 pb-2">
                  <Mail className="w-4 h-4 text-blue-600" />
                  <CardTitle className="text-sm font-medium">Contact Information</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-gray-500">Primary Email</p>
                  <p className="text-base font-semibold truncate">{user.employeeEmail}</p>
                </CardContent>
              </Card>


              <Card className="shadow-sm border-slate-200">
                <CardHeader className="flex flex-row items-center space-x-2 pb-2">
                  <Building className="w-4 h-4 text-blue-600" />
                  <CardTitle className="text-sm font-medium">Department & Position</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="text-sm text-gray-500">Department</p>
                      <p className="text-base font-semibold">{user.departmentName || "N/A"}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>


              <Card className="shadow-sm border-slate-200">
                <CardHeader className="flex flex-row items-center space-x-2 pb-2">
                  <IndianRupee className="w-4 h-4 text-green-600" />
                  <CardTitle className="text-sm font-medium">Salary Overview</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-gray-500">Annual Gross</p>
                  <p className="text-xl font-bold text-slate-900">
                    {user.employeeSalary?.toLocaleString()}
                  </p>
                </CardContent>
              </Card>
            </div>


            <Card>
              <CardHeader>
                <CardTitle className="text-lg flex items-center gap-2">
                  <Calendar className="w-5 h-5 text-blue-600" />
                  Employment Timeline
                </CardTitle>
              </CardHeader>
              <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="flex gap-4 items-start">
                  <div className="p-2 bg-slate-100 rounded-lg">🎂</div>
                  <div>
                    <p className="text-sm font-medium text-gray-500">Date of Birth</p>
                    <p className="text-base">{new Date(user.employeeDob).toLocaleDateString()}</p>
                  </div>
                </div>
                <div className="flex gap-4 items-start">
                  <div className="p-2 bg-slate-100 rounded-lg">🏢</div>
                  <div>
                    <p className="text-sm font-medium text-gray-500">Date of Joining</p>
                    <p className="text-base">{new Date(user.employeeHireDate).toLocaleDateString()}</p>
                  </div>
                </div>
              </CardContent>
            </Card>

          </main>
        </SidebarInset>
      </SidebarProvider>
    )
  );
}


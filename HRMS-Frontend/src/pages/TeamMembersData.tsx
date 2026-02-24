import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { X, Bell, Search} from "lucide-react";
import Notifications from "@/components/Notifications.tsx";
import { apiService } from "@/api/apiService.ts";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import PostManagement from "./PostManagement";
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";

export default function TeamMemberData() {
  const { token, user, unreadNotifications } = useAuth();
  const [showNotification, setShowNotification] = useState(false);
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState("");

  const { data: orgData, isLoading, isError } = useQuery({
    queryKey: ["orgChart", user?.id],
    queryFn: () => apiService.fetchOrgChart(user?.id || 0, token || ""),
    enabled: !!user?.id,
  });

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            {/* <SidebarTrigger /> */}
            <h3 className="text-lg font-bold text-slate-800">Team Members Data</h3>
          </div>

          <div className="relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search by title or department..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
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
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowNotification(false);
                }}><X /></Button>
                <Notifications />
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {orgData?.directReports.map((org: any) => (
                searchTerm === "" || 
                org.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                org.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                org.employeeEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
                org.positionName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                org.departmentName.toLowerCase().includes(searchTerm.toLowerCase())) && (
                <Card 
                  key={org.id} 
                  className="border-slate-200"
                >
                  <CardHeader className="flex flex-row items-center gap-4">
                    <Avatar className="h-16 w-16 border-2 border-black-500">
                      <AvatarImage src={org.employeeProfileUrl} />
                      <AvatarFallback className="bg-blue-600 text-white">
                        {org.firstName.charAt(0)}{org.lastName.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <CardTitle className="text-xl font-bold">{org.firstName} {org.lastName}</CardTitle>
                      <p className="text-blue-600 font-bold text-sm">{org.employeeEmail}</p>
                    </div>
                  </CardHeader>
                  
                  <CardContent className="space-y-4">
                    <div className="flex items-center gap-2"></div>
                    <div className="space-y-1">
                        <div><span className="font-semibold">Position:</span> {org.positionName}</div>
                        <div><span className="font-semibold">Department:</span> {org.departmentName}</div> 
                    </div> 
                    <Button variant="outline" className="w-full" onClick={() => {
                      navigate(`/post-management?employeeEmail=${org.employeeEmail}`);
                      }
                    }>
                      View Achievements
                    </Button>                   
                  </CardContent>
                </Card>
            ))}
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

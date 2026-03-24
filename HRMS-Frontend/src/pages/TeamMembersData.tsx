import { useEffect, useState } from "react";
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
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { useAppDebounce } from "@/hooks/useAppDebounce";
import { ScrollToTop } from "@/components/ScrollToTop";
import { GlobalSearch } from "@/components/GlobalSearch";
import { useToast } from "@/context/ToastContext";

type OrgData = {
  id: number;
  employeeId: number;
  firstName: string;
  lastName: string;
  employeeEmail: string;
  positionName: string;
  departmentName: string;
  employeeProfileUrl: string;
};

type OrgDataDirectReport = {
  id: number;
  employeeId: number;
  firstName: string;
  lastName: string;
  employeeEmail: string;
  positionName: string;
  departmentName: string;
  employeeProfileUrl: string;
};

export default function TeamMemberData() {
  const toast = useToast();
  const { token, user, unreadNotifications } = useAuth();
  const [showNotification, setShowNotification] = useState(false);
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState(() => {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get("teamMemberId") || "";
  });
  const debouncedSearch = useAppDebounce(searchTerm);

  const { data: orgData, isError: orgDataError } = useQuery({
    queryKey: ["orgChart", user?.id],
    queryFn: () => apiService.fetchOrgChart(user?.id || 0, token || ""),
    enabled: !!user?.id,
  });

  const filteredData = orgData?.directReports.filter((org: OrgDataDirectReport) =>
    debouncedSearch === "" || 
    org.employeeId.toString().includes(debouncedSearch) ||
    org.firstName.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    org.lastName.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    org.employeeEmail.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    org.positionName.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    org.departmentName.toLowerCase().includes(debouncedSearch.toLowerCase())
  );  
    
  useEffect(() => {
    const clickOutside = (e: MouseEvent) => {
    const target = e.target as HTMLElement;
    if (!target.closest("div.manager")) {
        setShowNotification(false);
    }
    };
    if (showNotification) {
    document.addEventListener("click", clickOutside);
    } else {
    document.removeEventListener("click", clickOutside);
    }
    return () => document.removeEventListener("click", clickOutside);
  }, [showNotification]);

  if (orgDataError) {
    toast?.error("Failed to load organization data: " + orgDataError);
  }

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            {/* <SidebarTrigger /> */}
            <h3 className="text-lg font-bold text-slate-800">Team Members Data</h3>
          </div>

          <div className="manager relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search by title or department..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              autoFocus
            />
          </div>
          
          <div className="manager relative inline-block">
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
              <div className="manager bg-white rounded-xl max-w-3xl w-full relative h-150 overflow-y-auto">
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowNotification(false);
                }}><X /></Button>
                <Notifications />
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredData?.map((org: OrgData) => (
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
            {filteredData?.length === 0 && (
              <div className="col-span-full text-center text-gray-500">
                No team members found matching "{debouncedSearch}"
              </div>
            )}
          </div>
          
          <ScrollToTop />
          <GlobalSearch />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

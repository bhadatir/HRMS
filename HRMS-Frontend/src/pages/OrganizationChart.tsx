
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useQuery } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Breadcrumb, BreadcrumbItem, BreadcrumbList, BreadcrumbSeparator } from "@/components/ui/breadcrumb";
import { Badge } from "@/components/ui/badge";
import { ChevronDown } from "lucide-react";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Search, User, Bell, X } from "lucide-react";
import Notifications from "../components/Notifications.tsx";
import { useInView } from "react-intersection-observer";
import { useEmployeeSearch } from "../hooks/useInfinite";
import { ScrollToTop } from "@/components/ScrollToTop.tsx";
import { GlobalSearch } from "@/components/GlobalSearch.tsx";
import { useToast } from "@/context/ToastContext.tsx";

type Employee = {
  id: number;
  employeeFirstName: string;
  employeeLastName: string;
};

type OrganizationManager = {
  employeeId: number;
  firstName: string;
  lastName: string;
  positionName: string;
};

type OrganizationReport = {
  employeeId: number;
  firstName: string;
  lastName: string;
  positionName: string;
  employeeProfileUrl: string;
};

export default function OrganizationChart() {
  const toast = useToast(); 
  const { token, unreadNotifications } = useAuth();
  const [selectedId, setSelectedId] = useState(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const employeeId = urlParams.get("employeeId");
    return employeeId ? parseInt(employeeId) : 2;
  });
  const [searchTerm, setSearchTerm] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);  
  const [showNotification, setShowNotification] = useState(false);
  
  const { 
    data: infiniteData, 
    fetchNextPage, 
    hasNextPage, 
    isFetchingNextPage,
    isError: searchError 
  } = useEmployeeSearch(searchTerm, 0, token || "");
  const suggestions = infiniteData?.pages.flatMap(page => page.content) || [];

  const { data: orgData, isLoading, isError: orgDataError } = useQuery({
    queryKey: ["orgChart", selectedId],
    queryFn: () => apiService.fetchOrgChart(selectedId, token || ""),
    enabled: !!selectedId,
  });

  const { ref, inView } = useInView();
  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage]);

  const handleSelectUser = (id: number) => {
    setSelectedId(id);
    setSearchTerm(""); 
    setShowDropdown(false);
  };

  useEffect(() => {
    const clickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest("div.relative")) {
        setShowDropdown(false);
        setShowNotification(false);
      }
    };
    if (showDropdown || showNotification) {
      document.addEventListener("click", clickOutside);
    } else {
      document.removeEventListener("click", clickOutside);
    }
    return () => document.removeEventListener("click", clickOutside);
  }, [showDropdown, showNotification]);

  if(searchError || orgDataError) {
    toast?.error("Failed to load organization data: " + (searchError || orgDataError));
  }

  return (
    <SidebarProvider className="w-full">
      <AppSidebar />
      <SidebarInset className="flex flex-1 flex-col w-full bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-20">
          <div className="flex items-center gap-2">
            <SidebarTrigger />
            <h3 className="text-lg font-bold">Managerial Chain</h3>
          </div>

          <div className="relative max-w-sm w-full px-2">
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
              <Input 
                placeholder="Search by name (e.g. 'ti')..." 
                className="pl-9"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setShowDropdown(true);
                }}
                onFocus={() => setShowDropdown(true)}
              />
            </div>

            {showDropdown && suggestions.length > 0 && (
              <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-60 overflow-y-auto">
                {suggestions.map((emp: Employee) => (
                  <button
                    key={emp.id}
                    className="users w-full text-left px-4 py-3 flex items-center gap-3 border-b last:border-none"
                    onClick={() => handleSelectUser(emp.id)}
                  >
                    <div className="bg-gray-100 p-1.5 rounded-full">
                      <User size={14} className="text-gray-600" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{emp.employeeFirstName} {emp.employeeLastName}</p>
                    </div>
                  </button>
                ))}

                <div ref={ref} className="h-10 flex justify-center items-center">
                  {isFetchingNextPage ? <p className="text-xs">Loading more...</p> : null}
                </div>
              </div>
            )}
          </div>
          <div className="relative inline-block">
            <Bell 
              size={25} 
              onClick={() => setShowNotification(true)} 
              className="text-gray-600 cursor-pointer hover:text-gray-600 transition-colors"
            />
            {unreadNotifications > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center">
                {unreadNotifications}
              </span>
            )}
          </div>
        </header>

        {/* Notifications */}
        {showNotification && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-xl max-w-3xl w-full relative max-h-150 overflow-y-auto">
              <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                setShowNotification(false);
              }}><X /></Button>
              <Notifications />
            </div>
          </div>
        )}


        <div className="p-6 space-y-8 w-full">
          {isLoading ? (
            <div className="p-10 text-center text-slate-500 font-medium">Loading employee data...</div>
          ) : orgDataError ? (
            <div className="p-10 text-center text-red-500 font-medium">Employee data could not be retrieved.</div>
          ) : (
            <>
              <section className="bg-white p-4 rounded-lg border shadow-sm">
                <Breadcrumb>
                  <BreadcrumbList>
                    {orgData?.managerChain?.map((manager: OrganizationManager) => (
                      <div key={manager.employeeId} className="flex items-center">
                        <BreadcrumbItem>
                          <button 
                            title = {`View ${manager.firstName} ${manager.lastName}'s profile`}
                            onClick={() => setSelectedId(manager.employeeId)}
                            className="text-sm font-semibold text-slate-600 hover:text-gray-600"
                          >
                            {manager.firstName} {manager.lastName}
                            <span className="block text-[10px] font-normal text-slate-400">{manager.positionName}</span>
                          </button>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator className="mx-2" />
                      </div>
                    ))}
                    </BreadcrumbList>
                </Breadcrumb>
              </section>
              <section className="flex flex-col items-center">
                <Card className="w-full max-w-sm border-2 border-gray-500 shadow-xl ring-4 ring-gray-50">
                  <CardHeader className="flex flex-row items-center gap-4">
                    <Avatar className="h-16 w-16">
                      <AvatarImage src={orgData.employeeProfileUrl} />
                      <AvatarFallback className="bg-gray-600 text-white">
                        {orgData.firstName.charAt(0)}{orgData.lastName.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <CardTitle className="text -xl font -bold">{orgData.firstName} {orgData.lastName}</CardTitle>
                      <p className="text-gray-600 font-bold text-sm uppercase">{orgData.positionName}</p>
                    </div>
                  </CardHeader>
                  <CardContent className="bg-slate-50/50 border-t p-4 flex justify-between items-center">
                    <p className="text-xs font-medium text-slate-500">Department: <span className="text-slate-900">{orgData.departmentName}</span></p>
                    
                    <Badge variant="secondary" className="bg-gray-100 text-gray-700">{orgData.employeeEmail}</Badge>
                  </CardContent>
                </Card>
              </section>

              <section>
                <div className="flex items-center gap-2 mb-6">
                  <ChevronDown className="text-slate-400" />
                  <h3 className="text-lg font-bold text-slate-700">Direct Reports</h3>
                </div>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
                  {orgData.directReports?.map((report: OrganizationReport) => (
                    <Card 
                      key={report.employeeId} 
                      className="cursor-pointer hover:border-gray-400 transition-all"
                      onClick={() => setSelectedId(report.employeeId)}
                    >
                      <CardContent className="pt-6 flex flex-col items-center text-center gap-2">
                        <Avatar>
                          <AvatarImage src={report.employeeProfileUrl} />
                          <AvatarFallback>{report.firstName.charAt(0)}{report.lastName.charAt(0)}</AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-bold text-sm">{report.firstName} {report.lastName}</p>
                          <p className="text-[10px] text-gray-600 font-bold uppercase">{report.positionName}</p>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </section>
            </>
          )}
          
          <ScrollToTop />
          <GlobalSearch />
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}










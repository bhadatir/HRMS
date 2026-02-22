
import { useState, useEffect } from "react";
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

export default function OrganizationChart() {
  const { token, unreadNotifications } = useAuth(); 
  const [selectedId, setSelectedId] = useState(2);
  const [searchTerm, setSearchTerm] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);  
  const [showNotification, setShowNotification] = useState(false);

  const { data: suggestions } = useQuery({
    queryKey: ["employeeSearch", searchTerm],
    queryFn: () => apiService.searchEmployees(searchTerm, token || ""),
    enabled: searchTerm.length >= 1,
  });

  const { data: orgData, isLoading, isError } = useQuery({
    queryKey: ["orgChart", selectedId],
    queryFn: () => apiService.fetchOrgChart(selectedId, token || ""),
    enabled: !!selectedId,
  });

  const handleSelectUser = (id: number) => {
    setSelectedId(id);
    setSearchTerm(""); 
    setShowDropdown(false);
  };

  return (
    <SidebarProvider className="w-full">
      <AppSidebar />
      <SidebarInset className="flex flex-1 flex-col w-full bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-20">
          <div className="flex items-center gap-2">
            <SidebarTrigger />
            <h3 className="text-lg font-bold">Managerial Chain</h3>
          </div>

          <div className="relative max-w-sm w-full">
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

            {showDropdown && suggestions && suggestions.length > 0 && (
              <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-60 overflow-auto">
                {suggestions.map((emp: any) => (
                  <button
                    key={emp.id}
                    className="w-full text-left px-4 py-3 hover:bg-slate-100 flex items-center gap-3 border-b last:border-none"
                    onClick={() => handleSelectUser(emp.id)}
                  >
                    <div className="bg-blue-100 p-1.5 rounded-full">
                      <User size={14} className="text-blue-600" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{emp.employeeFirstName} {emp.employeeLastName}</p>
                    </div>
                  </button>
                ))}
              </div>
            )}
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

        <div className="p-6 max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500 w-250">
          {isLoading ? (
            <div className="p-10 text-center text-slate-500 font-medium">Loading employee data...</div>
          ) : isError ? (
            <div className="p-10 text-center text-red-500 font-medium">Employee data could not be retrieved.</div>
          ) : (
            <>

              {/* Notifications */}
              {showNotification && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
                  <div className="bg-white rounded-xl max-w-lg w-full relative h-150 overflow-y-auto">
                    <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                      setShowNotification(false);
                    }}><X /></Button>
                    <Notifications />
                  </div>
                </div>
              )}
              <section className="bg-white p-4 rounded-lg border shadow-sm">
                <Breadcrumb>
                  <BreadcrumbList>
                    {orgData?.managerChain?.map((manager: any, index: number) => (
                      <div key={manager.employeeId} className="flex items-center">
                        <BreadcrumbItem>
                          <button 
                            title = {`View ${manager.firstName} ${manager.lastName}'s profile`}
                            onClick={() => setSelectedId(manager.employeeId)}
                            className="text-sm font-semibold text-slate-600 hover:text-blue-600"
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
                <Card className="w-full max-w-sm border-2 border-blue-500 shadow-xl ring-4 ring-blue-50">
                  <CardHeader className="flex flex-row items-center gap-4">
                    <Avatar className="h-16 w-16">
                      <AvatarImage src={orgData.employeeProfileUrl} />
                      <AvatarFallback className="bg-blue-600 text-white">
                        {orgData.firstName.charAt(0)}{orgData.lastName.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <CardTitle className="text -xl font -bold">{orgData.firstName} {orgData.lastName}</CardTitle>
                      <p className="text-blue-600 font-bold text-sm uppercase">{orgData.positionName}</p>
                    </div>
                  </CardHeader>
                  <CardContent className="bg-slate-50/50 border-t p-4 flex justify-between items-center">
                    <p className="text-xs font-medium text-slate-500">Department: <span className="text-slate-900">{orgData.departmentName}</span></p>
                    
                    <Badge variant="secondary" className="bg-blue-100 text-blue-700">{orgData.employeeEmail}</Badge>
                  </CardContent>
                </Card>
              </section>

              <section>
                <div className="flex items-center gap-2 mb-6">
                  <ChevronDown className="text-slate-400" />
                  <h3 className="text-lg font-bold text-slate-700">Direct Reports</h3>
                </div>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
                  {orgData.directReports?.map((report: any) => (
                    <Card 
                      key={report.employeeId} 
                      className="cursor-pointer hover:border-blue-400 transition-all"
                      onClick={() => setSelectedId(report.employeeId)}
                    >
                      <CardContent className="pt-6 flex flex-col items-center text-center gap-2">
                        <Avatar>
                          <AvatarImage src={report.employeeProfileUrl} />
                          <AvatarFallback>{report.firstName.charAt(0)}{report.lastName.charAt(0)}</AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-bold text-sm">{report.firstName} {report.lastName}</p>
                          <p className="text-[10px] text-blue-600 font-bold uppercase">{report.positionName}</p>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </section>
            </>
          )}
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}










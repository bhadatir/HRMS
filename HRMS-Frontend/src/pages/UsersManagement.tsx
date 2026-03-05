import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent } from "@/components/ui/card";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { X, Bell, Plus, Search } from "lucide-react";
import Notifications from "@/components/Notifications.tsx";
import { apiService } from "@/api/apiService.ts";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import AddUser from "../components/AddUser";
import { Input } from "@/components/ui/input";
import UserDetails from "@/components/UserDetails";
import { cn } from "@/lib/utils";
import { useAppDebounce } from "../hooks/useAppDebounce";

export default function JobManagement() {
  const { token, user, unreadNotifications } = useAuth();
  const [showNotification, setShowNotification] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [showUserDetails, setShowUserDetails] = useState(false);
  const [selectedUserEmail, setSelectedUserEmail] = useState<string | null>(null);
  const [employeeType, setEmployeeType] = useState(0);
  const debouncedSearchTerm = useAppDebounce(searchTerm);

  const { data: allEmp, isLoading, isError: allEmpError } = useQuery({
    queryKey: ["allEmployees", debouncedSearchTerm],
    queryFn: () => apiService.getAllEmployees(debouncedSearchTerm, token || ""),
    enabled: !!token,
  });

  if (allEmpError) {
    alert("Failed to load employees: " + allEmpError);
  }

  const filterEmp = useMemo(() => {
    if (!allEmp || !user) return [];
    return allEmp?.filter((emp: any) => {
        if(employeeType === 0) return true;
        if(employeeType === 1) return emp.employeeIsActive === true;
        if(employeeType === 2) return emp.employeeIsActive === false;
        return false;
      }) || [];
    }, [allEmp, employeeType, user]);
  
  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            {/* <SidebarTrigger /> */}
            <h3 className="text-lg font-bold text-slate-800">User Management</h3>
          </div>
  
          <div className="flex items-center gap-2">
            <select className="border rounded-md px-2 py-1 text-sm" 
              value={employeeType} onChange={(e) => setEmployeeType(Number(e.target.value))}>
                <option value="0">All Employees</option>
                <option value="1">Active</option>
                <option value="2">InActive</option>
            </select>
          </div>

          <div className="relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input
              placeholder="Search users..." 
              className="pl-9" 
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
              }}
            />
          </div>
          
          <div className="flex items-center gap-4">
            <Button title="Create New User"
                onClick={() => setShowAddUserForm(true)} className="gap-2 text-gray-600">
                <Plus size={18} />
                New User
            </Button>

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
          </div>
        </header>

        <main className="p-6 max-w-7xl mx-auto space-y-6 w-250">
          
          {/* Notifications */}
          {showNotification && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-3xl w-full relative h-150 overflow-y-auto">
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowNotification(false);
                }}><X /></Button>
                <Notifications />
              </div>
            </div>
          )}

          {/* Add user */}
          {showAddUserForm && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-2xl w-full relative h-150 overflow-y-auto">
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowAddUserForm(false);
                }}><X /></Button>
                <AddUser editUserEmail={null} onSuccess={() => setShowAddUserForm(false)} />
              </div>
            </div>
          )}

          {/* User Details */}
          {showUserDetails && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-2xl w-full relative h-150 overflow-y-auto">
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowUserDetails(false);
                }}><X /></Button>
                <UserDetails userEmail={selectedUserEmail} />
              </div>
            </div>
          )}

          <Card className="border-slate-200">            
            <CardContent className="space-y-4">
                <Table>
                <TableHeader>
                  <TableRow className="bg-slate-50">
                    <TableHead className="text-center">No</TableHead>
                    <TableHead className="text-center">Name</TableHead>
                    <TableHead className="text-center">Email</TableHead>
                    <TableHead className="text-center">Department</TableHead>
                    <TableHead className="text-center">Role</TableHead>
                    <TableHead className="text-center">Position</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                    {isLoading ? (
                        <TableRow>
                            <TableCell colSpan={5} className="text-center p-4">Loading...</TableCell>
                        </TableRow>
                    ) : filterEmp?.length > 0 ? (
                        filterEmp.map((emp: any, index: number) => (
                            <TableRow key={emp.id} 
                              title = {emp.employeeIsActive ? "Active User" : "Inactive User"}
                              className={cn(
                              "border-t cursor-pointer",
                              emp.employeeIsActive ? "hover:bg-slate-100" : "bg-red-50 hover:bg-red-100"
                            )} onClick={() => {
                                setSelectedUserEmail(emp.employeeEmail);
                                setShowUserDetails(true);
                                }}>
                                <TableCell className="text-center">{index + 1}</TableCell>
                                <TableCell className="text-center">{emp.employeeFirstName} {emp.employeeLastName}</TableCell>
                                <TableCell className="text-center">{emp.employeeEmail}</TableCell>
                                <TableCell className="text-center">{emp.departmentName}</TableCell>
                                <TableCell className="text-center">{emp.roleName}</TableCell>
                                <TableCell className="text-center">{emp.positionName}</TableCell>
                            </TableRow>
                        ))
                    ) : (
                        <TableRow>
                            <TableCell colSpan={6} className="text-center p-4">No employees found.</TableCell>
                        </TableRow>
                    )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

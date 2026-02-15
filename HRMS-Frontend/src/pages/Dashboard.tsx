import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Mail, Calendar, DollarSign, Building } from "lucide-react";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
export default function Dashboard() {
  const { user } = useAuth();

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <p className="text-gray-500">Failed to load user profile.</p>
      </div>
    );
  }

  return (
    <SidebarProvider>
       <AppSidebar />
       <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-6 bg-white">
          <SidebarTrigger className="-ml-1" />
          <h3 className="text-lg font-bold">My Dashboard</h3>
        </header>
        <div className="p-4 md:p-8 space-y-8 max-w-7xl mx-auto animate-in fade-in duration-500 w-250">
          
          <div className="relative rounded p-8 text-black shadow-xl">
            <div className="relative z-10 flex flex-col md:flex-row items-center gap-6">
              <Avatar className="h-16 w-16">
                <AvatarImage src={user.employeeProfileUrl} />
                <AvatarFallback className="bg-blue-600 text-white">
                  {user.employeeFirstName.charAt(0)}{user.employeeLastName.charAt(0)}
                </AvatarFallback>
              </Avatar>
              <div className="text-center md:text-left">
                <h1 className="text-3xl font-bold">{user.employeeFirstName} {user.employeeLastName}</h1>
                <div className="flex flex-wrap justify-center md:justify-start gap-2 mt-2">
                  <Badge variant="secondary" className="bg-blue-500/20 text-blue-300 border-none">
                    ID: {user.id}
                  </Badge>
                  <Badge variant="outline" className="text-slate-300 border-slate-700">
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
                <DollarSign className="w-4 h-4 text-green-600" />
                <CardTitle className="text-sm font-medium">Salary Overview</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-gray-500">Annual Gross</p>
                <p className="text-xl font-bold text-slate-900">
                  ${user.employeeSalary?.toLocaleString()}
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

        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}


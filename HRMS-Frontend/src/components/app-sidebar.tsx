import { Home, Users, Settings, LogOut } from "lucide-react"
import { useAuth } from "../context/AuthContext"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"

export function AppSidebar() {
  const { logout, user } = useAuth();

  const items = [
    { title: "Home", icon: Home, url: "/dashboard" },
    { title: "Organization Chart", icon: Users, url: "/organization-chart" },
    { title: "Travel Plan", icon: Users, url: "/travel-plan" },
  ]

  return (
    <Sidebar collapsible="icon">

      <SidebarHeader className="p-4 font-bold text-lg">HRMS</SidebarHeader>
      <SidebarContent>
        <SidebarMenu>
          {items.map((item) => (
            <SidebarMenuItem key={item.title}>
              <SidebarMenuButton asChild tooltip={item.title}>
                <a href={item.url}>
                  <item.icon />
                  <span>{item.title}</span>
                </a>
              </SidebarMenuButton>
            </SidebarMenuItem>
          ))}
        </SidebarMenu>
        <div className="p-4 mt-auto">
          <p className="text-sm text-gray-500 uppercase">{user?.employeeEmail}</p>
          <p className="text-sm text-gray-500">{user?.roleName}</p>
          <p className="text-sm text-gray-500">{user?.departmentName}</p>
        </div>
      </SidebarContent>
      <SidebarFooter className="p-4 border-t">
        <SidebarMenuButton onClick={logout} className="text-red-500 hover:text-red-600">
          <LogOut />
          <span>Logout</span>
        </SidebarMenuButton>
      </SidebarFooter>
    </Sidebar>
  )
}
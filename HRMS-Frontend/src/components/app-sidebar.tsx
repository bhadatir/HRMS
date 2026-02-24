import { Home, Plane, LogOut, Network, Briefcase, Mail, Building, ShieldCheck, Building2 } from "lucide-react"
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
    { title: "Organization Chart", icon: Network, url: "/organization-chart" },
    { title: "Travel Plan", icon: Plane, url: "/travel-plan" },
    { title: "Job Management", icon: Briefcase, url: "/job-management" },
    { title: "Post Management", icon: Mail, url: "/post-management" },
    { title: "Game Management", icon: Building, url: "/game-management" },
    ...(user?.roleName === "MANAGER" ? [{ title: "Team Member Data", icon: Building2, url: "/team-member-data" }] : []),
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
          <p className="text-sm text-blue-500 flex gap-2"><Mail size={14} className="mt-1"/>{user?.employeeEmail}</p>
          <p className="text-sm text-blue-500 flex gap-2"><ShieldCheck size={14} className="mt-1"/>{user?.roleName}</p>
          <p className="text-sm text-blue-500 flex gap-2"><Building2 size={14} className="mt-1"/>{user?.departmentName}</p>
        </div>
      </SidebarContent>
      <SidebarFooter className="p-4 border-t">
        <SidebarMenuButton onClick={logout} className="text-red-500 bg-slate-500">
          <LogOut />
          <span >Logout</span>
        </SidebarMenuButton>
      </SidebarFooter>
    </Sidebar>
  )
}
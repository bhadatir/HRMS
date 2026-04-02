import { Plane, LogOut, Network, Briefcase, Mail, Building, ShieldCheck, Building2 } from "lucide-react"
import { useAuth } from "../context/AuthContext"
import { Link, useLocation, useNavigate } from "react-router-dom"
import roimaLogo from "@/assets/roima_logo.png"
import roimaFavicon from "@/assets/roima_loader.png"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar"
import { useIsMobile } from "@/hooks/use-mobile"

export function AppSidebar() {
  const { logout, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const { state, setOpen } = useSidebar();
  const isMobile = useIsMobile();

  const items = [
    { title: "Organization Chart", icon: Network, url: "/organization-chart" },
    { title: "Travel Plan", icon: Plane, url: "/travel-plan" },
    { title: "Job Management", icon: Briefcase, url: "/job-management" },
    { title: "Post Management", icon: Mail, url: "/post-management" },
    { title: "Game Management", icon: Building, url: "/game-management" },
    ...(user?.roleName === "MANAGER" ? [{ title: "Team Member Data", icon: Building2, url: "/team-member-data" }] : []),
    ...((user?.roleName === "ADMIN" || user?.roleName === "HR" ) ? [{ title: "User Management", icon: Building2, url: "/users-management" }] : []),
  
  ]

  return (
    <Sidebar
      collapsible="icon"
      className="px-2 bg-sidebar text-inherit"
      onMouseEnter={() => {
        if (!isMobile) setOpen(true)
      }}
      onMouseLeave={() => {
        if (!isMobile) setOpen(false)
      }}
    >

      <SidebarHeader className={`cursor-pointer justify-between font-bold text-lg items-center gap-2 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:p-2`} 
          onClick={() => navigate("/dashboard")}
        >
        <img
          src={state === "collapsed" ? !isMobile ? roimaFavicon : roimaLogo : roimaLogo}
          alt="Roima logo"
          className={`shrink-0 ${ state === "collapsed" && !isMobile ? "h-8 mt-2" : "h-12"} w-auto object-contain`}
          title="Dashboard Page"
        />
        {/* <SidebarTrigger className="bg-slate-50 text-black group-data-[collapsible=icon]:hidden" /> */}
      </SidebarHeader>
      <SidebarContent>
        <SidebarMenu>
          {items.map((item) => (
            <SidebarMenuItem key={item.title} className="group-data-[collapsible=icon]:flex group-data-[collapsible=icon]:justify-center">
              <SidebarMenuButton
                asChild
                isActive={location.pathname === item.url}
                tooltip={item.title}
                className="group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:mx-auto group-data-[collapsible=icon]:[&>svg]:size-5 data-[active=true]:border data-[active=true]:border-sidebar-primary-foreground data-[active=true]:border-l-4"
              >
                <Link to={item.url} className="text-inherit hover:text-inherit hover:border hover:border-sidebar-primary-foreground hover:border-l-4 [&>*]:text-inherit">
                  <item.icon className="shrink-0" />
                  <span className="group-data-[collapsible=icon]:hidden">{item.title}</span>
                </Link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          ))}
        </SidebarMenu>
        <div className="p-4 mt-auto space-y-2 group-data-[collapsible=icon]:p-2">
          <p title={user?.employeeEmail} className="text-sm text-inherit flex items-center gap-2 group-data-[collapsible=icon]:justify-center">
            <Mail size={14} className="shrink-0 group-data-[collapsible=icon]:size-5" />
            <span className="group-data-[collapsible=icon]:hidden">{user?.employeeEmail}</span>
          </p>
          <p title={user?.roleName} className="text-sm text-inherit flex items-center gap-2 group-data-[collapsible=icon]:justify-center">
            <ShieldCheck size={14} className="shrink-0 group-data-[collapsible=icon]:size-5" />
            <span className="group-data-[collapsible=icon]:hidden">{user?.roleName}</span>
          </p>
          <p title={user?.departmentName} className="text-sm text-inherit flex items-center gap-2 group-data-[collapsible=icon]:justify-center">
            <Building2 size={14} className="shrink-0 group-data-[collapsible=icon]:size-5" />
            <span className="group-data-[collapsible=icon]:hidden">{user?.departmentName}</span>
          </p>
        </div>
      </SidebarContent>
      <SidebarFooter className="p-4 border-t group-data-[collapsible=icon]:p-2">
        <SidebarMenuButton
          onClick={logout}
          tooltip="Logout"
          className="text-red-600 hover:text-red-700 hover:bg-red-50 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:mx-auto group-data-[collapsible=icon]:[&>svg]:size-5"
        >
          <LogOut />
          <span className="group-data-[collapsible=icon]:hidden">Logout</span>
        </SidebarMenuButton>
      </SidebarFooter>
    </Sidebar>
  )
}
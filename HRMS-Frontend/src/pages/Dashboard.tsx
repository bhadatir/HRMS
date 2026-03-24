import { useAuth } from "../context/AuthContext";
import { Calendar } from "@/components/ui/calendar";
import { isSameDay, parseISO } from "date-fns";
import { useEffect, useRef, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Mail, Building, X, Bell, IndianRupee, Pencil } from "lucide-react";
import Notifications from "../components/Notifications.tsx";
import { Button } from "@/components/ui/button";
import {
  SidebarInset,
  SidebarProvider,
} from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { Input } from "@/components/ui/input.tsx";
import { apiService } from "@/api/apiService.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { travelService } from "@/api/travelService.ts";
import { gameService } from "@/api/gameService.ts";
import { useNavigate } from "react-router-dom";
import { ScrollToTop } from "@/components/ScrollToTop.tsx";
import { GlobalSearch } from "@/components/GlobalSearch.tsx";

type TravelPlan = {
    id: number;
    travelPlanName: string;
    travelPlanFrom: string;
    travelPlanTo: string;
    travelPlanStartDate: string;
    travelPlanEndDate: string;
    travelPlanIsDeleted: boolean;
};

type GameBooking = {
    id: number;
    gameTypeId: number;
    gameTypeName: string;
    gameBookingStartTime: string;
    gameBookingIsDeleted: boolean;
    hostEmployeeId: number;
    hostEmployeeEmail: string;
    bookingParticipantResponses: BookingParticipantResponse[];
};

type BookingParticipantResponse = {
    employeeId: number;
    employeeEmail: string;
};

type WaitingList = {
    id: number;
    gameTypeId: number;
    gameTypeName: string;
    targetSlotDatetime: string;
};

export default function Dashboard() {
  const { setIsFirstLogin, token, isFirstLogin, user, unreadNotifications, isAuthenticated } = useAuth();
  const [showNotification, setShowNotification] = useState(false);
  const [newPassword, setNewPassword] = useState(""); 
  const fileInputRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());
  const navigate = useNavigate();

  if (!isAuthenticated) {
    navigate("/login");
  }

  const profilePicMutation = useMutation({
    mutationFn: async (file: File) => {
    
      const formData = new FormData();
      formData.append("file", file);
  
      return apiService.updateProfileImage(user?.id || 0, formData, token || "");
    },
    onSuccess: () => {
      alert("Profile picture updated!");
      queryClient.invalidateQueries({ queryKey: ["user"] });
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      alert("Failed to update profile details: " + (error.response?.data || error.message)); }
  });

  const updatePasswordMutation = useMutation({
    mutationFn: () => apiService.updatePassword(user?.id || 0, newPassword, token || ""),
    onSuccess: () => {
      alert("Password updated successfully!");
      localStorage.setItem("isFirstLogin", "no");
      setIsFirstLogin("no");
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      alert("Failed to update password: " + (error.response?.data || error.message)); }
  });
  
  const { data: travelPlans, isError: travelPlansError } = useQuery({
    queryKey: ["travelPlanByEmpId", user?.id],
    queryFn: () => travelService.findTravelPlanByEmployeeId(user?.id || 0, "", 0, 0, 100, token || ""),
    enabled: !!token && !!user?.id,
  });

  const { data: gameBookings, isError: gameBookingsError } = useQuery({
    queryKey: ["gameBookings", user?.id],
    queryFn: () => gameService.findGameBookingByUserId(user?.id || 0, "", 0, 0, 0, 100, token || ""), 
    enabled: !!token && !!user?.id,
  });

  const { data: WaitingList, isError: waitingListError } = useQuery({ 
      queryKey: ["WaitingList", user?.id], 
      queryFn: () => gameService.findGameBookingWaitingListByEmpId(user?.id || 0, 0, token!) 
  });

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <p className="text-gray-500">Failed to load user profile.</p>
      </div>
    );
  }

  const modifiers = {
    travel: (date: Date) => travelPlans?.content?.some((p: TravelPlan) => 
      (isSameDay(parseISO(p.travelPlanStartDate), date) || 
      (date >= parseISO(p.travelPlanStartDate) && date <= parseISO(p.travelPlanEndDate)))
      && p.travelPlanIsDeleted === false
    ),
    game: (date: Date) => gameBookings?.content?.some((b: GameBooking) => 
      isSameDay(parseISO(b.gameBookingStartTime), date) && !b.gameBookingIsDeleted
    ),
    wait: (date: Date) => WaitingList?.some((w: WaitingList) => 
      isSameDay(parseISO(w.targetSlotDatetime), date)
    ),
  };

  const modifiersStyles = {
    travel: { border: "1px solid #1e40af", borderRadius: "4px" },
    game: { border: "1px solid #22c55e",borderRadius: "4px" },
    wait: { border: "1px solid #f59e0b", borderRadius: "4px" },
    selected: { border: "2px solid #000000", borderRadius: "4px", text: "black" },
  };

  const getEventsForDay = (date: Date | undefined) => {
    if (!date) return null;
    const dayTravel = travelPlans?.content?.filter((p: TravelPlan) => 
       (isSameDay(parseISO(p.travelPlanStartDate), date) || (date >= parseISO(p.travelPlanStartDate) && date <= parseISO(p.travelPlanEndDate))) 
        && p.travelPlanIsDeleted === false
    );
    const dayGames = gameBookings?.content?.filter((b: GameBooking) => isSameDay(parseISO(b.gameBookingStartTime), date) && !b.gameBookingIsDeleted);
    const dayWaiting = WaitingList?.filter((w: WaitingList) => isSameDay(parseISO(w.targetSlotDatetime), date));
    
    return { dayTravel, dayGames, dayWaiting };
  };

  const dayEvents = getEventsForDay(selectedDate);

  // eslint-disable-next-line react-hooks/rules-of-hooks
  useEffect(() => {
      const clickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest("div.relative")) {
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

  if(travelPlansError || gameBookingsError || waitingListError) {
    alert("Failed to load events: " + (travelPlansError || gameBookingsError || waitingListError));
  }

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
                <div className="bg-white rounded-xl max-w-3xl w-full relative h-150 overflow-y-auto">
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
                  <Input type="file" accept=".jpg,.jpeg,.png" className="hidden" onChange={(e) => e.target.files && profilePicMutation.mutate(e.target.files[0])} ref={fileInputRef} />
                  {profilePicMutation.isPending && (
                    <div className="absolute inset-0 bg-black/30 flex items-center justify-center rounded">
                      <span className="text-white text-sm p-4">Uploading.</span>
                    </div>
                  )}
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
                    <div>
                      <p className="text-sm text-gray-500">Position</p>
                      <p className="text-base font-semibold">{user.positionName || "N/A"}</p>
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
                  <p className="text-sm text-gray-500">Monthly</p>
                  <p className="text-xl font-bold text-slate-900">
                    {user.employeeSalary?.toLocaleString()}
                  </p>
                </CardContent>
              </Card>
            </div>


            <Card>
              <CardHeader>
                <CardTitle className="text-lg flex items-center gap-2">
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

            <Card className="col-span-1 md:col-span-3 lg:col-span-2">
              <CardHeader>
                <CardTitle className="text-lg flex items-center gap-2">
                  Schedule Overview
                </CardTitle>
              </CardHeader>
              <CardContent className="flex flex-col md:flex-row gap-8">
                
                <div className="border rounded-md p-2 bg-white">
                  <Calendar
                    mode="single"
                    selected={selectedDate}
                    onSelect={setSelectedDate}
                    modifiers={modifiers}
                    modifiersStyles={modifiersStyles}
                    className="rounded-md"
                  />
                  <div className="mt-4 flex gap-4 text-xs p-2 border-t">
                    <div className="flex items-center gap-1">
                      <div className="w-3 h-3 border-1 border-blue-400 rounded" /> Travel
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-3 h-3 border-1 border-green-500 rounded" /> Game
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-3 h-3 border-1 border-yellow-500 rounded" /> Waiting List
                    </div>
                  </div>
                </div>

                {/* Details Panel for Selected Date */}
                <div className="flex-1 space-y-4">
                  <h4 className="font-bold text-slate-700 border-b pb-2">
                    Details: {selectedDate?.toLocaleDateString()}
                  </h4>
                  
                  {dayEvents?.dayTravel?.length > 0 ? (
                    dayEvents?.dayTravel.map((p: TravelPlan) => (
                      <div key={p.id} 
                        onClick={() => navigate(`/travel-plan?travelPlanId=${p.id}`)}
                        className="p-3 bg-blue-50 border-l-4 border-blue-500 rounded text-sm cursor-pointer">
                        <p className="font-bold text-blue-800">✈️ {p.travelPlanName}</p>
                        <p className="text-blue-600 text-xs">{p.travelPlanFrom} → {p.travelPlanTo}</p>
                      </div>
                    ))
                  ) : null}

                  {dayEvents?.dayGames?.length > 0 ? (
                    dayEvents?.dayGames.map((g: GameBooking) => (
                      <div key={g.id} 
                        onClick={() => navigate(`/game-management?gameBookingId=${g.id}`)}
                        className="p-3 bg-green-50 border-l-4 border-green-500 rounded text-sm cursor-pointer">
                        <p className="font-bold text-green-800">🎮 {g.gameTypeName}</p>
                        <p className="text-green-600 text-xs">
                            {new Date(g.gameBookingStartTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                        </p>
                      </div>
                    ))
                  ) : null}

                  {dayEvents?.dayWaiting?.length > 0 ? (
                    dayEvents?.dayWaiting.map((w: WaitingList) => (
                      <div key={w.id}
                        className="p-3 bg-yellow-50 border-l-4 border-yellow-500 rounded text-sm cursor-pointer"
                        onClick={() => navigate(`/game-management`)}>
                        <p className="font-bold text-yellow-800">⏳ {w.gameTypeName}</p>
                        <p className="text-yellow-600 text-xs">
                            {new Date(w.targetSlotDatetime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                        </p>
                      </div>
                    ))
                  ) : null}

                  {!dayEvents?.dayTravel?.length && !dayEvents?.dayGames?.length && !dayEvents?.dayWaiting?.length && (
                    <p className="text-slate-400 italic text-sm">No events scheduled for this day.</p>
                  )}
                </div>
              </CardContent>
            </Card>


            <ScrollToTop />
            <GlobalSearch />
          </main>
        </SidebarInset>
      </SidebarProvider>
    )
  );
}

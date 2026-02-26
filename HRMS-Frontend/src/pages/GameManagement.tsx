import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { useAuth } from "../context/AuthContext";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Plus, Gamepad2, X, Bell, Gamepad, Calendar, Clock, Trash } from "lucide-react";
import GameBookingForm from "../components/GameBookingForm";
import GameTypeManager from "@/components/GameTypeManager";
import Notifications from "@/components/Notifications";
import BookingCard from "@/components/BookingCard";
import GameInterestToggle from "@/components/GameInterestToggle";
import { Card, CardContent } from "@/components/ui/card";
import WaitingList from "@/components/WaitingList";

export default function GameManagement() {
    const { token, user, unreadNotifications } = useAuth();
    const queryClient = useQueryClient();
    const [showGameBookingForm, setShowGameBookingForm] = useState(false);
    const [showGameTypeForm, setShowGameTypeForm] = useState(false);
    const [gameType, setGameType] = useState<number | null>(null);
    const [gameBookingStatusId, setgameBookingStatusId] = useState<number | null>(null);
    const [showNotification, setShowNotification] = useState(false);
    const [showGameIntrestForm, setShowGameIntrestForm] = useState(false);
    const [showWaitingList, setShowWaitingList] = useState(false);
    const [viewMode, setViewMode] = useState<"My Bookings" | "Waiting List">("My Bookings");
    const [waitingListId, setWaitingListId] = useState<number>(0);

    const { data: gameTypes = [] } = useQuery({
        queryKey: ["gameTypes"],
        queryFn: () => gameService.getAllGames(token!)
    });

    const { data: gameBookingStatusOptions = [] } = useQuery({
        queryKey: ["gameBookingStatusOptions"],
        queryFn: () => gameService.getAllGameBookingStatus(token!)
    });

    const { data: allWaitingList = [] } = useQuery({ 
        queryKey: ["allWaitingList"], 
        queryFn: () => gameService.getAllWaitingList(token!) });

    const { data: bookings = [], isLoading } = useQuery({
        queryKey: ["allBookings"],
        queryFn: () => gameService.showAllBookings(token!),
        select: (bookings) => bookings.filter((b: any) => (!gameType || b.gameTypeId === gameType) && (!gameBookingStatusId || b.gameBookingStatusId === gameBookingStatusId))
    });

    const statusMutation = useMutation({
        mutationFn: ({ id, status }: { id: number, status: number }) => 
            gameService.updateBookingStatus(id, status, token!),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["allBookings"] })
    });

    const removeWaitingListMutation = useMutation({
        mutationFn: (id: number) => gameService.deleteWaitingList(id, token!),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["allWaitingList"] })
    });

    if(isLoading) return <p>Loading Slots...</p>;    

    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset className="bg-slate-50">
                <header className="flex h-16 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
                    <div className="flex items-center gap-2">
                        {/* <SidebarTrigger /> */}
                        <h3 className="font-bold">Game Zone</h3>
                        <select className="border rounded-md px-2 py-1 text-sm" value={gameType || ""} onChange={(e) => setGameType(Number(e.target.value))}>
                            <option value="">All Games</option>
                            {gameTypes.map((g: any) => <option key={g.id} value={g.id}>{g.gameName}</option>)}
                        </select>
                    </div>
                    <div className="flex items-center gap-4">
                        <Button onClick={() => setShowGameBookingForm(true)} className="gap-2 text-gray-600">
                            <Plus size={18}/> New Booking
                        </Button>

                        <Button onClick={() => setShowGameTypeForm(true)} className="gap-2 text-gray-600">
                            <Gamepad2 size={18}/> Game Types
                        </Button>

                        <Button onClick={() => setShowGameIntrestForm(true)} className="gap-2 text-gray-600">
                            <Gamepad size={18}/> Game Interests
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

                <main className="p-6 max-w-5xl mx-auto w-250">
                    {/* Notifications */}
                    {showNotification && (
                      <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
                        <div className="bg-white rounded-xl max-w-lg w-full relative h-150 overflow-y-auto">
                          <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                            onClick={() => {
                            setShowNotification(false);
                          }}><X /></Button>
                          <Notifications />
                        </div>
                      </div>
                    )}

                    {/* Waiting List */}
                    {showWaitingList && (
                      <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
                        <div className="bg-white rounded-xl max-w-3xl w-full relative h-100 overflow-y-auto">
                          <Button title="Close Waiting List" variant="ghost" className="absolute right-2 top-2" 
                            onClick={() => {
                            setShowWaitingList(false);
                          }}><X /></Button>
                          <WaitingList waitingListId={waitingListId} onSuccess={() => setShowWaitingList(false)} />
                        </div>
                      </div>
                    )}

                    {/* add game booking */}
                    {showGameBookingForm && (
                        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                            <div className="bg-white rounded-xl w-full max-w-2xl relative h-150 overflow-y-auto">     
                                <Button variant="ghost" className="absolute top-2 right-2" 
                                onClick={() => {
                                    setShowGameBookingForm(false);
                                }}>X</Button>
                                <GameBookingForm onSuccess={() => setShowGameBookingForm(false)} />
                            </div>
                        </div>
                    )}

                    {/* add game type */}
                    {showGameTypeForm && (
                        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
                            <div className="bg-white rounded-xl max-w-lg w-full relative h-120 overflow-y-auto">
                                <Button title="Close" variant="ghost" className="absolute right-2 top-2" 
                                onClick={() => setShowGameTypeForm(false)}>
                                <X />
                                </Button>
                                <GameTypeManager />
                            </div>
                        </div>
                    )}

                    {/* Game Interest */}
                    {showGameIntrestForm && (
                      <div className="absolute right-2 top-16 z-50 flex items-center justify-center p-4">
                        <div className="bg-white rounded-xl max-w-lg w-full relative h-max">
                          <Button title="Close Game Interests" variant="ghost" className="absolute right-2 top-2" 
                            onClick={() => {
                            setShowGameIntrestForm(false);
                          }}><X /></Button>
                          <GameInterestToggle />
                        </div>
                      </div>
                    )}

                    {/* upcomming games */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <h2 className="text-xl font-bold text-slate-900 col-span-full">Upcoming Bookings :</h2>
                        {bookings.filter((b: any) => {
                                        const bookingTime = new Date(b.gameBookingEndTime).getTime();
                                        const now = new Date().getTime();
                                        const upcommingEnd = bookingTime + (1 * 60 * 60 * 1000);
                                        return !b.gameBookingIsDeleted &&bookingTime >= now && bookingTime <= upcommingEnd && b.gameBookingStatusId === 1;
                                    }).length > 0 ? (
                                    bookings.filter((b: any) => {
                                        const bookingTime = new Date(b.gameBookingEndTime).getTime();
                                        const now = new Date().getTime();
                                        const upcommingEnd = now + (1 * 60 * 60 * 1000);
                                        return !b.gameBookingIsDeleted &&bookingTime >= now && bookingTime <= upcommingEnd && b.gameBookingStatusId === 1;
                                    }).map((b: any) => (
                                    <BookingCard key={b.id} booking={b} onStatusChange={() => statusMutation.mutate({ id: b.id, status: 3 })} />
                                ))
                            ) : (
                                <p className="text-slate-500 italic">No upcoming bookings in the next hour.</p>
                            )}
                    </div>

                    
                    <div className="flex items-center justify-between gap-2 my-5">
                        <div className="flex flex-row items-center gap-4">
                            <Button className={viewMode === "My Bookings" ? "rounded-md border text-gray-900" : "rounded-md text-gray-300"}
                                size="sm"
                                onClick={()=>setViewMode("My Bookings")}> My Bookings</Button>
                            <Button className={viewMode === "Waiting List" ? "rounded-md border text-gray-900" : "rounded-md text-gray-300"}
                                size="sm"
                                onClick={()=>setViewMode("Waiting List")}> Waiting List</Button>
                        </div>
                        {viewMode === "My Bookings" && <select className="border rounded-md px-2 py-1 text-sm" value={gameBookingStatusId || ""} 
                            onChange={(e) => setgameBookingStatusId(Number(e.target.value))}>
                            <option value="">All Statuses</option>
                            {gameBookingStatusOptions.map((g: any) => <option key={g.id} value={g.id}>{g.gameBookingStatusName}</option>)}
                        </select>}
                    </div>

                    {/* my bookings  */}
                    {viewMode === "My Bookings" && (
                    <div className="mt-2">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 my-5">
                        {bookings.length > 0 ? (
                            <>                                
                                {bookings.map((b: any) => ((b.employeeId === user?.id || b.bookingParticipantResponses.some((p: any) => p.employeeId === user?.id)) 
                                    && !b.gameBookingIsDeleted) && (
                                    <BookingCard key={b.id} booking={b} onStatusChange={() => statusMutation.mutate({ id: b.id, status: 3 })} />        
                                ))}
                            </>
                        ): (
                            <p className="text-slate-500 italic">You have no bookings yet.</p>
                        )}
                        </div>
                    </div>
                    )}

                    {/* WaitingList */}
                    {viewMode === "Waiting List" && (
                    <div className="my-5">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {allWaitingList.length > 0 ? (
                                allWaitingList.map((wait: any) => ((
                                    wait.hostEmployeeId === user?.id || wait.bookingParticipantResponses.some((p: any) => p.employeeId === user?.id)
                                    )
                                    && (
                                    <Card key={wait.id} className="hover:shadow-md transition-shadow border-slate-200 cursor-pointer"
                                        onClick={() => {
                                            setWaitingListId(wait.id);
                                            setShowWaitingList(true);
                                        }}>
                                        <CardContent className="p-4 space-y-3">
                                            <div className="flex justify-between items-start">
                                                <Badge variant="outline">Game Name: {wait.gameTypeName}</Badge>
                                        </div>
                                        
                                        <div className="flex items-center gap-2 text-sm font-semibold">
                                            <Calendar size={14} /> {new Date(wait.targetSlotDatetime).toLocaleDateString()}
                                        </div>
                                        <div className="flex items-center gap-2 text-sm text-slate-500">
                                            <Clock size={14} /> {new Date(wait.targetSlotDatetime).toTimeString().slice(0,5)} 
                                            - {(() => {
                                                const startDate = new Date(wait.targetSlotDatetime);
                                                const endDate = new Date(startDate.getTime() + wait.gameSlotDuration * 60000);
                                                const endHour = endDate.getHours();
                                                const endMinute = endDate.getMinutes();
                                                return `${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}`;
                                            })()}
                                        </div>

                                        <div className="pt-2 border-t flex justify-between items-center">
                                            <div className="space-y-1">
                                                <div className="text-[10px] text-slate-400">Host: {wait.hostEmployeeEmail}</div>
                                                {wait.bookingParticipantResponses.length > 0 && (
                                                    <div className="text-[10px] text-slate-400 ">
                                                        Participants: {wait.bookingParticipantResponses.map((p: any) => p.employeeEmail).join(", ")}
                                                    </div>
                                                )}
                                            </div>
                                            
                                                <div className="flex gap-2">
                                                    {wait.hostEmployeeId === user?.id && 
                                                        <>
                                                        <Button size="sm" variant="outline" className="h-7 text-red-600" 
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            if (confirm("Are you sure you want to delete this waiting list entry?")) {
                                                                removeWaitingListMutation.mutate(wait.id);
                                                            }
                                                        }}>
                                                            <Trash size={14} className="mr-1"/> Delete
                                                        </Button>
                                                        {/* <Button size="sm" variant="outline" className="h-7 text-gray-600" 
                                                        onClick={() => {
                                                            setShowGameBookingForm(true);
                                                        }}>
                                                            <Edit size={14} className="mr-1"/> Edit
                                                        </Button> */}
                                                        </>
                                                    }
                                                </div>
                                        </div>
                                    </CardContent>
                                    </Card>
                                    )
                                )
                        )) : (
                            <p className="text-slate-500 italic">No one is on the waiting list.</p>
                        )}
                        </div>
                    </div>
                    )}
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}

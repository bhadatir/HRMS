import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { useAuth } from "../context/AuthContext";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Plus, Gamepad2, X, Bell, Book, Gamepad } from "lucide-react";
import GameBookingForm from "../components/GameBookingForm";
import GameTypeManager from "@/components/GameTypeManager";
import Notifications from "@/components/Notifications";
import BookingCard from "@/components/BookingCard";
import GameInterestToggle from "@/components/GameInterestToggle";

export default function GameManagement() {
    const { token, user, unreadNotifications } = useAuth();
    const queryClient = useQueryClient();
    const [showGameBookingForm, setShowGameBookingForm] = useState(false);
    const [showGameTypeForm, setShowGameTypeForm] = useState(false);
    const [gameType, setGameType] = useState<number | null>(null);
    const [showNotification, setShowNotification] = useState(false);
    const [showGameInterestForm, setShowGameIntrestForm] = useState(false);

    const { data: gameTypes = [] } = useQuery({
        queryKey: ["gameTypes"],
        queryFn: () => gameService.getAllGames(token!)
    });

    const { data: bookings = [], isLoading } = useQuery({
        queryKey: ["allBookings"],
        queryFn: () => gameService.showAllBookings(token!),
        select: (bookings) => bookings.filter((b: any) => !gameType || b.gameTypeId === gameType)
    });

    const statusMutation = useMutation({
        mutationFn: ({ id, status }: { id: number, status: number }) => 
            gameService.updateBookingStatus(id, status, token!),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["allBookings"] })
    });

    if(isLoading) return <p>Loading Slots...</p>;    

    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset className="bg-slate-50">
                <header className="flex h-16 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
                    <div className="flex items-center gap-2">
                        <SidebarTrigger /><h3 className="font-bold">Game Zone</h3>
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

                    {/* add game booking */}
                    {showGameBookingForm && (
                        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                            <div className="bg-white rounded-xl w-full max-w-md relative h-150 overflow-y-auto"> 
                                <GameBookingForm onSuccess={() => setShowGameBookingForm(false)} />
                                <Button variant="ghost" className="absolute top-2 right-2" 
                                onClick={() => {
                                    setShowGameBookingForm(false);
                                }}>X</Button>
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
                    {showGameInterestForm && (
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
                        {bookings.filter((b: any) => 
                        {
                            const bookingTime = new Date(b.gameBookingStartTime).getTime();
                            const now = new Date().getTime();
                            const upcommingEnd = now + (1 * 60 * 60 * 1000);
                            return !b.gameBookingIsDeleted
                            && bookingTime >= now
                            && bookingTime <= upcommingEnd && b.gameBookingStatusId === 1
                        }).length > 0 ? (
                            <>
                                
                                {bookings.filter((b: any) => new Date(b.gameBookingStartTime) > new Date()).map((b: any) => (
                                    <BookingCard key={b.id} booking={b} onStatusChange={() => statusMutation.mutate({ id: b.id, status: 3 })} />
                                ))}
                            </>
                        ): (
                            <p className="text-slate-500 italic">No upcoming bookings.</p>
                        )}
                    </div>

                    {/* my bookings  */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <h2 className="text-xl font-bold text-slate-900 col-span-full mt-5">My Bookings :</h2>
                        {bookings.filter((b: any) => b.employeeId === user?.id).length > 0 ? (
                            <>
                                
                                {bookings.filter((b: any) => b.employeeId === user?.id).map((b: any) => (
                                    <BookingCard key={b.id} booking={b} onStatusChange={() => statusMutation.mutate({ id: b.id, status: 3 })} />        
                                ))}
                            </>
                        ): (
                            <p className="text-slate-500 italic">You have no bookings yet.</p>
                        )}
                    </div>
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}

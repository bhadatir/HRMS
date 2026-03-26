import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { useAuth } from "../context/AuthContext";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Plus, Gamepad2, X, Bell, Gamepad, Calendar, Clock, Trash, Search } from "lucide-react";
import GameBookingForm from "../components/GameBookingForm";
import GameTypeManager from "@/components/GameTypeManager";
import Notifications from "@/components/Notifications";
import BookingCard from "@/components/BookingCard";
import GameInterestToggle from "@/components/GameInterestToggle";
import { Card, CardContent } from "@/components/ui/card";
import WaitingList from "@/components/WaitingList";
import { Input } from "@/components/ui/input";
import { useInView } from "react-intersection-observer";
import { useAppDebounce } from "../hooks/useAppDebounce";
import { useFindGameBookingByUserId, useFindGameBookings } from "@/hooks/useInfinite";
import { ScrollToTop } from "@/components/ScrollToTop";
import { GlobalSearch } from "@/components/GlobalSearch";
import { useToast } from "@/context/ToastContext";

type GameType = {
    id: number;
    gameName: string;
};

type GameBookingStatus = {
    id: number;
    gameBookingStatusName: string;
};

type Booking = {
    id: number;
    gameTypeId: number;
    gameTypeName: string;
    hostEmployeeId: number;
    hostEmployeeEmail: string;
    slotDatetime: string;
    slotEndDatetime: string;
    gameBookingStatusId: number;
    gameBookingStatusName: string;
    gameBookingIsDeleted: boolean;
};

type WaitingList = {
    id: number;
    gameTypeId: number;
    gameTypeName: string;
    gameSlotDuration: number;
    targetSlotDatetime: string;
    targetSlotEndDatetime: string;
    hostEmployeeId: number;
    hostEmployeeEmail: string;
    bookingParticipantResponses: BookingParticipantResponse[];
};

type BookingParticipantResponse = {
    employeeEmail: string;
    responseStatus: string;
};

export default function GameManagement() {
    const toast = useToast();
    const { token, user, unreadNotifications } = useAuth();
    const queryClient = useQueryClient();
    const [showGameBookingForm, setShowGameBookingForm] = useState(false);
    const [showGameTypeForm, setShowGameTypeForm] = useState(false);
    const [gameType, setGameType] = useState<number>(0);
    const [gameBookingStatusId, setgameBookingStatusId] = useState<number>(0);
    const [showNotification, setShowNotification] = useState(false);
    const [showGameIntrestForm, setShowGameIntrestForm] = useState(false);
    const [showWaitingList, setShowWaitingList] = useState(false);
    const [viewMode, setViewMode] = useState<"My Bookings" | "Waiting List" | "All Bookings">("My Bookings");
    const [waitingListId, setWaitingListId] = useState<number>(0);
    const [bookingSearchTerm, setBookingSearchTerm] = useState(() => {
        const urlParams = new URLSearchParams(window.location.search);
        return !(user?.roleName === "HR" || user?.roleName === "ADMIN") ? urlParams.get("gameBookingId") || "" : "";
    });
    const [waitingListSearchTerm, setWaitingListSearchTerm] = useState("");
    const [allBookingsSearchTerm, setAllBookingsSearchTerm] = useState(() => {
        const urlParams = new URLSearchParams(window.location.search);
        if (user?.roleName === "HR" || user?.roleName === "ADMIN"){setViewMode("All Bookings");}
        return (user?.roleName === "HR" || user?.roleName === "ADMIN") ? urlParams.get("gameBookingId") || "" : "";
    });
    const debouncedBookingSearchTerm = useAppDebounce(bookingSearchTerm);
    const debouncedWaitingListSearchTerm = useAppDebounce(waitingListSearchTerm);
    const debouncedAllBookingsSearchTerm = useAppDebounce(allBookingsSearchTerm);

    const { data: gameTypes = [], isError: gameTypesOnError } = useQuery({
        queryKey: ["gameTypes"],
        queryFn: () => gameService.getAllGames(token!)
    });

    const { data: gameBookingStatusOptions = [], isError: gameBookingStatusOptionsOnError } = useQuery({
        queryKey: ["gameBookingStatusOptions"],
        queryFn: () => gameService.getAllGameBookingStatus(token!)
    });

    const { data: upcomingBookings = [], isError: upcomingBookingsOnError } = useQuery({
        queryKey: ["upcomingBookings"],
        queryFn: () => gameService.upcommingBookings(token!)
    });

    const { data: WaitingListByEmpId = [], isError: waitingListByEmpIdOnError } = useQuery({ 
        queryKey: ["WaitingList", user?.id, gameType], 
        queryFn: () => gameService.findGameBookingWaitingListByEmpId(user?.id || 0, gameType, token!) 
    });

    const {
        data: bookingsByEmpId,
        fetchNextPage: fetchNextPageByEmpId,
        hasNextPage: hasNextPageByEmpId,
        isFetchingNextPage: isFetchingNextPageByEmpId,
        isError: bookingsByEmpIdOnError,
    } = useFindGameBookingByUserId(bookingSearchTerm, gameType, gameBookingStatusId, token || "");
    const filteredBookings = bookingsByEmpId?.pages.flatMap(page => page.content) || [];

    const {
        data: bookings,
        fetchNextPage: fetchNextPageBookings,
        hasNextPage: hasNextPageBookings,
        isFetchingNextPage: isFetchingNextPageBookings,
        isError: bookingsOnError,
    } = useFindGameBookings(allBookingsSearchTerm, gameType, gameBookingStatusId, token || "");
    const filteredAllBookings = bookings?.pages.flatMap(page => page.content) || [];
    
    const { ref, inView } = useInView();
    useEffect(() => {
        if (inView && hasNextPageByEmpId && !isFetchingNextPageByEmpId) {
        fetchNextPageByEmpId();
        }
    }, [inView, hasNextPageByEmpId, isFetchingNextPageByEmpId, fetchNextPageByEmpId]);

    useEffect(() => {
        if (inView && hasNextPageBookings && !isFetchingNextPageBookings) {
        fetchNextPageBookings();
        }
    }, [inView, hasNextPageBookings, isFetchingNextPageBookings, fetchNextPageBookings]);

    if( bookingsOnError || gameTypesOnError || gameBookingStatusOptionsOnError || upcomingBookingsOnError || waitingListByEmpIdOnError || bookingsByEmpIdOnError) {
        toast?.error("Failed to load data: " + (bookingsOnError || gameTypesOnError || gameBookingStatusOptionsOnError || upcomingBookingsOnError || waitingListByEmpIdOnError || bookingsByEmpIdOnError));
    }

    const statusMutation = useMutation({
        mutationFn: ({ id, status, reason }: { id: number, status: number, reason: string }) => 
            gameService.updateBookingStatus(id, status, reason, token!),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["Bookings"] }),
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        onError: (error: any) => {
            const data = error.response?.data;  
            const detailedError = typeof data === 'object' 
            ? JSON.stringify(data, null, 2) 
            : data || error.message;
            toast?.error("Failed to update booking status" + detailedError);
        }
    });

    const removeWaitingListMutation = useMutation({
        mutationFn: (id: number) => gameService.deleteWaitingList(id, token!),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["WaitingList", user?.id] }),
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        onError: (error: any) => {
            const data = error.response?.data;  
            const detailedError = typeof data === 'object' 
            ? JSON.stringify(data, null, 2) 
            : data || error.message;
            toast?.error("Failed to remove waiting list item" + detailedError);
        }
    });

    const filteredWaitingList = WaitingListByEmpId.filter((w: WaitingList) => {
        const searchTerm = debouncedWaitingListSearchTerm?.toLowerCase();
        return w.gameTypeName.toLowerCase().includes(searchTerm) ||
            w.targetSlotDatetime.toLowerCase().includes(searchTerm) ||
            w.targetSlotEndDatetime.toLowerCase().includes(searchTerm) ||
            w.hostEmployeeEmail.toLowerCase().includes(searchTerm) ||
            w.bookingParticipantResponses.some((p: BookingParticipantResponse) => p.employeeEmail.toLowerCase().includes(searchTerm));
    });  

    useEffect(() => {
        const clickOutside = (e: MouseEvent) => {
        const target = e.target as HTMLElement;
        if (!target.closest("div.game")) {
            setShowGameIntrestForm(false);
            setShowGameTypeForm(false);
            setShowGameBookingForm(false);
            setShowWaitingList(false);
            setShowNotification(false);
        }
        };
        if (showGameIntrestForm || showGameTypeForm || showGameBookingForm || showWaitingList || showNotification) {
        document.addEventListener("click", clickOutside);
        } else {
        document.removeEventListener("click", clickOutside);
        }
        return () => document.removeEventListener("click", clickOutside);
    }, [showGameIntrestForm, showGameTypeForm, showGameBookingForm, showWaitingList, showNotification]);

    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset className="bg-slate-50">
                <header className="flex h-16 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
                    <div className="flex items-center gap-2">
                        {/* <SidebarTrigger /> */}
                        <h3 className="font-bold">Game Zone</h3>
                        {(debouncedBookingSearchTerm && debouncedBookingSearchTerm.length > 0) ? (
                        <Badge variant="outline">{filteredBookings.length} results</Badge>
                        ) : (debouncedWaitingListSearchTerm && debouncedWaitingListSearchTerm.length > 0) ? (
                        <Badge variant="outline">{filteredWaitingList.length} results</Badge>
                        ) : (debouncedAllBookingsSearchTerm && debouncedAllBookingsSearchTerm.length > 0) ? (
                        <Badge variant="outline">{filteredAllBookings.length} results</Badge>
                        ) : gameBookingStatusId ? (
                        <Badge variant="outline">{bookingsByEmpId?.pages[0]?.totalElements} results</Badge>
                        ) : gameType ? ( 
                            viewMode == "My Bookings" ? (
                            <Badge variant="outline">{bookingsByEmpId?.pages[0]?.totalElements} results</Badge>
                            ) : (
                            <Badge variant="outline">{WaitingListByEmpId.length} results</Badge>
                            )
                        ) : (
                        <Badge variant="outline">No filter</Badge>
                        )}
                        <select className="border rounded-md px-2 py-1 text-sm" value={gameType || 0} onChange={(e) => setGameType(Number(e.target.value))}>
                            <option value="0">All Games</option>
                            {gameTypes.map((g: GameType) => <option key={g.id} value={g.id}>{g.gameName}</option>)}
                        </select>
                    </div>
                    <div className="game flex items-center gap-4">
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

                <main className="p-6 max-w-5xl mx-auto w-254">
                    {/* Notifications */}
                    {showNotification && (
                      <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
                        <div className="game bg-white rounded-xl max-w-3xl w-full relative h-150 overflow-y-auto">
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
                        <div className="game bg-white rounded-xl max-w-3xl w-full relative h-100 overflow-y-auto">
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
                            <div className="game bg-white rounded-xl w-full max-w-2xl relative h-150 overflow-y-auto">     
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
                            <div className="game bg-white rounded-xl max-w-lg w-full relative h-120 overflow-y-auto">
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
                        <div className="game bg-white rounded-xl max-w-lg w-full relative h-max">
                          <Button title="Close Game Interests" variant="ghost" className="absolute right-2 top-2" 
                            onClick={() => {
                            setShowGameIntrestForm(false);
                          }}><X /></Button>
                          <GameInterestToggle />
                        </div>
                      </div>
                    )}

                    {/* upcomming games */}
                    <div className="game grid grid-cols-1 md:grid-cols-2 gap-4">
                        <h2 className="text-xl font-bold text-slate-900 col-span-full">Upcoming Bookings :</h2>
                        
                        {upcomingBookings.map((b: Booking) => (
                                !b.gameBookingIsDeleted && b.gameBookingStatusId === 1 && (
                                    <BookingCard key={b.id} booking={b} onStatusChange={(reason) => statusMutation.mutate({ id: b.id, status: 3, reason})} />
                                )))
                        }
                        {upcomingBookings.length === 0 && (
                            <p className="text-slate-500 italic">No upcoming bookings.</p>
                        )}
                    </div>

                    
                    <div className="game flex items-center justify-between gap-2 my-5">
                        <div className="flex flex-row items-center gap-4">
                            <Button className={viewMode === "My Bookings" ? "rounded-md border text-gray-900" : "rounded-md text-gray-300"}
                                size="sm"
                                onClick={()=>{
                                    setViewMode("My Bookings");
                                    setWaitingListSearchTerm("");
                                }}> My Bookings</Button>
                            <Button className={viewMode === "Waiting List" ? "rounded-md border text-gray-900" : "rounded-md text-gray-300"}
                                size="sm"
                                onClick={()=>{
                                    setViewMode("Waiting List");
                                    setBookingSearchTerm("");
                                }}> Waiting List</Button>
                            {(user?.roleName === "HR" || user?.roleName === "ADMIN") && <Button className={viewMode === "All Bookings" ? "rounded-md border text-gray-900" : "rounded-md text-gray-300"}
                                size="sm"
                                onClick={()=>{
                                    setViewMode("All Bookings");
                                    setBookingSearchTerm("");
                                }}> All Bookings</Button>}
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="relative">
                                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
                                <Input 
                                    placeholder={`Search ${viewMode.toLowerCase()}...`} 
                                    className="pl-9"
                                    value={viewMode === "My Bookings" ? bookingSearchTerm : viewMode === "Waiting List" ? waitingListSearchTerm : allBookingsSearchTerm}
                                    onChange={(e) => {
                                    if(viewMode === "My Bookings") {
                                        setBookingSearchTerm(e.target.value);
                                    } else if(viewMode === "Waiting List") {
                                        setWaitingListSearchTerm(e.target.value);
                                    } else {
                                        setAllBookingsSearchTerm(e.target.value);
                                    }
                                    }}
                                    autoFocus
                                />
                            </div>
                            {viewMode === "My Bookings" || viewMode === "All Bookings" && <select className="border rounded-md px-2 py-1 text-sm" value={gameBookingStatusId || ""} 
                                onChange={(e) => setgameBookingStatusId(Number(e.target.value))}>
                                <option value="0">All Statuses</option>
                                {gameBookingStatusOptions.map((g: GameBookingStatus) => <option key={g.id} value={g.id}>{g.gameBookingStatusName}</option>)}
                            </select>}
                        </div>
                    </div>

                    {/* my bookings  */}
                    {viewMode === "My Bookings" && (
                    <div className="mt-2">
                        <div className="game grid grid-cols-1 md:grid-cols-2 gap-4 my-5">
                        {filteredBookings && filteredBookings.length > 0 ? (
                            <>                                
                                {filteredBookings.map((b: Booking) => 
                                    <BookingCard key={b.id} booking={b} onStatusChange={(reason) => statusMutation.mutate({ id: b.id, status: 3, reason })} />        
                                )}
                            </>
                        ) : (
                            <p className="text-slate-500 italic">You have no bookings yet.</p>
                        )}
                        </div>
                        <div ref={ref} className="h-10 flex justify-center items-center">
                            { isFetchingNextPageByEmpId ? <p className="text-xs">Loading more...</p> : null}
                        </div>
                    </div>
                    )}

                    {/* WaitingList */}
                    {viewMode === "Waiting List" && (
                    <div className="my-5">
                        <div className="game grid grid-cols-1 md:grid-cols-2 gap-4">
                        {filteredWaitingList.length > 0 ? (
                                filteredWaitingList.map((wait: WaitingList) => (
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
                                                {wait?.bookingParticipantResponses?.length > 0 && (
                                                    <div className="text-[10px] text-slate-400 ">
                                                        Participants: {wait?.bookingParticipantResponses?.map((p: BookingParticipantResponse) => p.employeeEmail).join(", ")}
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
                            ) : (
                                <p className="text-slate-500 italic">You are not on any waiting list.</p>
                            )}
                        </div>
                    </div>
                    )}

                    {/* all bookings  */}
                    {viewMode === "All Bookings" && (user?.roleName === "Admin" || user?.roleName === "HR") && (
                    <div className="mt-2">
                        <div className="game grid grid-cols-1 md:grid-cols-2 gap-4 my-5">
                        {filteredAllBookings && filteredAllBookings.length > 0 ? (
                            <>                                
                                {filteredAllBookings.map((b: Booking) => 
                                    <BookingCard key={b.id} booking={b} onStatusChange={(reason) => statusMutation.mutate({ id: b.id, status: 3, reason })} />        
                                )}
                            </>
                        ) : (
                            <p className="text-slate-500 italic">No bookings yet.</p>
                        )}
                        </div>
                        <div ref={ref} className="h-10 flex justify-center items-center">
                            { isFetchingNextPageBookings ? <p className="text-xs">Loading more...</p> : null}
                        </div>
                    </div>
                    )}
                    
                    <ScrollToTop />
                    <GlobalSearch />
                </main>
            </SidebarInset>
        </SidebarProvider>
    );
}

import { useState, useMemo, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { apiService } from "../api/apiService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Card, CardTitle, CardHeader, CardContent } from "./ui/card";
import { Search, User, X, Users } from "lucide-react";
import { cn } from "@/lib/utils";

export default function GameBookingForm({ editBookingId, onSuccess }: { editBookingId?: number | null; onSuccess: () => void }) {
    const { token, user } = useAuth();
    const queryClient = useQueryClient();
    
    const [selectedTime, setSelectedTime] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [showDropdown, setShowDropdown] = useState(false);
    const [selectedParticipants, setSelectedParticipants] = useState<{id: number, name: string}[]>([]);

    const { register, handleSubmit, watch, reset, formState: { errors } } = useForm();

    const { data: gameTypes = [] } = useQuery({ 
        queryKey: ["allGameTypes"], 
        queryFn: () => gameService.getAllGames(token!) });

    const { data: bookingsByEmpId = [] } = useQuery({
        queryKey: ["Bookings", user?.id],
        queryFn: () => gameService.findGameBookingById(user?.id, token!),
    });

    const { data: myInterests = [] } = useQuery({
        queryKey: ["myInterests", user?.id],
        queryFn: () => gameService.getEmployeeGameInterests(user?.id!, token!),
        enabled: !!user?.id
    });

    const gameTypeId = Number(watch("gameTypeId"));
    const date = watch("date");

    const { data: slots = [] } = useQuery({
        queryKey: ["availableSlots", gameTypeId, date],
        queryFn: () => gameService.getAvalaibleSlots(gameTypeId, user?.id!, date, token!),
        enabled: !!gameTypeId && !!date
    });

    
    const mutation = useMutation({
        mutationFn: (data: any) => editBookingId 
            ? gameService.updateBooking(editBookingId, data, token!) 
            : gameService.addBooking(data, token!),
        onSuccess: (data) => {
            window.alert(typeof data === "string" ? data : "Booking saved!");
            queryClient.invalidateQueries({ queryKey: ["allBookings"] });
            queryClient.invalidateQueries({ queryKey: ["allWaitingList"] });
            onSuccess();
        },
        onError: (error: any) => {
        alert(error.response?.data?.message || "Booking failed");
    }
    });

    const filteredGames = useMemo(() => {
        const interestIds = myInterests.filter((i: any) => !i.interestDeleted).map((i: any) => i.gameTypeId);
        return gameTypes.filter((g: any) => interestIds.includes(g.id));
    }, [gameTypes, myInterests]);

        const selectedGameId = watch("gameTypeId");
    const selectedDate = watch("date");
    const selectedGame = filteredGames.find((g: any) => g.id === Number(selectedGameId));

    const slotStartDateTime = selectedDate ? `${selectedDate} ${`${selectedTime}:00.0000000` || "00:00:00.0000000"}` : null;

    const {
        data: infiniteData,
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage
    } = useInfiniteQuery({
        queryKey: ["employeeSearchInfinite", searchTerm, slotStartDateTime, selectedGameId],
        queryFn: ({ pageParam = 0 }) => 
        apiService.searchParticipants(searchTerm, pageParam, 10, slotStartDateTime || "", selectedGameId || 0, token || ""),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => 
        lastPage.last ? undefined : lastPage.number + 1,
        enabled: searchTerm.length >= 1 && !!slotStartDateTime && !!selectedGameId,
    });
    const suggestions = infiniteData?.pages.flatMap(page => page.content) || [];

    useEffect(() => {
        if (editBookingId) {
            const booking = bookingsByEmpId.find((b: any) => b.id === editBookingId);
            if (booking) {
                const startTime = new Date(booking.gameBookingStartTime);
                const timeStr = `${startTime.getHours().toString().padStart(2, '0')}:${startTime.getMinutes().toString().padStart(2, '0')}`;
                
                setSelectedTime(timeStr);
                const participants = booking.bookingParticipantResponses?.map((p: any) => ({ id: p.employeeId, name: `${p.employeeFirstName} ${p.employeeLastName}` })) || [];
                setSelectedParticipants(participants);

                reset({
                    gameTypeId: booking.gameTypeId,
                    date: booking.gameBookingStartTime.split("T")[0],
                });
            }
        }
    }, [editBookingId, bookingsByEmpId, reset]);

    const handleAddParticipant = (emp: any) => {
        if(!selectedGame) {
            window.alert("Please select a game first");
            return;
        }
        if (!selectedParticipants.find(p => p.id === emp.id)) {
            setSelectedParticipants([...selectedParticipants, { id: emp.id, name: `${emp.employeeFirstName} ${emp.employeeLastName}` }]);
        }
        setSearchTerm("");
        setShowDropdown(false);
    };

    const removeParticipant = (id: number) => {
        setSelectedParticipants(selectedParticipants.filter(p => p.id !== id));
    };

    const onSubmit = (data: any) => {
        if (!selectedTime) return alert("Please select a slot");
        if (selectedParticipants.length === 0) return alert("Please add at least one participant");

        const payload = {
            empId: user?.id,
            gameTypeId: Number(data.gameTypeId),
            requestedSlotStartTime: `${data.date}T${selectedTime}:00`,
            bookingParticipantsEmpId: selectedParticipants.map(p => p.id)
        };
        mutation.mutate(payload);
    };

    return (
        <Card className="border-none shadow-none">
            <CardHeader><CardTitle>{editBookingId ? "Edit Booking" : "Book a Game Slot"}</CardTitle></CardHeader>
            <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 p-2">
                <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <label className="text-xs font-bold uppercase text-slate-500">Select Game</label>
                        <select {...register("gameTypeId", { required: true })} className="w-full border rounded-md p-2 text-sm h-10">
                            <option value=""> Choose Game </option>
                            {filteredGames.map((g: any) => <option key={g.id} value={g.id}>{g.gameName}</option>)}
                        </select>
                        {errors.gameTypeId && <p className="text-red-500 text-xs">Game selection is required.</p>}
                        {filteredGames.length === 0 && <p className="text-xs text-gray-500">You haven't marked any interests yet! Please mark them first.</p>}
                    </div>
                    <div className="space-y-2">
                        <label className="text-xs font-bold uppercase text-slate-500">Date</label>
                        <Input type="date" {...register("date", { required: true })} min={new Date().toISOString().split("T")[0]} />
                        {errors.date && <p className="text-red-500 text-xs">Date is required.</p>}
                    </div>
                </div>

                {/* slot selection */}
                {selectedGame && selectedDate && (
                    <div className="border rounded-lg overflow-hidden">
                        <Table>
                            <TableHeader className="bg-slate-50">
                                <TableRow>
                                    <TableHead className="text-center">Available Slots ({selectedGame.gameName})</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                <TableRow>
                                    <TableCell className="p-4">
                                        <div className="grid grid-cols-5 gap-2 p-4">
                                            {slots?.map((slot: any) => {                                                
                        
                                                const isSelected = selectedTime === slot.time;
                                                const isPersonallyBusy = slot.status === "BUSY";
                                                const isBooked = slot.status === "FULL";
                                                const isWaited = slot.status === "WAIT";

                                                return (
                                                    <Button
                                                        key={slot.time}
                                                        type="button"
                                                        title={isPersonallyBusy ? "you are not avalible at this time." : ""}
                                                        disabled={isPersonallyBusy || isWaited}
                                                        className={cn(
                                                            "text-xs h-10 transition-all border-2",                    
                                                            isBooked && "bg-slate-200 text-slate-500",                                         
                                                            isPersonallyBusy || isWaited && "bg-slate-200 text-slate-500 cursor-not-allowed",                                                    
                                                            isSelected && "bg-gray-900 text-black",
                                                            !isBooked && !isSelected && "text-slate-500"
                                                        )}
                                                        onClick={() => setSelectedTime(slot.time)}
                                                    >
                                                        {slot.time}
                                                        {isPersonallyBusy ? <span className="block text-[8px] mt-1 uppercase">Busy</span>
                                                        : isWaited ? <span className="block text-[8px] mt-1 uppercase">Wait</span>
                                                        : isBooked ? <span className="block text-[8px] mt-1 uppercase">Full</span>
                                                        : null}
                                                        
                                                    </Button>
                                                );
                                            })}
                                        </div>
                                    </TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                        {errors.slot && <p className="text-red-500 text-xs">Please select a slot.</p>}
                    </div>
                )}

                {/* Participant Multi-Search */}
                <div className="space-y-3">
                    <label className="text-xs font-bold uppercase text-slate-500 flex items-center gap-2"><Users size={14}/> Participants</label>
                    {selectedGame && selectedParticipants.length < selectedGame.gameMaxPlayerPerSlot-1 ? 
                    <div className="relative">
                        <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
                        <Input placeholder="Search participants..."
                            disabled={!selectedTime} 
                            className="pl-9" value={searchTerm}
                            onChange={(e) => { setSearchTerm(e.target.value); setShowDropdown(true); }}
                            onFocus={() => setShowDropdown(true)}
                        />

                        {showDropdown && suggestions.length > 0 && (
                            <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-40 overflow-y-auto">
                                {suggestions.map((emp: any,) => {

                                if (emp.id === user?.id || selectedParticipants.find(e => e.id === emp.id)) return null;

                                return (
                                <button
                                    key={emp.id}
                                    className="w-full text-left px-4 py-2 flex items-center gap-3 border-b last:border-none transition-colors"
                                    onClick={() => handleAddParticipant(emp)}
                                >
                                    <User size={14} className="text-blue-600" />
                                    <div className="flex flex-col">
                                        <span className="text-sm font-medium">{emp.employeeFirstName} {emp.employeeLastName}</span>
                                    </div>
                                </button>
                                );
                                })} 

                                {hasNextPage && (
                                <Button
                                    variant="ghost"
                                    className="w-full text-[10px] text-blue-600 h-8"
                                    onClick={() => fetchNextPage()}
                                    disabled={isFetchingNextPage}
                                >
                                    {isFetchingNextPage ? "Loading more..." : "Show More Results"}
                                </Button>
                                )}
                            </div>
                        )}
                    </div>
                    : <p className="text-xs text-gray-500">
                        {selectedParticipants.length > 0 ?
                            <p>
                                {selectedGame?.gameName} Game only allow maximum {selectedGame?.gameMaxPlayerPerSlot} player per game slot.
                            </p>
                            : null
                        }      
                      </p>}

                    {/* Selected Participant */}
                    <div className="flex flex-wrap gap-2 min-h-[40px] p-2 bg-slate-50 rounded-lg border border-dashed">
                        {selectedParticipants.length === 0 && <p className="text-xs text-slate-400">No participants added.</p>}
                        {selectedParticipants.map((p) => (
                            <Badge key={p.id} variant="secondary" className="pl-2 pr-1 py-1 gap-1 bg-white border shadow-sm">
                                <span className="text-xs font-medium">{p.name}</span>
                                <button 
                                type="button" 
                                onClick={() => removeParticipant(p.id)}
                            >
                                <X size={12} className="text-red-500" />
                            </button>
                            </Badge>
                        ))}
                        {errors.employeesInTravelPlanId && <p className="text-red-500 text-xs">At least one employee must be assigned.</p>}
                    </div>
                </div>
                <Button type="submit" className="w-full text-black h-12 text-lg" disabled={mutation.isPending || !selectedTime  || selectedParticipants.length === 0}>
                    {mutation.isPending ? "Saving..." : editBookingId ? "Update Booking" : `Confirm ${selectedTime || ''} Slot`}
                </Button>
            </form>
            </CardContent>
        </Card>
    );
}

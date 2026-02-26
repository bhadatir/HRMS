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
import { cn } from "@/lib/utils";
import { Card, CardTitle, CardHeader, CardContent } from "./ui/card";
import { Search, User, X, Users } from "lucide-react";

export default function GameBookingForm({ editBookingId, onSuccess }: { editBookingId?: number | null; onSuccess: () => void }) {
    const { token, user } = useAuth();
    const queryClient = useQueryClient();
    
    const [selectedTime, setSelectedTime] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [showDropdown, setShowDropdown] = useState(false);
    const [selectedParticipants, setSelectedParticipants] = useState<{id: number, name: string}[]>([]);

    const { register, handleSubmit, watch, setValue, reset, formState: { errors } } = useForm();

    const { data: gameTypes = [] } = useQuery({ 
        queryKey: ["allGameTypes"], 
        queryFn: () => gameService.getAllGames(token!) });

    const { data: allBookings = [] } = useQuery({ 
        queryKey: ["allBookings"], 
        queryFn: () => gameService.showAllBookings(token!) });

    const { data: allWaitingList = [] } = useQuery({ 
        queryKey: ["allWaitingList"], 
        queryFn: () => gameService.getAllWaitingList(token!) });

    const {
        data: infiniteData,
        fetchNextPage,
        hasNextPage,
        isFetchingNextPage
    } = useInfiniteQuery({
        queryKey: ["employeeSearchInfinite", searchTerm],
        queryFn: ({ pageParam = 0 }) => 
        apiService.searchEmployees(searchTerm, pageParam, 10, token || ""),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => 
        lastPage.last ? undefined : lastPage.number + 1,
        enabled: searchTerm.length >= 1,
    });
    const suggestions = infiniteData?.pages.flatMap(page => page.content) || [];

    const { data: myInterests = [] } = useQuery({
        queryKey: ["myInterests", user?.id],
        queryFn: () => gameService.getEmployeeGameInterests(user?.id!, token!),
        enabled: !!user?.id
    });

    const filteredGames = useMemo(() => {
        const interestIds = myInterests.map((i: any) => i.gameTypeId);
        return gameTypes.filter((g: any) => interestIds.includes(g.id));
    }, [gameTypes, myInterests]);

    const selectedGameId = watch("gameTypeId");
    const selectedDate = watch("date");
    const selectedGame = filteredGames.find((g: any) => g.id === Number(selectedGameId));

    useEffect(() => {
        if (editBookingId) {
            const booking = allBookings.find((b: any) => b.id === editBookingId);
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
    }, [editBookingId, allBookings, reset]);

    const slots = useMemo(() => {
        if (!selectedGame) return [];
        const items = [];
        const start = selectedGame.operatingStart.split(":");
        const end = selectedGame.operatingEnd.split(":");
        let current = parseInt(start[0]) * 60 + parseInt(start[1]);
        const endTime = parseInt(end[0]) * 60 + parseInt(end[1]);
        while (current < endTime) {
            const h = Math.floor(current / 60).toString().padStart(2, '0');
            const m = (current % 60).toString().padStart(2, '0');
            items.push(`${h}:${m}`);
            current += selectedGame.gameSlotDuration;
        }
        return items;
    }, [selectedGame]);

    // if slot this perticular sloat book by other so in sloat selection show this slot is full
    const bookedTimes = useMemo(() => {
        if (!selectedDate || !selectedGameId) return new Set();
        return new Set(
            allBookings
                .filter((b: any) => !b.gameBookingIsDeleted &&
                     b.gameBookingStatusId === 1 &&
                     b.gameTypeId === Number(selectedGameId) 
                    && b.gameBookingStartTime.startsWith(selectedDate) && b.id !== editBookingId)
                .map((b: any) => {
                    const dateObj = new Date(b.gameBookingStartTime);
                    return `${dateObj.getHours().toString().padStart(2, '0')}:${dateObj.getMinutes().toString().padStart(2, '0')}`;
                })
        );
    }, [allBookings, selectedDate, selectedGameId, editBookingId]);

    // if i am in waiting list for 11-12 for any one game and then i try to book full sloat 11:30-12:00 so it not give me this access to wait for this sloat 
    const waitedTimes = useMemo(() => {
        if (!selectedDate || !user?.id) return [];
        return allWaitingList.filter((w: any) => 
                w.waitingStatusIsActive &&
                w.targetSlotDatetime.startsWith(selectedDate) &&
                (w.hostEmployeeId === user.id || w.bookingParticipantResponses?.some((p: any) => p.employeeId === user.id))
            ).map((w: any) => {
                const start = new Date(w.targetSlotDatetime);
                return {
                start: start.getHours() * 60 + start.getMinutes(),
                end: start.getHours() * 60 + start.getMinutes() + w.gameSlotDuration
                };
            });
    }, [allWaitingList, selectedDate, user?.id]);

    // this is for like if i book 11-12 and then try to book 11:30-12:00 in other game then 11:30 should be marked as busy slot.
    const myBusySlots = useMemo(() => {
        if (!selectedDate || !user?.id) return [];
        return allBookings.filter((b: any) => 
            !b.gameBookingIsDeleted && 
            b.gameBookingStatusId === 1 &&
            b.gameBookingStartTime.startsWith(selectedDate) &&
            (b.employeeId === user.id || b.bookingParticipantResponses?.some((p: any) => p.employeeId === user.id))
        ).map((b: any) => {
            const start = new Date(b.gameBookingStartTime);
            const end = new Date(b.gameBookingEndTime);
            return {
            start: start.getHours() * 60 + start.getMinutes(),
            end: end.getHours() * 60 + end.getMinutes()
            };
        });
    }, [allBookings, selectedDate, user?.id]);

    // if i book sloat with Participant but Participant is in other waiting list so that employee mark as In Waiting List
    const busyWaitingEmployeeIds = useMemo(() => {
        if (!selectedDate || !selectedTime || !selectedGame) return new Set<number>();

        const selectedStartStr = `${selectedDate}T${selectedTime}:00`;
        const selectedStart = new Date(selectedStartStr).getTime();
        const selectedEnd = selectedStart + (selectedGame.gameSlotDuration * 60000);

        const busyIds = new Set<number>();

        allWaitingList.forEach((w: any) => {
            if (!w.waitingStatusIsActive) return;

            const wStart = new Date(w.targetSlotDatetime).getTime();
            const wEnd = new Date(w.targetSlotDatetime).getTime() + (w.gameSlotDuration * 60000);

            const isOverlapping = (selectedStart < wEnd && selectedEnd > wStart);

            if (isOverlapping) {
                busyIds.add(w.hostEmployeeId);
                w.bookingParticipantResponses?.forEach((p: any) => busyIds.add(p.employeeId));
            }
        });

        return busyIds;
    }, [allWaitingList, selectedDate, selectedTime, selectedGame]);

    // if i book sloat with Participant but Participant is in other booking so that employee mark as Already in a game
    const busyEmployeeIds = useMemo(() => {
        if (!selectedDate || !selectedTime || !selectedGame) return new Set<number>();

        const selectedStartStr = `${selectedDate}T${selectedTime}:00`;
        const selectedStart = new Date(selectedStartStr).getTime();
        const selectedEnd = selectedStart + (selectedGame.gameSlotDuration * 60000);

        const busyIds = new Set<number>();

        allBookings.forEach((b: any) => {
            if (b.gameBookingIsDeleted || b.gameBookingStatusId !== 1) return;

            const bStart = new Date(b.gameBookingStartTime).getTime();
            const bEnd = new Date(b.gameBookingEndTime).getTime();

            const isOverlapping = (selectedStart < bEnd && selectedEnd > bStart);

            if (isOverlapping) {
                busyIds.add(b.employeeId);
                b.bookingParticipantResponses?.forEach((p: any) => busyIds.add(p.employeeId));
            }
        });

        return busyIds;
    }, [allBookings, selectedDate, selectedTime, selectedGame]);

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

    const mutation = useMutation({
        mutationFn: (data: any) => editBookingId 
            ? gameService.updateBooking(editBookingId, data, token!) 
            : gameService.addBooking(data, token!),
        onSuccess: (data) => {
            window.alert(typeof data === "string" ? data : "Booking saved!");
            queryClient.invalidateQueries({ queryKey: ["allBookings"] });
            queryClient.invalidateQueries({ queryKey: ["allWaitingList"] });
            onSuccess();
        }
    });

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
                                            {slots.map((slot) => {
                                                
                                                const slotMinutes = parseInt(slot.split(":")[0]) * 60 + parseInt(slot.split(":")[1]);
                                                const isPersonallyBusy = myBusySlots.some((busy: any) => 
                                                slotMinutes >= busy.start && slotMinutes < busy.end
                                                );
                                                const isWaited = waitedTimes.some((wait: any) => 
                                                slotMinutes >= wait.start && slotMinutes < wait.end
                                                );

                                                const slotDateTime = new Date(`${selectedDate}T${slot}:00`);
                                                if (slotDateTime < new Date()) return null;
                                                
                                                const isBooked = bookedTimes.has(slot);
                                                const isSelected = selectedTime === slot;

                                                return (
                                                    <Button
                                                        key={slot}
                                                        type="button"
                                                        title={isPersonallyBusy ? "you are not avalible at this time." : ""}
                                                        disabled={isPersonallyBusy || isWaited}
                                                        className={cn(
                                                            "text-xs h-10 transition-all border-2",                                                           isBooked && "bg-slate-200 text-slate-500",        
                                                            isPersonallyBusy || isWaited && "bg-slate-200 text-slate-500 cursor-not-allowed",                                                    
                                                            isSelected && "bg-gray-900 text-black",
                                                            !isBooked && !isSelected && "text-slate-500"
                                                        )}
                                                        onClick={() => setSelectedTime(slot)}
                                                    >
                                                        {slot}
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
                                const isBusy = busyEmployeeIds.has(emp.id) || busyWaitingEmployeeIds.has(emp.id);

                                if (emp.id === user?.id || selectedParticipants.find(e => e.id === emp.id)) return null;

                                return (
                                <button
                                    key={emp.id}
                                    className={cn(
                                                "w-full text-left px-4 py-2 flex items-center gap-3 border-b last:border-none transition-colors",
                                                isBusy ? "bg-slate-50 opacity-60 cursor-not-allowed" : ""
                                            )}
                                    onClick={() => !isBusy && handleAddParticipant(emp)}
                                    disabled={isBusy}
                                >
                                    <User size={14} className={isBusy ? "text-slate-400" : "text-blue-600"} />
                                    <div className="flex flex-col">
                                        <span className="text-sm font-medium">{emp.employeeFirstName} {emp.employeeLastName}</span>
                                        {isBusy ? busyWaitingEmployeeIds.has(emp.id) ? <span className="text-[10px] text-orange-500 font-bold">In waiting list</span> 
                                         : <span className="text-[10px] text-red-500 font-bold">Already in a game</span>
                                         : <span className="text-[10px] text-green-500 font-bold">Available</span>}
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

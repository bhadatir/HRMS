import { useState, useMemo, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { apiService } from "../api/apiService"; // Assuming this handles employee search
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
    
    // States for Time and Employee Selection
    const [selectedTime, setSelectedTime] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [showDropdown, setShowDropdown] = useState(false);
    const [selectedParticipants, setSelectedParticipants] = useState<{id: number, name: string}[]>([]);

    const { register, handleSubmit, watch, setValue, reset, formState: { errors } } = useForm();

    // 1. Fetch Game Types and All Bookings
    const { data: gameTypes = [] } = useQuery({ 
        queryKey: ["allGameTypes"], 
        queryFn: () => gameService.getAllGames(token!) });
    const { data: allBookings = [] } = useQuery({ 
        queryKey: ["allBookings"], 
        queryFn: () => gameService.showAllBookings(token!) });

    // 2. Employee Search Query
    const { data: suggestions } = useQuery({
        queryKey: ["employeeSearch", searchTerm],
        queryFn: () => apiService.searchEmployees(searchTerm, token || ""),
        enabled: searchTerm.length >= 1,
    });

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

    // 3. Handle Edit Mode Loading
    useEffect(() => {
        if (editBookingId) {
            const booking = allBookings.find((b: any) => b.id === editBookingId);
            if (booking) {
                const startTime = new Date(booking.gameBookingStartTime);
                const timeStr = `${startTime.getHours().toString().padStart(2, '0')}:${startTime.getMinutes().toString().padStart(2, '0')}`;
                
                setSelectedTime(timeStr);
                // Note: Ensure your backend booking response includes participant names
                const participants = booking.participants?.map((p: any) => ({ id: p.id, name: p.name })) || [];
                setSelectedParticipants(participants);

                reset({
                    gameTypeId: booking.gameTypeId,
                    date: booking.gameBookingStartTime.split("T")[0],
                });
            }
        }
    }, [editBookingId, allBookings, reset]);

    // 4. Slots & Booked Logic
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

    const bookedTimes = useMemo(() => {
        if (!selectedDate || !selectedGameId) return new Set();
        return new Set(
            allBookings
                .filter((b: any) => !b.gameBookingIsDeleted && b.gameTypeId === Number(selectedGameId) && b.gameBookingStartTime.startsWith(selectedDate) && b.id !== editBookingId)
                .map((b: any) => {
                    const dateObj = new Date(b.gameBookingStartTime);
                    return `${dateObj.getHours().toString().padStart(2, '0')}:${dateObj.getMinutes().toString().padStart(2, '0')}`;
                })
        );
    }, [allBookings, selectedDate, selectedGameId, editBookingId]);

    // 5. Participant Selection Logic
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

    // 6. Final Mutation
    const mutation = useMutation({
        mutationFn: (data: any) => editBookingId 
            ? gameService.updateBooking(editBookingId, data, token!) // Assuming update method exists
            : gameService.addBooking(data, token!),
        onSuccess: (data) => {
            window.alert(typeof data === "string" ? data : "Booking saved!");
            queryClient.invalidateQueries({ queryKey: ["allBookings"] });
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
                                        <div className="grid grid-cols-4 gap-2 p-4">
                                            {slots.map((slot) => {
                                                
                                                const slotDateTime = new Date(`${selectedDate}T${slot}:00`);
                                                if (slotDateTime < new Date()) return null;

                                                const isBooked = bookedTimes.has(slot);
                                                const isSelected = selectedTime === slot;

                                                return (
                                                    <Button
                                                        key={slot}
                                                        type="button"
                                                        variant={isSelected ? "default" : "outline"}
                                                        className={cn(
                                                            "text-xs h-10 transition-all border-2",
                                                            isBooked && "bg-slate-200 text-slate-400 border-slate-300 cursor-not-allowed",
                                                            isSelected && "bg-gray-600 text-black border-blue-700",
                                                            !isBooked && !isSelected && "hover:border-blue-500 text-slate-700"
                                                        )}
                                                        onClick={() => setSelectedTime(slot)}
                                                    >
                                                        {slot}
                                                        {isBooked && <span className="block text-[8px] mt-1 uppercase">Full</span>}
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
                        <Input placeholder="Search participants..." className="pl-9" value={searchTerm}
                            onChange={(e) => { setSearchTerm(e.target.value); setShowDropdown(true); }}
                            onFocus={() => setShowDropdown(true)}
                        />
                        {showDropdown && suggestions && suggestions.length > 0 &&(
                            <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-48 overflow-auto">
                                {suggestions.map((emp: any) => (
                                    <button key={emp.id} type="button" className="w-full text-left px-4 py-2 hover:bg-slate-50 flex items-center gap-3 border-b"
                                        onClick={() => handleAddParticipant(emp)}>
                                        <User size={14} className="text-blue-600" />
                                        <span className="text-sm">{emp.employeeFirstName} {emp.employeeLastName}</span>
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                    : <p className="text-xs text-gray-500   ">
                        {selectedParticipants.length > 0 ?
                            <p>
                                {selectedGame.gameName} Game only allow maximum {selectedGame.gameMaxPlayerPerSlot} player per game slot.
                            </p>
                            : null
                        }      
                      </p>}

                    {/* Selected Participant Chips */}
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

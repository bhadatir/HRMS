
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Calendar, Clock, CheckCircle } from "lucide-react";
import { useState } from "react";
import GameBookingForm from "./GameBookingForm";
import { useAuth } from "@/context/AuthContext";
import { gameService } from "@/api/gameService";
import { useQuery } from "@tanstack/react-query";
import { useToast } from "@/context/ToastContext";
import { ConformationDialog } from "./ConformationDialog";

type Booking = {
    id: number;
    gameBookingIsDeleted: boolean;
    gameBookingStartTime: string;
    gameBookingEndTime: string;
    gameBookingStatusId: number;
    gameTypeId: number;
    gameTypeName: string;
    gameSlotDuration: number;
    employeeId: number;
    employeeEmail: string;
    gameBookingCreatedAt: string;
    bookingParticipantResponses: BookingParticipantResponse[];
}

type BookingParticipantResponse = {
    employeeEmail: string;
}

type GameBookingStatus = {
    id: number;
    gameBookingStatusName: string;
}

export default function BookingCard({ booking, onStatusChange }: { booking: Booking, onStatusChange: (reason: string) => void }) {
    const [showGameBookingForm, setShowGameBookingForm] = useState(false);
    const { user, token } = useAuth();
    const toast = useToast();
    const [isDialogOpen, setIsDialogOpen] = useState(false);

    const { data: gameBookingStatusOptions = [], isError: gameBookingStatusOptionsError } = useQuery({
        queryKey: ["gameBookingStatusOptions"],
        queryFn: () => gameService.getAllGameBookingStatus(token!)
    });

    if (gameBookingStatusOptionsError) toast?.error("Failed to load booking status options: " + gameBookingStatusOptionsError);

    return (
        <>
        {/* Confirmation Dialog */}
        {isDialogOpen && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="post bg-white bottom-52 rounded-xl max-w-lg w-full relative">
              <ConformationDialog
                onClose={() => setIsDialogOpen(false)} 
                onConfirm={(reason) => onStatusChange(`${reason} (Cancelled by : ${user?.employeeEmail} at ${new Date().toLocaleString()})`)} 
                iteam="game booking"
                action="Cancel"
              />
            </div>
          </div>
        )}

        {/* add game booking */}
        {showGameBookingForm && (
            <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                <div className="bg-white rounded-xl w-full max-w-md relative h-150 overflow-y-auto">     
                    <Button variant="ghost" className="absolute top-2 right-2" 
                    onClick={() => {
                        setShowGameBookingForm(false);
                    }}>X</Button>
                    <GameBookingForm editBookingId={booking.id} onSuccess={() => setShowGameBookingForm(false)} />
                </div>
            </div>
        )}
        <Card key={booking.id} className="hover:shadow-md transition-shadow border-slate-200">
            <CardContent className="p-4 space-y-3">
            <div className="flex justify-between items-start">
                    <Badge variant="outline">Game Name: {booking.gameTypeName}</Badge>
                    <Badge variant="outline" className="capitalize">{gameBookingStatusOptions.find((s: GameBookingStatus) => s.id === booking.gameBookingStatusId)?.gameBookingStatusName || "Unknown Status"}</Badge>   
            </div>
            
            <div className="flex items-center gap-2 text-sm font-semibold">
                <Calendar size={14} /> {new Date(booking.gameBookingStartTime).toLocaleDateString()}
            </div>
            <div className="flex items-center gap-2 text-sm text-slate-500">
                <Clock size={14} /> {new Date(booking.gameBookingStartTime).toTimeString().slice(0,5)}  
                - {new Date(booking.gameBookingEndTime).toTimeString().slice(0,5)}
                {(() => {
                    const startDate = new Date(booking.gameBookingStartTime);
                    const endDate = new Date(booking.gameBookingEndTime);
                    const running = (startDate.getTime() < new Date().getTime() && endDate.getTime() > new Date().getTime() && booking.gameBookingStatusId === 1 && !booking.gameBookingIsDeleted);
                    return running ? " Running..." : null;
                })()}
            </div>

            <div className="pt-2 border-t flex justify-between items-center">
                <div className="space-y-1">
                    <div className="text-[10px] text-slate-400">Host: {booking.employeeEmail}</div>
                    {booking?.bookingParticipantResponses?.length > 0 && (
                        <div className="text-[10px] text-slate-400 ">
                            Participants: {booking.bookingParticipantResponses.map((p: BookingParticipantResponse) => p.employeeEmail).join(", ")}
                        </div>
                    )}
                </div>
                
                {booking.gameBookingStatusId === 1 && (booking.employeeId === user?.id || user?.roleName === "Admin" || user?.roleName === "HR")
                    && !booking.gameBookingIsDeleted && new Date(booking.gameBookingStartTime) > new Date() ? (
                    <div className="flex gap-2">
                        <Button size="sm" variant="outline" className="h-7 text-red-600" 
                        onClick={(e) => {
                            e.stopPropagation();
                            setIsDialogOpen(true);
                        }}>
                            <CheckCircle size={14} className="mr-1"/> Cancel
                        </Button>
                        {/* <Button size="sm" variant="outline" className="h-7 text-gray-600" 
                        onClick={() => {
                            setShowGameBookingForm(true);
                        }}>
                            <Edit size={14} className="mr-1"/> Edit
                        </Button> */}
                    </div>
                ):null}
            </div>
        </CardContent>
        </Card>
        </>
    );
}
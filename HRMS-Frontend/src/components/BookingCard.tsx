
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Calendar, Clock, CheckCircle } from "lucide-react";

export default function BookingCard({ booking, onStatusChange }: { booking: any, onStatusChange: () => void }) {
    return (
        <Card key={booking.id} className="hover:shadow-md transition-shadow border-slate-200">
            <CardContent className="p-4 space-y-3">
                <div className="flex justify-between items-start">
                    <Badge variant="outline">Game Name: {booking.gameTypeName}</Badge>
                    {booking.isSecondTimePlay && <Badge className="bg-orange-100 text-orange-700">2nd Play</Badge>}
                    <Badge className={booking.gameBookingStatusId === 1 ? "bg-green-100 text-green-700" : booking.gameBookingStatusId === 2 ? "bg-blue-100 text-blue-700" : "bg-red-100 text-red-700"}>
                    {booking.gameBookingStatusId === 1 ? "Confirmed" : booking.gameBookingStatusId === 2 ? "Completed" : "Cancelled"}
                </Badge>    
            </div>
            
            <div className="flex items-center gap-2 text-sm font-semibold">
                <Calendar size={14} /> {new Date(booking.gameBookingStartTime).toLocaleDateString()}
            </div>
            <div className="flex items-center gap-2 text-sm text-slate-500">
                <Clock size={14} /> {new Date(booking.gameBookingStartTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} 
                - {new Date(booking.gameBookingEndTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
            </div>

            <div className="pt-2 border-t flex justify-between items-center">
                <div className="space-y-1">
                    <div className="text-[10px] text-slate-400">Host: {booking.employeeEmail}</div>
                    {booking.bookingParticipantResponses.length > 0 && (
                        <div className="text-[10px] text-slate-400 ">
                            Participants: {booking.bookingParticipantResponses.map((p: any) => p.employeeEmail).join(", ")}
                        </div>
                    )}
                </div>
                
                {booking.gameBookingStatusId === 1 && (
                    <div className="flex gap-2">
                        <Button size="sm" variant="outline" className="h-7 text-red-600" 
                        onClick={() => {
                            if (confirm("Are you sure you want to cancel this booking?")) {
                                onStatusChange();
                            }
                        }}>
                            <CheckCircle size={14} className="mr-1"/> Cancel
                        </Button>
                    </div>
                )}
            </div>
        </CardContent>
        </Card>
    );
}
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { gameService } from "@/api/gameService";
import { Calendar, Clock, Search } from "lucide-react";
import { Input } from "./ui/input";
import { useState } from "react";

export default function WaitingList({waitingListId, onSuccess}: {waitingListId: number, onSuccess: () => void}) {
  const { token } = useAuth();
  const [waitingListSearchTerm, setWaitingListSearchTerm] = useState("");

  const { data: waitingList } = useQuery({
    queryKey: ["waitingList", waitingListId],
    queryFn: () => gameService.getWaitingListById(waitingListId, token || ""),
    enabled: !!waitingListId && !!token,
  });

  const { data: waitingListSeq } = useQuery({
    queryKey: ["waitingListSeq", waitingListId],
    queryFn: () => gameService.getWaitingListSeqById(waitingListId, token || ""),
    enabled: !!waitingListId && !!token,
  });

  const filteredWaitingListSeq = waitingListSeq?.filter((wls: any) =>
    wls.hostEmployeeEmail.toLowerCase().includes(waitingListSearchTerm.toLowerCase()) ||
    wls.bookingParticipantResponses.some((p: any) => p.employeeEmail.toLowerCase().includes(waitingListSearchTerm.toLowerCase())) ||
    wls.waitingListCreatedAt.toLowerCase().includes(waitingListSearchTerm.toLowerCase())
  );

  return (
        <>
        <Card className="border-none shadow-none">
            <CardHeader className="flex flex-row items-center justify-between space-y-0">
              <div>
                <CardTitle className="text-2xl font-bold text-slate-900 flex gap-2">
                    <div className="flex justify-between items-start">
                        Game Name : {waitingList?.gameTypeName}
                    </div>
                </CardTitle>
                <CardDescription className="mt-1 flex gap-5">                   
                    <div className="flex items-center gap-2 text-sm font-semibold">
                        <Calendar size={14} /> {new Date(waitingList?.targetSlotDatetime).toLocaleDateString()}
                    </div>
                    <div className="flex items-center gap-2 text-sm text-slate-500">
                        <Clock size={14} /> {new Date(waitingList?.targetSlotDatetime).toTimeString().slice(0,5)} 
                        - {(() => {
                            const startDate = new Date(waitingList?.targetSlotDatetime);
                            const endDate = new Date(startDate.getTime() + waitingList?.gameSlotDuration * 60000);
                            const endHour = endDate.getHours();
                            const endMinute = endDate.getMinutes();
                            return `${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}`;
                        })()}
                    </div>
                </CardDescription>
              </div>
              <div className="relative">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
                <Input
                  placeholder="Search Waiting List..."
                  className="pl-9"
                  value={waitingListSearchTerm}
                  onChange={(e) => {                  
                    setWaitingListSearchTerm(e.target.value);
                  }}
                />
              </div>
              <Badge variant="outline" className="text-blue-600 border-blue-200 bg-blue-50 mt-2">
                {waitingList?.waitingStatusIsActive ? "Active" : "Inactive"}
              </Badge>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow className="bg-slate-50">
                      <>
                        <TableHead className="text-center">Rank</TableHead>
                        <TableHead className="text-center">Host</TableHead>
                        <TableHead className="text-center">Created At</TableHead>
                        <TableHead className="text-center">Is FirstGame</TableHead>
                        <TableHead className="text-center">Participates</TableHead>
                      </>
                  </TableRow>
                </TableHeader>
                <TableBody>
                    {filteredWaitingListSeq?.map((wls: any, index: number) => (
                    <TableRow key={wls.id}>
                      <TableCell>{index + 1}</TableCell>
                      <TableCell>{wls.hostEmployeeEmail}</TableCell>
                      <TableCell>
                        <p className="font-medium">{wls.waitingListCreatedAt?.split("T")[0]}</p>
                      </TableCell>
                      <TableCell className="font-bold flex text-slate-900">{wls.isFirstGame ? "Yes" : "No"}</TableCell>
                      <TableCell className="text-slate-900">
                        {wls.bookingParticipantResponses.map((p: any) => 
                            <div key={p.id} className="text-sm text-slate-500">
                                {p.employeeEmail}
                            </div>
                        )}
                      </TableCell>
                    </TableRow>
                    ))}
                </TableBody>
              </Table>
            </CardContent>
        </Card>

        </>
  );
}
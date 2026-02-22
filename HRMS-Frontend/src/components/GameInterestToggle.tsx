import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { useAuth } from "../context/AuthContext";
import { Badge } from "@/components/ui/badge";
import { Heart } from "lucide-react";
import { Card, CardContent } from "./ui/card";

export default function GameInterestToggle() {
    const { token, user } = useAuth();
    const queryClient = useQueryClient();

    const { data: allGames = [] } = useQuery({ 
        queryKey: ["gameTypes"], 
        queryFn: () => gameService.getAllGames(token!) 
    });

    const { data: myInterests = [] } = useQuery({
        queryKey: ["myInterests", user?.id],
        queryFn: () => gameService.getEmployeeGameInterests(user?.id!, token!),
        enabled: !!user?.id
    });

    const mutation = useMutation({
        mutationFn: (gameTypeId: number) => {
            const existingInterest = myInterests.find((i: any) => Number(i.gameTypeId) === Number(gameTypeId));
            if(existingInterest) {
                return gameService.updateEmployeeGameInterests(existingInterest.id, token!);
            } else {
                return gameService.addEmployeeGameInterest(user?.id!, gameTypeId, token!);
            }
        }, 
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["myInterests", user?.id]})
    });

    const isInterested = (gameId: number) => myInterests.some((i: any) => Number(i.gameTypeId) === Number(gameId));

    return (
        <Card className="bg-slate-50 border-dashed">
            <CardContent className="p-4">
                <p className="text-xs font-bold text-slate-500 mb-3 uppercase flex items-center gap-2">
                    <Heart size={14} className="text-red-500 fill-red-500" /> Mark Your Interests
                </p>
                <div className="flex flex-wrap gap-2">
                    {allGames.map((game: any) => (
                        <Badge 
                            key={game.id}
                            className={isInterested(game.id) ? "cursor-pointer bg-green-100 text-green-700"
                                : "cursor-pointer bg-gray-100 text-gray-700"}
                            onClick={() => mutation.mutate(game.id)}
                        >
                            {game.gameName}
                        </Badge>
                    ))}
                </div>
                <p className="text-[10px] text-slate-400 mt-2 italic">
                    * Only interested games will appear in your booking options.
                </p>
            </CardContent>
        </Card>
    );
}
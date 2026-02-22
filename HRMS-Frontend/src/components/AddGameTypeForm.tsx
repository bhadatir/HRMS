
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { useAuth } from "../context/AuthContext";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Save } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";


type GameTypeInputs = {
    gameName: string;
    operatingStart: string;
    operatingEnd: string;
    gameSlotDuration: number;
    gameMaxPlayerPerSlot: number;
};


export default function AddGameTypeForm({ editGameTypeId ,onSuccess}: { editGameTypeId?: number | null, onSuccess: () => void }) {
    const { token, user } = useAuth();
    const queryClient = useQueryClient();
    const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<GameTypeInputs>(
        {
            defaultValues: {
                gameName: "",
                operatingStart: "",
                operatingEnd: "",
                gameSlotDuration: 30,
                gameMaxPlayerPerSlot: 4
            }
        }
     );  

      const getMutation = useMutation({
        mutationFn: () => gameService.findGameById(editGameTypeId!, token || ""),
        onSuccess: (data) => {
            reset({
                gameName: data.gameName,
                operatingStart: data.operatingStart.split(":").slice(0,2).join(":"),
                operatingEnd: data.operatingEnd.split(":").slice(0,2).join(":"),
                gameSlotDuration: data.gameSlotDuration,
                gameMaxPlayerPerSlot: data.gameMaxPlayerPerSlot
            });
        }
      });

        useEffect(() => {
          if (editGameTypeId) getMutation.mutate();
        }, [editGameTypeId]);

        const gameTypeMutation = useMutation({
            mutationFn: async (data: GameTypeInputs) => {
              data.operatingStart = data.operatingStart + ":00";
              data.operatingEnd = data.operatingEnd + ":00";
              return editGameTypeId 
                ? gameService.updateGameType(editGameTypeId, data, token || "")
                : gameService.addGameType(data, token || "");
            },
            onSuccess: () => {
              queryClient.invalidateQueries({ queryKey: ["allGameTypes"] });
              onSuccess();
            },
            onError: (err: any) => alert("Error: " + err.message)
          });

            return (
                <Card className="border-none shadow-none">
                <CardHeader>
                    <CardTitle className="text-xl">
                    {editGameTypeId ? `Update Game Type` : "Post New Game Type"}
                    </CardTitle>
                </CardHeader>
                <CardContent className="space-y-400">
                { user?.roleName === "HR" && (
                    <form onSubmit={handleSubmit((data) => gameTypeMutation.mutate(data))} 
                        className="bg-white p-4 rounded-xl border items-end">
                        <div className="space-y-1">
                            <label className="text-xs font-bold uppercase text-slate-500">Game Name</label>
                            <Input {...register("gameName", { required: true })} placeholder="Pool, Chess..." />
                        </div>
                        <div className="space-y-2 my-2 flex gap-12">
                            <div>
                            <label className="text-xs font-bold uppercase text-slate-500">Start Time</label>
                            <Input type="time" {...register("operatingStart", { required: true })} />
                            </div>
                        
                            <div>
                            <label className="text-xs font-bold uppercase text-slate-500">End Time</label>
                            <Input type="time" {...register("operatingEnd", { required: true })} />
                            </div>
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-bold uppercase text-slate-500">Duration (Min)</label>
                            <Input type="number" {...register("gameSlotDuration", { required: true })} />
                        </div>
                        <div className="space-y-1">
                            <label className="text-xs font-bold uppercase text-slate-500">Max Players Per Sloat </label>
                            <Input type="number" {...register("gameMaxPlayerPerSlot", { required: true })} />
                        </div>
                        <Button type="submit" className="my-2 text-black bg-blue-500 w-full">
                            <Save size={16} className="mr-2"/> Save Game
                        </Button>
                    </form>
                )}
                </CardContent>
                </Card>
            );
        }
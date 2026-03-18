import { useQuery } from "@tanstack/react-query";
import { gameService } from "../api/gameService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Edit, Plus, X } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { useEffect, useState } from "react";
import AddGameTypeForm from "./AddGameTypeForm";

type GameType = {
    id: number;
    gameName: string;
    operatingStart: string;
    operatingEnd: string;
    gameSlotDuration: number;
    gameMaxPlayerPerSlot: number;
}

export default function GameTypeManager() {
    const { token, user } = useAuth();
    const [showForm, setShowForm] = useState(false);
    const [editGameTypeId, setEditGameTypeId] = useState<number | null>(null);

    const { data: games = [], isError: gamesError } = useQuery({
        queryKey: ["allGameTypes"],
        queryFn: () => gameService.getAllGames(token!)
    });

    useEffect(() => {
        const clickOutside = (e: MouseEvent) => {
        const target = e.target as HTMLElement;
        if (!target.closest("div.gameType")) {
            setShowForm(false);
        }
        };
        if (showForm) {
        document.addEventListener("click", clickOutside);
        } else {
        document.removeEventListener("click", clickOutside);
        }
        return () => document.removeEventListener("click", clickOutside);
    }, [showForm]);

    if(gamesError) alert("Failed to load game types: " + gamesError);
return (
    <main className="p-2 max-w-5xl mx-auto w-full">

        {showForm && (
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                <div className="gameType bg-white rounded-xl max-w-lg w-full relative h-120 overflow-y-auto">
                    <Button title="Close" variant="ghost" className="absolute right-2 top-2" 
                    onClick={() => setShowForm(false)}>
                    <X />
                    </Button>
                    <AddGameTypeForm editGameTypeId={editGameTypeId} onSuccess={() => {
                        setShowForm(false); 
                        setEditGameTypeId(null);
                    }}/>
                </div>
            </div>
        )}

       <Card className="border-none shadow-none">
        <CardHeader className="flex flex-row items-center justify-between">
            <div>
                <CardTitle className="text-2xl font-bold text-slate-900 flex items-center gap-2">
                Game Types
                </CardTitle>
                <CardDescription className="mt-1">List of all game types</CardDescription>
            </div>
            {user?.roleName === "HR" &&
                <div className="gameType">
                    <Button variant="outline" onClick={() => setShowForm(!showForm)} className="gap-2">
                        <Plus size={18}/> Game Type
                    </Button>
                </div>
            }
        </CardHeader>

        <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                    <TableHead>Game</TableHead>
                    <TableHead>Operating Hours</TableHead>
                    <TableHead>Slot Size</TableHead>
                    <TableHead>Max Players</TableHead>
                    {user?.roleName === "HR" &&
                    <TableHead>Actions</TableHead>
                    }
                </TableRow>
              </TableHeader>
            <TableBody>
                {games.map((g: GameType) => (
                    <TableRow key={g.id}>
                        <TableCell className="font-bold">{g.gameName}</TableCell>
                        <TableCell>{g.operatingStart} - {g.operatingEnd}</TableCell>
                        <TableCell>{g.gameSlotDuration} Mins</TableCell>
                        <TableCell>{g.gameMaxPlayerPerSlot} per slot</TableCell>
                        {user?.roleName === "HR" &&
                        <TableCell className="text-right">
                            <div className="gameType">
                                <Button variant="ghost" size="sm" onClick={() => {
                                    setShowForm(true);
                                    setEditGameTypeId(g.id);
                                }}>
                                    <Edit size={14} className="text-blue-600"/>
                                </Button>
                            </div>
                        </TableCell>
                }
                    </TableRow>
                ))}
            </TableBody>
            </Table>               
          </CardContent>
      </Card>
      
    </main>    
    );
}
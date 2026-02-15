import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAuth } from "../context/AuthContext";
import { Label } from "radix-ui";

export default function TravelPlanForm({ onSuccess }: { onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [form, setForm] = useState({
    travelPlanName: "",
    travelPlanDetails: "",
    travelPlanFrom: "",
    travelPlanTo: "",
    travelPlanIsReturn: true,
    travelPlanStartDate: "",
    travelPlanEndDate: "",
    fkTravelPlanHREmployeeId: user?.id || 0,
    employeesInTravelPlanId: [] as number[]
  });

  const createMutation = useMutation({
    mutationFn: () => apiService.createTravelPlan(form, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allTravelPlans"] });
      onSuccess();
    },
    onError: (err: any) => alert("Error: " + err.message)
  });

  return (
    <Card className="w-full border-2 border-blue-100 shadow-lg">
      <CardHeader><CardTitle>Create New Trip</CardTitle></CardHeader>
      <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
        
        <Input placeholder="Plan Name" onChange={e => setForm({...form, travelPlanName: e.target.value})} />
        <div className="space-y-2 flex">
          <label className="text-sm font-medium text-gray-500 w-30 mt-2">Start Date :</label>
          <Input type="date" placeholder="Start Date" onChange={e => setForm({...form, travelPlanStartDate: e.target.value})} />
        </div>
        <Input placeholder="From" onChange={e => setForm({...form, travelPlanFrom: e.target.value})} />
        <Input placeholder="To" onChange={e => setForm({...form, travelPlanTo: e.target.value})} />
        <div className="space-y-2 flex">
          <label className="text-sm font-medium text-gray-500 w-30 mt-2">End Date :</label>
          <Input type="date" placeholder="End Date" onChange={e => setForm({...form, travelPlanEndDate: e.target.value})} />
        </div>
        <select 
          className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm"
          onChange={e => setForm({...form, travelPlanIsReturn: e.target.value === "true"})}
        >
          <option value="true">Return Trip</option>
          <option value="false">One Way</option>
        </select>
        <Textarea className="md:col-span-2" placeholder="Details" onChange={e => setForm({...form, travelPlanDetails: e.target.value})} />
        <Input className="md:col-span-2" placeholder="Employee IDs (comma separated)" 
          onChange={e => setForm({...form, employeesInTravelPlanId: e.target.value.split(",").map(id => parseInt(id.trim())).filter(id => !isNaN(id))})} />
        
        <Button 
          className="md:col-span-2 text-black" 
          onClick={() => createMutation.mutate()} 
          disabled={createMutation.isPending}
        >
          {createMutation.isPending ? "Saving..." : "Deploy Travel Plan"}
        </Button>
      </CardContent>
    </Card>
  );
}



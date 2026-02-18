import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { travelService } from "../api/travelService";
import { useAuth } from "../context/AuthContext";
import { useForm } from "react-hook-form";


type TravelPlanFormInputs = {
  travelPlanName: string;
  travelPlanDetails: string;
  travelPlanFrom: string;
  travelPlanTo: string;
  travelPlanIsReturn: boolean;
  travelPlanStartDate: string;
  travelPlanEndDate: string;
  fkTravelPlanHREmployeeId: number;
  employeesInTravelPlanId: number[];
}

export default function TravelPlanForm( { editTravelPlanId, onSuccess }: { editTravelPlanId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [createdAt, setCreatedAt] = useState("");

  const {register, handleSubmit, reset, watch, formState: { errors }} = useForm<TravelPlanFormInputs>({
    defaultValues: {
      travelPlanName: "",
      travelPlanDetails: "",
      travelPlanFrom: "",
      travelPlanTo: "",
      travelPlanIsReturn: true,
      travelPlanStartDate: "",
      travelPlanEndDate: "",
      fkTravelPlanHREmployeeId: user?.id || 0,
      employeesInTravelPlanId: []
    }
  });

  const getMutation = useMutation({
    mutationFn: () => travelService.getTravelPlanById(editTravelPlanId!, token || ""),
    onSuccess: (data) => {
      setCreatedAt(data.travelPlanCreatedAt.split("T")[0]);
      reset({
        travelPlanName: data.travelPlanName,
        travelPlanDetails: data.travelPlanDetails,
        travelPlanFrom: data.travelPlanFrom,
        travelPlanTo: data.travelPlanTo,
        travelPlanIsReturn: data.travelPlanIsReturn,
        travelPlanStartDate: data.travelPlanStartDate.split("T")[0],
        travelPlanEndDate: data.travelPlanEndDate.split("T")[0],
        fkTravelPlanHREmployeeId: data.employeeId,
        employeesInTravelPlanId: data.employeeTravelPlanResponses.map((e: any) => e.employeeId && !e.employeeIsDeletedFromTravel ? e.employeeId : null).filter((id: any) => id !== null)
      });
    },
    onError: (err: any) => alert("Error: " + err.message)
  });

  useEffect(() => {
    if (editTravelPlanId) {
      getMutation.mutate();
    }
  }, [editTravelPlanId]);

  const travelPlanMutation = useMutation({
    mutationFn: async (data: TravelPlanFormInputs) => {

      data.employeesInTravelPlanId = data.employeesInTravelPlanId.toString().split(",").map(id => Number(id.trim())).filter(id => !isNaN(id));

      if (editTravelPlanId) {
        return travelService.updateTravelPlan(editTravelPlanId, data, token || "");
      } else {
        return travelService.createTravelPlan(data, token || "");
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allTravelPlans"] });
      queryClient.invalidateQueries({ queryKey: ["travelPlanDocs", editTravelPlanId] });
      onSuccess();
    },
    onError: (err: any) => alert("Error: " + err.message)
  });
  
  return (
    <Card className="border-none shadow-none">
      <CardHeader><CardTitle>{editTravelPlanId ? "Update Travel Plan - Created on: " + createdAt : "Create New Trip"}</CardTitle></CardHeader>
      <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <form onSubmit={handleSubmit(data => travelPlanMutation.mutate(data))} className="md:col-span-2 space-y-4">
        <div className="gap-5 flex">
          <Input placeholder="Plan Name"
           {...register("travelPlanName", { required: "Travel plan name is required" })} />
          {errors.travelPlanName && <p className="text-red-500 text-xs">{errors.travelPlanName.message}</p>}
        
          <div className="space-y-2 flex">
            <label className="text-sm font-medium text-gray-500 w-30 mt-2">Start Date :</label>
            <Input type="date" placeholder="Start Date" 
              {...register("travelPlanStartDate", { required: "Start date is required" })} />
              {errors.travelPlanStartDate && <p className="text-red-500 text-xs">{errors.travelPlanStartDate.message}</p>}
          </div>
        </div>
        <div className="gap-5 flex">
          <Input placeholder="From" 
            {...register("travelPlanFrom", { required: "Travel plan from is required" })} />
            {errors.travelPlanFrom && <p className="text-red-500 text-xs">{errors.travelPlanFrom.message}</p>}
          <Input placeholder="To" 
            {...register("travelPlanTo", { required: "Travel plan to is required" })} />
            {errors.travelPlanTo && <p className="text-red-500 text-xs">{errors.travelPlanTo.message}</p>}
        </div>
        
        <div className="gap-5 flex">
          <div className="space-y-2 flex">
            <label className="text-sm font-medium text-gray-500 w-30 mt-2">End Date :</label>
            <Input type="date" placeholder="End Date" 
              {...register("travelPlanEndDate", { required: "End date is required" })} />
              {errors.travelPlanEndDate && <p className="text-red-500 text-xs">{errors.travelPlanEndDate.message}</p>}
          </div>

          <select 
            className="flex h-9 w-full rounded-md border border-input px-3 py-2 text-sm"
            {...register("travelPlanIsReturn", { required: "Please select trip type" })}
          >
            <option value="true">Return Trip</option>
            <option value="false">One Way</option>
          </select>
          {errors.travelPlanIsReturn && <p className="text-red-500 text-xs">{errors.travelPlanIsReturn.message}</p>}
        </div>
        <Textarea className="md:col-span-2" placeholder="Details" 
          {...register("travelPlanDetails", { required: "Travel plan details are required" })} />
          {errors.travelPlanDetails && <p className="text-red-500 text-xs">{errors.travelPlanDetails.message}</p>}
        
          <Input className="md:col-span-2" placeholder="Employee IDs (comma separated)"
            {...register( "employeesInTravelPlanId", { required: "Employee IDs are required" })} />
            {errors.employeesInTravelPlanId && <p className="text-red-500 text-xs">{errors.employeesInTravelPlanId.message}</p>}

        <Button 
          className="w-full text-black" 
          disabled={travelPlanMutation.isPending || !watch("travelPlanName") || !watch("travelPlanFrom") || !watch("travelPlanTo") || !watch("travelPlanStartDate") || !watch("travelPlanEndDate")}
        >
          {travelPlanMutation.isPending ? "Processing..." : editTravelPlanId ? "Update Travel Plan" : "Deploy Travel Plan"}
        </Button>
        </form>
      </CardContent>
    </Card>
  );
}



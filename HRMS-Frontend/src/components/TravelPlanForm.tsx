import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQueries, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import { travelService } from "../api/travelService";
import { apiService } from "../api/apiService"; 
import { useAuth } from "../context/AuthContext";
import { useForm } from "react-hook-form";
import { Search, User, X, Users } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

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

export default function TravelPlanForm({ editTravelPlanId, onSuccess }: { editTravelPlanId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  
  const [searchTerm, setSearchTerm] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);
  const [selectedEmployees, setSelectedEmployees] = useState<{id: number, name: string}[]>([]);
  const [createdAt, setCreatedAt] = useState("");

  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm<TravelPlanFormInputs>({
    defaultValues: {
      travelPlanIsReturn: true,
      fkTravelPlanHREmployeeId: user?.id || 0,
      employeesInTravelPlanId: []
    }
  });

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

const startDate = watch("travelPlanStartDate");
const endDate = watch("travelPlanEndDate");

const availabilityResults = useQueries({
  queries: (suggestions || []).map((emp: any) => ({
    queryKey: ["empAvailability", emp.id, startDate, endDate],
    queryFn: () => travelService.isEmpAvailableForTravel(emp.id, startDate, endDate, token || ""),
    enabled: !!emp.id && !!startDate && !!endDate && showDropdown,
  })),
});

const getMutation = useMutation({
  mutationFn: () => travelService.getTravelPlanById(editTravelPlanId!, token || ""),
  onSuccess: (data) => {
    setCreatedAt(data.travelPlanCreatedAt.split("T")[0]);
    
    const existingEmps = data.employeeTravelPlanResponses
      .filter((e: any) => !e.employeeIsDeletedFromTravel)
      .map((e: any) => ({ id: e.employeeId, name: `${e.employeeFirstName} ${e.employeeLastName}` }));
    
    setSelectedEmployees(existingEmps);
    
    reset({
      travelPlanName: data.travelPlanName,
      travelPlanDetails: data.travelPlanDetails,
      travelPlanFrom: data.travelPlanFrom,
      travelPlanTo: data.travelPlanTo,
      travelPlanIsReturn: data.travelPlanIsReturn,
      travelPlanStartDate: data.travelPlanStartDate.split("T")[0],
      travelPlanEndDate: data.travelPlanEndDate.split("T")[0],
      fkTravelPlanHREmployeeId: data.employeeId,
      employeesInTravelPlanId: existingEmps.map((e: any) => e.id)
    });
  }
});

  useEffect(() => {
    if (editTravelPlanId) getMutation.mutate();
  }, [editTravelPlanId]);

  useEffect(() => {
    if (watch("travelPlanEndDate") && watch("travelPlanStartDate") > watch("travelPlanEndDate")) {
      setValue("travelPlanEndDate", "");
      setSelectedEmployees([]);
      setValue("employeesInTravelPlanId", []);
    }
  }, [watch("travelPlanStartDate")]);
    

  const handleSelectEmployee = (emp: any) => {
    if (!selectedEmployees.find(e => e.id === emp.id)) {
      const newList = [...selectedEmployees, { id: emp.id, name: `${emp.employeeFirstName} ${emp.employeeLastName}` }];
      setSelectedEmployees(newList);
      setValue("employeesInTravelPlanId", newList.map(e => e.id));
    }
    setSearchTerm("");
    setShowDropdown(false);
  };

  const removeEmployee = (id: number) => {
    const newList = selectedEmployees.filter(e => e.id !== id);
    setSelectedEmployees(newList);
    setValue("employeesInTravelPlanId", newList.map(e => e.id));
  };

  const travelPlanMutation = useMutation({
    mutationFn: async (data: TravelPlanFormInputs) => {
      
      return editTravelPlanId 
        ? travelService.updateTravelPlan(editTravelPlanId, data, token || "")
        : travelService.createTravelPlan(data, token || "");
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allTravelPlans"] });
      queryClient.invalidateQueries({ queryKey: ["travelPlan", editTravelPlanId] });
      queryClient.invalidateQueries({ queryKey: ["empTravelPlans", user?.id] });
      queryClient.invalidateQueries({ queryKey: ["employeeSearch"] });
      queryClient.invalidateQueries({ queryKey: ["empAvailability"] });   
      reset();
      setSelectedEmployees([]);
      setShowDropdown(false);
      onSuccess();
    },
    onError: (err: any) => alert("Error: " + err.message)
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle>{editTravelPlanId ? `Update Plan (Created: ${createdAt})` : "Create New Trip"}</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(data => travelPlanMutation.mutate(data))} className="md:col-span-2 space-y-4">
        <div className="gap-5 flex">
          <Input placeholder="Plan Name"
           {...register("travelPlanName", { required: "Travel plan name is required" })} />
          {errors.travelPlanName && <p className="text-red-500 text-xs">{errors.travelPlanName.message}</p>}
        
          <div className="space-y-2 flex">
            <label className="text-sm font-medium text-gray-500 w-30 mt-2">Start Date :</label>
            <Input type="date" placeholder="Start Date" min={new Date().toISOString().split("T")[0]}
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
            {errors.travelPlanTo && errors.travelPlanStartDate && watch("travelPlanStartDate") > watch("travelPlanEndDate") && (
              <p className="text-red-500 text-xs">End date cannot be before start date</p>
            )}
        </div>
        
        <div className="gap-5 flex">
          <div className="space-y-2 flex">
            <label className="text-sm font-medium text-gray-500 w-30 mt-2">End Date :</label>
            <Input type="date" placeholder="End Date" min={watch("travelPlanStartDate")} disabled={!watch("travelPlanStartDate")}
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

          <div className="space-y-3">
            <label className="text-sm font-semibold flex items-center gap-2">
              <Users size={16} /> Assign Employees
            </label>
            
            <div className="relative">
              <div className="relative">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
                <Input 
                  placeholder="Search employees to add..." 
                  className="pl-9"
                  disabled={!watch("travelPlanStartDate") || !watch("travelPlanEndDate")}
                  value={searchTerm}
                  onChange={(e) => { setSearchTerm(e.target.value); setShowDropdown(true); }}
                  onFocus={() => setShowDropdown(true)}
                />
              </div>

            {showDropdown && suggestions.length > 0 && (
              <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-40 overflow-y-auto">
                {suggestions.map((emp: any, index: number) => {
                  const queryStatus = availabilityResults[index];
                  const isBusy = queryStatus?.data === true;
                  const isLoadingBusy = queryStatus?.isLoading;

                  if (emp.id === user?.id || selectedEmployees.find(e => e.id === emp.id)) return null;

                  return (
                  <button
                      key={emp.id}
                      className={cn(
                                  "w-full text-left px-4 py-2 flex items-center gap-3 border-b last:border-none transition-colors",
                                  isBusy ? "bg-slate-50 opacity-60 cursor-not-allowed" : ""
                              )}
                      onClick={() => !isBusy && handleSelectEmployee(emp)}
                      disabled={isBusy || isLoadingBusy}
                  >
                      <User size={14} className={isBusy ? "text-slate-400" : "text-blue-600"} />
                      <div className="flex flex-col">
                          <span className="text-sm font-medium">{emp.employeeFirstName} {emp.employeeLastName}</span>
                          {isBusy ? <span className="text-[10px] text-red-500 font-bold">Already in a other travel plan</span>
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

            <div className="flex flex-wrap gap-2 min-h-[40px] p-2 bg-slate-50 rounded-lg border border-dashed">
              {selectedEmployees.length === 0 && <p className="text-xs text-slate-400">No employees assigned yet.</p>}
              {selectedEmployees.map((emp) => (
                <Badge key={emp.id} variant="secondary" className="pl-2 pr-1 py-1 gap-1 bg-white border shadow-sm">
                  <span className="text-xs font-medium">{emp.name}</span>
                  <button 
                    type="button" 
                    onClick={() => removeEmployee(emp.id)}
                  >
                    <X size={12} className="text-red-500" />
                  </button>
                </Badge>
              ))}
            </div>
            {errors.employeesInTravelPlanId && <p className="text-red-500 text-xs">At least one employee must be assigned.</p>}
          </div>

          <Button 
            type="submit" 
            className="w-full text-black font-bold" 
            disabled={travelPlanMutation.isPending || selectedEmployees.length === 0}
          >
            {travelPlanMutation.isPending ? "Deploying..." : editTravelPlanId ? "Update Plan" : "Create Plan"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}


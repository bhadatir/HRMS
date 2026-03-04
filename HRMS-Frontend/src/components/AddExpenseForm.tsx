import { use, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { travelService } from "../api/travelService";
import { useAuth } from "../context/AuthContext";
import { UploadCloud, X } from "lucide-react";
import { useForm } from "react-hook-form";

type ProofEntry = {
  file: File;
  typeId: string;
};

type ExpenseForm = {
  expenseAmount: number;
  expenseDate: string;
  expenseRemark: string;
  fkExpenseExpenseStatusId: number;
  fkEmployeeTravelPlanId: number | null;
};

export default function AddExpenseForm({ travelPlanId, onSuccess }: { travelPlanId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  
  const [proofs, setProofs] = useState<ProofEntry[]>([]);
  const { register, handleSubmit, watch, setError, clearErrors, reset, formState: { errors } } = useForm<ExpenseForm>(
    {
      defaultValues: {
      expenseDate: new Date().toISOString().split("T")[0],
      expenseRemark: "",
      fkExpenseExpenseStatusId: 1,
      fkEmployeeTravelPlanId: null
      }
    }
  ); 
  
  const { data: employeeTravelPlanId, isLoading, isError: employeeTravelPlanError } = useQuery({
    queryKey: ["employeeTravelPlan", user?.id, travelPlanId],
    queryFn: () => travelService.findEmployeeTravelPlanId(user?.id, travelPlanId, token || ""),
    enabled: !!travelPlanId && !!user?.id && !!token,
  });

  const { data: plan, isLoading: planLoading, isError: planError } = useQuery({
    queryKey: ["travelPlan", travelPlanId],
    queryFn: () => travelService.getTravelPlanById(travelPlanId, token || ""),
    enabled: !!travelPlanId && !!token,
  });

  const { data: expenseTypes, isError: expenseTypesError } = useQuery({
    queryKey: ["expenseTypes"],
    queryFn: () => travelService.getAllExpenseTypes(token || ""),
    enabled: !!token,
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newFiles = Array.from(e.target.files).map(file => ({
        file,
        typeId: ""
      }));
      setProofs([...proofs, ...newFiles]);
    }
  };

  const updateProofType = (index: number, typeId: string) => {
    const updated = [...proofs];
    updated[index].typeId = typeId;
    setProofs(updated);
  };

  const removeProof = (index: number) => {
    setProofs(proofs.filter((_, i) => i !== index));
  };

  const selectedDate = watch("expenseDate");
  const { data: alreadySpent } = useQuery({
    queryKey: ["dailyExpenseTotal", travelPlanId, user?.id, selectedDate],
    queryFn: () => travelService.getTotalSpentByDate(travelPlanId, user?.id, selectedDate, token || ""),
    enabled: !!selectedDate && !!token,
  });

  const currentAmount = Number(watch("expenseAmount") || 0);
  const dailyLimit = plan?.travelMaxExpenseAmountPerDay || 0;
  const remainingAllowance = dailyLimit - alreadySpent - currentAmount;
  
  const isOverLimit = remainingAllowance < 0;

  useEffect(() => {
    if (plan && selectedDate) {
      if (isOverLimit) {
        setError("expenseAmount", {
          type: "manual",
          message: `Exceeds daily limit. Available: ₹${(dailyLimit - alreadySpent).toLocaleString()}`,
        });
      } else {
        clearErrors("expenseAmount");
      }
    }
  }, [currentAmount, alreadySpent, dailyLimit, selectedDate, setError, clearErrors, isOverLimit]);
    
  const expenseMutation = useMutation({
    mutationFn: async (data: ExpenseForm) => {
      if (!employeeTravelPlanId) throw new Error("Employee travel plan not found");
      
      const formData = new FormData();

      data.fkEmployeeTravelPlanId = employeeTravelPlanId;
      
      const jsonBlob = new Blob([JSON.stringify(data)], { type: "application/json" });
      formData.append("expenseRequest", jsonBlob);
      
      proofs.forEach(p => formData.append("files", p.file));
      
      const idsArray = proofs.map(p => Number(p.typeId));
      const idsBlob = new Blob([JSON.stringify(idsArray)], { type: "application/json" });
      formData.append("proofTypes", idsBlob);
  
      return travelService.addExpenseWithProof(formData, token || "");
    },
    onSuccess: () => {
      alert("Expense submitted!");
      queryClient.invalidateQueries({ queryKey: ["travelPlanByEmpId", user?.id] });
      queryClient.invalidateQueries({ queryKey: ["travelPlanExpense"] });
      queryClient.invalidateQueries({ queryKey: ["dailyExpenseTotal", travelPlanId, user?.id] });
      onSuccess();
    },
    onError: (err: any) => {
    const errorMessage = err.response?.data?.message || err.response?.data || err.message;
    alert("Failed to submit expense: " + (typeof errorMessage === 'object' ? JSON.stringify(errorMessage) : errorMessage));
  }
  });

  if (isLoading || planLoading) return <div>Loading...</div>;
  if (employeeTravelPlanError || expenseTypesError || planError) alert("Failed to load data: " + (employeeTravelPlanError || expenseTypesError || planError));

  return (
    <Card className="border-none shadow-none">
      <CardHeader><CardTitle className="text-xl">Submit Expense Claim</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        <form onSubmit={handleSubmit(data => expenseMutation.mutate(data))} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
               <div className="flex justify-between items-center mb-1">
                <label className="text-[10px] font-bold text-slate-500 uppercase">Amount</label>
                {selectedDate && (
                  <span className={`text-[10px] font-bold ${isOverLimit ? 'text-red-600' : 'text-green-600'}`}>
                    Rem: ₹{remainingAllowance.toLocaleString()}
                  </span>
                )}
              </div>
              <Input 
                type="number" 
                placeholder="Amount" 
                {...register("expenseAmount", { required: "Amount is required", min: 1 })} 
              />
              {errors.expenseAmount && (
                <p className="text-[10px] font-bold text-red-500">
                  {errors.expenseAmount.message}
                </p>
              )}
            </div>
            <div className="space-y-1">
              <div className="flex justify-between items-center mb-1">
                <label className="text-[10px] font-bold text-slate-500 uppercase">Date</label>
              </div>
              <Input type="date" max={new Date().toISOString().split("T")[0]} {...register("expenseDate", { required: true })} />
            </div>
          </div>   
          <Input disabled={isOverLimit} placeholder="Remark" {...register("expenseRemark")} />
         
          <div className="border-2 border-dashed rounded-lg p-4 text-center border-slate-200">
            <label className="cursor-pointer block">
              <UploadCloud className="mx-auto text-slate-400" size={32} />
              <span className="text-sm text-slate-500">Click to upload receipts</span>
              <input disabled={isOverLimit} type="file" multiple className="hidden" onChange={handleFileChange} />
            </label>
          </div>

          <div className="space-y-3">
            {proofs.map((entry, index) => (
              <div key={index} className="flex items-center gap-2 p-2 border rounded-md bg-slate-50">
                <span className="text-xs truncate flex-1 font-medium">{entry.file.name}</span>
                
                <select 
                  className="text-sm border rounded p-1 bg-white"
                  value={entry.typeId}
                  onChange={(e) => updateProofType(index, e.target.value)}
                >
                  <option value="">Select Type</option>
                  {expenseTypes?.map((type: any) => (
                    <option key={type.id} value={type.id}>{type.expenseProofTypeName}</option>
                  ))}
                </select>

                <Button variant="ghost" size="icon" onClick={() => removeProof(index)} className="h-8 w-8">
                  <X size={16} />
                </Button>
              </div>
            ))}
          </div>
          
          <Button 
            className="w-full text-gray-700"
            type="submit"
            title="submit expense claim"
            disabled={expenseMutation.isPending || isOverLimit || proofs.length === 0 || proofs.some(p => !p.typeId)|| !watch("expenseAmount") || !watch("expenseDate")}
          >
            {expenseMutation.isPending ? "Uploading..." : "Submit Claim"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

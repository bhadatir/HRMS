import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { travelService } from "../api/travelService";
import { useAuth } from "../context/AuthContext";
import { UploadCloud, X } from "lucide-react";

type ProofEntry = {
  file: File;
  typeId: string;
};

export default function AddExpenseForm({ travelPlanId, onSuccess }: { travelPlanId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  
  const { data: employeeTravelPlan, isLoading, isError: employeeTravelPlanError } = useQuery({
    queryKey: ["employeeTravelPlan", user?.id, travelPlanId],
    queryFn: () => travelService.findEmployeeTravelPlans(user?.id, travelPlanId, token || ""),
    enabled: !!travelPlanId && !!user?.id && !!token,
  });

  const { data: expenseTypes, isError: expenseTypesError } = useQuery({
    queryKey: ["expenseTypes"],
    queryFn: () => travelService.getAllExpenseTypes(token || ""),
    enabled: !!token,
  });

  const [form, setForm] = useState({
    expenseAmount: 0,
    expenseDate: "",
    expenseRemark: "",
    fkExpenseExpenseStatusId: 1,
    fkEmployeeTravelPlanId: null,
  });

  const [proofs, setProofs] = useState<ProofEntry[]>([]);

  useEffect(() => {
    if (employeeTravelPlan) {
      setForm(prev => ({ ...prev, fkEmployeeTravelPlanId: employeeTravelPlan }));
    }
  }, [employeeTravelPlan]);

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

  const expenseMutation = useMutation({
    mutationFn: async () => {
      if (!employeeTravelPlan) throw new Error("Employee travel plan not found");
      
      const formData = new FormData();
      
      const jsonBlob = new Blob([JSON.stringify(form)], { type: "application/json" });
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
      queryClient.invalidateQueries({ queryKey: ["travelPlan", travelPlanId] });
      onSuccess();
    },
    onError: (err: any) => alert("Failed to submit expense: " + (err.response?.data || err.message))
  });

  if (isLoading) return <div>Loading...</div>;
  if (employeeTravelPlanError || expenseTypesError) alert("Failed to load data: " + (employeeTravelPlanError || expenseTypesError));

  return (
    <Card className="border-none shadow-none">
      <CardHeader><CardTitle className="text-xl">Submit Expense Claim</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input type="number" placeholder="Amount" onChange={(e) => setForm({...form, expenseAmount: Number(e.target.value)})} />
          <Input type="date" max={new Date().toISOString().split("T")[0]} onChange={(e) => setForm({...form, expenseDate: e.target.value})} />
        </div>
        <Input placeholder="Remark" onChange={(e) => setForm({...form, expenseRemark: e.target.value})} />
        
        <div className="border-2 border-dashed rounded-lg p-4 text-center border-slate-200">
          <label className="cursor-pointer block">
            <UploadCloud className="mx-auto text-slate-400" size={32} />
            <span className="text-sm text-slate-500">Click to upload receipts</span>
            <input type="file" multiple className="hidden" onChange={handleFileChange} />
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
          onClick={() => expenseMutation.mutate()} 
          disabled={expenseMutation.isPending || proofs.length === 0 || proofs.some(p => !p.typeId)}
        >
          {expenseMutation.isPending ? "Uploading..." : "Submit Claim"}
        </Button>
      </CardContent>
    </Card>
  );
}

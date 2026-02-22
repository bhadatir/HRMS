
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation } from "@tanstack/react-query";
import { travelService } from "../api/travelService";
import { useAuth } from "../context/AuthContext";
import { UploadCloud } from "lucide-react";
import { useQuery } from "@tanstack/react-query";

export default function AddExpenseForm({ travelPlanId, startDate, endDate, onSuccess }: { travelPlanId: number, startDate: string, endDate: string, onSuccess: () => void }) {
  const { token, user } = useAuth();

  const { data: employeeTravelPlan, isLoading, isError } = useQuery({
    queryKey: ["employeeTravelPlan", user?.id, travelPlanId],
    queryFn: () => travelService.findEmployeeTravelPlans(user?.id, travelPlanId, token || ""),
    enabled: !!travelPlanId && !!user?.id && !!token,
  });

  const [form, setForm] = useState({
    expenseAmount: 0,
    expenseDate: "",
    expenseRemark: "",
    fkExpenseExpenseStatusId: 1,
    fkEmployeeTravelPlanId: employeeTravelPlan,
  });

  useEffect(() => {
    if (employeeTravelPlan) {
      setForm(form => ({ ...form, fkEmployeeTravelPlanId: employeeTravelPlan }));
    }
  }, [employeeTravelPlan]);

  const [proofFiles, setProofFiles] = useState<File[]>([]);
  const [proofTypeIds, setProofTypeIds] = useState<string>("");

  const expenseMutation = useMutation({
    mutationFn: async () => {
      
      if (!employeeTravelPlan) {
        throw new Error("Employee travel plan not found");
      }

      const formData = new FormData();
      const jsonBlob = new Blob([JSON.stringify(form)], { type: "application/json" });
      formData.append("expenseRequest", jsonBlob);
      proofFiles.forEach(file => formData.append("files", file));
      const idsArray = proofTypeIds.split(",").map(id => Number(id.trim()));
      const idsBlob = new Blob([JSON.stringify(idsArray)], { type: "application/json" });
      formData.append("proofTypes", idsBlob);
  
      return travelService.addExpenseWithProof(formData, token || "");
    },
    onSuccess: () => {
      alert("Expense submitted!");
      onSuccess();
    },
    onError: (err: any) => alert(err.message)
  });

  if (isLoading) return <div>Loading...</div>;
  if (isError) return <div>Error loading travel plan data.</div>;

  return (
    <Card className="border-none shadow-none">
      <CardHeader><CardTitle className="text-xl">Submit Expense Claim</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input type="number" placeholder="Amount" onChange={(e) => setForm({...form, expenseAmount: Number(e.target.value)})} />
          
          <div className="space-y-2 flex">
            <label className="text-sm font-medium text-gray-500 w-30 mt-2">Expense Date :</label>
            <Input type="date" placeholder="Select expense Date"
            max={endDate}
            onChange={(e) => setForm({...form, expenseDate: e.target.value})} />
          </div>
        </div>
        <Input placeholder="Remark" onChange={(e) => setForm({...form, expenseRemark: e.target.value})} />
        
        <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
          <UploadCloud className="mx-auto text-slate-400" size={32} />
          <Input type="file" multiple className="cursor-pointer" onChange={(e) => e.target.files && setProofFiles(Array.from(e.target.files))} />
          <p className="text-xs text-slate-400">Upload receipts or proof of travel</p>
        </div>

        <Input placeholder="Proof Type IDs (e.g. 1, 2)" value={proofTypeIds} onChange={(e) => setProofTypeIds(e.target.value)} />
        
        <Button 
          title="Submit Claim"
          className="w-full text-black" 
          onClick={() => expenseMutation.mutate()} 
          disabled={expenseMutation.isPending || proofFiles.length === 0}
        >
          {expenseMutation.isPending ? "Uploading..." : "Submit Claim"}
        </Button>
      </CardContent>
    </Card>
  );
}


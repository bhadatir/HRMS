
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAuth } from "../context/AuthContext";
import { UploadCloud } from "lucide-react";
import { useQuery } from "@tanstack/react-query";

export default function AddTravelDocumentForm({ travelPlanId, onSuccess }: { travelPlanId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();

  const [docFile, setDocFile] = useState<File>(null as any);
  const [docType, setDocType] = useState<number>(0);

  const { data: employeeTravelPlan, isLoading, isError } = useQuery({
    queryKey: ["employeeTravelPlan", user?.id, travelPlanId],
    queryFn: () => apiService.findEmployeeTravelPlans(1, 1, token || ""),
    enabled: !!travelPlanId && !!user?.id && !!token,
  });

  const expenseMutation = useMutation({
    mutationFn: async () => {
      
      const formData = new FormData();
      formData.append("file", docFile);

      if(user?.roleName === "HR"  ) {
          return apiService.addTravelPlanDocByHr(user?.id || 0, travelPlanId, docType, formData, token || "");
      } else if(user?.roleName === "EMPLOYEE") {
            if (!employeeTravelPlan) {
                throw new Error("Employee travel plan not found");
            }
          return apiService.addTravelPlanDocByEmployee(user?.id || 0, employeeTravelPlan, docType, formData, token || "");
      }
    },
    onSuccess: () => {
      alert("Travel document submitted!");
      onSuccess();
    },
    onError: (err: any) => alert(err.message)
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader><CardTitle className="text-xl">Submit Travel Document</CardTitle></CardHeader>
      <CardContent className="space-y-4">

        <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
          <UploadCloud className="mx-auto text-slate-400" size={32} />
          <Input type="file" multiple className="cursor-pointer" onChange={(e) => e.target.files && setDocFile(Array.from(e.target.files)[0])} />
          <p className="text-xs text-slate-400">Upload receipts or proof of travel</p>
        </div>

        <div className="space-y-2">
              <label className="text-sm font-medium">Document Type</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                onChange={e => setDocType(Number(e.target.value))}
              >
                <option value="1">Rules</option>
                <option value="2">Visa</option>
                <option value="3">Ticket</option>
                <option value="4">Passport</option>
                <option value="5">Hotel Booking</option>
                <option value="6">Other</option>
              </select>
        </div>
        
        <Button 
          className="w-full text-black" 
          onClick={() => expenseMutation.mutate()} 
          disabled={expenseMutation.isPending || !docFile}
        >
          {expenseMutation.isPending ? "Uploading..." : "Submit Document"}
        </Button>
      </CardContent>
    </Card>
  );
}


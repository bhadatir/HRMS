import { useQuery, useQueries, useMutation, useQueryClient } from "@tanstack/react-query";
import { travelService } from "../api/travelService";
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Plane, Calendar, MapPin, FileText, CheckCircle, XCircle, ExternalLink } from "lucide-react";

export default function TravelPlanDetails({travelPlan, onSuccess } : {travelPlan: number| null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<"EXPENSES" | "DOCUMENTS">("EXPENSES");

  const { data: plan, isLoading: planLoading } = useQuery({
    queryKey: ["travelPlan", travelPlan],
    queryFn: () => travelService.getTravelPlanById(travelPlan!, token || ""),
    enabled: !!travelPlan && !!token,
  });
  
  const expenseResults = useQueries({
    queries: (plan?.employeeTravelPlanResponses || []).map((exp: any) => ({
      queryKey: ["travelPlanExpense", exp.employeeId, travelPlan],
      queryFn: () => travelService.findExpenseById(exp.employeeId , travelPlan!, token || ""),
      enabled: !!travelPlan && !!token && !!exp.employeeId && viewMode === "EXPENSES",
    }))
  });

  const docResults = useQueries({
    queries: (plan?.employeeTravelPlanResponses || []).map((exp: any) => ({
      queryKey: ["travelPlanDocs", exp.employeeId, travelPlan],
      queryFn: () => travelService.findTravelDocByEmpId(exp.employeeId, travelPlan!, token || ""),
      enabled: !!token && !!exp.employeeId && !!travelPlan && viewMode === "DOCUMENTS",
    }))
  });

  const allExpenses = expenseResults.flatMap((result) => result.data || []);
  const allDocs = docResults.flatMap((result) => result.data || []);
  const isLoadingData = planLoading || (viewMode === "EXPENSES" ? expenseResults.some(r => r.isLoading) : docResults.some(r => r.isLoading));

  const approveMutation = useMutation({
    mutationFn: ({ expenseId, statusId }: { expenseId: number; statusId: number }) =>
      travelService.updateExpenseStatus(expenseId, statusId, token || ""),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["travelPlan", travelPlan]});
      await queryClient.invalidateQueries({ queryKey: ["travelPlanExpense"] });

      await queryClient.invalidateQueries({ queryKey: ["allTravelPlans"] });
      
      alert("Status updated successfully");
      onSuccess();
    },
    onError: (err: any) => alert(err.message)
  });

  if (isLoadingData) return <div className="p-10">Loading Details...</div>;

  return (
        <>
        <Card className="border-none shadow-none">
            <CardHeader className="flex flex-row items-center justify-between space-y-0">
              <div>
                <CardTitle className="text-2xl font-bold text-slate-900">{plan?.travelPlanName}</CardTitle>
                <CardDescription className="mt-1">{plan?.travelPlanDetails}</CardDescription>
              </div>
              <Badge variant="outline" className="text-blue-600 border-blue-200 bg-blue-50">
                {plan?.travelPlanIsReturn ? "Return Trip" : "One Way"}
              </Badge>
            </CardHeader>
            <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4">
              <div className="flex items-center gap-3">
                <MapPin className="text-blue-500" />
                <div>
                  <p className="text-xs text-slate-500 font-bold uppercase">Route</p>
                  <p className="font-semibold">{plan?.travelPlanFrom} → {plan?.travelPlanTo}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Calendar className="text-blue-500" />
                <div>
                  <p className="text-xs text-slate-500 font-bold uppercase">Duration</p>
                  <p className="font-semibold">{plan?.travelPlanStartDate} to {plan?.travelPlanEndDate}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Plane className="text-blue-500" />
                <div>
                  <p className="text-xs text-slate-500 font-bold uppercase">Created At</p>
                  <p className="font-semibold">{plan?.travelPlanCreatedAt}</p>
                </div>
              </div>
            </CardContent>
        </Card>

        <Card className="border-none shadow-none">
            <CardHeader>
              <CardTitle className="text-xl flex items-center gap-2">
                <FileText className="text-blue-600" /> {viewMode === "EXPENSES" ? "Expenses" : "Travel Documents"}
                <div className="flex bg-gray-100 p-1 rounded-lg w-max right-10 absolute">
                  <Button className={viewMode === "EXPENSES" ? "rounded-md border text-gray-700" : "rounded-md text-gray-400"}
                    size="sm"
                    onClick={()=>setViewMode("EXPENSES")}>Expenses</Button>
                  <Button className={viewMode === "DOCUMENTS" ? "rounded-md border text-gray-700" : "rounded-md text-gray-400"}
                    size="sm"
                    onClick={()=>setViewMode("DOCUMENTS")}>Documents</Button>
              </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow className="bg-slate-50">
                    {viewMode === "EXPENSES" ? (
                      <>
                        <TableHead>Date</TableHead>
                        <TableHead>Description</TableHead>
                        <TableHead>Amount</TableHead>
                        <TableHead>Proofs</TableHead>
                        <TableHead>Status</TableHead>
                        {user?.roleName !== "EMPLOYEE" && <TableHead className="text-right">Actions</TableHead>}
                      </>
                    ) : (
                      <>
                        <TableHead>Document Type</TableHead>
                        <TableHead>Uploaded By</TableHead>
                        <TableHead>Uploaded At</TableHead>
                        <TableHead>View</TableHead>
                      </>
                    )}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  { viewMode === "EXPENSES" ? allExpenses.map((exp: any) => (
                    <TableRow key={exp.id}>
                      <TableCell>{exp.expenseDate}</TableCell>
                      <TableCell>
                        <p className="font-medium">{exp.expenseRemark}</p>
                        <p className="text-[10px] text-slate-400">ID: {exp.id}</p>
                      </TableCell>
                      <TableCell className="font-bold text-slate-900">${exp.expenseAmount}</TableCell>
                      <TableCell>
                        <div className="flex gap-2">
                          {exp.expenseProofResponses?.map((proof: any, idx: number) => (
                            <a 
                              key={idx} 
                              href={proof.expenseProofUrl} 
                              target="_blank" 
                              rel="noreferrer"
                              className="text-blue-600 hover:underline flex items-center gap-1 text-xs"
                            >
                              <ExternalLink size={12} /> Doc {idx + 1}
                            </a>
                          ))}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge className={exp.expenseTravelPlanStatusName === "APPROVED" ? "bg-green-100 text-green-700" : "bg-yellow-100 text-yellow-700"}>
                          {exp.expenseTravelPlanStatusName}
                        </Badge>
                      </TableCell>
                      {user?.roleName !== "EMPLOYEE" && (
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            {exp.expenseTravelPlanStatusName === "PENDDING" ? (
                            <>
                              <Button 
                                size="sm" 
                                variant="outline" 
                                className="text-green-600 border-green-200 hover:bg-green-50"
                                onClick={() => approveMutation.mutate({ expenseId: exp.id, statusId: 2 })}
                                disabled={approveMutation.isPending}
                              >
                                <CheckCircle size={16} />
                              </Button>
                              <Button 
                                size="sm" 
                                variant="outline" 
                                className="text-red-600 border-red-200 hover:bg-red-50"
                                onClick={() => approveMutation.mutate({ expenseId: exp.id, statusId: 4 })}
                                disabled={approveMutation.isPending}
                              >
                                <XCircle size={16} />
                              </Button>
                            </>
                            ) : (
                              <span className="text-green-600 flex items-center gap-1 justify-end">
                                <CheckCircle size={16} /> Done
                              </span>
                            )}
                          </div>
                        </TableCell>
                      )}
                    </TableRow>
                  )
                ):(
                  allDocs.map((doc: any) => (
                    <TableRow key={doc.id}>
                      <TableCell className="font-medium">{doc.travelDocsTypeName}</TableCell>
                      <TableCell>{doc.employeeEmail || "System"}</TableCell>
                      <TableCell >{doc.travelDocUploadedAt}</TableCell>
                      <TableCell className="text-right">
                        <a href = {doc.travelDocUrl} target="_blank" rel="noreferrer" className="text-blue-600 hover:underline flex items-center gap-1 justify-end">
                        <Button variant="outline" size="sm" className="text-blue-600 border-blue-200 hover:bg-blue-50">
                          <ExternalLink size={12} /> View
                        </Button>
                        </a>
                      </TableCell>
                    </TableRow>
                ))
              )}
              {viewMode === "DOCUMENTS" && allDocs.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} className="text-center py-10">
                    <div className="flex flex-col items-center gap-2">
                      <FileText size={32} className="text-slate-400" />
                      <p className="text-sm text-slate-400">No travel documents uploaded yet.</p>
                    </div>
                  </TableCell>
                </TableRow>
              )}
              {viewMode === "EXPENSES" && allDocs.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-10">
                    <div className="flex flex-col items-center gap-2">
                      <FileText size={32} className="text-slate-400" />
                      <p className="text-sm text-slate-400">No travel expenses uploaded yet.</p>
                    </div>
                  </TableCell>
                </TableRow>
              )}
                </TableBody>
              </Table>
            </CardContent>
        </Card>
    </>
  );
}
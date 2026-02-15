
import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { apiService } from "../api/apiService";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar"
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Plus, X, MapPin, Calendar } from "lucide-react";
import TravelPlanForm from "../components/TravelPlanForm";
import AddExpenseForm from "../components/AddExpenseForm";
import { DropdownMenu } from "radix-ui";

export default function TravelPlan() {
  const { token, user } = useAuth(); 
  const [showForm, setShowForm] = useState(false);
  const [activeExpenseId, setActiveExpenseId] = useState<number | null>(null);

  const { data: allPlans, isLoading } = useQuery({
    queryKey: ["allTravelPlans"],
    queryFn: () => apiService.getAllTravelPlans(token || ""),
    enabled: !!token,
  });

  const filteredPlans = useMemo(() => {
    if (!allPlans || !user) return [];
    if (user.roleName === "EMPLOYEE") {
      return allPlans.filter((plan: any) =>
        plan.employeeTravelPlanResponses.some((resp: any) => resp.employeeEmail === user.employeeEmail)
      );
    }
    return allPlans;
  }, [allPlans, user]);

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            <SidebarTrigger />
            <h3 className="text-lg font-bold text-slate-800">Travel Management</h3>
          </div>
          {user?.roleName === "HR" && (
            <Button onClick={() => setShowForm(!showForm)} className="gap-2 text-gray-600">
              {showForm ? <X size={18} /> : <Plus size={18} />}
              {showForm ? "Cancel" : "New Plan"}
            </Button>
          )}
        </header>

        <main className="p-6 max-w-7xl mx-auto space-y-6 w-250">
          {showForm && (
            <div className="animate-in slide-in-from-top duration-300">
              <TravelPlanForm onSuccess={() => setShowForm(false)} />
            </div>
          )}

          {activeExpenseId && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative">
                <Button variant="ghost" className="absolute right-2 top-2" onClick={() => setActiveExpenseId(null)}><X /></Button>
                <AddExpenseForm travelPlanId={activeExpenseId} onSuccess={() => setActiveExpenseId(null)} />
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {isLoading ? (
               <p>Loading plans...</p>
            ) : filteredPlans.length > 0 ? (
              filteredPlans.map((plan: any) => (
                <Card key={plan.id} className="hover:shadow-md transition-shadow border-slate-200">
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <CardTitle className="text-xl font-bold text-blue-600">{plan.travelPlanName}</CardTitle>
                      <Badge variant="outline">{plan.travelPlanIsReturn ? "Return" : "One-Way"}</Badge>
                    </div>
                    <CardDescription className="flex items-center gap-1 mt-1">
                      <MapPin size={14} /> {plan.travelPlanFrom} → {plan.travelPlanTo}
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center gap-4 text-sm text-slate-600 bg-slate-100 p-2 rounded-lg">
                      <div className="flex items-center gap-1"><Calendar size={14}/> {plan.travelPlanStartDate}</div>
                      <div className="text-slate-400">|</div>
                      <div className="flex items-center gap-1">{plan.travelPlanEndDate}</div>
                    </div>
                    
                    <p className="text-sm text-slate-500 line-clamp-2">{plan.travelPlanDetails}</p>

                    <div className="space-y-2">
                      <p className="text-xs font-bold text-slate-400 uppercase">Team Members</p>
                      <div className="flex flex-wrap gap-1">
                        {plan.employeeTravelPlanResponses.map((m: any) => (
                          <Badge key={m.employeeEmail} variant="secondary" className="text-[10px]">
                            {m.employeeEmail.split('@')[0]}
                          </Badge>
                        ))}
                      </div>
                    </div>

                    {user?.roleName === "EMPLOYEE" && (
                      <Button 
                        onClick={() => setActiveExpenseId(plan.id)}
                        className="w-full text-gray-600 mt-2"
                      >
                        Claim Expense
                      </Button>
                    )}
                  </CardContent>
                </Card>
              ))
            ) : (
              <div className="col-span-full py-20 text-center text-slate-400">No travel plans found.</div>
            )}
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}








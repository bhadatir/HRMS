
import { useState, useMemo } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { travelService } from "../api/travelService";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar"
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Plus, X, MapPin, Calendar, Edit, ImagePlus, Bell, Search, Trash2 } from "lucide-react";
import TravelPlanForm from "../components/TravelPlanForm";
import AddExpenseForm from "../components/AddExpenseForm";
import AddTravelDocumentForm from "../components/AddTravelDocumentForm";
import FullTravelDetail from "../components/FullTravelDetail.tsx";
import { Input } from "@/components/ui/input";
import Notifications from "../components/Notifications.tsx";

export default function TravelPlan() {
  const { token, user, unreadNotifications } = useAuth(); 
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [showNotification, setShowNotification] = useState(false);
  const [fullTravelDetails, setFullTravelDetails] = useState<number | null>(null);
  const [editTravelPlanId, setEditTravelPlanId] = useState<number | null>(null);
  const [activeExpenseId, setActiveExpenseId] = useState<number | null>(null);
  const [selectedTravelId, setSelectedTravelId] = useState<number | null>(null);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [searchTerm, setSearchTerm] = useState("");

  const { data: allPlans, isLoading } = useQuery({
    queryKey: ["allTravelPlans"],
    queryFn: () => travelService.getAllTravelPlans(token || ""),
    enabled: !!token,
  });

  const { data: searchFilter, isLoading: isSearchLoading } = useQuery({
    queryKey: ["travelPlanSearch", searchTerm],
    queryFn: () => travelService.searchTravelPlan(searchTerm, token || ""),
    enabled: searchTerm.length >= 1,
  });

  const deleteTravelPlanMutation = useMutation({
    mutationFn: (travelPlanId: number) => travelService.deleteTravelPlan(travelPlanId, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allTravelPlans"] });
      alert("Travel plan deleted successfully");
    },
    onError: (err: any) => alert("Error: " + err.message)
  });

  const filteredPlans = useMemo(() => {
    if (!allPlans || !user) return [];
    if (user.roleName === "EMPLOYEE" && !isSearchLoading && searchFilter) {
      return allPlans.filter((plan: any) =>
        plan.employeeTravelPlanResponses.some((resp: any) => {
          return ( resp.employeeEmail === user.employeeEmail && resp.employeeIsDeletedFromTravel === false
          && searchFilter?.includes(resp.travelPlanId))
        })
      );
    }
    if (user.roleName === "EMPLOYEE" && !isSearchLoading && !searchFilter) {
      return allPlans.filter((plan: any) =>
        plan.employeeTravelPlanResponses.some((resp: any) => {
          return ( resp.employeeEmail === user.employeeEmail && resp.employeeIsDeletedFromTravel === false)
        })
      );
    }
  
    if(user?.roleName === "MANAGER" && !isSearchLoading && searchFilter) {
      return allPlans.filter((plan: any) => 
        plan.employeeFkManagerEmployeeId === user.id 
        || (plan.employeeTravelPlanResponses.some((resp: any) => resp.employeeEmail === user.employeeEmail && resp.employeeIsDeletedFromTravel === false))
        && searchFilter.includes(plan.id));
    }

    if(user?.roleName === "MANAGER" && !isSearchLoading && !searchFilter) {
      return allPlans.filter((plan: any) => plan.employeeFkManagerEmployeeId === user.id
      || (plan.employeeTravelPlanResponses.some((resp: any) => resp.employeeEmail === user.employeeEmail && resp.employeeIsDeletedFromTravel === false)));
    }

    // if role hr so hr add exp only on that in which hr is going in travel is remaining
    if (!searchFilter) return allPlans;
    return allPlans.filter((plan: any) => searchFilter.includes(plan.id));
  }, [allPlans, user, searchFilter, isSearchLoading]);

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            <SidebarTrigger />
            <h3 className="text-lg font-bold text-slate-800">Travel Management</h3>
            {searchFilter && searchFilter.length > 0 ?(
              <Badge variant="outline">{searchFilter.length} results</Badge>
            ) : (<Badge variant="outline">No filter applied</Badge>)
            }
          </div>

          <div className="relative max-w-sm w-full">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search Travel Plans (e.g. 'ti')..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
              }}
            />
          </div>

          {user?.roleName === "HR" && (
            <Button title="Create New Travel Plan"
              onClick={() => setShowForm(true)} className="gap-2 text-gray-600">
              <Plus size={18} />
              New Plan
            </Button>
          )}

          <div className="relative inline-block">
            <Bell 
              size={25} 
              onClick={() => setShowNotification(true)} 
              className="text-gray-600 cursor-pointer hover:text-blue-600 transition-colors"
            />
            {unreadNotifications > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center">
                {unreadNotifications}
              </span>
            )}
          </div>

        </header>

        <main className="p-6 max-w-7xl mx-auto space-y-6 w-250">
          
          {/* Notifications */}
          {showNotification && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative h-150 overflow-y-auto">
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowNotification(false);
                }}><X /></Button>
                <Notifications />
              </div>
            </div>
          )}

          {/* Create or edit travel plan form */}
          {showForm && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative">
                <Button title="Close Form" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setShowForm(false);
                  setEditTravelPlanId(null);
                }}><X /></Button>
                <TravelPlanForm editTravelPlanId={editTravelPlanId} onSuccess={() => setShowForm(false)} />
              </div>
            </div>
          )}

          {/*Add travel expense by employee */}
          {activeExpenseId && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative">
                <Button title="Close Expense Form" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setActiveExpenseId(null);
                  setStartDate("");
                  setEndDate("");
                }}><X /></Button>
                <AddExpenseForm travelPlanId={activeExpenseId} startDate={startDate} endDate={endDate} onSuccess={() => {
                  setActiveExpenseId(null);
                  setStartDate("");
                  setEndDate("");
                }} />
              </div>
            </div>
          )}

          {/* Add travel doc by employee or HR */}
          {selectedTravelId && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative">
                <Button title="Close Document Form" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setSelectedTravelId(null);
                  setEditTravelPlanId(null);
                  setStartDate("");
                  setEndDate("");
                }}><X /></Button>
                <AddTravelDocumentForm travelPlanId={selectedTravelId}  onSuccess={() => {
                  setSelectedTravelId(null);
                  setEditTravelPlanId(null);
                  setStartDate("");
                  setEndDate("");
                }} />
              </div>
            </div>
          )}

          {/* Full travel details modal */}
          {fullTravelDetails && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-2xl w-full relative p-6 h-150 overflow-y-auto">
                <Button title="Close Travel Details" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setFullTravelDetails(null);
                }}><X /></Button>
                <FullTravelDetail travelPlan={fullTravelDetails} onSuccess={() => {
                  setFullTravelDetails(null);
                }} />
              </div>
          </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {isLoading ? (
               <p>Loading plans...</p>
            ) : filteredPlans.length > 0 ? (
              filteredPlans.sort((a: any, b: any) => new Date(b.travelPlanStartDate).getTime() - new Date(a.travelPlanStartDate).getTime()).map((plan: any) => (
                <Card key={plan.id} 
                  onClick={() => setFullTravelDetails(plan.id)}
                  className="hover:shadow-md transition-shadow border-slate-200 cursor-pointer">
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
                          !m.employeeIsDeletedFromTravel ? (
                            <Badge key={m.employeeEmail} variant="secondary" className="text-[10px]">
                              {m.employeeEmail.split('@')[0]}
                            </Badge>
                        ): null
                        ))}
                      </div>
                    </div>

                    {/* manager also add doc so update thia */}

                    
                    {user?.roleName === "HR" || user?.roleName === "EMPLOYEE" || (user?.roleName === "MANAGER" && plan.employeeFkManagerEmployeeId !== user.id) ? (
                      <div className="mt-2 flex justify-between gap-2">
                        { new Date(plan.travelPlanStartDate) > new Date() ? (
                        <Button 
                          title="Add Travel Document"
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedTravelId(plan.id);
                          }}
                          className="text-gray-600 mt-2"
                        >
                          <ImagePlus size={14} /> Doc
                        </Button>
                        ):(<></>)}

                        {(() => {
                          const now = new Date().getTime();
                          const planEndDate = new Date(plan.travelPlanEndDate).getTime();
                          const tenDays = 10 * 24 * 60 * 60 * 1000;
                          const expiryDate = planEndDate + tenDays;
                          const canClaim = now >= planEndDate && now <= expiryDate;
                          return canClaim && (  
                            <Button 
                              title="Claim Travel Expense"
                              onClick={(e) => {
                                e.stopPropagation();
                                setActiveExpenseId(plan.id);
                                setStartDate(plan.travelPlanStartDate);
                                setEndDate(plan.travelPlanEndDate);
                              }}
                              className="text-gray-600 mt-2"
                            >
                              <Plus size={14} /> Exp
                            </Button>
                            );
                        //     : 
                        //   new Date(plan.travelPlanStartDate) > new Date() ? (
                        //   <Button disabled className="w-full text-gray-400 mt-2 cursor-not-allowed">
                        //     Claim Expense Not Available Yet
                        //   </Button>
                        // ) : (
                        //   <Button disabled className="w-full text-gray-400 mt-2 cursor-not-allowed">
                        //     Claim Expense Expire
                        //   </Button>
                        // );
                        })()}

                        {user?.roleName === "HR" && new Date(plan.travelPlanStartDate) > new Date() ? (
                          <>
                          <Button 
                            title="Edit Travel Plan"
                            onClick={(e) => {
                              e.stopPropagation();
                              setShowForm(true);
                              setEditTravelPlanId(plan.id);
                            }}
                            className="text-gray-600 mt-2"
                          >
                          <Edit size={14} />
                          </Button>
                          <Button 
                            title="Delete Travel Plan"
                            onClick={(e) => {
                              e.stopPropagation();
                              deleteTravelPlanMutation.mutate(plan.id);
                            }}
                            className="text-red-500 hover:text-red-700 right-0 mt-2"
                          >
                            <Trash2 size={14} />
                          </Button>
                        </>
                        ):(<></>)}
                      </div>
                    ) : null}
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








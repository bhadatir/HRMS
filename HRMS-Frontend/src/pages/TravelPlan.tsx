
import { useState, useMemo, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { travelService } from "../api/travelService";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
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
import { useInView } from "react-intersection-observer";
import { useFindTravelPlanByEmployeeId, useGetAllTravelPlans } from "../hooks/useInfinite";
import { ScrollToTop } from "@/components/ScrollToTop.tsx";
import { useAppDebounce } from "@/hooks/useAppDebounce.tsx";
import { GlobalSearch } from "@/components/GlobalSearch.tsx";
import { useToast } from "@/context/ToastContext.tsx";

type Plan = {
  id: number;
  travelPlanName: string;
  travelPlanFrom: string;
  travelPlanTo: string;
  travelPlanStartDate: string;
  travelPlanEndDate: string;
  travelPlanDetails: string;
  travelPlanIsReturn: boolean;
  travelPlanIsDeleted: boolean;
  employeeId: number;
  employeeEmail: string;
  employeeTravelPlanResponses: EmployeeTravelPlanResponse[];
}

type EmployeeTravelPlanResponse = {
  employeeId: number;
  employeeEmail: string;
  employeeIsDeletedFromTravel: boolean;
}

export default function TravelPlan() {
  const toast = useToast();
  const { token, user, unreadNotifications } = useAuth(); 
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [showNotification, setShowNotification] = useState(false);
  const [fullTravelDetails, setFullTravelDetails] = useState<number | null>(null);
  const [editTravelPlanId, setEditTravelPlanId] = useState<number | null>(null);
  const [activeExpenseId, setActiveExpenseId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState(() => {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get("travelPlanId") || "";
  });
  const [travelPlanType, setTravelPlanType] = useState<number>(() => {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get("travelPlanId") ? 0 : 1;
  });
  const [selectedTravelId, setSelectedTravelId] = useState<number | null>(null);
  const debouncedSearchTerm = useAppDebounce(searchTerm);

  const {
    data: allTravelPlansData,
    fetchNextPage: fetchAllTravelPlansNextPage,
    hasNextPage: hasAllTravelPlansNextPage,
    isFetchingNextPage: isFetchingAllTravelPlansNextPage,
    isError: allTravelPlansError,
  } = useGetAllTravelPlans(searchTerm, token || "");

  const {
    data: travelPlanDataByEmpId,
    fetchNextPage: fetchTravelPlanByEmpIdNextPage,
    hasNextPage: hasTravelPlanByEmpIdNextPage,
    isFetchingNextPage: isFetchingTravelPlanByEmpIdNextPage,
    isError: travelPlanByEmpIdError,
  } = useFindTravelPlanByEmployeeId(searchTerm, travelPlanType, token || "");

  const allTravelPlans = useMemo(
    () => allTravelPlansData?.pages.flatMap(page => page.content) || [],
    [allTravelPlansData]
  );

  const travelPlanByEmpId = useMemo(
    () => travelPlanDataByEmpId?.pages.flatMap(page => page.content) || [],
    [travelPlanDataByEmpId]
  );

  const { ref, inView } = useInView();
  
  useEffect(() => {
    if (inView && hasAllTravelPlansNextPage && !isFetchingAllTravelPlansNextPage) {
      fetchAllTravelPlansNextPage();
    }
  }, [inView, hasAllTravelPlansNextPage, isFetchingAllTravelPlansNextPage, fetchAllTravelPlansNextPage]);

  useEffect(() => {
    if (inView && hasTravelPlanByEmpIdNextPage && !isFetchingTravelPlanByEmpIdNextPage) {
      fetchTravelPlanByEmpIdNextPage();
    }
  }, [inView, hasTravelPlanByEmpIdNextPage, isFetchingTravelPlanByEmpIdNextPage, fetchTravelPlanByEmpIdNextPage]);

  const deleteTravelPlanMutation = useMutation({
    mutationFn: ({ travelPlanId, reason }: { travelPlanId: number; reason: string }) => travelService.deleteTravelPlan(travelPlanId, reason, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allTravelPlans"] });
      queryClient.invalidateQueries({ queryKey: ["travelPlanByEmpId"] });
      toast?.success("Travel plan deleted successfully");
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      const data = error.response?.data;  
      const detailedError = typeof data === 'object' 
      ? JSON.stringify(data, null, 2) 
      : data || error.message;
      toast?.error("Failed to delete travel plan: " + detailedError);
    }
  });

  const filteredPlans = useMemo(() => {
    if (!user) return [];
    if(user?.roleName === "ADMIN") {
      if (!allTravelPlans) return [];
      return allTravelPlans;
    }
    if (!travelPlanByEmpId) return [];
    return travelPlanByEmpId;   
  }, [user, travelPlanByEmpId, allTravelPlans]);

  const handleDelete = (travelPlanId: number) => {
    const reason = window.prompt("Please enter reason for deleting this travel plan:", "")?.trim();
    if (reason) {
      deleteTravelPlanMutation.mutate({ travelPlanId, reason });
    }else{
      toast?.error("Deletion reason is required");
    }
  };

  useEffect(() => {
    const clickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest("div.travel")) {
        setFullTravelDetails(null);
        setSelectedTravelId(null);
        setActiveExpenseId(null);
        setShowForm(false);
        setShowNotification(false);
      }
    };
    if (fullTravelDetails || selectedTravelId || activeExpenseId || showForm || showNotification) {
      document.addEventListener("click", clickOutside);
    } else {
      document.removeEventListener("click", clickOutside);
    }
    return () => document.removeEventListener("click", clickOutside);
  }, [fullTravelDetails, selectedTravelId, activeExpenseId, showForm, showNotification]);

  if (travelPlanByEmpIdError || allTravelPlansError) {
    toast?.error("Failed to load travel plans: " + (travelPlanByEmpIdError || allTravelPlansError));
  }

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            {/* <SidebarTrigger /> */}
            <h3 className="text-lg font-bold text-slate-800">Travel Management</h3>
            {(debouncedSearchTerm && debouncedSearchTerm.length > 0) ? (
              <Badge variant="outline">{filteredPlans.length} results</Badge>
            ) : travelPlanType ? (
              <Badge variant="outline">{travelPlanDataByEmpId?.pages[0]?.totalElements} results</Badge>
            ) : (
              <Badge variant="outline">No filter</Badge>
            )}
          </div>
          {user?.roleName !== "ADMIN" && 
          <div className="flex items-center gap-2">
            <select className="border rounded-md px-2 py-1 text-sm" 
              value={travelPlanType} onChange={(e) => setTravelPlanType(Number(e.target.value))}>
                <option value="0">All Travel Plan</option>
                <option value="1">Active</option>
                <option value="2">Deleted Plan</option>
                <option value="3">Removed By Hr</option>
                <option value="4">Return</option>
                <option value="5">One-Way</option>
            </select>
          </div>}

          <div className="relative max-w-sm w-full">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search Travel Plans (e.g. 'ti')..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
              }}
              autoFocus
            />
          </div>

          <div className="travel">
            {user?.roleName === "HR" && (
              <Button title="Create New Travel Plan"
                onClick={() => setShowForm(true)} className="gap-2 text-gray-600">
                <Plus size={18} />
                New Plan
              </Button>
            )}
          </div>

          <div className="travel relative inline-block">
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

        <main className="p-6 max-w-7xl mx-auto space-y-6 w-254">
          
          {/* Notifications */}
          {showNotification && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="travel relative bg-white rounded-xl max-w-3xl w-full h-150 overflow-y-auto">
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
              <div className="travel relative bg-white rounded-xl max-w-lg w-full h-150 overflow-y-auto">
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
              <div className="travel relative bg-white rounded-xl max-w-lg w-full">
                <Button title="Close Expense Form" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setActiveExpenseId(null);
                }}><X /></Button>
                <AddExpenseForm travelPlanId={activeExpenseId} onSuccess={() => {
                  setActiveExpenseId(null);
                }} />
              </div>
            </div>
          )}

          {/* Add travel doc by employee or HR */}
          {selectedTravelId && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="travel relative bg-white rounded-xl max-w-lg w-full">
                <Button title="Close Document Form" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setSelectedTravelId(null);
                  setEditTravelPlanId(null);
                }}><X /></Button>
                <AddTravelDocumentForm travelPlanId={selectedTravelId}  onSuccess={() => {
                  setSelectedTravelId(null);
                  setEditTravelPlanId(null);
                }} />
              </div>
            </div>
          )}

          {/* Full travel details modal */}
          {fullTravelDetails && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="travel relative bg-white rounded-xl max-w-4xl w-full p-6 h-150 overflow-y-auto">
                <Button title="Close Travel Details" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setFullTravelDetails(null);
                }}><X /></Button>
                <FullTravelDetail travelPlan={fullTravelDetails} onSuccess={() => {
                  setFullTravelDetails(null);
                }} />
              </div>
          </div>
          )}

          <div className="travel grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredPlans.length > 0 ? (
              filteredPlans.sort((a: Plan, b: Plan) => new Date(b.travelPlanStartDate).getTime() 
                                                      - new Date(a.travelPlanStartDate).getTime())
              .map((plan: Plan) => 
                (
                <Card key={plan.id}
                  onClick={() => setFullTravelDetails(plan.id)}
                  className="hover:shadow-md transition-shadow border-slate-200 cursor-pointer">
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <CardTitle className="text-xl font-bold text-blue-600">{plan.travelPlanName}</CardTitle>
                      <div className="flex items-center gap-1">  
                        <Badge variant="outline">{plan.travelPlanIsReturn ? "Return" : "One-Way"}</Badge>
                        <Badge variant="outline">{plan.travelPlanIsDeleted ? "Deleted" : "Active"}</Badge>
                      </div>
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
                    {user?.id !== plan.employeeId && <p className="text-xs text-slate-400">Created by: {plan.employeeEmail}</p>}

                    <div className="space-y-2">
                      <p className="text-xs font-bold text-slate-400 uppercase">Team Members</p>
                      <div className="flex flex-wrap gap-1">
                        {plan.employeeTravelPlanResponses.map((m: EmployeeTravelPlanResponse) => (
                          !m.employeeIsDeletedFromTravel ? (
                            <Badge key={m.employeeEmail} variant="secondary" className="text-[10px]">
                              {m.employeeEmail.split('@')[0]}
                            </Badge>
                        ): null
                        ))}
                      </div>
                    </div>
                    
                    {user?.roleName === "HR" || user?.roleName === "EMPLOYEE" || user?.roleName === "ADMIN" || 
                      (user?.roleName === "MANAGER" && plan.employeeTravelPlanResponses.some((resp: EmployeeTravelPlanResponse) => 
                          resp.employeeEmail === user.employeeEmail)) ? (
                      <div className="mt-2 flex justify-between gap-2">
                        { new Date(plan.travelPlanStartDate) > new Date()
                          && user?.roleName !== "ADMIN" 
                          && (user?.id === plan.employeeId || 
                              plan?.employeeTravelPlanResponses.some((resp: EmployeeTravelPlanResponse) => resp.employeeId === user?.id && resp.employeeIsDeletedFromTravel === false))
                          && !plan.travelPlanIsDeleted ? (
                        <Button
                          disabled={plan.travelPlanIsDeleted}
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

                        { (user?.roleName !== "HR" || (user?.roleName === "HR" && user?.id !== plan.employeeId )) &&
                          (plan.employeeTravelPlanResponses.some((resp: EmployeeTravelPlanResponse) => resp.employeeId === user?.id && resp.employeeIsDeletedFromTravel === false))
                          && user?.roleName !== "ADMIN"
                          && !plan.travelPlanIsDeleted ? ((() => {
                          const now = new Date().getTime();
                          const planStartDate = new Date(plan.travelPlanStartDate).getTime();
                          const planEndDate = new Date(plan.travelPlanEndDate).getTime();
                          const tenDays = 10 * 24 * 60 * 60 * 1000;
                          const expiryDate = planEndDate + tenDays;
                          const canClaim = now >= planStartDate && now <= expiryDate;
                          return canClaim && (  
                            <Button                             
                              disabled={plan.travelPlanIsDeleted}
                              title="Claim Travel Expense"
                              onClick={(e) => {
                                e.stopPropagation();
                                setActiveExpenseId(plan.id);
                              }}
                              className="text-gray-600 mt-2"
                            >
                              <Plus size={14} /> Exp
                            </Button>
                            );
                        }))():null}

                        {((user?.roleName === "HR" && user?.id === plan.employeeId) || user?.roleName === "ADMIN") && !plan.travelPlanIsDeleted && new Date(plan.travelPlanStartDate) > new Date() ? (
                          <>
                          <Button 
                            disabled={plan.travelPlanIsDeleted}
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
                            disabled={plan.travelPlanIsDeleted}
                            title="Delete Travel Plan"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDelete(plan.id);
                            }}
                            className="text-red-500 hover:text-red-700 right-0 mt-2"
                          >
                            <Trash2 size={14} />
                            {deleteTravelPlanMutation.isPending && deleteTravelPlanMutation.variables.travelPlanId === plan.id ? (
                            <span className="text-[10px] text-red-600">Deleting...</span>
                            ) : null}
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
          
          <div ref={ref} className="h-10 flex justify-center items-center">
            {( isFetchingAllTravelPlansNextPage || isFetchingTravelPlanByEmpIdNextPage )? <p className="text-xs">Loading more...</p> : null}
          </div>
          
          <ScrollToTop />
          <GlobalSearch />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}








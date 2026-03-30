import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { jobService } from "../api/jobService";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import ReferFriend from "../components/ReferFriend.tsx";
import ShareJob from "../components/ShareJob.tsx";
import { Plus, X, Calendar, Search, Share, UserPlus, Edit, Bell, IndianRupee, BookOpenIcon, Trash } from "lucide-react";
import JobForm from "../components/JobForm.tsx";
import JobDetailView from "@/components/JobDetailView.tsx";
import Notifications from "@/components/Notifications.tsx";
import { useAppDebounce } from "../hooks/useAppDebounce";
import { useGetAllJobs } from "../hooks/useInfinite";
import { useInView } from "react-intersection-observer";
import { ScrollToTop } from "@/components/ScrollToTop.tsx";
import { GlobalSearch } from "@/components/GlobalSearch.tsx";
import { useToast } from "@/context/ToastContext.tsx";
import { ConformationDialog } from "@/components/ConformationDialog.tsx";
import { useIsMobile } from "@/hooks/use-mobile.ts";

type Job = {
  id: number;
  jobTitle: string;
  jobDescriptionUrl: string;
  jobSalary: number;
  jobIsActive: boolean;
  employeeId: number;
  employeeEmail: string;
  jobCreatedAt: string;
};

export default function JobManagement() {
  const isMobile = useIsMobile();
  const toast = useToast();
  const { token, user, unreadNotifications } = useAuth();
  const [showForm, setShowForm] = useState(false);
  const [showNotification, setShowNotification] = useState(false);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [deleteJobId, setDeleteJobId] = useState<number>(0);
  const [searchTerm, setSearchTerm] = useState(() => {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get("jobId") || "";
  });
  const [shareJobId, setShareJobId] = useState<number | null>(null);
  const [editJobId, setEditJobId] = useState<number | null>(null);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [referJobId, setReferJobId] = useState<number | null>(null);
  const queryClient = useQueryClient();
  const [jobType, setJobType] = useState(() => {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get("jobId") ? 0 : 1;
  });
  const debouncedSearchTerm = useAppDebounce(searchTerm);

  const {
    data: allJobsData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isError: allJobsOnError,
  } = useGetAllJobs(searchTerm, jobType, token || "");
  const allJobs = allJobsData?.pages.flatMap(page => page.content) || [];

  const { ref, inView } = useInView();
  
  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage]);

  if (allJobsOnError) {
    toast?.error("Failed to load jobs: " + allJobsOnError);
  }

  const jobStatusmutation = useMutation({
    mutationFn: ({ jobId, reason }: { jobId: number; reason: string }) => jobService.updateJobStatus(jobId, reason, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allJobs"] });
      queryClient.invalidateQueries({ queryKey: ["jobDetail"] });
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      const data = error.response?.data;  
      const detailedError = typeof data === 'object' 
      ? JSON.stringify(data, null, 2) 
      : data || error.message;
      toast?.error("Failed to update job status: " + detailedError);
    }
  });

  useEffect(() => {
    const clickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest("div.job")) {
        setEditJobId(null);
        setSelectedJobId(null);
        setReferJobId(null);
        setShareJobId(null);
        setShowForm(false);
        setShowNotification(false);
      }
    };
    if (editJobId || referJobId || shareJobId || selectedJobId || showForm || showNotification) {
      document.addEventListener("click", clickOutside);
    } else {
      document.removeEventListener("click", clickOutside);
    }
    return () => document.removeEventListener("click", clickOutside);
  }, [editJobId, referJobId, shareJobId, selectedJobId, showForm, showNotification]);

  const handleDelete = (reason: string) => {
    jobStatusmutation.mutate({ jobId: deleteJobId, reason });
  };

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 text-white sticky top-0 z-10">
          <div className="flex items-center gap-2 pr-2">
            <SidebarTrigger/>
            <h3 className="text-lg font-bold">Job Board</h3>
            {(debouncedSearchTerm && debouncedSearchTerm.length > 0) ? (
              <Badge variant="outline" className="text-white">{allJobs.length} results</Badge>
            ) : jobType ? (
              <Badge variant="outline" className="text-white">{allJobsData?.pages[0]?.totalElements} results</Badge>
            ) : (
              <Badge variant="outline" className="text-white">No filter</Badge>
            )}
          </div>

          <div className="flex items-center gap-2">
            <select className="border rounded-md px-2 py-1 text-sm" 
              value={jobType} onChange={(e) => setJobType(Number(e.target.value))}>
                <option value="0">All Job</option>
                <option value="1">Open</option>
                <option value="2">Closed</option>
                {user?.roleName === "HR" && <option value="3">My Jobs</option>}
            </select>
          </div>

          <div className="job relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search by title or department..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              autoFocus
            />
          </div>

          <div className="job pr-2">
            {( user?.roleName === "HR" || user?.roleName === "ADMIN" ) && (
              <Button title="Post New Job" onClick={() => setShowForm(true)} className="gap-2 text-gray-700">
                <Plus size={18} />
                {!isMobile ? "Post New Job" : ""}
              </Button>
            )}
          </div>

          <div className="job relative inline-block">
            <Bell 
              size={25} 
              onClick={() => setShowNotification(true)} 
              className="text-white cursor-pointer"
            />
            {unreadNotifications > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center">
                {unreadNotifications}
              </span>
            )}
          </div>
        </header>

        {/* Confirmation Dialog */}
        {isDialogOpen && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="post bg-white bottom-52 rounded-xl max-w-lg w-full relative">
              <ConformationDialog
                onClose={() => setIsDialogOpen(false)} 
                onConfirm={(reason) => handleDelete(`${reason} (Deleted by : ${user?.employeeEmail} at ${new Date().toLocaleString()})`)} 
                iteam="job"
                action="Delete"
              />
            </div>
          </div>
        )}
        {/* Notifications */}
        {showNotification && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="job bg-white rounded-xl max-w-3xl w-full relative max-h-150 overflow-y-auto">
              <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                onClick={() => {
                setShowNotification(false);
              }}><X /></Button>
              <Notifications />
            </div>
          </div>
        )}
        {/* job details with reviewer and referral management */}
        {selectedJobId && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="job bg-white rounded-xl max-w-3xl w-full relative max-h-150 overflow-y-auto">
              <Button title="Close" variant="ghost" className="absolute right-2 top-2" 
                onClick={() => setSelectedJobId(null)}>
                <X />
              </Button>
              <JobDetailView jobId={selectedJobId} onSuccess={() => setSelectedJobId(null)} />
            </div>
          </div>            
        )}
        {/* Job Post Form Modal */}
        {showForm && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="job bg-white rounded-xl max-w-lg w-full relative max-h-150 overflow-y-auto">
              <Button title="Close" variant="ghost" className="absolute right-2 top-2" 
                onClick={() => setShowForm(false)}>
                <X />
              </Button>
              <JobForm editJobId={null} onSuccess={() => setShowForm(false)} />
            </div>
          </div>
        )}
        {/* share job */}
        {shareJobId && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="job bg-white rounded-xl max-w-2xl w-full relative max-h-150 overflow-y-auto">
              <Button title="Close" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                setShareJobId(null);
              }}><X /></Button>
              <ShareJob jobId={shareJobId} onSuccess={() => {
                setShareJobId(null);
              }} />
            </div>
        </div>
        )}
        {/* refer friend to job */}
        {referJobId && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="job bg-white rounded-xl max-w-2xl w-full relative max-h-150 overflow-y-auto">
              <Button title="Close" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                setReferJobId(null);
              }}><X /></Button>
              <ReferFriend jobId={referJobId} onSuccess={() => {
                setReferJobId(null);
              }} />
            </div>
        </div>
        )}
        {/* edit job */}
        {editJobId && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="job bg-white rounded-xl max-w-2xl w-full relative max-h-150 overflow-y-auto">
              <Button title="Close" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                setEditJobId(null);
              }}><X /></Button>
              <JobForm editJobId={editJobId} onSuccess={() => {
                setEditJobId(null);
              }} />
            </div>
          </div>
        )}

        <main className="p-6 space-y-6 w-full">
          <div className="job grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {allJobs.length > 0 ? (
              allJobs.map((job: Job) => ((job.jobIsActive || user?.roleName === "HR" || user?.roleName === "ADMIN") && (
                <Card 
                  key={job.id} 
                  className="border-slate-200 cursor-pointer group"
                  onClick={() => setSelectedJobId(job.id)}
                >
                  <CardHeader>
                    <div className="flex flex-row items-center justify-between">
                      <div className="flex items-center gap-2 justify-between pr-2">
                        <CardTitle className="text-lg font-bold">{job.jobTitle}</CardTitle>
                        <Badge variant="outline" 
                          className={job.jobIsActive ? "border-green-500 text-green-500" : "border-red-500 text-red-500"} >
                          {job.jobIsActive ? "Open" : "Closed"}
                        </Badge>
                      </div>

                      <div className="flex justify-between items-start">                     
                        {((user?.id === job.employeeId && user?.roleName === "HR") || user?.roleName === "ADMIN") && job.jobIsActive && 
                          <div className="flex gap-2">
                            <Button 
                              title="Edit job"
                              onClick={(e) => {
                                e.stopPropagation();
                                setEditJobId(job.id);
                              }}
                              className="text-gray-600"
                            >
                              <Edit size={14} />
                            </Button>
                            <Button 
                              title="close job"
                              onClick={(e) => {
                                e.stopPropagation();
                                setIsDialogOpen(true);
                                setDeleteJobId(job.id);
                              }}
                              className="text-gray-600"
                            >
                              <Trash size={14} className="text-gray-500" />
                            </Button>
                          </div>
                          }
                      </div>
                      
                    </div>
                  </CardHeader>
                  
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-2 gap-2 text-sm text-slate-600">
                      <div className="flex items-center gap-2">
                        <IndianRupee size={14} className="text-slate-400" />
                        {job.jobSalary}
                      </div>
                      <div className="flex items-center gap-2">
                        <Calendar size={14} className="text-slate-400" />
                        {job.jobCreatedAt.split("T")[0]}
                      </div>
                    </div>
                     
                    <a 
                        key={job.id} 
                        href={job.jobDescriptionUrl}
                        onClick={(e) => e.stopPropagation()}
                        target="_blank" 
                        rel="noreferrer"
                        className="text-gray-600 flex items-center gap-1 text-xs"
                    ><BookOpenIcon size={12} className="mr-1" />View Job Description</a>

                    {user?.id != job.employeeId && <p className="text-xs text-slate-500 mt-1">Created by : {job.employeeEmail}</p>}

                    {user?.id != job.employeeId && job.jobIsActive && user?.roleName !== "ADMIN" && (
                    <div className="mt-2 flex justify-between gap-2">
                        <Button 
                        title="Share this job"
                          onClick={(e) => {
                            e.stopPropagation();
                            setShareJobId(job.id);
                          }}
                          className="text-gray-600 mt-2"
                        >
                          <Share size={14} />
                        </Button>

                        <Button 
                          title="Refer a friend"
                          onClick={(e) => {
                            e.stopPropagation();
                            setReferJobId(job.id);
                          }}
                          className="text-gray-600 mt-2"
                        >
                          <UserPlus size={14} />
                        </Button>
                    </div>
                    )}  

                  </CardContent>
                </Card>
              )))
            ) : (
              <div className="col-span-full py-20 text-center text-slate-400 italic">
                No jobs matching your criteria.
              </div>
            )}
          </div>
          <div ref={ref} className="h-10 flex justify-center items-center">
            { isFetchingNextPage ? <p className="text-xs">Loading more...</p> : null}
          </div>
          
          <ScrollToTop />
          <GlobalSearch />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

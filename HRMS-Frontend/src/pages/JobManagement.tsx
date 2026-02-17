import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { jobService } from "../api/jobService";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import ReferFriend from "../components/ReferFriend.tsx";
import ShareJob from "../components/ShareJob.tsx";
import { Plus, X, Briefcase, MapPin, Calendar, Search, Users, DollarSign, Share, UserPlus, Edit } from "lucide-react";
import JobForm from "../components/JobForm.tsx";
import JobDetailView from "@/components/JobDetailView.tsx";

export default function JobManagement() {
  const { token, user } = useAuth();
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");  
  const [shareJobId, setShareJobId] = useState<number | null>(null);
  const [editJobId, setEditJobId] = useState<number | null>(null);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [referJobId, setReferJobId] = useState<number | null>(null);

  const { data: allJobs, isLoading } = useQuery({
    queryKey: ["allJobs"],
    queryFn: () => jobService.getAllJobs(token || ""),
    enabled: !!token,
  });

  const filteredJobs = useMemo(() => {
    if (!allJobs) return [];
    return allJobs.filter((job: any) =>
      job.jobTitle.toLowerCase().includes(searchTerm.toLowerCase())
    )
  }, [allJobs, searchTerm]);

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2">
            <SidebarTrigger />
            <h3 className="text-lg font-bold text-slate-800">Job Board</h3>
          </div>

          <div className="relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search by title or department..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {user?.roleName === "HR" && (
            <Button title="Post New Job" onClick={() => setShowForm(true)} className="gap-2 text-gray-700">
              <Plus size={18} />
              Post New Job
            </Button>
          )}
        </header>

        <main className="p-6 max-w-7xl mx-auto space-y-6 w-250">

          {/* job details with reviewer and referral management */}
          {selectedJobId && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative h-150 overflow-y-auto">
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
              <div className="bg-white rounded-xl max-w-lg w-full relative">
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
              <div className="bg-white rounded-xl max-w-2xl w-full relative p-6 overflow-y-auto">
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
              <div className="bg-white rounded-xl max-w-2xl w-full relative p-6 overflow-y-auto">
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
              <div className="bg-white rounded-xl max-w-2xl w-full relative p-6 h-150 overflow-y-auto">
                <Button title="Close" variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setEditJobId(null);
                }}><X /></Button>
                <JobForm editJobId={editJobId} onSuccess={() => {
                  setEditJobId(null);
                }} />
              </div>
          </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {isLoading ? (
              <p>Loading jobs...</p>
            ) : filteredJobs.length > 0 ? (
              filteredJobs.map((job: any) => (
                <Card 
                  key={job.id} 
                  className="border-slate-200 cursor-pointer group"
                  onClick={() => setSelectedJobId(job.id)}
                >
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <Badge variant="outline" className={job.jobIsActive ? "border-green-500 text-green-500" : "border-red-500 text-red-500"} >
                        {job.jobIsActive ? "Open" : "Closed"}
                      </Badge>

                      {user?.roleName === "HR" && 
                        <Button 
                          title="Edit job"
                          onClick={(e) => {
                            e.stopPropagation();
                            setEditJobId(job.id);
                          }}
                          className="text-gray-600 mt-2"
                        >
                          <Edit size={14} />
                        </Button>
                        }
                    </div>
                    <CardTitle className="text-xl font-bold">{job.jobTitle}</CardTitle>
                  </CardHeader>
                  
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-2 gap-2 text-sm text-slate-600">
                      <div className="flex items-center gap-2">
                        <DollarSign size={14} className="text-slate-400" />
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
                        target="_blank" 
                        rel="noreferrer"
                        className="text-blue-600 hover:underline flex items-center gap-1 text-xs"
                    >View Job Description</a>

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

                  </CardContent>
                </Card>
              ))
            ) : (
              <div className="col-span-full py-20 text-center text-slate-400 italic">
                No jobs matching your criteria.
              </div>
            )}
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

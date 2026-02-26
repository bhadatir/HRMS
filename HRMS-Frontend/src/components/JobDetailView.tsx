import { useState } from "react";
import { useQuery, useMutation, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { 
  Briefcase, 
  Users, 
  FileText, 
  CheckCircle, 
  XCircle, 
  ExternalLink, 
  UserCheck, 
  User,
  IndianRupee,
  Search,
  Plus
} from "lucide-react";
import { apiService } from "@/api/apiService";

export default function JobDetailView({ jobId }: { jobId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<"REFERRALS" | "REVIEWERS">("REFERRALS");
  const [newReviewerId, setNewReviewerId] = useState<number>(0);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [showDropdown, setShowDropdown] = useState<boolean>(false);
 
  const {
    data: infiniteData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage
  } = useInfiniteQuery({
    queryKey: ["employeeSearchInfinite", searchTerm],
    queryFn: ({ pageParam = 0 }) => 
      apiService.searchEmployees(searchTerm, pageParam, 10, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => 
      lastPage.last ? undefined : lastPage.number + 1,
    enabled: searchTerm.length >= 1,
  });
  const suggestions = infiniteData?.pages.flatMap(page => page.content) || [];
    
  const { data: job, isLoading: jobLoading } = useQuery({
    queryKey: ["jobDetail", jobId],
    queryFn: () => jobService.getJobById(jobId!, token || ""),
    enabled: !!jobId && !!token,
  });

  const { data: referrals = [], isLoading: referralsLoading } = useQuery({
    queryKey: ["jobReferrals", jobId],
    queryFn: () => jobService.getReferDataByJobId(jobId!, token || ""),
    enabled: !!jobId && !!token && viewMode === "REFERRALS",
  });

  const filteredReferrals = referrals.filter((ref: any) => 
    ref.referFriendName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ref.referFriendEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ref.employeeEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ref.cvStatusTypeName.toLowerCase().includes(searchTerm.toLowerCase()) 
  );

  const updateCvStatusMutation = useMutation({
    mutationFn: ({ referId, statusId, reason }: { referId: number; statusId: number; reason: string }) =>
      jobService.updateReferCvStatus(referId, statusId, reason, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobReferrals", jobId] });
      alert("CV Status updated successfully");
    },
    onError: (err: any) => alert(err.message)
  });

  const addReviewerMutation = useMutation({
    mutationFn: () => jobService.addCvReviewer(jobId!, newReviewerId, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobDetail", jobId] });
      setNewReviewerId(0);
      setSearchTerm("");
      alert("Reviewer added successfully");
    },
    onError: (err: any) => alert(err.message)
  });

  const handleSelectUser = (id: number) => {
    setNewReviewerId(id);
    setSearchTerm(""); 
    setShowDropdown(false);
    addReviewerMutation.mutate();
  };
  
  const handleUpdateCvStatus = (referId: number, statusId: number) => {
    if(statusId === 5) {
      const confirm = window.confirm("Are you sure you want to approve this cv?");
      if (confirm) updateCvStatusMutation.mutate({ referId, statusId, reason: "Approved by HR" });
    } else {
      const reason = window.prompt("Please enter reason for approval:", "")?.trim();
      if (reason) {
        updateCvStatusMutation.mutate({ referId, statusId, reason });
      }
    }
  };

  const isLoadingData = jobLoading || (viewMode === "REFERRALS" && referralsLoading);

  if (isLoadingData) return <div className="p-10 text-center text-slate-500">Loading Job Data...</div>;

  return (
    <div>
      <Card className="border-none shadow-none">
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle className="text-2xl font-bold text-slate-900 flex items-center gap-2">
              <Briefcase className="text-blue-600" /> {job?.jobTitle}
            </CardTitle>
            <CardDescription className="mt-1 flex">
              <div>
                <div className="flex">
                Salary: <IndianRupee size={14} className="mt-1"/> {job?.jobSalary} 
                </div>
                Category ID: {job?.jobTypeName}
              </div>
            </CardDescription>
          </div>
          {job?.employeeId != user?.id && <Badge title="Owner Email" variant="outline" className="bg-white">{job?.employeeEmail}</Badge>}
        </CardHeader>
      </Card>

      <Card className="border-none shadow-none">
        <CardHeader className="flex flex-row items-center justify-between">
            <Button className={viewMode === "REFERRALS" ? "rounded-md border text-gray-700" : "rounded-md text-gray-400"}
                size="sm"
                onClick={()=>setViewMode("REFERRALS")}><Users size={18} /> Referrals</Button>

            {viewMode === "REFERRALS" ? (
              <div className="relative max-w-sm w-full">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
                <Input 
                  placeholder="Search refer data..." 
                  className="pl-9"
                  value={searchTerm}
                  onChange={(e) => {
                    setSearchTerm(e.target.value);
                  }}
                />
              </div>
            ):(
              <>
              {user?.id === job.employeeId && user?.roleName === "HR" && job?.jobIsActive &&  (
                <div className="relative max-w-sm w-full">                 
                  <Plus className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400"/>
                  <Input 
                    placeholder="Add Reviewer..." 
                    className="pl-9"
                    value={searchTerm}
                    onChange={(e) => {
                      setSearchTerm(e.target.value);
                      setShowDropdown(true);
                    }}
                    onFocus={() => setShowDropdown(true)}
                  />

                  {showDropdown && suggestions.length > 0 && (
                    <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-60 overflow-y-auto">
                      {suggestions.map((emp: any) => {

                        if (emp.id === user?.id || emp.roleName !== "EMPLOYEE" || job?.cvReviewerResponses?.some((rev: any) => rev.employeeId === emp.id)) return null;

                        return (
                        <button
                          key={emp.id}
                          className="w-full text-left px-4 py-3 hover:bg-slate-100 flex items-center gap-3 border-b last:border-none"
                          onClick={() => handleSelectUser(emp.id)}
                        >
                            <User size={14} className="text-blue-600" />
                            <div>
                            <p className="text-sm font-semibold text-slate-900">{emp.employeeFirstName} {emp.employeeLastName}</p>
                            </div>
                        </button>
                        );
                      })} 

                      {hasNextPage && (
                        <Button
                          variant="ghost"
                          className="w-full text-[10px] text-blue-600 h-8"
                          onClick={() => fetchNextPage()}
                          disabled={isFetchingNextPage}
                        >
                          {isFetchingNextPage ? "Loading more..." : "Show More Results"}
                        </Button>
                      )}
                    </div>
                  )}
                </div>
              )}
              </>
            )}

            <Button className={viewMode === "REVIEWERS" ? "rounded-md border text-gray-700" : "rounded-md text-gray-400"}
                size="sm"
                onClick={()=>setViewMode("REVIEWERS")}><UserCheck size={18} /> Reviewers</Button>
        </CardHeader>

        <CardContent>
          {viewMode === "REFERRALS" ? (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="text-center">Candidate</TableHead>
                  <TableHead className="text-center">Referred By</TableHead>
                  <TableHead className="text-center">CV</TableHead>
                  <TableHead className="text-center">Status</TableHead>
                  {user?.id === job.employeeId && user?.roleName === "HR" && job?.jobIsActive && <TableHead className="text-center">Actions</TableHead>}
                  {<TableHead className="text-center">Reason</TableHead>}
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredReferrals.map((ref: any) => (
                  <TableRow key={ref.id}>
                    <TableCell>
                      <p className="font-bold">{ref.referFriendName}</p>
                      <p className="text-xs text-slate-500 max-w-[120px] truncate" title={ref.referFriendEmail}>{ref.referFriendEmail}</p>
                    </TableCell>
                    <TableCell className="text-xs max-w-[120px] truncate" title={ref.employeeEmail}>{ref.employeeEmail}</TableCell>
                    <TableCell>
                      <div className="flex flex-col gap-1">
                        <a href={ref.referFriendCvUrl} target="_blank" rel="noreferrer" className="text-blue-600 flex items-center gap-1 text-xs">
                          <ExternalLink size={12} /> CV
                        </a>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge className={ref.cvStatusTypeName === "APPROVED" ? "bg-green-100 text-green-700" 
                                            : ref.cvStatusTypeName === "REJECTED" ? "bg-red-100 text-red-700" : "bg-yellow-100 text-yellow-700"}>
                        {ref.cvStatusTypeName}
                      </Badge>
                    </TableCell>
                    {user?.id === job.employeeId && user?.roleName === "HR" && job?.jobIsActive && (
                      <TableCell className="text-right space-x-2">
                        {ref.cvStatusTypeName === "PENDING" &&
                        <>
                            <Button 
                            size="sm" variant="outline" className="text-green-600 border-green-200"
                            onClick={() => handleUpdateCvStatus(ref.id, 5)}
                            >
                            <CheckCircle size={14} />
                            </Button>
                            <Button 
                            size="sm" variant="outline" className="text-red-600 border-red-200"
                            onClick={() => handleUpdateCvStatus(ref.id, 6)}
                            >
                            <XCircle size={14} />
                            </Button>
                        </>
                        }
                      </TableCell>
                    )}                    
                    <TableCell>{ref.reasonForCvStatusChange || "-"}</TableCell>
                  </TableRow>
                ))}
                {filteredReferrals.length === 0 && (
                <TableRow>
                  <TableCell colSpan={user?.roleName === "HR" ? 6 : 5} className="text-center py-10">
                    <div className="flex flex-col items-center gap-2">
                      <FileText size={32} className="text-slate-400" />
                      <p className="text-sm text-slate-400">No referrals added yet.</p>
                    </div>
                  </TableCell>
                </TableRow>
              )}
              </TableBody>
            </Table>
          ) : (
            <div className="space-y-2">
              <h4 className="text-sm font-bold text-slate-700 flex items-center gap-2">
                <UserCheck size={16} className="text-blue-600" /> Assigned Reviewers
              </h4>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-3">                   
                {job?.cvReviewerResponses?.map((rev: any) => (
                <Card key={rev.id} className="border-slate-200 flex items-start gap-4 p-4">
                  <div className="flex flex-col gap-1">
                      <p className="text-sm font-semibold truncate max-w-[180px]" title={rev.employeeEmail}>
                          {rev.employeeEmail}
                      </p>
                  </div>                    
                </Card>
                ))}
                {(!job?.cvReviewerResponses || job.cvReviewerResponses.length === 0) && (
                  <p className="text-xs text-slate-400 italic">No reviewers assigned yet.</p>
                )}
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

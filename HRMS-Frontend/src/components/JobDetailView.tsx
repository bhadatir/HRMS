import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
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
  UserPlus, 
  UserCheck, 
  User,
  IndianRupee
} from "lucide-react";
import { apiService } from "@/api/apiService";

export default function JobDetailView({ jobId, onSuccess }: { jobId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<"REFERRALS" | "REVIEWERS">("REFERRALS");
  const [newReviewerId, setNewReviewerId] = useState<number>(0);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [showDropdown, setShowDropdown] = useState<boolean>(false);

  const { data: suggestions } = useQuery({
      queryKey: ["employeeSearch", searchTerm],
      queryFn: () => apiService.searchEmployees(searchTerm, token || ""),
      enabled: searchTerm.length >= 1,
  });
    
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
    const reason = window.prompt("Please enter reason for updating CV status:", "")?.trim();
    if (reason) {
      updateCvStatusMutation.mutate({ referId, statusId, reason });
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
            <Button className={viewMode === "REVIEWERS" ? "rounded-md border text-gray-700" : "rounded-md text-gray-400"}
                size="sm"
                onClick={()=>setViewMode("REVIEWERS")}><UserCheck size={18} /> Reviewers</Button>
        </CardHeader>

        <CardContent>
          {viewMode === "REFERRALS" ? (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Candidate</TableHead>
                  <TableHead>Referred By</TableHead>
                  <TableHead>CV</TableHead>
                  <TableHead>Status</TableHead>
                  {user?.id === job.employeeId && user?.roleName === "HR" && job?.jobIsActive && <TableHead>Actions</TableHead>}
                </TableRow>
              </TableHeader>
              <TableBody>
                {referrals.map((ref: any) => (
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
                  </TableRow>
                ))}
                {referrals.length === 0 && (
                <TableRow>
                  <TableCell colSpan={user?.roleName === "HR" ? 5 : 4} className="text-center py-10">
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
            <div className="space-y-6">
              {user?.id === job.employeeId && user?.roleName === "HR" && job?.jobIsActive &&  (
                <div className="relative max-w-sm w-full">
                  <div className="flex gap-2 items-end max-w-sm">
                    <div className="flex-1 space-y-1">
                      <label className="text-xs font-bold text-slate-500">ADD REVIEWER (ID)</label>
                      <Input 
                        placeholder="Enter Employee Name..." 
                        value={searchTerm}
                        onChange={(e) => {
                          setSearchTerm(e.target.value);
                          setShowDropdown(true);
                        }}
                        onFocus={() => setShowDropdown(true)}
                      />
                    </div>
                  </div>

                  {showDropdown && suggestions && suggestions.length > 0 && (
                    <div className="absolute top-full left-0 w-full bg-white border rounded-md shadow-lg mt-1 z-50 max-h-60 overflow-auto">
                      {suggestions.map((emp: any) => ( emp.id !== job?.employeeId 
                          && emp.roleName === "EMPLOYEE"
                          && !job?.cvReviewerResponses?.some((rev: any) => rev.employeeId === emp.id) && (
                        <button
                          key={emp.id}
                          className="w-full text-left px-4 py-3 hover:bg-slate-100 flex items-center gap-3 border-b last:border-none"
                          onClick={() => handleSelectUser(emp.id)}
                        >
                          <div className="bg-blue-100 p-1.5 rounded-full">
                            <User size={14} className="text-blue-600" />
                          </div>
                          <div>
                            <p className="text-sm font-semibold text-slate-900">{emp.employeeFirstName} {emp.employeeLastName}</p>
                          </div>
                        </button>
                      )))}
                    </div>
                  )}
                </div>
              )}

              <div className="space-y-2">
                <h4 className="text-sm font-bold text-slate-700 flex items-center gap-2">
                  <UserCheck size={16} className="text-blue-600" /> Assigned Reviewers
                </h4>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">                   
                  {job?.cvReviewerResponses?.map((rev: any) => (
                  <Card key={rev.id} className="border-slate-200 flex items-start gap-4 p-4">
                    <div className="flex flex-col gap-1">
                        <p className="text-sm font-semibold truncate max-w-[120px]" title={rev.employeeEmail}>
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
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

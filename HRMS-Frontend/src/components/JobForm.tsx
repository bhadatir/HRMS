import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { UploadCloud } from "lucide-react";

export default function JobForm({ editJobId, onSuccess }: { editJobId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();

  const [docFile, setDocFile] = useState<File | null>(null);
  const [createdAt, setCreatedAt] = useState("");
  const [form, setForm] = useState({
    jobTitle: "",
    jobSalary: 0,
    fkJobTypeId: 1,
    fkJobOwnerEmployeeId: user?.id || 0
  });

  const getJobMutation = useMutation({
    mutationFn: () => jobService.getJobById(editJobId!, token || ""),
    onSuccess: (data) => {
      setCreatedAt(data.jobCreatedAt?.split("T")[0] || "");
      setForm({
        jobTitle: data.jobTitle,
        jobSalary: data.jobSalary,
        fkJobTypeId: data.jobTypeId,
        fkJobOwnerEmployeeId: data.employeeId
      });
    },
    onError: (err: any) => alert("Error loading job: " + err.message)
  });

  useEffect(() => {
    if (editJobId) {
      getJobMutation.mutate();
    }
  }, [editJobId]);

  const jobMutation = useMutation({
    mutationFn: async () => {
      const formData = new FormData();
      const jsonBlob = new Blob([JSON.stringify(form)], { type: "application/json" });
      formData.append("jobRequest", jsonBlob);
      
      if (docFile) {
        formData.append("file", docFile);
      }

      if (editJobId) {
        return jobService.updateJobWithJD(editJobId, formData, token || "");
      } else {
        return jobService.createJobWithJD(formData, token || "");
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allJobs"] });
      alert(editJobId ? "Job updated successfully!" : "Job created successfully!");
      onSuccess();
    },
    onError: (err: any) => alert(err.message)
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle className="text-xl">
          {editJobId ? `Update Job (Posted: ${createdAt})` : "Post New Job"}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Job Title</label>
          <Input 
            type="text" 
            value={form.jobTitle} 
            placeholder="e.g. Senior Java Developer" 
            onChange={(e) => setForm({ ...form, jobTitle: e.target.value })} 
          />
        </div>

        <div className="space-y-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Salary ($)</label>
          <Input 
            type="number" 
            value={form.jobSalary} 
            placeholder="Annual Salary" 
            onChange={(e) => setForm({ ...form, jobSalary: Number(e.target.value) })} 
          />
        </div>

        <div className="space-y-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Job Category</label>
          <select
            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
            value={form.fkJobTypeId}
            onChange={(e) => setForm({ ...form, fkJobTypeId: Number(e.target.value) })}
          >
            <option value="1">Software Developer</option>
            <option value="2">Data Analyst</option>
            <option value="3">Data Science</option>
            <option value="4">Frontend Developer</option>
            <option value="5">Backend Developer</option>
            <option value="6">Other</option>
          </select>
        </div>

        <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
          <UploadCloud className="mx-auto text-slate-400" size={32} />
          <Input
            type="file"
            className="cursor-pointer"
            onChange={(e) => e.target.files && setDocFile(e.target.files[0])}
          />
          <p className="text-xs text-slate-400">
            {editJobId ? "Upload new JD to replace existing file (Optional)" : "Upload job description (Required)"}
          </p>
          {docFile && <p className="text-xs text-blue-600 font-medium">{docFile.name}</p>}
        </div>

        <Button
          title={editJobId ? "Update Job Posting" : "Create Job Posting"}
          className="w-full text-black mt-4"
          onClick={() => jobMutation.mutate()}
          disabled={jobMutation.isPending || (!editJobId && !docFile)}
        >
          {jobMutation.isPending ? "Processing..." : editJobId ? "Update Job Posting" : "Create Job Posting"}
        </Button>
      </CardContent>
    </Card>
  );
}

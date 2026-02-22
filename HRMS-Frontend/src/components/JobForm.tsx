import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { UploadCloud } from "lucide-react";
import { useForm } from "react-hook-form";

type JobFormInputs ={
    jobTitle: string;
    jobSalary: number;
    fkJobTypeId: number;
    fkJobOwnerEmployeeId: number;
    file?: File[];
}

export default function JobForm({ editJobId, onSuccess }: { editJobId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [createdAt, setCreatedAt] = useState("");

  const { data: allJobTypes = [] } = useQuery({
    queryKey: ["allJobTypes"],
    queryFn: () => jobService.getAllJobTypes(token || ""),
    enabled: !!token,
  });

  const {register, handleSubmit, reset, watch, formState: { errors }} = useForm<JobFormInputs>({
    defaultValues: {
      jobTitle: "",
      jobSalary: 0,
      fkJobTypeId: 1,
      fkJobOwnerEmployeeId: user?.id || 0
    }
  });

  const getJobMutation = useMutation({
    mutationFn: () => jobService.getJobById(editJobId!, token || ""),
    onSuccess: (data) => {
      setCreatedAt(data.jobCreatedAt?.split("T")[0] || "");
      reset({
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
    mutationFn: async (data: JobFormInputs) => {
      const formData = new FormData();
      const jsonBlob = new Blob([JSON.stringify(data)], { type: "application/json" });
      formData.append("jobRequest", jsonBlob);
      
      if (data.file) {
        formData.append("file", data.file[0]);
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
        <form onSubmit={handleSubmit(data => jobMutation.mutate(data))} className="space-y-4">
        <div className="space-y-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Job Title</label>
          <Input 
            type="text" 
            {...register("jobTitle", { required: "Job title is required" })}
            placeholder="e.g. Senior Java Developer" 
          />
          {errors.jobTitle && <p className="text-red-500 text-xs">{errors.jobTitle.message}</p>}
        </div>

        <div className="space-y-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Salary</label>
          <Input 
            type="number" 
            {...register("jobSalary", { required: "Job salary is required" })}
            placeholder="Annual Salary" 
          />
          {errors.jobSalary && <p className="text-red-500 text-xs">{errors.jobSalary.message}</p>}
        </div>

        <div className="space-y-1">
          <label className="text-xs font-bold text-slate-500 uppercase">Job Category</label>
          <select
            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
            {...register("fkJobTypeId", { required: "Job category is required" })}
          >
            <option value="">Select job category</option>
            {allJobTypes.map((type: any) => (
              <option key={type.id} value={type.id}>{type.jobTypeName}</option>
            ))}
          </select>
          {errors.fkJobTypeId && <p className="text-red-500 text-xs">{errors.fkJobTypeId.message}</p>}
        </div>

        <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
          <UploadCloud className="mx-auto text-slate-400" size={32} />
          <Input
            type="file"
            className="cursor-pointer"
            accept=".pdf,.doc,.docx"
              {...register("file", {required: !editJobId && "Job description is required for new jobs"})}
          />
          {errors.file && <p className="text-red-500 text-xs">{errors.file.message}</p>}
          <p className="text-xs text-slate-400">
            {editJobId ? "Upload new JD to replace existing file (Optional)" : "Upload job description (Required)"}
          </p>
          {watch("file") && <p className="text-xs text-blue-600 font-medium">{watch("file")?.[0]?.name}</p>}
        </div>

        <Button
          title={editJobId ? "Update Job Posting" : "Create Job Posting"}
          className="w-full text-black mt-4"
          disabled={jobMutation.isPending || (!editJobId && !watch("file")) || !watch("jobTitle") || !watch("jobSalary") || !watch("fkJobTypeId") }
        >
          {jobMutation.isPending ? "Processing..." : editJobId ? "Update Job Posting" : "Create Job Posting"}
        </Button>
        </form>
      </CardContent>
    </Card>
  );
}

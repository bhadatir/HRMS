import { useMutation } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Send } from "lucide-react";
import { useForm } from "react-hook-form";
import { useToast } from "@/context/ToastContext";

type ShareJobFormInputs = {
    fkJobShareEmployeeId: number;
    fkJobId: number;
    emails: string;
}

export default function ShareJob({ jobId, onSuccess }: { jobId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();
  const toast = useToast();
  
  const {register, handleSubmit, watch, formState: { errors }} = useForm<ShareJobFormInputs>({
    defaultValues: {
      fkJobShareEmployeeId: user?.id || 0,
      fkJobId: jobId,
      emails: ""
    }
  });

  const shareMutation = useMutation({
    mutationFn: (data: ShareJobFormInputs) => {
      const payload = {
        fkJobShareEmployeeId: data.fkJobShareEmployeeId,
        fkJobId: data.fkJobId,
        emails: data.emails.split(",").map(email => email.trim()).filter(email => email !== "")
      };

      return jobService.shareJob(payload, token || "");
    },
    onSuccess: () => {
      toast?.success("Job shared successfully!");
      onSuccess();
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      const data = error.response?.data;  
      const detailedError = typeof data === 'object' 
      ? JSON.stringify(data, null, 2) 
      : data || error.message;
      toast?.error("Failed to share job: " + detailedError); }
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle className="text-xl flex items-center gap-2">
          <Send size={20} className="text-gray-600" /> Share Job
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <form onSubmit={handleSubmit(data => shareMutation.mutate(data))} className="space-y-4">
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-500">Recipient Emails (Comma separated)</label>
          <Input 
            placeholder="e.g. friend1@gmail.com, friend2@gmail.com"
              {...register("emails", {
                required: "Emails are required"
                ,
                pattern: {
                  value: /^\s*[\w.-]+@[\w.-]+\.[a-z]{2,}(\s*,\s*[\w.-]+@[\w.-]+\.[a-z]{2,})*\s*$/i,
                  message: "Please enter valid email addresses separated by commas"
                }
              })}
          />
          {errors.emails && <p className="text-red-500 text-xs">{errors.emails.message}</p>}
        </div>
        <Button 
          title="Send Job Details"
          className="w-full text-black" 
          // eslint-disable-next-line react-hooks/incompatible-library
          disabled={shareMutation.isPending || !watch("emails")?.trim()}
        >
          {shareMutation.isPending ? "Sharing..." : "Send Job Details"}
        </Button>
        </form>
      </CardContent>
    </Card>
  );
}


import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Send, X } from "lucide-react";

export default function ShareJob({ jobId, onSuccess }: { jobId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();
  const [emails, setEmails] = useState<string>("");

  const shareMutation = useMutation({
    mutationFn: () => {
      const payload = {
        fkJobShareEmployeeId: user?.id || 0,
        fkJobId: jobId,
        emails: emails.split(",").map(email => email.trim()).filter(email => email !== "")
      };
      return jobService.shareJob(payload, token || "");
    },
    onSuccess: () => {
      alert("Job shared successfully!");
      onSuccess();
    },
    onError: (err: any) => alert("Error sharing job: " + err.message)
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle className="text-xl flex items-center gap-2">
          <Send size={20} className="text-blue-600" /> Share Job
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-500">Recipient Emails (Comma separated)</label>
          <Input 
            placeholder="e.g. friend1@gmail.com, friend2@gmail.com" 
            value={emails}
            onChange={(e) => setEmails(e.target.value)}
          />
        </div>
        <Button 
          title="Send Job Details"
          className="w-full text-black" 
          onClick={() => shareMutation.mutate()}
          disabled={shareMutation.isPending || !emails}
        >
          {shareMutation.isPending ? "Sharing..." : "Send Job Details"}
        </Button>
      </CardContent>
    </Card>
  );
}

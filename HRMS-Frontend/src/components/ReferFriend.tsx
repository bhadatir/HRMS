
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { UploadCloud, UserPlus } from "lucide-react";

export default function ReferFriend({ jobId, onSuccess }: { jobId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();
  const [cvFile, setCvFile] = useState<File | null>(null);
  const queryClient = useQueryClient();
  const [form, setForm] = useState({
    referFriendName: "",
    referFriendEmail: "",
    referFriendShortNote: "",
    fkCvStatusTypeId: 4,
    fkReferFriendEmployeeId: user?.id || 0,
    fkJobId: jobId
  });

  const referMutation = useMutation({
    mutationFn: async () => {
      if (!cvFile) throw new Error("Please upload a CV");

      const formData = new FormData();
      
      const jsonBlob = new Blob([JSON.stringify(form)], { type: "application/json" });
      formData.append("referFriendRequest", jsonBlob);
      
      formData.append("file", cvFile);

      return jobService.referFriend(formData, token || "");
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allJobs"] });
      queryClient.invalidateQueries({ queryKey: ["jobDetail", jobId] });
      alert("Referral submitted successfully!");
      onSuccess();
    },
    onError: (err: any) => alert("Referral Error: " + err.message)
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle className="text-xl flex items-center gap-2">
          <UserPlus size={20} className="text-blue-600" /> Refer a Friend
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input 
            placeholder="Friend's Full Name" 
            onChange={(e) => setForm({...form, referFriendName: e.target.value})} 
          />
          <Input 
            type="email" 
            placeholder="Friend's Email" 
            onChange={(e) => setForm({...form, referFriendEmail: e.target.value})} 
          />
        </div>
        
        <Textarea 
          placeholder="Short note about the candidate..." 
          onChange={(e) => setForm({...form, referFriendShortNote: e.target.value})} 
        />

        <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
          <UploadCloud className="mx-auto text-slate-400" size={32} />
          <Input 
            type="file" 
            className="cursor-pointer" 
            accept=".pdf,.doc,.docx"
            onChange={(e) => e.target.files && setCvFile(e.target.files[0])} 
          />
          <p className="text-xs text-slate-400">Upload CV/Resume (PDF or Word)</p>
          {cvFile && <p className="text-xs text-blue-600 font-bold">{cvFile.name}</p>}
        </div>

        <Button 
          title="Submit Referral"
          className="w-full text-black" 
          onClick={() => referMutation.mutate()}
          disabled={referMutation.isPending || !cvFile || !form.referFriendName}
        >
          {referMutation.isPending ? "Submitting..." : "Submit Referral"}
        </Button>
      </CardContent>
    </Card>
  );
}

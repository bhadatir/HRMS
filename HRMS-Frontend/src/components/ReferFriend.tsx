import { useMutation, useQueryClient } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { UploadCloud, UserPlus } from "lucide-react";
import { useForm, type SubmitHandler } from "react-hook-form";


type ReferFriendFormInputs ={
    referFriendName: string;
    referFriendEmail: string;
    referFriendShortNote: string;
    fkCvStatusTypeId: number;
    fkReferFriendEmployeeId: number;
    fkJobId: number;
    file?: File[];
}


export default function ReferFriend({ jobId, onSuccess }: { jobId: number, onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();

    const {register, handleSubmit, watch, formState: { errors }} = useForm<ReferFriendFormInputs>({
      defaultValues: {
        referFriendName: "",
        referFriendEmail: "",
        referFriendShortNote: "",
        fkCvStatusTypeId: 4,
        fkReferFriendEmployeeId: user?.id || 0,
        fkJobId: jobId
      }
    });

  const referMutation = useMutation({
    mutationFn: async (data: ReferFriendFormInputs) => {

      const formData = new FormData();
      
      const jsonBlob = new Blob([JSON.stringify(data)], { type: "application/json" });
      formData.append("referFriendRequest", jsonBlob);
      
      formData.append("file", data.file?.[0] || new File([], ""));

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
        <form onSubmit={handleSubmit(data => referMutation.mutate(data))} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input 
              placeholder="Friend's Full Name" 
              {...register("referFriendName", { required: "Friend's name is required" })}
            />
            {errors.referFriendName && <p className="text-red-500 text-xs">{errors.referFriendName.message}</p>}
            <Input 
              type="email" 
              placeholder="Friend's Email" 
              {...register("referFriendEmail", { required: "Friend's email is required" , 
                pattern: { value: /^\S+@\S+$/i, message: "Invalid email address" } })}
            />
            {errors.referFriendEmail && <p className="text-red-500 text-xs">{errors.referFriendEmail.message}</p>}
          </div>
          
          <Textarea 
            placeholder="Short note about the candidate..." 
            {...register("referFriendShortNote", { required: "Short note is required" })}
          />
          {errors.referFriendShortNote && <p className="text-red-500 text-xs">{errors.referFriendShortNote.message}</p>}

          <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
            <UploadCloud className="mx-auto text-slate-400" size={32} />
            <Input 
              type="file" 
              className="cursor-pointer" 
              accept=".pdf,.doc,.docx"
              {...register("file", { required: "CV is required" })}
            />
            <p className="text-xs text-slate-400">Upload CV/Resume (PDF or Word)</p>
            {errors.file && <p className="text-red-500 text-xs">{errors.file.message}</p>}
          </div>

          <Button 
            title="Submit Referral"
            className="w-full text-black" 
            type="submit"
            disabled={referMutation.isPending || !watch("file") || !watch("referFriendName") || !watch("referFriendEmail") || !watch("referFriendShortNote")}
          >
            {referMutation.isPending ? "Submitting..." : "Submit Referral"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

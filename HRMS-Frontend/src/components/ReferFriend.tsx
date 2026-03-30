import { useMutation, useQueryClient } from "@tanstack/react-query";
import { jobService } from "../api/jobService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { UploadCloud, UserPlus } from "lucide-react";
import { useForm } from "react-hook-form";
import { useToast } from "@/context/ToastContext";


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
  const toast = useToast(); 

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
      toast?.success("Referral submitted successfully!");
      onSuccess();
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      const data = error.response?.data;  
      const detailedError = typeof data === 'object' 
      ? JSON.stringify(data, null, 2) 
      : data || error.message;
        toast?.error("Failed to submit referral: " + detailedError); }
    });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle className="text-xl flex items-center gap-2">
          <UserPlus size={20} className="text-gray-600" /> Refer a Friend
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <form onSubmit={handleSubmit(data => referMutation.mutate(data))} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input 
              placeholder="Friend's Full Name" 
              {...register("referFriendName")}
            />
            {errors.referFriendName && <p className="text-red-500 text-xs">{errors.referFriendName.message}</p>}
            <Input 
              type="email" 
              placeholder="Friend's Email" 
              {...register("referFriendEmail", { pattern: { value: /^\S+@\S+$/i, message: "Invalid email address" } })}
            />
            {errors.referFriendEmail && <p className="text-red-500 text-xs">{errors.referFriendEmail.message}</p>}
          </div>
          
          <Textarea 
            placeholder="Short note about the candidate..." 
            {...register("referFriendShortNote")}
          />
          {errors.referFriendShortNote && <p className="text-red-500 text-xs">{errors.referFriendShortNote.message}</p>}

          <div className="border-2 border-dashed rounded-lg p-6 text-center space-y-2 border-slate-200">
            <UploadCloud className="mx-auto text-slate-400" size={32} />
            <Input 
              type="file" 
              className="cursor-pointer" 
              accept=".jpg,.jpeg,.png,.pdf,.docx,.doc"
              {...register("file")}
            />
            <p className="text-xs text-slate-400">Upload CV/Resume (PDF or Word)</p>
            {errors.file && <p className="text-red-500 text-xs">{errors.file.message}</p>}
          </div>

          <Button 
            title="Submit Referral"
            className="w-full text-black" 
            type="submit"
            // eslint-disable-next-line react-hooks/incompatible-library
            disabled={referMutation.isPending || !watch("file") || !watch("referFriendName")?.trim() || !watch("referFriendEmail")?.trim() || !watch("referFriendShortNote")?.trim()}
          >
            {referMutation.isPending ? "Submitting..." : "Submit Referral"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService.ts";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { UploadCloud, Eye } from "lucide-react";
import { useForm, type SubmitHandler } from "react-hook-form";

type PostFormInputs ={
  postTitle: string;
  postContent: string;
  fkPostEmployeeId: number;
  fkPostVisibilityId: number;
  file?: File[];
}
export default function PostForm({ editPostId, onSuccess }: { editPostId: number | null; onSuccess: () => void }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  
  const { data: allVisibilities = [] } = useQuery({
    queryKey: ["allVisibilities"],
    queryFn: () => postService.getAllVisibilities(token || ""),
    enabled: !!token,
  });
  
  const {register, handleSubmit, reset, watch, formState: { errors }} = useForm<PostFormInputs>({
    defaultValues: {
      postTitle: "",
      postContent: "",
      fkPostEmployeeId: user?.id || 0,
      fkPostVisibilityId: 1
    }
  });

  const getPostMutation = useMutation({
    mutationFn: () => postService.postByPostId(editPostId!, token || ""),
    onSuccess: (data) => {
      reset({
        postTitle: data.postTitle,
        postContent: data.postContent,
        fkPostEmployeeId: data.employeeId,
        fkPostVisibilityId: data.postVisibilityId
      });
    },
    onError: (err: any) => alert("Error loading post: " + err.message)
  });

  useEffect(() => {
    if (editPostId) {
      getPostMutation.mutate();
    }
  }, [editPostId]);

  const postMutation = useMutation({
    mutationFn: async (data: PostFormInputs) => {
      const formData = new FormData();

      const postRequest = {
        postTitle: data.postTitle,
        postContent: data.postContent,
        fkPostEmployeeId: data.fkPostEmployeeId,
        fkPostVisibilityId: data.fkPostVisibilityId
      };
      
      const jsonBlob = new Blob([JSON.stringify(postRequest)], { type: "application/json" });
      formData.append("postRequest", jsonBlob);
      
      if (data.file) {
        formData.append("file", data.file[0]);
      } else if (!editPostId) {
          formData.append("file", new Blob([], { type: "application/octet-stream" })); 
      }

      if (editPostId) {
        return postService.updatePost(editPostId, formData, token || "");
      } else {
        return postService.addPost(formData, token || "");
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allPosts"] });
      alert(editPostId ? "Post updated!" : "Post shared to feed!");
      onSuccess();
    },
    onError: (err: any) => alert(err.message)
  });

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle className="text-xl">
          {editPostId ? "Edit Post" : "Create New Post"}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <form onSubmit={handleSubmit(data => postMutation.mutate(data))} className="space-y-4">
        <Input 
          placeholder="Catchy Title" 
          {...register("postTitle", { required: "Title is required" })}
        />
        {errors.postTitle && <p className="text-red-500 text-xs">{errors.postTitle.message}</p>}
        
        <Textarea 
          placeholder="What would you like to share?" 
          className="min-h-[120px]"
          {...register("postContent", { required: "Content is required" })}
        />
        {errors.postContent && <p className="text-red-500 text-xs">{errors.postContent.message}</p>}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-500 uppercase flex items-center gap-1">
              <Eye size={12} /> Visibility
            </label>
            <select 
              className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
              {...register("fkPostVisibilityId", { required: "Visibility is required" })}
            >
              {allVisibilities.map((vis: any) => (
                <option key={vis.id} value={vis.id}>{vis.postVisibilityName}</option>
              ))}
            </select>
            {errors.fkPostVisibilityId && <p className="text-red-500 text-xs">{errors.fkPostVisibilityId.message}</p>}
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-500 uppercase flex items-center gap-1">
              <UploadCloud size={12} /> Attachment
            </label>
            <Input 
              type="file" 
              accept=".jpg,.jpeg,.png"
              {...register("file", {required: !editPostId && "File is required for new posts"})}
              className="cursor-pointer text-xs" 
            />
            {errors.file && <p className="text-red-500 text-xs">{errors.file.message}</p>}
          </div>
        </div>

        <Button 
          type="submit"
          className="w-full text-black"
          disabled={postMutation.isPending || (!editPostId && !watch("postTitle") || !watch("postContent") || !watch("fkPostVisibilityId") || !watch("file"))}
        >
          {postMutation.isPending ? "Processing..." : editPostId ? "Update Post" : "Post to Feed"}
        </Button>
        </form>
      </CardContent>
    </Card>
  );
}

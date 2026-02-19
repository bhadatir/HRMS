
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService";
import { useAuth } from "../context/AuthContext";
import { Badge } from "@/components/ui/badge";
import { Delete, Plus, Tag as TagIcon } from "lucide-react";

export default function PostTags({ postId, isOwner }: { postId: number; isOwner: boolean }) {
  const { token } = useAuth();
  const queryClient = useQueryClient();
  const [showTagInput, setShowTagInput] = useState(false);

  const { data: tags = [] } = useQuery({
    queryKey: ["postTags", postId],
    queryFn: () => postService.getPostTagsById(postId, token || ""),
    enabled: !!postId,
  });

  const { data: allTagTypes = [] } = useQuery({
    queryKey: ["allTagTypes"],
    queryFn: () => postService.getAllTagTypes(token || ""),
    enabled: !!token,
  });

  const addTagMutation = useMutation({
    mutationFn: (tagTypeId: number) => postService.addPostTagOnPost(postId, tagTypeId, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["postTags", postId] });
      queryClient.invalidateQueries({ queryKey: ["allPosts"] });
      setShowTagInput(false);
    }
  });

  const removeTagMutation = useMutation({
    mutationFn: (tagId: number) => postService.removePostTagFromPost(postId, tagId, token || ""),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["postTags", postId] })
  });

  return (
    <div className="flex flex-wrap gap-2 items-center mt-2">
      <TagIcon size={14} className="text-slate-400" />
      {tags.map((tag: any) => (
        <Badge key={tag.id} variant="secondary" className="text-[10px] bg-slate-100">
          {tag.tagTypeName}
          {isOwner && (
            <div className="ml-1 cursor-pointer" onClick={() => removeTagMutation.mutate(tag.id)}>
            <Delete size={12} className="text-slate-500" />
            </div>
          )}
        </Badge>
      ))}
      
      {isOwner && (
        <div className="relative">
          <div className="cursor-pointer" onClick={() => setShowTagInput(!showTagInput)} hidden={showTagInput}>
             <Plus size={20}  
            className="text-slate-400" />
          </div>
          
          {showTagInput && (
            <div className="space-y-2">
              <select 
                className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                onChange={(e) => addTagMutation.mutate(Number(e.target.value))}
              >
                {allTagTypes.map((tagType: any) => ( tagType.id !== 1 && !tags.find((tag: any) => tag.tagTypeId === tagType.id) && (
                  <option key={tagType.id} value={tagType.id}>{tagType.tagTypeName}</option>
                )))}
              </select> 
            </div>
          )}
        </div>
      )}
    </div>
  );
}

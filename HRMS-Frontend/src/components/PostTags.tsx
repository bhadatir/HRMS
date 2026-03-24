
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService";
import { useAuth } from "../context/AuthContext";
import { Badge } from "@/components/ui/badge";
import { Delete, Plus, Tag as TagIcon } from "lucide-react";
import { useToast } from "@/context/ToastContext";

type Tag = {
  id: number;
  tagTypeId: number;
  tagTypeName: string;
}

type TagType = {
  id: number;
  tagTypeName: string;
}

export default function PostTags({ postId, isOwner }: { postId: number; isOwner: boolean }) {
  const { token } = useAuth();
  const queryClient = useQueryClient();
  const [showTagInput, setShowTagInput] = useState(false);
  const toast = useToast();

  const { data: tags = [], isError: tagsError } = useQuery({
    queryKey: ["postTags", postId],
    queryFn: () => postService.getPostTagsById(postId, token || ""),
    enabled: !!postId,
  });

  const { data: allTagTypes = [], isError: allTagTypesError } = useQuery({
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
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      toast?.error("Failed to add tag: " + (error.response?.data || error.message)); }
  });

  const removeTagMutation = useMutation({
    mutationFn: (tagId: number) => postService.removePostTagFromPost(postId, tagId, token || ""),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["postTags", postId] }),
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      toast?.error("Failed to remove tag: " + (error.response?.data || error.message)); }
  });

  if (tagsError || allTagTypesError) toast?.error("Failed to load tags: " + (tagsError || allTagTypesError));
  
  return (
    <div className="flex flex-wrap gap-2 items-center mt-2">
      <TagIcon size={14} className="text-slate-400" />
      {tags.map((tag: Tag) => (
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
                <option value="">Select tag to add</option>
                {allTagTypes.map((tagType: TagType) => ( tagType.id !== 1 && !tags.find((tag: Tag) => tag.tagTypeId === tagType.id) && (
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


import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService";
import { useAuth } from "../context/AuthContext";
import { Badge } from "@/components/ui/badge";
import { Plus, Tag as TagIcon } from "lucide-react";

export default function PostTags({ postId, isOwner }: { postId: number; isOwner: boolean }) {
  const { token } = useAuth();
  const queryClient = useQueryClient();
  const [showTagInput, setShowTagInput] = useState(false);

  const { data: tags = [] } = useQuery({
    queryKey: ["postTags", postId],
    queryFn: () => postService.getPostTagsById(postId, token || ""),
    enabled: !!postId,
  });

  const addTagMutation = useMutation({
    mutationFn: (tagTypeId: number) => postService.addPostTagOnPost(postId, tagTypeId, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["postTags", postId] });
      setShowTagInput(false);
    }
  });

  return (
    <div className="flex flex-wrap gap-2 items-center mt-2">
      <TagIcon size={14} className="text-slate-400" />
      {tags.map((tag: any) => (
        <Badge key={tag.id} variant="secondary" className="text-[10px] bg-slate-100">
          {tag.tagTypeName}
        </Badge>
      ))}
      
      {isOwner && (
        <div className="relative">
          <button 
            onClick={() => setShowTagInput(!showTagInput)}
            className="text-blue-600 hover:bg-blue-50 rounded-full p-1"
          >
            <Plus size={14} />
          </button>
          
          {showTagInput && (
            <div className="absolute top-8 left-0 z-20 bg-white border shadow-xl rounded-lg p-2 flex flex-col gap-1 w-32">
              <button onClick={() => addTagMutation.mutate(1)} className="text-[10px] hover:bg-slate-100 p-1 text-left">Urgent</button>
              <button onClick={() => addTagMutation.mutate(2)} className="text-[10px] hover:bg-slate-100 p-1 text-left">Announcement</button>
              <button onClick={() => addTagMutation.mutate(3)} className="text-[10px] hover:bg-slate-100 p-1 text-left">Event</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

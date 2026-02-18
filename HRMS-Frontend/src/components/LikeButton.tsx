import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService";
import { useAuth } from "../context/AuthContext";
import { Heart, ThumbsUp } from "lucide-react";

export default function LikeButton({ postId, commentId }: { postId: number, commentId?: number }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();

  const { data: likes = [] } = useQuery({
    queryKey: commentId ? ["commentLikes", commentId] : ["postLikes", postId],
    queryFn: () => commentId ? postService.getLikeByCommentId(commentId, token || "") 
                                : postService.getLikeByPostId(postId, token || ""),
    enabled: !!postId,
  });

  const isLiked = likes.some((l: any) => l.employeeId === user?.id);

  const likeMutation = useMutation({
    mutationFn: () => {
      if (isLiked) {
        return commentId ? postService.removeLikeByComment(commentId, user?.id || 0, token || "") 
                            : postService.removeLikeByPost(postId, user?.id || 0, token || "");
      }
      const payload = {
        fkPostId: postId,
        fkLikeEmployeeId: user?.id || 0,
        fkCommentId: commentId || null
      };
      return postService.addLike(payload, token || "");
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: commentId ? ["commentLikes", commentId] : ["postLikes", postId] });
    }
  });

  return (
    <>
      <Heart className="text-gray-500" size={18} fill={isLiked ? "red" : "none"} onClick={() => likeMutation.mutate()}/>
      <span className="text-xs text-gray-500 font-bold">{likes.length}</span>
    </>
  );
}

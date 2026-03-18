import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService";
import { useAuth } from "../context/AuthContext";
import { Heart } from "lucide-react";

type Like = {
  id: number;
  fkPostId: number | null;
  fkCommentId: number | null;
  employeeId: number;
}

export default function LikeButton({ postId, commentId }: { postId: number, commentId?: number }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();

  const { data: likes = [], isError: likesError } = useQuery({
    queryKey: commentId ? ["commentLikes", commentId] : ["postLikes", postId],
    queryFn: () => commentId ? postService.getLikeByCommentId(commentId, token || "") 
                                : postService.getLikeByPostId(postId, token || ""),
    enabled: !!postId,
  });

  if (likesError) {
    alert("Failed to load likes for post/comment: " + likesError);
  }

  const isLiked = likes.some((l: Like) => l.employeeId === user?.id);

  const likeMutation = useMutation({
    mutationFn: () => {
      if (isLiked) {
        return commentId ? postService.removeLikeByComment(commentId, user?.id || 0, token || "") 
                            : postService.removeLikeByPost(postId, user?.id || 0, token || "");
      }
      let payload
      if(commentId) {
      payload = {
        fkPostId: null,
        fkLikeEmployeeId: user?.id || 0,
        fkCommentId: commentId
      };} else {
      payload = {
        fkPostId: postId,
        fkLikeEmployeeId: user?.id || 0,
        fkCommentId: null
      };
      }
      return postService.addLike(payload, token || "");
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: commentId ? ["commentLikes", commentId] : ["postLikes", postId] });
      queryClient.invalidateQueries({ queryKey: ["postComments", postId] });
      queryClient.invalidateQueries({ queryKey: ["allPosts"] });
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      alert("Failed to update like status: " + (error.response?.data || error.message)); }
  });

  return (
    <>
      <Heart className="text-gray-500" size={18} fill={isLiked ? "red" : "none"} onClick={() => likeMutation.mutate()}/>
      <span className="text-xs text-gray-500 font-bold">{likes.length}</span>
    </>
  );
}

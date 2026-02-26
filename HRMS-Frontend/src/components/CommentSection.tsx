import { useRef, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { postService } from "../api/postService";
import { useAuth } from "../context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Reply, Send, Trash2, X } from "lucide-react";
import LikeButton from "./LikeButton";

export default function CommentSection({ postId }: { postId: number }) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [newComment, setNewComment] = useState("");
  const [replyTo, setReplyTo] = useState<{ id: number; name: string } | null>(null);
  const commentref = useRef<HTMLInputElement>(null);

  const { data: comments = [] } = useQuery({
    queryKey: ["postComments", postId],
    queryFn: () => postService.getCommentsById(postId, token || ""),
    enabled: !!postId,
  });

  const addCommentMutation = useMutation({
    mutationFn: () => {
      const payload = {
        commentContent: newComment,
        fkPostId: postId,
        fkCommentEmployeeId: user?.id || 0,
        parentCommentId: replyTo ? replyTo.id : null
      };
      return postService.addComment(payload, token || "");
    },
    onSuccess: () => {
      setNewComment("");
      setReplyTo(null);
      queryClient.invalidateQueries({ queryKey: ["postComments", postId] });
      queryClient.invalidateQueries({ queryKey: ["allPosts"] });
    }
  });

  const removeCommentMutation = useMutation({
    mutationFn: ({ commentId, reason }: { commentId: number; reason: string }) => {
      if (user?.roleName === "HR" && comments.find((c: any) => c.id === commentId)?.employeeId !== user.id) {
        return postService.removeCommentByHr(commentId, reason, token || "");
      } else {
        return postService.removeCommentByOwner(commentId, reason, token || "");
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["postComments", postId] });
      queryClient.invalidateQueries({ queryKey: ["allPosts"] });
    }
  });

  const handleReplyClick = (comment: any) => {
    setReplyTo({ id: comment.id, name: `User ${comment.employeeId}` });
    commentref.current?.focus();
  }

    const handleDelete = (commentId: number) => {
    const reason = window.prompt("Please enter reason for deleting this comment:", "")?.trim();
    if (reason) {
      removeCommentMutation.mutate({ commentId, reason });
    }
  };

  return (
    <div className="mt-4 space-y-4 pt-4 border-t">
      <div className="space-y-3">
        {comments.filter((c: any) => !c.commentIsDeleted).map((comment: any) => (
          <div key={comment.id} className={`p-2 rounded-lg group ${comment.parentCommentId ? 
                                            "ml-8 bg-slate-50 border-l-2 border-blue-200" 
                                            : "bg-white border"}`}>
            <div className="flex justify-between items-start">
              <div className="text-xs">
                <span className="font-bold text-blue-600"> {comment.employeeEmail} </span>

                {comment.parentCommentId && (
                      <span className="text-[10px] text-slate-400">replied to #{comment.parentCommentEmployeeEmail}</span>
                    )}
                <p className="text-slate-700 mt-1">{comment.commentContent}</p>

                <div className="flex items-center gap-4 mt-2">
                    <LikeButton postId={postId} commentId={comment.id} />
                    <span className="text-[10px] text-slate-400">{comment.commentCreatedAt?.split("T")[0]}</span>
                </div>
              </div>
              <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <button 
                      onClick={() => handleReplyClick(comment)}
                      className="text-slate-400 hover:text-blue-600"
                    >
                      <Reply size={12} />
                </button>
                {(user?.roleName === "HR" || user?.id === comment.employeeId) && (
                    <button
                    onClick={() => handleDelete(comment.id)}
                          className="flex opacity-0 group-hover:opacity-100 text-red-400 hover:text-red-600 transition-opacity"
                  >
                    <Trash2 size={12} />
                    {removeCommentMutation.isPending && removeCommentMutation.variables?.commentId === comment.id ? <span className="text-[10px] text-red-600"> Deleting...</span> : null}
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="space-y-2">
        {replyTo && (<div className="flex items-center justify-between bg-blue-50 px-2 py-1 rounded text-[10px] text-blue-700">
            <span>Replying to {replyTo.name}</span>
            <X size={12} className="cursor-pointer" onClick={() => setReplyTo(null)} />
            </div>
        )}
         <div className="flex gap-2">
          <Input 
            ref={commentref}
            placeholder={replyTo ? "Write a reply..." : "Add a comment..."}
            className="h-8 text-xs" 
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
          />
          <Button 
            size="sm" className="h-8 px-3 text-gray-600" 
            onClick={() => addCommentMutation.mutate()}
            disabled={!newComment || addCommentMutation.isPending}
          >
            <Send size={14} />
          </Button>
        </div>
      </div>
    </div>
  );
}

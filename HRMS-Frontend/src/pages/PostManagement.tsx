import { useState, useMemo } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { postService } from "../api/postService";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Plus, X, MessageSquare, Search, Eye, Edit, Delete, Bell, Trash } from "lucide-react";
import PostForm from "../components/PostForm";
import LikeButton from "@/components/LikeButton";
import CommentSection from "@/components/CommentSection";
import Notifications from "@/components/Notifications";
import PostTags from "@/components/PostTags";

export default function PostManagement() {
  const { token, user, unreadNotifications } = useAuth();
  const [showForm, setShowForm] = useState(false);
  const [showNotification, setShowNotification] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [editPostId, setEditPostId] = useState<number>(0);
  const [showComments, setShowComments] = useState(false);
  const queryClient = useQueryClient();

  const postVisibilityOptions = {
    [user?.roleName as string]: true,
    "EVERYONE": true,
    [user?.positionName as string]: true,
    [user?.departmentName as string]: true
  };

  const { data: allPosts, isLoading } = useQuery({
    queryKey: ["allPosts"],
    queryFn: () => postService.showAllPosts(token || ""),
    enabled: !!token,
  });

  const removePost = useMutation({
    mutationFn: ({ postId, reason }: { postId: number; reason: string }) => {
      if (user?.roleName === "HR") {
        return postService.removePostByHr(postId, reason, token || "");
      } else {
        return postService.removePostByEmp(postId, reason, token || "");
      }
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["allPosts"] })
  });

  const filteredPosts = useMemo(() => {
    if (!allPosts) return [];
    return allPosts
      .filter((post: any) => !post.postIsDeleted && 
        (post.postTitle.toLowerCase().includes(searchTerm.toLowerCase()) || 
         post.postTags?.toLowerCase().includes(searchTerm.toLowerCase()) || 
         post.employeeFirstName.toLowerCase().includes(searchTerm.toLowerCase()) || 
         post.employeeEmail.toLowerCase().includes(searchTerm.toLowerCase()) || 
         post.postTagResponses?.map((tag: any) => ` ${tag.tagTypeName}`).join("")?.toLowerCase().includes(searchTerm.toLowerCase()) ||
         post.postTagResponses?.map((tag: any) => tag.tagTypeName.toLowerCase()).includes(searchTerm.toLowerCase()) || 
         post.postCreatedAt.split("T")[0].includes(searchTerm.toLowerCase()) ||
         post.postContent.toLowerCase().includes(searchTerm.toLowerCase())) && 
        (post.postVisibilityName in postVisibilityOptions))
      .sort((a: any, b: any) => new Date(b.postCreatedAt).getTime() - new Date(a.postCreatedAt).getTime());
  }, [allPosts, searchTerm]);

  const handleDelete = (postId: number) => {
    const reason = window.prompt("Please enter reason for deleting this post:", "")?.trim();
    if (reason) {
      removePost.mutate({ postId, reason });
    }
  };

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 bg-white sticky top-0 z-10">
          <div className="flex items-center gap-2 w-150">
            <SidebarTrigger />
            <h3 className="text-lg font-bold text-slate-800">Company Feed</h3>
          </div>

          <div className="relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search posts..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <Button onClick={() => setShowForm(true)} className="gap-2 text-black">
            <Plus size={18} /> New Post
          </Button>

          <div className="relative inline-block">
            <Bell 
              size={25} 
              onClick={() => setShowNotification(true)} 
              className="text-gray-600 cursor-pointer hover:text-blue-600 transition-colors"
            />
            {unreadNotifications > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center">
                {unreadNotifications}
              </span>
            )}
          </div>
        </header>

        <main className="p-6 max-w-3xl mx-auto space-y-6 w-full">
          
          {/* Notifications */}
          {showNotification && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative h-150 overflow-y-auto">
                <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
                  onClick={() => {
                  setShowNotification(false);
                }}><X /></Button>
                <Notifications />
              </div>
            </div>
          )}

          {/* Post Form Modal */}
          {showForm && (
            <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-xl max-w-lg w-full relative">
                <Button variant="ghost" className="absolute right-2 top-2" onClick={() => {
                  setShowForm(false);
                  setEditPostId(0);
                }}><X /></Button>
                <PostForm editPostId={editPostId} onSuccess={() => {
                  setShowForm(false);
                  setEditPostId(0);
                }} />
              </div>
            </div>
          )}

          <div className="space-y-6">
            {isLoading ? (
              <p className="text-center text-slate-400">Loading feed...</p>
            ) : filteredPosts.length > 0  ? (
              filteredPosts.map((post: any) => (
                ( (post.postVisibilityName in postVisibilityOptions) ? (
                  <Card key={post.id} className="hover:shadow-md transition-shadow border-slate-200">
                    <CardHeader>
                      <div className="flex justify-between items-start">
                        <div className="flex justify-between w-full">
                          <CardTitle className="text-xl font-bold text-slate-900">{post.postTitle}</CardTitle>
                          <div className="flex gap-2 items-center mt-1">
                            <Badge variant="secondary" className="text-[10px] bg-blue-50 text-blue-700">
                              <Eye size={10} className="mr-1" /> {post.postVisibilityName}
                            </Badge>
                          </div>
                        </div>
                        <div className="flex gap-2">
                          {(user?.id === post.employeeId) && (
                            <div className="ml-5 flex gap-2">
                              <Button variant="ghost" size="sm" onClick={(e) => {
                                e.stopPropagation();
                                setEditPostId(post.id);
                                setShowForm(true);
                              }}>
                              <Edit size={16} className="text-blue-600" />
                              </Button>
                            </div>
                          )}
                          {(user?.roleName === "HR" || user?.id === post.employeeId) && (  
                            <Button variant="ghost" size="sm" className="ml-2" onClick={(e) => {
                              e.stopPropagation();
                              handleDelete(post.id);
                            }}>
                            <Trash size={16} className="text-red-600" />
                            {removePost.isPending && <span className="text-[10px] text-red-600">Deleting...</span>}
                            </Button>
                          )}
                        </div>
                      </div>
                    </CardHeader>
                    
                    <CardContent className="space-y-4">
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-sm text-slate-600 whitespace-pre-wrap">{post.postContent}</p>              
                        <span className="text-[10px] text-slate-400">{post.postCreatedAt?.split("T")[0]}</span>                          
                      </div>
                      {post.postContentUrl && (
                        <div className="rounded-lg overflow-hidden border">
                          <img 
                            src={post.postContentUrl} 
                            alt="Post Content" 
                            className="w-full object-cover max-h-96"
                          />
                        </div>
                      )}
                      <div className="flex items-center justify-between pt-4 border-t">
                        <div className="flex items-center gap-6">
                          <div className="flex items-center gap-1 text-slate-500 cursor-pointer transition-colors">
                            <LikeButton postId={post.id} />
                          </div>
                          <div className="flex items-center gap-1 text-slate-500 cursor-pointer transition-colors"
                            onClick={() => setShowComments(!showComments)}>
                            <MessageSquare size={18} />
                            <span className="text-xs font-bold">Comments</span>
                          </div>
                        </div>                      
                        <PostTags postId={post.id} isOwner={user?.id === post.employeeId} />
                      </div>
                      {showComments && <CommentSection postId={post.id}/>}
                    </CardContent>
                  </Card>
                ) : null)
              ))
            ) : (
              <div className="text-center py-20 text-slate-400">No posts found.</div>
            )}
          </div>
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

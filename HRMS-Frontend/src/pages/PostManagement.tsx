import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { postService } from "../api/postService";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Plus, X, MessageSquare, Search, Eye, Edit, Bell, Trash } from "lucide-react";
import PostForm from "../components/PostForm";
import LikeButton from "@/components/LikeButton";
import CommentSection from "@/components/CommentSection";
import Notifications from "@/components/Notifications";
import PostTags from "@/components/PostTags";
import { useInView } from "react-intersection-observer";
import { useShowAllPosts } from "@/hooks/useInfinite";
import { ScrollToTop } from "@/components/ScrollToTop";
import { useAppDebounce } from "@/hooks/useAppDebounce";
import { GlobalSearch } from "@/components/GlobalSearch";
import { useToast } from "@/context/ToastContext";
import { ConformationDialog } from "@/components/ConformationDialog";
import { useIsMobile } from "@/hooks/use-mobile";
import { Spinner } from "@/components/ui/spinner";

type Post = {
  id: number;
  employeeId: number;
  employeeEmail: string;
  postTitle: string;
  postContent: string;
  postContentUrl?: string;
  postVisibilityName: string;
  postCreatedAt: string;
  recentLikerNames?: string[];
  commentCount: number;
};

export default function PostManagement() {
  const isMobile = useIsMobile();
  const toast = useToast();
  const { token, user, unreadNotifications } = useAuth();
  const [showForm, setShowForm] = useState(false);
  const [showNotification, setShowNotification] = useState(false);
  const [showFullContent, setShowFullContent] = useState(false);
  
  const getInitialSearchTerm = () => {
    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get("postId");
    if (postId) return postId;
    const employeeEmail = urlParams.get("employeeEmail");
    if (employeeEmail) return employeeEmail;
    return "";
  };
  
  const [searchTerm, setSearchTerm] = useState(getInitialSearchTerm());
  const [editPostId, setEditPostId] = useState<number>(0);
  const [showComments, setShowComments] = useState(false);
  const queryClient = useQueryClient();
  const debouncedSearch = useAppDebounce(searchTerm);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [deletingPostId, setDeletingPostId] = useState<number>(0);
  
  const {
    data: allPosts,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isError: allPostsError,
    isLoading: isAllPostsLoading,
  } = useShowAllPosts(searchTerm, token || "");
  const filteredPosts = allPosts?.pages.flatMap(page => page.content) || [];
  
  const [ref, inView] = useInView();
  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage]);
  
  const removePost = useMutation({
    mutationFn: ({ postId, reason }: { postId: number; reason: string }) => {
      if ((user?.roleName === "HR" || user?.roleName === "ADMIN" ) && filteredPosts?.find((post: Post) => post.id === postId)?.employeeId !== user.id) {
      return postService.removePostByHr(postId, reason, token || "");
      } else {
        return postService.removePostByEmp(postId, reason, token || "");
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["allPosts"] });
      toast?.success("Post deleted successfully!");
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      const data = error.response?.data;  
      const detailedError = typeof data === 'object' 
      ? JSON.stringify(data, null, 2) 
      : data || error.message;
      toast?.error("Failed to delete post: " + detailedError); }
  });

  const handleDelete = (reason: string) => {
    removePost.mutate({ postId: deletingPostId, reason });
  };
   
  useEffect(() => {
    const clickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest("div.post")) {
        setShowForm(false);
        setShowNotification(false);
      }
    };
    if (showForm || showNotification) {
      document.addEventListener("click", clickOutside);
    } else {
      document.removeEventListener("click", clickOutside);
    }
    return () => document.removeEventListener("click", clickOutside);
  }, [showForm, showNotification]);
  
  if (allPostsError) {
    toast?.error("Failed to load posts: " + allPostsError);
  }  

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset className="bg-slate-50">
        <header className="flex h-16 shrink-0 items-center justify-between border-b px-6 text-white sticky top-0 z-10">
          <div className="flex items-center gap-2 w-150">
            <SidebarTrigger />
            <h3 className="text-lg font-bold">Company Feed</h3>
            {(debouncedSearch && debouncedSearch.length > 0) ?(
              <Badge variant="outline" className="text-white">{filteredPosts.length} results</Badge>
            ) : (<Badge variant="outline" className="text-white">No filter</Badge>)
            }
          </div>

          <div className="post relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search posts..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
              }}
              autoFocus
            />
          </div>
          <div className="post">
            <Button onClick={() => setShowForm(true)} className="gap-2 mr-2 text-gray-600">
              <Plus size={18} /> {!isMobile ? "New Post" : ""}
            </Button>
          </div>
          <div className="post relative inline-block">
            <Bell 
              size={25} 
              onClick={() => setShowNotification(true)} 
              className="text-white cursor-pointer"
            />
            {unreadNotifications > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center">
                {unreadNotifications}
              </span>
            )}
          </div>
        </header>

        {( isAllPostsLoading || removePost.isPending ) && <Spinner />}

        {/* Confirmation Dialog */}
        {isDialogOpen && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="post bg-white bottom-52 rounded-xl max-w-lg w-full relative">
              <ConformationDialog
                onClose={() => setIsDialogOpen(false)} 
                onConfirm={(reason) => handleDelete(`${reason} (Deleted by : ${user?.employeeEmail} at ${new Date().toLocaleString()})`)} 
                iteam="post"
                action="Delete"
              />
            </div>
          </div>
        )}
        
        {/* Notifications */}
        {showNotification && (
          <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="post bg-white rounded-xl max-w-3xl w-full relative max-h-150 overflow-y-auto">
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
            <div className={`post bg-white rounded-xl max-w-lg w-full relative max-h-150 overflow-y-auto`}>
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

        <main className="p-6 space-y-6 w-full">          
          <div className="post space-y-6">
            {filteredPosts.length > 0  ? (
              filteredPosts.map((post: Post) => (
                  <Card key={post.id} className="hover:shadow-md transition-shadow border-slate-200">
                    <CardHeader>
                      <div className="flex justify-between items-start">
                        <div className="flex justify-between w-full">
                          <CardTitle className="text-xl font-bold text-slate-900">
                            <p className="text-lg text-gray-700">{post.postTitle} </p>
                            <p className="text-[10px] text-gray-500">By {post?.employeeEmail || "ROIMA"}</p>
                          </CardTitle>
                          <div className="flex gap-2 items-center mt-1">
                            <Badge variant="secondary" className="text-[10px] bg-gray-50 text-gray-700">
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
                              <Edit size={16} className="text-gray-600" />
                              </Button>
                            </div>
                          )}
                          {(user?.roleName === "HR" || user?.roleName === "ADMIN" || user?.id === post.employeeId) 
                            && ( 
                            <>
                              <Button variant="ghost" size="sm" className="ml-2" onClick={(e) => {
                                e.stopPropagation();
                                setIsDialogOpen(true);
                                setDeletingPostId(post.id);
                              }}>
                              <Trash size={16} className="text-gray-600" />
                              {removePost.isPending && removePost.variables?.postId === post?.id ? <span className="text-[10px] text-red-600">Deleting...</span> : null}
                              </Button>
                            </>
                          )}
                        </div>
                      </div>
                    </CardHeader>
                    
                    <CardContent className="space-y-4">
                    <div>
                    {post.postContent.length < 100 ? (
                      <p className="text-sm text-slate-600 whitespace-pre-wrap">{post.postContent}</p>
                    ) : (
                      <div>
                        {showFullContent ? (
                          <p className="text-sm text-slate-600">{post.postContent}</p>
                        ) : (
                          <p className="text-sm text-slate-600">{post.postContent.substring(0, 200)}...</p>
                        )}
                        <p className="text-[12px] text-gray-600 underline cursor-pointer" onClick={() => {
                          setShowFullContent(!showFullContent);
                        }}>{showFullContent ? "Show Less" : "Read More"}</p>
                      </div>
                    )}
                    </div>
                      {post.postContentUrl && (
                        <div className="relative">
                        <div className="rounded-lg overflow-hidden border">
                          <img 
                            src={post.postContentUrl} 
                            alt="Post Content" 
                            className="w-full object-cover max-h-96"
                          />
                        </div>
                        <span className="text-[10px] text-slate-400 absolute right-0 m-2">{post.postCreatedAt?.split("T")[0]}</span>   
                        </div>
                      )}
                      <hr className="border-slate-200 mt-8" />
                      <div className="flex items-center justify-between pt-4">
                        <div className="flex items-center gap-6">
                          <div className="flex items-center gap-1 text-slate-500 cursor-pointer transition-colors">
                            <LikeButton postId={post.id} />
                            {post.recentLikerNames && post.recentLikerNames.length > 0 && (
                              <p className="text-[10px] text-slate-500">
                                Liked by <span className="font-bold text-slate-700">{post.recentLikerNames.join(", ")}</span>
                                {post.recentLikerNames.length > 2 && ` and ${post.recentLikerNames.length - 2} others`}
                              </p>
                            )}
                          </div>
                           
                          <div className="flex items-center gap-1 text-slate-500 cursor-pointer transition-colors"
                            onClick={() => setShowComments(!showComments)}>
                            <MessageSquare size={18} />
                            <span className="text-xs font-bold">Comments</span>
                            <span className="text-xs font-bold">{post.commentCount > 0 ? `${post.commentCount}` : "0"}
                            </span>
                          </div>
                        </div>                      
                        <PostTags postId={post.id} isOwner={user?.id === post.employeeId} />
                      </div>
                      {showComments && <CommentSection postId={post.id}/>}
                    </CardContent>
                  </Card>
              )
            )
            ) : (
              <div className="text-center py-20 text-slate-400">No posts found.</div>
            )}

          </div>
          <div ref={ref} className="h-10 flex justify-center items-center">
            { isFetchingNextPage ? <p className="text-xs">Loading more...</p> : null}
          </div>
          
          <ScrollToTop />
          <GlobalSearch />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}

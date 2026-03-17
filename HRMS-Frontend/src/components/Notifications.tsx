
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { CheckCheck, Search } from "lucide-react";
import { Input } from "./ui/input";
import { useInView } from "react-intersection-observer";
import { useGetUserNotifications } from "@/hooks/useInfinite";
import { ScrollToTop } from "./ScrollToTop";

export default function Notifications() {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState("");

  const {
    data: notificationsData,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isError: notificationsError,
  } = useGetUserNotifications(searchTerm, token || "");
  const notifications = notificationsData?.pages.flatMap(page => page.content) || [];

  const { ref, inView } = useInView();
  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage]);

  if (notificationsError) {
    alert("Failed to load notifications: " + notificationsError);
  }

  const markReadMutation = useMutation({
    mutationFn: (id: number) => apiService.markNotificationRead(id, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
    onError: (error: any) => {
      alert("Failed to mark notification as read: " + (error.response?.data || error.message)); }
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => apiService.markAllNotificationsRead(user?.id || 0, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
    onError: (error: any) => {
      alert("Failed to mark all notifications as read: " + (error.response?.data || error.message)); }
  });

  const handleMarkAllRead = () => {
    window.confirm("Are you sure you want to mark all notifications as read?") &&
    markAllReadMutation.mutate();
  };

return (
    <main className="p-4 w-full">
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="font-bold text-lg">Notifications</h3>
            {searchTerm && searchTerm.length > 0 ?(
              <Badge variant="outline">{notifications?.length} results</Badge>
            ) : (<Badge variant="outline" className="ml-4">No filter</Badge>)
          }
          <div className="relative max-w-sm w-full mx-4">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search posts..." 
              className="pl-9"
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
              }}
            />
          </div>
          <Button 
            className="mr-16"
            variant="outline" 
            size="sm"
            onClick={handleMarkAllRead}
          >
            Mark all as read
          </Button>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {notifications?.length > 0 ? (
            notifications?.map((notif: any) => (
              <div className="relative flex-1">
                <p className="text-sm text-gray-500">
                  <div dangerouslySetInnerHTML={{ __html: notif.message }} />
                </p>
                <Button 
                  variant="link" 
                  title="Mark as read"
                  className="text-xs text-blue-600 absolute right-2 top-2"
                  onClick={() => markReadMutation.mutate(notif.id)}
                >
                <CheckCheck />
                </Button>
              </div>
            )
          )) : (
            <div className="text-center py-10 text-gray-400 italic">No notifications.</div>
          )}
        </div>
        <div ref={ref} className="h-10 flex justify-center items-center">
          {isFetchingNextPage ? <p className="text-xs">Loading more...</p> : null}
        </div>        
      </div>
    </main>
  );
}

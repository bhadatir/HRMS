
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAuth } from "../context/AuthContext";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { 
  Bell, 
  CheckCircle2, 
  Info, 
  AlertCircle, 
  Clock, 
  CheckCheck,
  Trash2 
} from "lucide-react";

export default function Notifications() {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();

  const { data: notifications, isLoading } = useQuery({
    queryKey: ["notifications"],
    queryFn: () => apiService.getUserNotifications(user?.id, token || ""),
    enabled: !!token,
  });

  const markReadMutation = useMutation({
    mutationFn: (id: number) => apiService.markNotificationRead(id, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });

  if (isLoading) return <div className="p-10">Loading Notifications...</div>;

  return (
        <main className="p-4 md:p-8 w-full max-w-4xl mx-auto flex-1">
          <div className="space-y-4">
            {notifications?.length > 0 ? (
              notifications.map((notif: any) => (
                <Card 
                  key={notif.id} 
                  className="shadow-none"
                >
                  <CardContent className="p-4 flex items-start gap-4">                                        
                    <div className="flex-1 space-y-1">
                      <div className="flex justify-between items-start">
                        <h4 className={`text-sm font-bold ${notif.read ? "text-gray-500" : "text-gray-900"}`}>
                          {notif.title}
                        </h4>
                        <span className="text-[10px] text-gray-400 flex items-center gap-1">
                          <Clock size={10} /> {notif.createdAt}
                        </span>
                      </div>
                      <p className="text-sm text-gray-500 leading-relaxed">
                        {notif.message}
                      </p>
                      
                      {!notif.read && (
                        <div className="pt-2 flex gap-2">
                          <Button 
                            size="sm"  
                            className="h-auto p-0 text-xs text-gray-600"
                            onClick={() => markReadMutation.mutate(notif.id)}
                          >
                            Mark as read
                          </Button>
                          <span className="text-slate-300">|</span>
                          <Button size="sm" className="h-auto p-0 text-xs text-gray-400 hover:text-red-500">
                            Delete
                          </Button>
                        </div>
                      )}
                    </div>

                    {!notif.read && (
                      <div className="w-2 h-2 rounded-full bg-blue-600 mt-2 shrink-0" />
                    )}
                  </CardContent>
                </Card>
              ))
            ) : (
              <div className="flex flex-col items-center justify-center py-20 text-gray-400 space-y-4">
                <div className="bg-slate-100 p-6 rounded-full">
                  <Bell className="w-12 h-12 opacity-20" />
                </div>
                <p className="italic">You're all caught up! No new notifications.</p>
              </div>
            )}
          </div>
        </main>
  );
}

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";


export default function Notifications() {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();

  const { data: notifications, isLoading } = useQuery({
    queryKey: ["notifications"],
    queryFn: () => apiService.getUserNotifications(user?.id, token || ""),
    enabled: !!token && !!user?.id,
  });

  const markReadMutation = useMutation({
    mutationFn: (id: number) => apiService.markNotificationRead(id, token || ""),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });

  if(isLoading) return <div className="p-10">Loading Notifications...</div>;

return (
    <main className="p-4 w-full">
      <div className="space-y-4">
        <h3 className="font-bold text-lg">Notifications</h3>
        {notifications?.length > 0 && notifications.filter((n: any) => !n.read).length > 0 ? (
          notifications.map((notif: any) => ( !notif.read && (
            <Card key={notif.id} className={`shadow-none border ${!notif.read ? 'bg-blue-50/30' : ''}`}>
              <CardContent className="p-4 flex items-start gap-4">
                <div className="flex-1 space-y-1">
                  <div className="flex justify-between">
                    <h4 className={`text-sm font-bold ${notif.read ? "text-gray-500" : "text-gray-900"}`}>
                      {notif.title}
                    </h4>
                  </div>
                  <p className="text-sm text-gray-500">{notif.message}</p>
                  {!notif.read && (
                    <Button 
                      variant="link" 
                      className="text-xs text-blue-600"
                      onClick={() => markReadMutation.mutate(notif.id)}
                    >
                      Mark as read
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>)
          ))
        ) : (
          <div className="text-center py-10 text-gray-400 italic">No notifications.</div>
        )}
      </div>
    </main>
  );
}

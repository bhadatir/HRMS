import { createContext, useContext, useState, useEffect, type ReactNode, use } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { io } from "socket.io-client";
import SockJs from "sockjs-client";
import Stomp from "stompjs";

type AuthContextType = {
  token: string | null;
  user: any;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, email: string) => void;
  logout: () => void;
  unreadNotifications: number;
  setUnreadNotifications: (count: number) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export default function AuthProvider({ children }: { children: ReactNode }) {

  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem("email"));
  const [unreadNotifications, setUnreadNotifications] = useState<number>(0);

  const queryClient = useQueryClient();

  const { data: userData, isLoading, isError } = useQuery({
    queryKey: ["user", email],
    queryFn: () => apiService.getUserByEmail(email!, token!),
    enabled: !!token && !!email,
  });

  const { data: notifications } = useQuery({
    queryKey: ["notifications"],
    queryFn: () => apiService.getUserNotifications(userData?.id, token || ""),
    enabled: !!token && !!userData,
  });
  

  useEffect(() => {
    if(notifications) {
      const unreadCount = notifications.filter((n: any) => !n.read).length;
      setUnreadNotifications(unreadCount);
    }
  },[notifications]);

  useEffect(() => {
    if(!userData?.id || !token) return;

    const socket = new SockJs("http://localhost:8080/ws-notifications");
    const stompClient = Stomp.over(socket);

    stompClient.connect({Authorization: `Bearer ${token}`}, () => {
      stompClient.subscribe(`/topic/user/${userData.id}/notifications`, () => {
        queryClient.invalidateQueries({ queryKey: ["notifications"] });
      });
    });

    return () => {
      if (stompClient.connected) 
        stompClient.disconnect(() => {});
    };
  }, [userData?.id, token, queryClient]);

  useEffect(() => {
    if (isError) {
      logout(); 
    }
  }, [isError]);

  const login = (newToken: string, newEmail: string) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("email", newEmail);
    setToken(newToken);
    setEmail(newEmail);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    setToken(null);
    setEmail(null);
    setUnreadNotifications(0);
    queryClient.clear();
  };

  return (
    <AuthContext.Provider 
      value={{ 
        token, 
        user: userData || null, 
        isAuthenticated: !!token, 
        isLoading,
        login, 
        logout,
        unreadNotifications,
        setUnreadNotifications
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};


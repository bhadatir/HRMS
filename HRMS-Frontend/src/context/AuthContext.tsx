import { createContext, useContext, useState, useEffect, type ReactNode, use } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { io } from "socket.io-client";
import SockJs from "sockjs-client";
import Stomp from "stompjs";

type AuthContextType = {
  token: string | null;
  isFirstLogin: string;
  user: any;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, email: string, isFirstLogin: string) => void;
  logout: () => void;
  unreadNotifications: number;
  setUnreadNotifications: (count: number) => void;
  setIsFirstLogin: (isFirst: string) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export default function AuthProvider({ children }: { children: ReactNode }) {

  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem("email"));
  const [isFirstLogin, setIsFirstLogin] = useState<string>(() => {
    const isFirstLogin = localStorage.getItem("isFirstLogin");
    return isFirstLogin==="yes" ? "yes" : "no";
  });

  const [unreadNotifications, setUnreadNotifications] = useState<number>(0);

  const queryClient = useQueryClient();

  const { data: userData, isLoading, isError: userError } = useQuery({
    queryKey: ["user", email],
    queryFn: () => apiService.getUserByEmail(email!, token!),
    enabled: !!token && !!email,
  });

  const { data: notifications, isError: notificationsError } = useQuery({
    queryKey: ["notifications"],
    queryFn: () => apiService.getUserNotifications(userData?.id, "", 0, 1000, token || ""),
    enabled: !!token && !!userData,
  });

  const logoutMutation = useMutation({
    mutationFn: () => apiService.logout(token || ""),
    onSuccess: () => {
      localStorage.removeItem("token");
      localStorage.removeItem("email");
      localStorage.removeItem("isFirstLogin");
      setToken(null);
      setEmail(null);
      setUnreadNotifications(0);
      queryClient.clear();
    },
    onError: (error: any) => {
      alert("Failed to log out: " + (error.response?.data || error.message));
    }
  });
  
  useEffect(() => {
    if(notifications) {
      const unreadCount = notifications.totalElements;
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
    if (userError || notificationsError) {
      alert("Session expired or failed to load user data. Please log in again.");
      logout(); 
    }
  }, [userError, notificationsError]);

  const login = (newToken: string, newEmail: string, isFirstLogin: string) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("email", newEmail);
    localStorage.setItem("isFirstLogin", isFirstLogin);
    setToken(newToken);
    setEmail(newEmail);
    setIsFirstLogin(isFirstLogin);
  };

  const logout = () => {
    logoutMutation.mutate();
  };

  return (
    <AuthContext.Provider 
      value={{ 
        token, 
        setIsFirstLogin,
        isFirstLogin,
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


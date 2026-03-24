import { createContext, useContext, useState, useEffect, type ReactNode, useCallback } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import SockJs from "sockjs-client";
import Stomp from "stompjs";
import { useToast } from "./ToastContext";

type User = {
  id: number;
  employeeFirstName: string;
  employeeLastName: string;
  employeeEmail: string;
  employeeDob: Date;
  employeeGender: string;
  employeeProfileUrl: string;
  employeeHireDate: Date;
  employeeSalary: number;
  employeeIsActive: boolean;
  employeeCreatedAt: Date;
  lastLoginAt: Date;
  departmentId: number;
  departmentName: string;
  positionId: number;
  positionName: string;
  roleId: number;
  roleName: string;
  managerEmployeeId: number;
  managerEmployeeEmail: string;
};

type AuthContextType = {
  token: string | null;
  isFirstLogin: string;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (isFirstLogin: string) => void;
  logout: () => void;
  unreadNotifications: number;
  setUnreadNotifications: (count: number) => void;
  setIsFirstLogin: (isFirst: string) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export default function AuthProvider({ children }: { children: ReactNode }) {
  const toast = useToast();
  const token = localStorage.getItem("token");
  const [unreadNotifications, setUnreadNotifications] = useState<number>(0);
  const queryClient = useQueryClient();

  const { data: userData, isLoading, isError: userError } = useQuery({
    queryKey: ["user"],
    queryFn: () => apiService.getUser(token!),
    enabled: !!token,
  });
  const [isFirstLogin, setIsFirstLogin] = useState<string>(userData?.isFirstLogin || "no");

  const { data: notifications, isError: notificationsError } = useQuery({
    queryKey: ["notifications"],
    queryFn: () => apiService.getUserNotifications(userData?.id, "", 0, 1000, token || ""),
    enabled: !!token && !!userData,
  });

  const logoutMutation = useMutation({
    mutationFn: () => apiService.logout(token || ""),
    onSuccess: () => {
      localStorage.clear();
      localStorage.removeItem("token");
      queryClient.clear();
      setUnreadNotifications(0);
      setIsFirstLogin("no");
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      toast?.error("Failed to log out: " + (error.response?.data || error.message));
    }
  });
  
  useEffect(() => {
    if(notifications) {
      const unreadCount = notifications.totalElements;
      // eslint-disable-next-line react-hooks/set-state-in-effect
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

  const login = (isFirstLogin: string) => {
    setIsFirstLogin(isFirstLogin);
  };

  const logout = useCallback(() => {
    logoutMutation.mutate();
  }, [logoutMutation]);

  useEffect(() => {
    if (userError || notificationsError) {
      logout(); 
      localStorage.removeItem("token");
      toast?.error("Session expired or failed to load user data. Please log in again.");
    }
  }, [userError, notificationsError, logout]);

  return (
    <AuthContext.Provider 
      value={{ 
        token, 
        setIsFirstLogin,
        isFirstLogin,
        user: userData || null, 
        isAuthenticated: !!userData, 
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

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};


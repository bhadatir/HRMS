
import axios from "axios";

const BASE_URL = "http://localhost:8080/api";

const api = axios.create({
  baseURL: BASE_URL,
});

const authHeader = (token: string) => ({
  headers: { 
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json" 
  },
});

export const apiService = {

  login: async (data: any) => {
    const res = await api.post("/auth/login", data);
    return res.data;
  },

  register: async (data: any) => {
    const res = await api.post("/auth/register", data);
    return res.data;
  },

  forgotPassword: async (email: string) => {
    const res = await api.post(`/auth/forgot-password?email=${email}`);
    return res.data;
  },

  resetPassword: async ({ token, newPassword }: { token: string; newPassword: string }) => {
    const res = await api.post(`/auth/reset-password?token=${token}&newPassword=${newPassword}`);
    return res.data;
  },

  getUserByEmail: async (email: string, token: string) => {
    const res = await api.get(`/user/email?email=${email}`, authHeader(token));
    return res.data;
  },

  searchEmployees: async (query: string, page: number, size: number, token: string) => {
    const res = await api.get(`/user/search?query=${query}&page=${page}&size=${size}`, authHeader(token));
    return res.data;
  },

  searchAvailableEmployeeForTravel: async (query: string, page: number, size: number, startDate: string, endDate: string, token: string) => {
    const res = await api.get(`/user/travel/search?query=${query}&page=${page}&size=${size}&startDate=${startDate}&endDate=${endDate}`, authHeader(token));
    return res.data;
  },

  fetchOrgChart: async (employeeId: number, token: string) => {
    const res = await api.get(`/org-chart/${employeeId}`, authHeader(token));
    return res.data;
  },

  getUserNotifications: async (empId: number, token: string) => {
    const res = await api.get(`/notification/${empId}`, authHeader(token));
    return res.data;
  },

  markNotificationRead: async (notifId: number, token: string) => {
    const res = await api.post(`/notification/markAsSeen/notifId/${notifId}`, {}, authHeader(token));
    return res.data;
  }

};




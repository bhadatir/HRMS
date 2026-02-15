
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

  searchEmployees: async (query: string, token: string) => {
    const res = await api.get(`/user/search?query=${query}`, authHeader(token));
    return res.data;
  },

  fetchOrgChart: async (employeeId: number, token: string) => {
    const res = await api.get(`/org-chart/${employeeId}`, authHeader(token));
    return res.data;
  },

  getAllTravelPlans: async (token: string) => {
    const res = await api.get("/travel/allTravelPlans", authHeader(token));
    return res.data;
  },

  createTravelPlan: async (data: any, token: string) => {
    const res = await api.post("/hr/travelPlan", data, authHeader(token));
    return res.data;
  },

  addExpenseWithProof: async (formData: FormData, token: string) => {
    const res = await api.post("/travel/expenseWithProof", formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  },

  findEmployeeTravelPlans: async (empId: number, travelId: number, token: string) => {
    const res = await api.get(`/travel/employeeTravelPlan/${empId}/${travelId}`, authHeader(token));
    return res.data;
  }
};




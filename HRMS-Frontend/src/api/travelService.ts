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

export const travelService = {
    
  getAllTravelPlans: async (token: string) => {
    const res = await api.get("/travel/allTravelPlans", authHeader(token));
    return res.data;
  },

  getTravelPlanById: async (travelPlanId: number, token: string) => {
    const res = await api.get(`/travel/travelPlanId/${travelPlanId}`, authHeader(token));
    return res.data;
  },

  createTravelPlan: async (data: any, token: string) => {
    const res = await api.post("/hr/travelPlan", data, authHeader(token));
    return res.data;
  },

  updateTravelPlan: async (travelPlanId: number, data: any, token: string) => {
    const res = await api.put(`/hr/travelPlan/${travelPlanId}`, data, authHeader(token));
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

  findExpenseById: async (employeeId: number, travelPlanId: number, token: string) => {
    const res = await api.get(`/travel/expense/${employeeId}/${travelPlanId}`, authHeader(token));
    return res.data;
  },

  findExpenseProofById: async (expenseId: number, token: string) => {
    const res = await api.get(`/travel/expenseProof/${expenseId}`, authHeader(token));
    return res.data;
  },

  updateExpenseStatus: async (expenseId: number, statusId: number, token:String) => {
    const res = await api.patch(`/hr/expense/${expenseId}/${statusId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  },

  findEmployeeTravelPlans: async (empId: number, travelId: number, token: string) => {
    const res = await api.get(`/travel/employeeTravelPlan/${empId}/${travelId}`, authHeader(token));
    return res.data;
  },

  searchTravelPlan : async (query: string, token: string) => {
    const res = await api.get(`/travel/search?query=${query}`, authHeader(token));
    return res.data;
  },

  addTravelPlanDocByHr: async (employeeId: number, travelPlanId: number, docTypeId: number, formData: FormData, token: string) => {
    const res = await api.post(`/hr/travelPlanDoc/${employeeId}/${travelPlanId}/${docTypeId}`, formData, {
      headers: {
        Authorization: `Bearer ${token}`
      },
    });
    return res.data;
  },

  addTravelPlanDocByEmployee: async (employeeId: number, employeeTravelPlanId: number, docTypeId: number, formData: FormData, token: string) => {
    const res = await api.post(`/employee/travelPlanDoc/${employeeId}/${employeeTravelPlanId}/${docTypeId}`, formData, {
      headers: {
        Authorization: `Bearer ${token}`
      },
    });
    return res.data;
  },

  findTravelDocByEmpId: async (empId: number, travelPlanId: number, token: string) => {
    const res = await api.get(`/hr/travelDoc/${empId}/${travelPlanId}`, authHeader(token));
    return res.data;
  }

};
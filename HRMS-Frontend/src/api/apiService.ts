
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

type AddUserFormInputs = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  dob: string;
  gender: "male" | "female" | "other";
  hireDate: string;
  salary: number;
  departmentId: number;
  positionId: number;
  roleId: number;
}

type LoginData = {
  email: string;
  password: string;
}

export const apiService = {

  updateProfileImage: async (empId: number, formData: FormData, token: string) => {
    const res = await api.patch(`/user/profileImage/${empId}`, formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  },

  login: async (data: LoginData) => {
    const res = await api.post("/auth/login", data);
    return res.data;
  },

  logout : async (token: string) => {
    const res = await api.post("/user/logout", null, authHeader(token));
    return res.data;
  },

  register: async (data: AddUserFormInputs, token: string) => {
    const res = await api.post("/admin/register", data, authHeader(token));
    return res.data;
  },

  updateUserByEmail: async (userEmail: string, data: AddUserFormInputs, token: string) => {
    const res = await api.put(`/admin/user/${userEmail}`, data, authHeader(token));
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

  updatePassword: async (empId: number, newPassword: string, token: string) => {
    const res = await api.patch(`/user/update-password?empId=${empId}&newPassword=${newPassword}`, null, authHeader(token));
    return res.data;
  },

  inActiveUserByID: async (userId: number, reason: string, token: string) => {
    const res = await api.patch(`/admin/inActiveUser/${userId}?reason=${reason}`, null, authHeader(token));
    return res.data;
  },

  getUserByEmail: async (email: string, token: string) => {
    const res = await api.get(`/user/email?email=${email}`, authHeader(token));
    return res.data;
  },

  getAllRoles: async (token: string) => {
    const res = await api.get("/admin/allRoles", authHeader(token));
    return res.data;
  },

  getAllDepartments: async (token: string) => {
    const res = await api.get("/admin/allDepartments", authHeader(token));
    return res.data;
  },

  getAllPositions: async (token: string) => {
    const res = await api.get("/admin/allPositions", authHeader(token));
    return res.data;
  },

  searchEmployees: async (query: string, employeeType: number, page: number, size: number, token: string) => {
    const res = await api.get(`/user/search?query=${query}&employeeType=${employeeType}&page=${page}&size=${size}`, authHeader(token));
    return res.data;
  },

  searchAvailableEmployeeForTravel: async (query: string, page: number, size: number, startDate: string, endDate: string, token: string) => {
    const res = await api.get(`/user/travel/search?query=${query}&page=${page}&size=${size}&startDate=${startDate}&endDate=${endDate}`, authHeader(token));
    return res.data;
  },

  searchParticipants: async (query: string, page: number, size: number, startDateTime: string, gameTypeId: number, token: string) => {
    const res = await api.get(`/user/participants/search?query=${query}&page=${page}&size=${size}&startDate=${startDateTime}&gameTypeId=${gameTypeId}`, authHeader(token));
    return res.data;
  },

  fetchOrgChart: async (employeeId: number, token: string) => {
    const res = await api.get(`/org-chart/${employeeId}`, authHeader(token));
    return res.data;
  },

  getUserNotifications: async (empId: number, searchTerm: string, page: number, size: number, token: string) => {
    const res = await api.get(`/notification/${empId}?searchTerm=${searchTerm}&page=${page}&size=${size}`, authHeader(token));
    return res.data;
  },

  markNotificationRead: async (notifId: number, token: string) => {
    const res = await api.post(`/notification/markAsSeen/notifId/${notifId}`, {}, authHeader(token));
    return res.data;
  },

  markAllNotificationsRead: async (empId: number, token: string) => {
    const res = await api.post(`/notification/markAsSeen/empId/${empId}`, {}, authHeader(token));
    return res.data;
  },

  globalSearch: async (searchTerm: string, page: number, size: number, token: string) => {
    const res = await api.get(`/user/global-search?searchTerm=${searchTerm}&page=${page}&size=${size}`, authHeader(token));
    return res.data;
  },

  getActiveTimeByUserEmail: async(email: string, token: string) => {
    const res = await api.get(`/user/activeTime?email=${email}`, authHeader(token));
    return res.data;
  }
};




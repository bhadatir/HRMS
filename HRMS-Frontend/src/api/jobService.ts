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

export const jobService = {

 

  createJobWithJD: async (formData: FormData, token: string) => {
    const res = await api.post("/hr/job", formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  },

  shareJob: async (data: any, token: string) => {
    const res = await api.post("/job/shareJob", data, authHeader(token));
    return res.data;
  },

  referFriendRequest: async (formData: FormData, token: string) => {
    const res = await api.post("/job/referFriend", formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  }
};
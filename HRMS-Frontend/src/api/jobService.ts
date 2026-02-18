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

  getAllJobs: async (token: string) => {
    const res = await api.get("/job/", authHeader(token));
    return res.data;
  },

  getJobById: async (jobId: number, token: string) => {
    const res = await api.get(`/job/${jobId}`, authHeader(token));
    return res.data;
  },

  getReferDataByJobId: async (jobId: number, token: string) => {
    const res = await api.get(`/job/referData/${jobId}`, authHeader(token));
    return res.data;
  },

  addCvReviewer: async (jobId: number, employeeId: number, token: string) => {
    const res = await api.post(`/hr/cvReviewer/${jobId}/${employeeId}`, null, authHeader(token));
    return res.data;
  },

  updateReferCvStatus: async (referId: number, statusId: number, token: string) => {
    const res = await api.patch(`/hr/referCV/${referId}/${statusId}`, null, authHeader(token));
    return res.data;
  },

  updateJobWithJD: async (jobId: number, formData: FormData, token: string) => {
    const res = await api.put(`/hr/job/${jobId}`, formData, {
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

  referFriend: async (formData: FormData, token: string) => {
    const res = await api.post("/job/referFriend", formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  }
};
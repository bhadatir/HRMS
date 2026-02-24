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

export const gameService = {

    getAllGames: async (token: string) => {
    const res = await api.get("/gameType/allGames", authHeader(token));
    return res.data;
    },

    getAllGameBookingStatus: async (token: string) => {
        const res = await api.get("/game/allGameBookingStatus", authHeader(token));
        return res.data;
    },

    getAllWaitingList: async (token: string) => {
        const res = await api.get("/game/waitList", authHeader(token));
        return res.data;
    },

    addGameType: async (payload: any, token: string) => {
        const res = await api.post("/hr/gameType", payload, authHeader(token));
        return res.data;
    },

    updateGameType: async (gameTypeId: number, payload: any, token: string) => {
        const res = await api.put(`/hr/gameType/${gameTypeId}`, payload, authHeader(token));
        return res.data;
    },

    getGameById: async (gameTypeId: number, token: string) => {
    const res = await api.get(`/gameType/${gameTypeId}`, authHeader(token));
    return res.data;
    },
 
    addBooking: async (payload: any, token: string) => {
        const res = await api.post("/game/", payload, authHeader(token));
        return res.data;
    },
    
    showAllBookings: async (token: string) => {
        const res = await api.get("/game/", authHeader(token));
        return res.data;
    },

    findGameById: async (gameBookingId: number, token: string) => {
        const res = await api.get(`/gameType/${gameBookingId}`, authHeader(token));
        return res.data;
    },
    
    updateBookingStatus: async (bookingId: number, statusId: number, token: string) => {
        const res = await api.patch(`/game/status?gameBookingId=${bookingId}&statusId=${statusId}`, null, authHeader(token));
        return res.data;
    },

    updateBooking: async (bookingId: number, payload: any, token: string) => {
        const res = await api.put(`/game/booking/${bookingId}`, payload, authHeader(token));
        return res.data;
    },

    addEmployeeGameInterest: async (employeeId: number, gameTypeId: number, token: string) => {
        const res = await api.post(`/game/interest/${employeeId}/${gameTypeId}`, null, authHeader(token));
        return res.data;
    },

    getEmployeeGameInterests: async (employeeId: number, token: string) => {
        const res = await api.get(`/game/interest/${employeeId}`, authHeader(token));
        return res.data;
    },

    updateEmployeeGameInterests: async (gameInterestId: number, token: string) => {
        const res = await api.delete(`/game/interest/${gameInterestId}`, authHeader(token));
        return res.data;
    }
    
};
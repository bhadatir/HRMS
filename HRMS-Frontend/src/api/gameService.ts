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

type GameBookingFormPayload = {
    empId: number | undefined;
    gameTypeId: number;
    requestedSlotStartTime: string;
    bookingParticipantsEmpId: number[];
}

type GameTypeInputs = {
    gameName: string;
    operatingStart: string;
    operatingEnd: string;
    gameSlotDuration: number;
    gameMaxPlayerPerSlot: number;
};


export const gameService = {

    getAllGames: async (token: string) => {
    const res = await api.get("/gameType/allGames", authHeader(token));
    return res.data;
    },

    getAllGameBookingStatus: async (token: string) => {
        const res = await api.get("/game/allGameBookingStatus", authHeader(token));
        return res.data;
    },

    getWaitingListById: async (waitingListId: number, token: string) => {
        const res = await api.get(`/game/waitList/${waitingListId}`, authHeader(token));
        return res.data;
    },

    getWaitingListSeqById: async (waitingListId: number, token: string) => {
        const res = await api.get(`/game/waitListSeq/${waitingListId}`, authHeader(token));
        return res.data;
    },

    getAllWaitingList: async (token: string) => {
        const res = await api.get("/game/waitList", authHeader(token));
        return res.data;
    },

    deleteWaitingList: async (waitingListId: number, token: string) => {
        const res = await api.delete(`/game/waitList/${waitingListId}`, authHeader(token));
        return res.data;
    },

    addGameType: async (payload: GameTypeInputs, token: string) => {
        const res = await api.post("/hr/gameType", payload, authHeader(token));
        return res.data;
    },

    updateGameType: async (gameTypeId: number, payload: GameTypeInputs, token: string) => {
        const res = await api.put(`/hr/gameType/${gameTypeId}`, payload, authHeader(token));
        return res.data;
    },

    getGameById: async (gameTypeId: number, token: string) => {
    const res = await api.get(`/gameType/${gameTypeId}`, authHeader(token));
    return res.data;
    },
 
    addBooking: async (payload: GameBookingFormPayload, token: string) => {
        const res = await api.post("/game/", payload, authHeader(token));
        return res.data;
    },
    
    showAllBookings: async (searchTerm: string, gameType: number, gameBookingStatusId: number, page: number, size: number, token: string) => {
        const res = await api.get(`/game/?searchTerm=${searchTerm}&gameType=${gameType}&gameBookingStatusId=${gameBookingStatusId}&page=${page}&size=${size}`, authHeader(token));
        return res.data;
    },





    findGameBookingByUserId: async (userId: number, searchTerm: string, gameType: number, gameBookingStatusId: number, page: number, size: number, token: string) => {
        const res = await api.get(`/game/empId/${userId}?searchTerm=${searchTerm}&gameType=${gameType}&gameBookingStatusId=${gameBookingStatusId}&page=${page}&size=${size}`, authHeader(token));
        return res.data;
    },

    findGameBookingWaitingListByEmpId: async (empId: number, gameType: number, token: string) => {
        const res = await api.get(`/game/waitList/emp/${empId}?gameType=${gameType}`, authHeader(token));
        return res.data;
    },

    upcommingBookings: async (token: string) => {
        const res = await api.get("/game/upcommingBooking", authHeader(token));
        return res.data;
    },

    getAvalaibleSlots: async (gameTypeId: number, empId: number, date: string, token: string) => {
        const res = await api.get(`/game/getSlot?gameTypeId=${gameTypeId}&empId=${empId}&date1=${date}`, authHeader(token));
        return res.data;
    },









    findGameById: async (gameBookingId: number, token: string) => {
        const res = await api.get(`/gameType/${gameBookingId}`, authHeader(token));
        return res.data;
    },
    
    updateBookingStatus: async (bookingId: number, statusId: number, reason: string, token: string) => {
        const res = await api.patch(`/game/status?gameBookingId=${bookingId}&statusId=${statusId}&reason=${reason}`, null, authHeader(token));
        return res.data;
    },

    updateBooking: async (bookingId: number, payload: GameBookingFormPayload, token: string) => {
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
        const res = await api.patch(`/game/interest/${gameInterestId}`, null, authHeader(token));
        return res.data;
    }
    
};
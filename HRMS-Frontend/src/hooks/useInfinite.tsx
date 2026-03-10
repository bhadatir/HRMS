import { useInfiniteQuery } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAppDebounce } from "./useAppDebounce";
import { travelService } from "@/api/travelService";
import { useAuth } from "@/context/AuthContext";
import { jobService } from "@/api/jobService";
import { postService } from "@/api/postService";
import { gameService } from "@/api/gameService";

export function useEmployeeSearch(searchTerm: string, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
    queryKey: ["searchEmployees", debouncedSearch],
    queryFn: ({ pageParam = 0 }) =>
      apiService.searchEmployees(debouncedSearch, pageParam, 5, token),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useGlobalSearch(searchTerm: string, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
    queryKey: ["globalSearch", debouncedSearch],
    queryFn: ({ pageParam = 0 }) =>
      apiService.globalSearch(debouncedSearch, pageParam, 10, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useFindTravelPlanByEmployeeId(searchTerm: string, travelPlanType: number, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);
  const { user } = useAuth();

  const query = useInfiniteQuery({
    queryKey: ["travelPlanByEmpId", debouncedSearch, travelPlanType],
    queryFn: ({ pageParam = 0 }) => 
      travelService.findTravelPlanByEmployeeId(user?.id, debouncedSearch, travelPlanType, pageParam, 3, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });  

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useGetAllTravelPlans(searchTerm: string, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
    queryKey: ["allTravelPlans", debouncedSearch],
    queryFn: ({ pageParam = 0 }) => 
      travelService.getAllTravelPlans(debouncedSearch, pageParam, 9, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useSearchAvailableEmployeeForTravel(searchTerm: string, token: string, startDate: string, endDate: string) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
  queryKey: ["searchAvailableEmployeeForTravel", debouncedSearch, startDate, endDate],
  queryFn: ({ pageParam = 0 }) => 
    apiService.searchAvailableEmployeeForTravel(debouncedSearch, pageParam, 5, startDate, endDate, token),
  initialPageParam: 0,
  getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
  enabled: debouncedSearch.length >= 1 && !!startDate && !!endDate,
  placeholderData: (previousData) => previousData,
});

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useGetAllJobs(searchTerm: string, jobType: number, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
    queryKey: ["allJobs", debouncedSearch, jobType],
    queryFn: ({ pageParam = 0 }) => 
      jobService.getAllJobs(debouncedSearch, jobType, pageParam, 3, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useShowAllPosts(searchTerm: string, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
    queryKey: ["allPosts", debouncedSearch],
    queryFn: ({ pageParam = 0 }) => 
      postService.showAllPosts(debouncedSearch, pageParam, 2, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useFindGameBookingByUserId(searchTerm: string, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);
  const { user } = useAuth();

  const query = useInfiniteQuery({
    queryKey: ["Bookings", user?.id, debouncedSearch],
    queryFn: ({ pageParam = 0 }) => 
      gameService.findGameBookingByUserId(user?.id, debouncedSearch, pageParam, 4, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

export function useGetUserNotifications(searchTerm: string, token: string) {
  const debouncedSearch = useAppDebounce(searchTerm);
  const { user } = useAuth();

  const query = useInfiniteQuery({
    queryKey: ["notifications", user?.id, debouncedSearch],
    queryFn: ({ pageParam = 0 }) => 
      apiService.getUserNotifications(user?.id, debouncedSearch, pageParam, 4, token || ""),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token,
    placeholderData: (previousData) => previousData,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 

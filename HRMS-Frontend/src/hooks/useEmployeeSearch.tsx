import { useInfiniteQuery } from "@tanstack/react-query";
import { apiService } from "../api/apiService";
import { useAppDebounce } from "./useAppDebounce";

export function useEmployeeSearch(searchTerm: string, token: string, pageSize: number = 10) {
  const debouncedSearch = useAppDebounce(searchTerm);

  const query = useInfiniteQuery({
    queryKey: ["employeeSearchInfinite", debouncedSearch, pageSize],
    queryFn: ({ pageParam = 0 }) =>
      apiService.searchEmployees(debouncedSearch, pageParam, pageSize, token),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.number + 1),
    enabled: !!token && debouncedSearch.length >= 1,
  });

  return {
    ...query,
    debouncedSearch,
  };
} 
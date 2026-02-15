
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiService } from "../api/apiService";

export const useLogin = () => {
  return useMutation({ 
    mutationFn: apiService.login 
  });
};

export const useRegister = () => {
  return useMutation({ 
    mutationFn: apiService.register 
  });
};

export const useUserByEmail = (email: string, token: string) => {
  return useQuery({
    queryKey: ["user", email],
    queryFn: () => apiService.getUserByEmail(email, token),
    enabled: !!email && !!token,
  });
};

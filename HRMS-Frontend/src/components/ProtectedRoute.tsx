import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Spinner } from "./ui/spinner";

export default function ProtectedRoute() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <Spinner />
    );
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  return <Outlet />;
}


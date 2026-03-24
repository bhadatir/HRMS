import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AuthProvider from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ResetPassword from "./pages/ResetPassword";
import OrganizationChart from "./pages/OrganizationChart";
import TravelPlan from "./pages/TravelPlan";
import JobManagement from "./pages/JobManagement";
import PostManagement from "./pages/PostManagement";
import GameManagement from "./pages/GameManagement";
import TeamMemberData from "./pages/TeamMembersData";
import UsersManagement from "./pages/UsersManagement";
import { GlobalSearch } from "./components/GlobalSearch";
import { ToastProvider } from "./context/ToastContext";
import Toast from "./Toast/Toast";

export default function App() {
  return (
    
    <ToastProvider>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/organization-chart" element={<OrganizationChart />} />
            <Route path="/travel-plan" element={<TravelPlan />} />
            <Route path="/job-management" element={<JobManagement />} />
            <Route path="/post-management" element={<PostManagement />} />
            <Route path="/game-management" element={<GameManagement />} />
            <Route path="/team-member-data" element={<TeamMemberData />} />    
            <Route path="/users-management" element={<UsersManagement />} />
            <Route path="/global-Search" element={<GlobalSearch />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
    <Toast />
    </ToastProvider>
  );
}

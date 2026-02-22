import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AuthProvider from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import ResetPassword from "./pages/ResetPassword";
import OrganizationChart from "./pages/OrganizationChart";
import TravelPlan from "./pages/TravelPlan";
import JobManagement from "./pages/JobManagement";
import PostManagement from "./pages/PostManagement";
import GameManagement from "./pages/GameManagement";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/organization-chart" element={<OrganizationChart />} />
            <Route path="/travel-plan" element={<TravelPlan />} />
            <Route path="/job-management" element={<JobManagement />} />
            <Route path="/post-management" element={<PostManagement />} />
            <Route path="/game-management" element={<GameManagement />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { 
  X,
  Edit,
  IndianRupee,
  Trash,
  Badge
} from "lucide-react";
import { apiService } from "@/api/apiService";
import AddUser from "./AddUser";

export default function UserDetails({ userEmail }: { userEmail: string | null}) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const [showEditUserForm, setShowEditUserForm] = useState(false); 
    
  const { data: userData, isLoading } = useQuery({
    queryKey: ["user", userEmail],
    queryFn: () => apiService.getUserByEmail(userEmail!, token!),
    enabled: !!token && !!userEmail,
  });

  const inactivateMutation = useMutation({
    mutationFn: (reason: string) => apiService.inActiveUserByID(userData?.id, reason, token!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user", userEmail] });
      queryClient.invalidateQueries({ queryKey: ["allEmployees"] });
    },
    onError: (error: any) => {
      alert("Failed to inactivate user: " + (error.response?.data || error.message));
    }
  });

  const handleInactivate = () => {
    const reason = prompt("Please enter the reason for inactivating this user:");
    if (reason) {
      inactivateMutation.mutate(reason);
    }
  };

  if (isLoading) return <div className="p-10 text-center text-slate-500">Loading User Data...</div>;

  return (
    <div>
      {/* Edit user */}
      {showEditUserForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl max-w-2xl w-full relative h-150 overflow-y-auto">
            <Button title="Close Notifications" variant="ghost" className="absolute right-2 top-2" 
              onClick={() => {
              setShowEditUserForm(false);
            }}><X /></Button>
            <AddUser editUserEmail={userData?.employeeEmail || null} onSuccess={() => setShowEditUserForm(false)} />
          </div>
        </div>
      )}

      <Card className="border-none shadow-none">
        <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-xl font-bold">{userData?.employeeFirstName} {userData?.employeeLastName}</CardTitle>
        </CardHeader>

        <CardContent>
            <div className="grid grid-cols-2 gap-6">
                <div className="space-y-4">
                    <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Personal Information</h3>
                    <p><span className="font-medium">Email:</span> {userData?.employeeEmail}</p>
                    <p><span className="font-medium">Date of Birth:</span> {new Date(userData?.employeeDob || "").toLocaleDateString()}</p>
                    <p><span className="font-medium">Gender:</span> {userData?.employeeGender}</p>
                    <p>
                      {userData?.employeeIsActive ? "Active" : "Inactive"}
                    </p>
                </div>
                <div className="space-y-4">
                    <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Job Information</h3>
                    <p><span className="font-medium">Hire Date:</span> {new Date(userData?.employeeHireDate || "").toLocaleDateString()}</p>
                    <p><span className="font-medium">Salary:</span> <IndianRupee className="inline mb-1" /> {userData?.employeeSalary}</p>
                    <p><span className="font-medium">Department:</span> {userData?.departmentName}</p>
                    <p><span className="font-medium">Position:</span> {userData?.positionName}</p>
                    <p><span className="font-medium">Role:</span> {userData?.roleName}</p>
                </div>
                <div className="space-y-4 md:col-span-2">
                    <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Account Information</h3>
                    <p><span className="font-medium">Account Created:</span> {new Date(userData?.employeeCreatedAt || "").toLocaleDateString()} : {new Date(userData?.employeeCreatedAt || "").toLocaleTimeString()}</p>
                    <p><span className="font-medium">Last Login:</span> {new Date(userData?.lastLoginAt || "").toLocaleDateString()} : {new Date(userData?.lastLoginAt || "").toLocaleTimeString()}</p>
                </div>
                {userData?.employeeIsActive && user?.id !== userData?.id && 
                  <div className="flex gap-2">
                    <Button title="Edit User"
                        onClick={() => setShowEditUserForm(true)} className="gap-2 text-gray-600">
                        <Edit size={18} />
                        Edit User
                    </Button>
                    <Button title="Inactivate User"
                        onClick={handleInactivate} className="gap-2 text-gray-600">
                        <Trash size={18} />
                        Inactivate User
                    </Button>
                </div>}
             </div>
        </CardContent>
      </Card>
    </div>
  );
}

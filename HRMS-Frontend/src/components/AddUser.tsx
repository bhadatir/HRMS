import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiService } from "@/api/apiService";
import { useAuth } from "@/context/AuthContext";
import { useForm } from "react-hook-form";
import { useToast } from "@/context/ToastContext";
import { Spinner } from "./ui/spinner";

type AddUserFormInputs = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  dob: string;
  gender: "male" | "female" | "other";
  hireDate: string;
  salary: number;
  departmentId: number;
  positionId: number;
  roleId: number;
}

type Role = {
  id: number;
  roleName: string;
}

type Department = {
  id: number;
  departmentName: string;
}

type Position = {
  id: number;
  positionName: string;
}

export default function AddUser({editUserEmail, onSuccess}: {editUserEmail: string | null, onSuccess: () => void}) {
  const { token, user } = useAuth();
  const queryClient = useQueryClient();
  const toast = useToast();
  
  const { register, handleSubmit, watch, reset } = useForm<AddUserFormInputs>({
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      dob: "",
      gender: "male",
      hireDate: "",
      salary: 0,
      departmentId: 1,
      positionId: 1,
      roleId: 1
    }
  });

  const { data: allRoles, isLoading: allRolesLoading, isError: allRolesError } = useQuery({
    queryKey: ["allRoles"],
    queryFn: () => apiService.getAllRoles(token || ""),
    enabled: !!token,
  });

  const { data: allDepartments, isLoading: allDepartmentsLoading, isError: allDepartmentsError } = useQuery({
    queryKey: ["allDepartments"],
    queryFn: () => apiService.getAllDepartments(token || ""),
    enabled: !!token,
  });

  const { data: allPositions, isLoading: allPositionsLoading, isError: allPositionsError } = useQuery({
    queryKey: ["allPositions"],
    queryFn: () => apiService.getAllPositions(token || ""),
    enabled: !!token,
  });

  const getMutation = useMutation({
    mutationFn: () => apiService.getUserByEmail(editUserEmail!, token || ""),
    onSuccess: (data) => {
      
      reset({
        firstName: data.employeeFirstName,
        lastName: data.employeeLastName,
        email: data.employeeEmail,
        password: "null",
        dob: data.employeeDob.split("T")[0],
        gender: data.employeeGender,
        hireDate: data.employeeHireDate.split("T")[0],
        salary: data.employeeSalary,
        departmentId: data.departmentId,
        positionId: data.positionId,
        roleId: data.roleId
      });
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onError: (error: any) => {
      const data = error.response?.data;  
      const detailedError = typeof data === 'object' 
      ? JSON.stringify(data, null, 2) 
      : data || error.message;
      toast?.error("Failed to get user details: " + detailedError);
    }
  });
  
  useEffect(() => {
    if (editUserEmail) getMutation.mutate();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editUserEmail]);

  const registerMutation = useMutation({
    mutationFn: (data: AddUserFormInputs) => 
      {
        return editUserEmail ? 
          apiService.updateUserByEmail(editUserEmail, data, token || "") :
          apiService.register(data, token || "");
      },
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["user", editUserEmail] });
        queryClient.invalidateQueries({ queryKey: ["searchEmployees"] });
        reset();
        toast?.success(editUserEmail ? "User updated successfully!" : "User created successfully!");
        onSuccess();
      },
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      onError: (error: any) => {
        const data = error.response?.data;  
        const detailedError = typeof data === 'object' 
        ? JSON.stringify(data, null, 2) 
        : data || error.message;
        toast?.error("Registration failed: " + detailedError);
      }
    }
  );

  if (allRolesError || allDepartmentsError || allPositionsError) toast?.error("Failed to load data: " + (allRolesError || allDepartmentsError || allPositionsError));

  return (
    <Card className="border-none shadow-none">
      <CardHeader>
        <CardTitle>{editUserEmail ? "Edit User" : "Create New User"}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-400">
        {( allRolesLoading || allDepartmentsLoading || allPositionsLoading || getMutation.isPending || registerMutation.isPending ) && <Spinner />} 
                
          <form onSubmit={handleSubmit(data => registerMutation.mutate(data))} className="grid grid-cols-1 md:grid-cols-2 gap-6">                    
            <div className="space-y-4 md:col-span-2 border-b pb-2">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Personal Information</h3>
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">First Name</label>
              <Input required placeholder="Tirth" {...register("firstName" )} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Last Name</label>
              <Input required placeholder="Bhadani" {...register("lastName" )} />
            </div>
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">Email Address</label>
              <Input required type="email" placeholder="tirth.bhadani@roimaint.com" {...register("email" )} />
            </div>
            { !editUserEmail && (
              <div className="space-y-2 md:col-span-2">
                <label className="text-sm font-medium">Password</label>
                <Input required type="password" placeholder="••••••••" {...register("password" )} />
              </div>
            )}
            <div className="space-y-4 md:col-span-2 border-b pb-2 pt-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-500">Employment Details</h3>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Date of Birth</label>
              <Input required type="date" max={new Date().toISOString().split("T")[0]} {...register("dob" )} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Hire Date</label>
              <Input required type="date" {...register("hireDate" )} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Gender</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:ring-2 focus:ring-gray-500"
                {...register("gender" )}
              >
                <option value="male">Male</option>
                <option value="female">Female</option>
                <option value="other">Other</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Annual Salary</label>
              <Input required type="number" placeholder="0.00" {...register("salary" )} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Department</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm"
                {...register("departmentId" )}
              >
                <option value="">Select Department</option>
                {allDepartments?.map((dept: Department) => (
                  <option key={dept.id} value={dept.id}>{dept.departmentName}</option>
                ))}
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Role</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm"
                {...register("roleId" )}
              >
                <option value="">Select Role</option>
                {allRoles?.map((role: Role) => ((user?.roleName == "ADMIN" || (user?.roleName == "HR" && role.roleName != "ADMIN")) &&
                  <option key={role.id} value={role.id}>{role.roleName}</option>
                ))}
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Position</label>
              <select 
                className="flex h-10 w-full rounded-md border border-input px-3 py-2 text-sm "
                {...register("positionId" )}
              >
                <option value="">Select Position</option>
                {allPositions?.map((position: Position) => (
                  <option key={position.id} value={position.id}>{position.positionName}</option>
                ))}
              </select>
            </div>
            <Button 
              type="submit" 
              className="md:col-span-2 w-full h-11 bg-gray-600 hover:bg-gray-700 text-black font-bold mt-4"
              disabled={registerMutation.isPending || !watch("firstName")?.trim() || !watch("lastName")?.trim() || !watch("email")?.trim() || (!editUserEmail && !watch("password")) || !watch("dob") || !watch("hireDate") || !watch("salary") || !watch("departmentId") || !watch("roleId") || !watch("positionId")}
            >
              {registerMutation.isPending ? editUserEmail ? "Updating User..." : "Registering User..." : "Submit User Data"}
            </Button>
        </form>
      </CardContent>
    </Card>
  );
}

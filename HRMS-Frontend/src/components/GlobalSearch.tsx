import { Command } from "cmdk";
import { DialogTitle, DialogDescription } from "@/components/ui/dialog"; 
import { useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { useGlobalSearch } from "@/hooks/useInfinite";
import { User, Plane, Loader2, X, Search } from "lucide-react"; 
import { Button } from "./ui/button";

export function GlobalSearch() {
  const { token, user} = useAuth();    
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const navigate = useNavigate();
  const currentPageUrl = window.location.href;
  const currentPage = currentPageUrl.substring(currentPageUrl.lastIndexOf("/") + 1);

  useEffect(() => {    
    const down = (e: KeyboardEvent) => {
      if (e.key === "k" && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setOpen((prev) => !prev);
      }
    };
    document.addEventListener("keydown", down);
    return () => document.removeEventListener("keydown", down);
  }, []);

  useEffect(() => {
    const clickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest(".cmdk-content")) {
        setOpen(false);
      }
    };
    if (open) {
      document.addEventListener("click", clickOutside);
    } else {
      document.removeEventListener("click", clickOutside);
    }
    return () => document.removeEventListener("click", clickOutside);
  }, [open]);

  const {
    data: globalSearchResults,
    isFetching,
    isError,
  } = useGlobalSearch(search, token || "");

  if(isError) alert("Failed to perform global search: " + isError);

  const latestResults = globalSearchResults?.pages[0];

  const handleSelect = (type: string, id: number) => {
    setOpen(false);
    setSearch("");
    if(type == "EMPLOYEE"){
        navigate(`/organization-chart?employeeId=${id}`)
    }
    if(type == "TRAVEL_PLAN"){
        navigate(`/travel-plan?travelPlanId=${id}`)
    }
    if(type == "JOB"){
        navigate(`/job-management?jobId=${id}`)
    }
    if(type == "POST"){
        navigate(`/post-management?postId=${id}`)
    }
    if(type == "GAME_BOOKING"){
        navigate(`/game-management?gameBookingId=${id}`)
    }
    if(type == "TEAM_MEMBER"){
        navigate(`/team-member-data?teamMemberId=${id}`)
    }
  };

  return (
    <Command.Dialog open={open} onOpenChange={setOpen} label="Global Search"
      contentClassName="fixed inset-0 z-[100] flex items-start justify-center pt-[15vh] bg-black/50 p-4"
      className="bg-white rounded-xl border w-full max-w-3xl overflow-hidden p-4">
      <div className="items-center border-b px-3">
        <div> 
          <DialogTitle>Global Search</DialogTitle>
          <DialogDescription>
          Search for employees, travel plans, jobs, posts, game bookings and team members across the organization.
          </DialogDescription>
        </div>
    
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center border-b px-3">
              <Search className="h-4 w-4 text-slate-400" />
              <Command.Input 
              placeholder="Search..." 
              className="flex h-8 p-2 m-4 w-full rounded-md bg-transparent text-sm"
              value={search}
              onValueChange={setSearch} 
              autoFocus
              />
          </div>
          {isFetching && <Loader2 className="ml-2 h-4 w-4 animate-spin text-slate-400" />}
          <Button variant="outline" size="sm" className="ml-2" onClick={() => setOpen(false)}>
              <X size={16} />
              <span className="sr-only">Close</span>
          </Button>
        </div>
      </div>

      <Command.List className="max-h-[300px] overflow-y-auto overflow-x-hidden p-2 scrollbar-thin">
        
        {isFetching && search.length > 0 && (
            <div className="p-4 text-center text-xs text-slate-400">Searching...</div>
        )}

        {isError && (
            <div className="p-4 text-center text-red-500 text-xs">
            Search failed. Please try again.
            </div>
        )}

        {!isFetching && search.length >= 1 && 
        !latestResults?.employees?.content.length && 
        !latestResults?.travelPlans?.content.length && (
            <Command.Empty>No results found for "{search}".</Command.Empty>
        )}

        <div className="flex">
          {!currentPage.includes("organization-chart") && latestResults?.employees?.content?.length > 0 && (
            <Command.Group heading={
              <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold text-blue-600">
                <User size={14} /> <span>Employees</span>
              </div>
            }>
              {latestResults?.employees?.content?.map((emp: any) => (
                <Command.Item 
                  key={`emp-${emp.id}`} 
                  onSelect={() => handleSelect(emp.type, emp.id)}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-2 py-2 text-sm hover:bg-slate-100"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{emp.title}</span>
                    <span className="text-[10px] text-slate-400">{emp.subtitle}</span>
                  </div>
                </Command.Item>
              ))}
            </Command.Group>
          )}

          {!currentPage.includes("travel-plan") && latestResults?.travelPlans?.content?.length > 0 && (
            <Command.Group heading={
              <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold text-green-600">
                <Plane size={14} /> <span>Travel Plans</span>
              </div>
            }>
              {latestResults?.travelPlans?.content?.map((plan: any) => (
                <Command.Item 
                  key={`plan-${plan.id}`} 
                  onSelect={() => handleSelect(plan.type, plan.id)}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-2 py-2 text-sm hover:bg-slate-100"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{plan.title}</span>
                    <span className="text-[10px] text-slate-400">{plan.subtitle}</span>
                  </div>
                </Command.Item>
              ))}
            </Command.Group>
          )}

          {!currentPage.includes("job-management") && latestResults?.job?.content?.length > 0 && (
            <Command.Group heading={
              <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold text-blue-600">
                <User size={14} /> <span>Jobs</span>
              </div>
            }>
              {latestResults?.job?.content?.map((job: any) => (
                <Command.Item 
                  key={`job-${job.id}`} 
                  onSelect={() => handleSelect(job.type, job.id)}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-2 py-2 text-sm hover:bg-slate-100"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{job.title}</span>
                    <span className="text-[10px] text-slate-400">{job.subtitle}</span>
                  </div>
                </Command.Item>
              ))}
            </Command.Group>
          )}

          {!currentPage.includes("post-management") && latestResults?.post?.content?.length > 0 && (
            <Command.Group heading={
              <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold text-blue-600">
                <User size={14} /> <span>Posts</span>
              </div>
            }>
              {latestResults?.post?.content?.map((post: any) => (
                <Command.Item 
                  key={`post-${post.id}`} 
                  onSelect={() => handleSelect(post.type, post.id)}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-2 py-2 text-sm hover:bg-slate-100"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{post.title}</span>
                    <span className="text-[10px] text-slate-400">{post.subtitle}</span>
                  </div>
                </Command.Item>
              ))}
            </Command.Group>
          )}

          {!currentPage.includes("game-management") && latestResults?.gameBooking?.content?.length > 0 && (
            <Command.Group heading={
              <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold text-blue-600">
                <User size={14} /> <span>Game Bookings</span>
              </div>
            }>
              {latestResults?.gameBooking?.content?.map((gameBooking: any) => (
                <Command.Item 
                  key={`gameBooking-${gameBooking.id}`} 
                  onSelect={() => handleSelect(gameBooking.type, gameBooking.id)}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-2 py-2 text-sm hover:bg-slate-100"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{gameBooking.title}</span>
                    <span className="text-[10px] text-slate-400">{gameBooking.subtitle}</span>
                  </div>
                </Command.Item>
              ))}
            </Command.Group>
          )}

          {user?.roleName == "MANAGER" && !currentPage.includes("team-member-data") && latestResults?.teamMember?.length > 0 && (
            <Command.Group heading={
              <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold text-blue-600">
                <User size={14} /> <span>Team Members</span>
              </div>
            }>
              {latestResults?.teamMember?.map((teamMember: any) => (
                <Command.Item 
                  key={`teamMember-${teamMember.id}`} 
                  onSelect={() => handleSelect(teamMember.type, teamMember.id)}
                  className="flex cursor-pointer items-center gap-2 rounded-sm px-2 py-2 text-sm hover:bg-slate-100"
                >
                  <div className="flex flex-col">
                    <span className="font-medium">{teamMember.title}</span>
                    <span className="text-[10px] text-slate-400">{teamMember.subtitle}</span>
                  </div>
                </Command.Item>
              ))}
            </Command.Group>
          )}

        </div>
      </Command.List>
    </Command.Dialog>
  );
}


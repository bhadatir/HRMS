import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

type ConformationReasonProps = {
  onClose: () => void;
  onConfirm: (reason: string) => void;
  iteam: string;
  action: string;
}

export function ConformationDialog({ onConfirm, onClose, iteam, action }: ConformationReasonProps) {
  const [reason, setReason] = useState("");

  const handleConfirm = () => {
    onConfirm(reason.trim());
    setReason("");
    onClose();
  };

  return (
    <Card className="border-none shadow-none gap-0">
      <CardHeader>
        <CardTitle className="text-xl">
          {action} this {iteam}?
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex-col">
            <label htmlFor="reason" className="text-md text-muted-foreground">
              {iteam === "all notifications" ? 
                `Are you sure you want to mark this all ${iteam} as read?` 
                : action === "Approve" ? 
                  `Are you sure you want to approve this ${iteam}?` 
                  : iteam === "waiting list entry" ? 
                    `Are you sure you want to delete this ${iteam}?` 
                    : `Please enter reason for ${action.toLowerCase()} this ${iteam}:`
              }
            </label>
            {(iteam !== "all notifications" && action !== "Approve" && iteam !== "waiting list entry") && (
              <Input
                id="reason"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="e.g. Inappropriate content"
                autoFocus
                className="focus-visible:ring-1 mt-1"
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && reason.trim()) handleConfirm();
                }}
              />
            )}
          </div>
          <div className="gameType manager job user game post notification sub travel flex justify-end space-x-2">
            <Button variant="ghost" onClick={onClose} className="h-9">
                Close
            </Button>
            <Button 
                variant="destructive" 
                onClick={handleConfirm}
                disabled={iteam !== "all notifications" && action !== "Approve" && iteam !== "waiting list entry" && !reason.trim()}
                className="h-9 px-4 text-black"
            >
                {action}
            </Button>
          </div>
      </CardContent>        
    </Card>
  );
}
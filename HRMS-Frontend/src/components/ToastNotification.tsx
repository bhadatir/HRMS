import { X } from "lucide-react";
import React from "react";

interface NotificationsProps {
  children: React.ReactNode;
  kind: string;
  onClose: () => void;
  className?: string; 
}

const Notifications = ({ children, kind, onClose, className = "" }: NotificationsProps) => {
  
  const variantStyles: Record<string, string> = {
    success: "bg-green-50 border-green-500 text-green-800",
    error: "bg-red-50 border-red-500 text-red-800",
    warning: "bg-yellow-50 border-yellow-500 text-yellow-800",
    info: "bg-blue-50 border-blue-500 text-blue-800",
  };

  const selectedStyle = variantStyles[kind] || variantStyles.info;

  return (
    <div
      className={`
        manager job user game post notification sub travel flex items-center w-full p-4 mb-3 border-l-4 rounded-r-lg shadow-lg
        ${selectedStyle} 
        ${className}
      `}
      role="alert"
    >
      <div className="flex-1 text-sm font-medium leading-5">
        {children}
      </div>

      <button
        onClick={onClose}
        className="ml-4 rounded-md transition-colors"
        aria-label="Close"
      >
        <X className="h-4 w-4 text-black" />
      </button>
    </div>
  );
};

export default Notifications;
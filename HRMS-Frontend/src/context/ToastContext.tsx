/* eslint-disable @typescript-eslint/no-explicit-any */
import { createContext, useCallback, useContext, useState } from "react";

const ToastContext = createContext<{
  toasts: any[];
  show: (message: string, type?: string) => void;
  hide: (id: number) => void;
  success: (message: string) => void;
  error: (message: string) => void;
  info: (message: string) => void;
  warning: (message: string) => void;
} | null>(null);

export const ToastProvider = ({ children } : { children: React.ReactNode }) => {
  const [toasts, setToasts] = useState<any[]>([]);

  const hide = useCallback((id : number) => {
    setToasts((prev) =>
      prev.map((t) => (t.id === id ? { ...t, leaving: true } : t)),
    );
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 250);
  }, []);

  const addToast = useCallback(
    (message: string, type = "info", duration = 100000) => {
      const id = Date.now() + Math.random();
      const newToast = {
        id,
        message,
        type,
        leaving: false,
      };

      setToasts((prev: any[]) => [...prev, newToast]);
      if (type !== "error") {
        if (duration) {
          setTimeout(() => hide(id), duration);
        }
      }
    },
    [hide],
  );

  const show = (message: string, type = "info") => addToast(message, type);
  const success = (message: string) => addToast(message, "info");
  const error = (message: string) => addToast(message, "error");
  const info = (message: string) => addToast(message, "notice");
  const warning = (message: string) => addToast(message, "warning");

  return (
    <ToastContext.Provider
      value={{ toasts, show, hide, success, error, info, warning }}
    >
      {children}
    </ToastContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useToast = () => useContext(ToastContext);

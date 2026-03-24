import Notification from "../components/ToastNotification";
import { useToast } from "../context/ToastContext";

export default function Toast() {
  const context = useToast();

  if (!context) {
    return null;
  }

  const { toasts, hide } = context;

  return (
    <div
      className="fixed bottom-5 z-[9999] flex w-full"
    >
      {toasts.map((toast) => (
        <Notification
          key={toast.id}
          kind={toast.type}
          onClose={() => hide(toast.id)}
          className={`pointer-events-auto ${toast.leaving ? "opacity-0 translate-x-10 scale-95" : "opacity-100 translate-x-0"}`}
        >
          {toast.message}
        </Notification>
      ))}
    </div>
  );
}

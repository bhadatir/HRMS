import { cn } from "@/lib/utils";
import roimaSpinner from "@/assets/roima_loader.png";

type SpinnerProps = {
  className?: string;
  sizeClassName?: string;
};

export function Spinner({ className, sizeClassName = "h-32 w-32" }: SpinnerProps) {
  return (
    <div className="fixed inset-0 bg-black/6 z-50 flex items-center justify-center">
      <div className={cn("relative flex items-center justify-center", sizeClassName)}>
        <span className="px-4 py-2">
          <img src={roimaSpinner} alt="Roima Spinner" className="h-24 w-auto" />
        </span>

        <div
          className={cn(
            "absolute inset-0 rounded-full border-4 border-white/60 border-t-gray-100 animate-spin",
            className
          )}
        />
      </div>
    </div>
  );
}

import { cn } from "@/lib/utils";

type SpinnerProps = {
  className?: string;
  sizeClassName?: string;
};

export function Spinner({ className, sizeClassName = "h-12 w-12" }: SpinnerProps) {
  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
        <div
        className={cn(
            "animate-spin rounded-full border-b-2 border-white",
            sizeClassName,
            className
        )}
        />
    </div>
  );
}

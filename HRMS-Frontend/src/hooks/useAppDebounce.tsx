import { useDebounce } from "use-debounce";

export function useAppDebounce(value: string, delay: number = 500) {
  const [debouncedValue] = useDebounce(value, delay);
  return debouncedValue;
}
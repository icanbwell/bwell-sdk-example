import { useEffect, useRef } from "react";

/** Calls `fn` every `ms` while `active`, skipping a tick while the previous call is still running. Stops on unmount. */
export function usePolling(fn: () => void | Promise<void>, ms: number, active: boolean) {
  const saved = useRef(fn);
  saved.current = fn;
  const running = useRef(false);
  useEffect(() => {
    if (!active) return;
    const id = setInterval(() => {
      if (running.current) return;
      running.current = true;
      void (async () => { try { await saved.current(); } finally { running.current = false; } })();
    }, ms);
    return () => clearInterval(id);
  }, [ms, active]);
}

import { useCallback, useEffect, useRef, useState } from "react";
import { listenForExternalReturn } from "@/sdk/externalFlow";
import { errorMessage } from "@/sdk/results";

export type ExternalFlowOptions = {
  /** Fetches the CLEAR or provider sign-in URL. Called again if the user reopens the window. */
  getUrl: () => Promise<string>;
  /**
   * Runs when the user says they're done, or the popup reports it is back. Check the real state
   * here and return false if the flow didn't actually finish.
   */
  onDone: () => Promise<boolean | void>;
  /** Shown when onDone returns false. */
  pendingMessage?: string;
};

export type ExternalFlow = ReturnType<typeof useExternalFlow>;

const WINDOW_NAME = "bwell-external-flow";
const WINDOW_FEATURES = "popup,width=520,height=720";
const DEFAULT_PENDING = "That doesn't look finished yet. Finish in the other window, then try again.";

/**
 * Runs CLEAR / provider sign-in in a popup so this tab (and the SDK session in it) stays put.
 * `start` must be called straight from a click so the browser allows the popup. If it is blocked,
 * the whole page is redirected instead, which is how the sample worked before.
 */
export function useExternalFlow() {
  const [active, setActive] = useState(false);
  const [checking, setChecking] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);
  const options = useRef<ExternalFlowOptions | null>(null);
  const popup = useRef<Window | null>(null);
  const checkingRef = useRef(false);

  const closePopup = () => {
    try { popup.current?.close(); } catch { /* already gone */ }
    popup.current = null;
  };

  const open = useCallback(async (opts: ExternalFlowOptions): Promise<"popup" | "redirect"> => {
    // Opened first, while the click still counts: the URL below takes a network call to fetch.
    const win = window.open("", WINDOW_NAME, WINDOW_FEATURES);
    if (!win) {
      window.location.assign(await opts.getUrl());
      return "redirect";
    }
    try {
      win.document.title = "One moment…";
      win.document.body.textContent = "One moment…";
      // The popup is about to visit third-party sites; they shouldn't be able to reach back into this tab.
      win.opener = null;
    } catch { /* a reused window may already be on another origin */ }
    let url: string;
    try { url = await opts.getUrl(); }
    catch (e) { win.close(); throw e; }
    win.location.href = url;
    popup.current = win;
    options.current = opts;
    setNotice(null);
    setActive(true);
    return "popup";
  }, []);

  const finish = useCallback(async () => {
    const opts = options.current;
    if (!opts || checkingRef.current) return;
    checkingRef.current = true;
    setChecking(true);
    setNotice(null);
    try {
      if ((await opts.onDone()) === false) {
        setNotice(opts.pendingMessage ?? DEFAULT_PENDING);
        return;
      }
      options.current = null;
      setActive(false);
      closePopup();
    } catch (e) {
      setNotice(errorMessage(e));
    } finally {
      checkingRef.current = false;
      setChecking(false);
    }
  }, []);

  const reopen = useCallback(async () => {
    if (!options.current) return;
    try { await open(options.current); }
    catch (e) { setNotice(errorMessage(e)); }
  }, [open]);

  const cancel = useCallback(() => {
    options.current = null;
    setActive(false);
    setNotice(null);
    closePopup();
  }, []);

  // The return page tells us when the popup is back, so the user usually doesn't need the button.
  useEffect(() => (active ? listenForExternalReturn(() => void finish()) : undefined), [active, finish]);

  // Leaving the page abandons the flow.
  useEffect(() => closePopup, []);

  return { active, checking, notice, start: open, finish, reopen, cancel };
}

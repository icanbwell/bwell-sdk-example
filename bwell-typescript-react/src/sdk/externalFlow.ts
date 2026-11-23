// How the return page (the redirect URI the popup lands on) tells the main tab that the user is back.
//
// Once the popup is on CLEAR or a provider portal, the browser may cut its link to this tab
// (Cross-Origin-Opener-Policy), so window.opener and popup.closed can't be trusted. A
// BroadcastChannel is same-origin only and doesn't depend on that link.
//
// The main tab acknowledges the message. If nobody does (the popup was blocked and the whole page
// was redirected instead), the return page knows it is the only tab and can carry on itself.
const CHANNEL_NAME = "bwell-sample-external-flow";

type FlowMessage = { type: "returned" } | { type: "ack" };

// A missing ack alone doesn't prove the page was redirected: a popup can outlive its flow (Done or
// Cancel pressed, close() blocked by COOP). So the tab that is about to redirect itself leaves a
// marker, and only a page that finds it carries on as the app. sessionStorage is per tab, and
// popups get a copy of the opener's at creation, so the marker is cleared before any popup opens.
const REDIRECT_MARKER = "bwell-sample-external-redirect";

/** Call just before navigating this tab away to CLEAR or a provider (the popup-blocked fallback). */
export const markExternalRedirect = () => sessionStorage.setItem(REDIRECT_MARKER, "1");

/** Call before opening a popup, so a stale marker can't be copied into it. */
export const clearExternalRedirect = () => sessionStorage.removeItem(REDIRECT_MARKER);

/** Return page: true once if this tab left for the external page itself. */
export function consumeExternalRedirect(): boolean {
  const found = sessionStorage.getItem(REDIRECT_MARKER) !== null;
  sessionStorage.removeItem(REDIRECT_MARKER);
  return found;
}

/** Main tab: calls `onReturn` when the popup reports it has come back. Returns a stop function. */
export function listenForExternalReturn(onReturn: () => void): () => void {
  const channel = new BroadcastChannel(CHANNEL_NAME);
  channel.onmessage = (e: MessageEvent<FlowMessage>) => {
    if (e.data?.type !== "returned") return;
    channel.postMessage({ type: "ack" } satisfies FlowMessage);
    onReturn();
  };
  return () => channel.close();
}

/** Return page: resolves true if a main tab was waiting and took over, false if nobody answered. */
export function announceExternalReturn(timeoutMs = 1000): Promise<boolean> {
  return new Promise((resolve) => {
    const channel = new BroadcastChannel(CHANNEL_NAME);
    const finish = (acknowledged: boolean) => {
      clearTimeout(timer);
      channel.close();
      resolve(acknowledged);
    };
    const timer = setTimeout(() => finish(false), timeoutMs);
    channel.onmessage = (e: MessageEvent<FlowMessage>) => {
      if (e.data?.type === "ack") finish(true);
    };
    channel.postMessage({ type: "returned" } satisfies FlowMessage);
  });
}

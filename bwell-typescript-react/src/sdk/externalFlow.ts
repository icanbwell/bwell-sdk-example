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

import type { BWellSDK } from "@icanbwell/bwell-sdk-ts";
import { BAILEY_BASE_URL, BAILEY_MODEL } from "@/config/app";
import { CLIENT_KEY_STORAGE } from "@/auth/SessionContext";

const ACCESS_TOKEN_KEY = "bwell-sdk:accessToken"; // written by SessionTokenStorage

/** Expiry (ms since epoch) from the token's `exp` claim, or 0 if it can't be read. */
function tokenExpiry(token: string): number {
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return typeof payload.exp === "number" ? payload.exp * 1000 : 0;
  } catch { return 0; }
}

/**
 * bailey takes the same access token as the SDK. The SDK only refreshes it when it makes a call, so if
 * it's about to expire, make a cheap SDK call first to trigger the refresh, then read it back.
 */
async function currentAccessToken(sdk: BWellSDK): Promise<string> {
  let token = sessionStorage.getItem(ACCESS_TOKEN_KEY);
  if (!token || tokenExpiry(token) < Date.now() + 60_000) {
    await sdk.user.getProfile();
    token = sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }
  if (!token) throw new Error("You're not signed in.");
  return token;
}

/** Streams bailey's answer to one prompt, calling `onDelta` with each piece of text as it arrives. */
export async function askBailey(sdk: BWellSDK, prompt: string, onDelta: (text: string) => void, signal?: AbortSignal) {
  const clientKey = sessionStorage.getItem(CLIENT_KEY_STORAGE);
  if (!clientKey) throw new Error("You're not signed in.");
  const response = await fetch(`${BAILEY_BASE_URL}/responses`, {
    method: "POST",
    signal,
    headers: {
      Authorization: `Bearer ${await currentAccessToken(sdk)}`,
      "api-key": clientKey,
      "Content-Type": "application/json",
    },
    // One-off questions: nothing to store or continue from.
    body: JSON.stringify({ model: BAILEY_MODEL, input: prompt, stream: true, store: false }),
  });
  if (!response.ok || !response.body) {
    // Include the server's explanation (an API error message, not user data) so a rejected request is diagnosable.
    const detail = (await response.text().catch(() => "")).trim().slice(0, 300);
    throw new Error(`The assistant couldn't answer (HTTP ${response.status})${detail ? `: ${detail}` : ""}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  for (;;) {
    const { done, value } = await reader.read();
    if (done) return;
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() ?? "";
    for (const line of lines) {
      if (!line.startsWith("data: ")) continue;
      const data = line.slice(6).trim();
      if (data === "[DONE]") return;
      let event: { type?: string; delta?: string; message?: string };
      try { event = JSON.parse(data); } catch { continue; }
      if (event.type === "response.output_text.delta" && event.delta) onDelta(event.delta);
      else if (event.type === "error" || event.type === "response.failed") throw new Error("The assistant couldn't finish that answer.");
    }
  }
}

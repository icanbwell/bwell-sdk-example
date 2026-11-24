import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { BWellSDK } from "@icanbwell/bwell-sdk-ts";
import { errorMessage, SdkError } from "@/sdk/results";
import {
  acceptTermsOfService, enableSmartConnect, getLatestConsent, getVerificationState, isPermitted,
} from "@/sdk/journey";
import { clearSmartConnectAgreement, hasAgreedToSmartConnect } from "@/sdk/smartConnectIntent";
import { SessionTokenStorage } from "@/sdk/sessionTokenStorage";

/** Either a token from your external IdP, or the three tokens from a b.well sign-in. */
export type Credentials =
  | { kind: "idp"; token: string }
  | { kind: "bwell"; accessToken: string; idToken: string; refreshToken?: string };

// The SDK always tries to refresh right after loading tokens. Without a refresh token that
// attempt fails, but the SDK stays usable on the access token until it expires. This
// placeholder lets the tokens load; the failed refresh is tolerated (see connect()).
const NO_REFRESH_TOKEN = "no-refresh-token-available";

type Status = "restoring" | "signed-out" | "signing-in" | "signed-in";

type Session = {
  status: Status;
  sdk: BWellSDK | null;
  tosAccepted: boolean;
  error: string | null;
  /** The stored session couldn't be restored (as opposed to the user signing out). */
  restoreFailed: boolean;
  signIn: (clientKey: string, credentials: Credentials) => Promise<boolean>;
  signOut: () => void;
  acceptTos: () => Promise<void>;
};

// Only the client key is stored here; the SDK keeps its tokens in SessionTokenStorage.
// Both are sessionStorage, so they survive CLEAR/OAuth redirects but not a closed tab.
const CLIENT_KEY = "bwell-sample-client-key";
export const CLIENT_KEY_STORAGE = CLIENT_KEY;

const SessionContext = createContext<Session | null>(null);

async function newSdk(clientKey: string): Promise<BWellSDK> {
  const sdk = new BWellSDK({ clientKey, tokenStorage: new SessionTokenStorage() });
  const init = await sdk.initialize();
  if (!init.success()) throw new SdkError("The client key was not accepted.");
  return sdk;
}

async function connect(clientKey: string, credentials: Credentials): Promise<BWellSDK> {
  const sdk = await newSdk(clientKey);
  if (credentials.kind === "idp") {
    const auth = await sdk.authenticate({ token: credentials.token });
    if (!auth.success()) throw new SdkError("The token was not accepted. It may have expired.");
    return sdk;
  }
  const auth = await sdk.setAuthTokens({
    accessToken: credentials.accessToken,
    idToken: credentials.idToken,
    refreshToken: credentials.refreshToken || NO_REFRESH_TOKEN,
  });
  // With no refresh token the refresh step is expected to fail; the first real API call
  // made after sign-in (loading consents) then proves whether the access token works.
  if (!auth.success() && credentials.refreshToken) {
    throw new SdkError("The tokens were not accepted. They may have expired, or one may be mistyped.");
  }
  return sdk;
}

/**
 * The user agreed to Smart Connect before identity verification; now that they've returned from
 * CLEAR, create the consent if (and only if) verification succeeded.
 */
async function resolvePendingSmartConnect(sdk: BWellSDK) {
  if (!hasAgreedToSmartConnect()) return;
  try {
    // Not verified yet (or still processing): keep holding the agreement, don't ask again.
    if ((await getVerificationState(sdk)) !== "validated") return;
    if (!isPermitted(await getLatestConsent(sdk, "ias:import:records"))) await enableSmartConnect(sdk);
  } catch {
    return; // leave the agreement in place so a later load can retry
  }
  clearSmartConnectAgreement();
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>("restoring");
  const [sdk, setSdk] = useState<BWellSDK | null>(null);
  const [tosAccepted, setTosAccepted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [restoreFailed, setRestoreFailed] = useState(false);

  const ready = useCallback(async (next: BWellSDK) => {
    setTosAccepted(isPermitted(await getLatestConsent(next, "tos")));
    await resolvePendingSmartConnect(next);
    setSdk(next);
    setStatus("signed-in");
  }, []);

  const signIn = useCallback(async (clientKey: string, credentials: Credentials) => {
    setStatus("signing-in");
    setError(null);
    setRestoreFailed(false);
    sessionStorage.clear();
    try {
      const key = clientKey.trim();
      const next = await connect(key, credentials);
      sessionStorage.setItem(CLIENT_KEY, key);
      await ready(next);
      return true;
    } catch (e) {
      sessionStorage.clear();
      setError(errorMessage(e, "Unable to sign in."));
      setStatus("signed-out");
      return false;
    }
  }, [ready]);

  const signOut = useCallback(() => {
    setRestoreFailed(false);
    sessionStorage.clear();
    clearSmartConnectAgreement();
    setSdk(null);
    setTosAccepted(false);
    setStatus("signed-out");
  }, []);

  const acceptTos = useCallback(async () => {
    if (!sdk) throw new SdkError("Not signed in.");
    await acceptTermsOfService(sdk);
    setTosAccepted(true);
  }, [sdk]);

  // After a CLEAR/OAuth redirect (or refresh) the page reloads: rebuild the SDK from stored tokens.
  // Runs once: StrictMode's second dev run would refresh the same stored tokens concurrently.
  const restored = useRef(false);
  useEffect(() => {
    if (restored.current) return;
    restored.current = true;
    const clientKey = sessionStorage.getItem(CLIENT_KEY);
    if (!clientKey) return setStatus("signed-out");
    newSdk(clientKey)
      .then(async (next) => {
        // A failed refresh is tolerated here too: ready() makes a real call and throws if the
        // stored access token is no longer usable.
        await next.authenticateFromStorage();
        await ready(next);
      })
      .catch((e) => {
        // The tokens stay in sessionStorage: the failure may be temporary (network, a 5xx), and the
        // SDK can't tell us whether the tokens were rejected. A reload retries the restore; signing
        // in again replaces them (signIn clears storage first).
        setRestoreFailed(true);
        setError(`We couldn't restore your session: ${errorMessage(e).replace(/\.$/, "")}. Reload to try again, or sign in again.`);
        setStatus("signed-out");
      });
  }, [ready]);

  const value = useMemo(
    () => ({ status, sdk, tosAccepted, error, restoreFailed, signIn, signOut, acceptTos }),
    [status, sdk, tosAccepted, error, restoreFailed, signIn, signOut, acceptTos],
  );
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): Session {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error("useSession must be used inside SessionProvider");
  return ctx;
}

/** For pages that only render once signed in. */
export function useSdk(): BWellSDK {
  const { sdk } = useSession();
  if (!sdk) throw new Error("SDK not available");
  return sdk;
}

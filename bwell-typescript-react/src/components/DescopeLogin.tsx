import { KeyboardEvent, useEffect, useState } from "react";
import { Alert, Button, Link, Stack, TextField, Typography } from "@mui/material";
import { AuthProvider, getRefreshToken, getSessionToken, useDescope } from "@descope/react-sdk";
import { fetchDescopeClientConfig } from "@/sdk/clientRecord";

const ENV_PROJECT_ID = import.meta.env.VITE_DESCOPE_PROJECT_ID ?? "";
const ENV_AUTH_DOMAIN = import.meta.env.VITE_DESCOPE_AUTH_DOMAIN ?? "";

export type DescopeAuthTokens = {
  accessToken: string;
  idToken: string;
  // Descope normally keeps the refresh token in an HttpOnly cookie, so a browser
  // app usually has only the session token.
  refreshToken?: string;
};

type DescopeLoginProps = {
  clientKey: string;
  disabled: boolean;
  onTokens: (tokens: DescopeAuthTokens) => void;
};

// Keeps Descope's tokens in memory only, but lets its getters read them back.
const createMemoryStorage = () => {
  const items = new Map<string, string>();
  return {
    getItem: (key: string) => items.get(key) ?? null,
    setItem: (key: string, value: string) => void items.set(key, value),
    removeItem: (key: string) => void items.delete(key),
  };
};

function PasswordForm({ disabled, onTokens }: Pick<DescopeLoginProps, "disabled" | "onTokens">) {
  const sdk = useDescope();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  const canSubmit = !!email && !!password && !disabled && !submitting;

  /** Signs in with Descope and returns its tokens, or null (with the reason shown) if that didn't work. */
  const authenticate = async (): Promise<DescopeAuthTokens | null> => {
    setSubmitting(true);
    setError(null);
    setCopied(false);
    try {
      const response = await sdk.password.signIn(email, password);
      if (!response.ok) {
        setError(response.error?.errorDescription || response.error?.errorMessage || "Descope sign-in failed.");
        return null;
      }
      const sessionJwt = response.data?.sessionJwt || getSessionToken();
      if (!sessionJwt) {
        setError("Descope sign-in succeeded but did not return a session token.");
        return null;
      }
      // The Descope session JWT is used as both the access and ID token.
      const refreshToken = response.data?.refreshJwt || getRefreshToken() || undefined;
      return { accessToken: sessionJwt, idToken: sessionJwt, refreshToken };
    } catch {
      setError("Descope sign-in failed.");
      return null;
    } finally {
      setSubmitting(false);
    }
  };

  const signIn = async () => {
    const tokens = await authenticate();
    if (tokens) onTokens(tokens);
  };

  // For developers who want a token to call the API from another tool (Postman, curl). Signs in with
  // Descope but not to this app, and puts only the access token on the clipboard; nothing is stored.
  const copyAccessToken = async () => {
    const tokens = await authenticate();
    if (!tokens) return;
    try {
      await navigator.clipboard.writeText(tokens.accessToken);
      setCopied(true);
    } catch {
      setError("Couldn't copy to the clipboard. Your browser may have blocked it.");
    }
  };

  const onEnter = (e: KeyboardEvent) => {
    if (e.key === "Enter" && canSubmit) {
      e.preventDefault();
      void signIn();
    }
  };

  return (
    <Stack spacing={2.5}>
      <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} onKeyDown={onEnter}
        autoComplete="off" disabled={disabled} fullWidth />
      <TextField label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)}
        onKeyDown={onEnter} autoComplete="off" disabled={disabled} fullWidth />
      {error && <Alert severity="error">{error}</Alert>}
      <Button variant="contained" size="large" disabled={!canSubmit} onClick={signIn}>
        {submitting ? "Signing in…" : "Login"}
      </Button>
      <Stack spacing={0.5} sx={{ alignItems: "center" }}>
        <Link component="button" type="button" variant="body2" underline="hover" color="text.secondary"
          disabled={!canSubmit} onClick={copyAccessToken}
          sx={{ "&:disabled": { opacity: 0.5, cursor: "default", textDecoration: "none" } }}>
          Copy access token to clipboard
        </Link>
        {copied && (
          <Typography variant="caption" color="success.dark" role="status">
            Access token copied. Use it as a Bearer token (it expires, so copy a fresh one when needed).
          </Typography>
        )}
      </Stack>
    </Stack>
  );
}

/**
 * Email and password sign-in for clients that use Descope as their identity provider.
 * The Descope project and auth domain are read from the client's identity provider
 * configuration (or VITE_DESCOPE_* when it can't be read). The session token goes to the
 * SDK through `setAuthTokens` (see SessionContext).
 */
export default function DescopeLogin({ clientKey, disabled, onTokens }: DescopeLoginProps) {
  const [projectId, setProjectId] = useState(ENV_PROJECT_ID);
  const [authDomain, setAuthDomain] = useState(ENV_AUTH_DOMAIN);
  const [memoryStorage] = useState(createMemoryStorage);

  useEffect(() => {
    const key = clientKey.trim();
    if (!key) return;
    let cancelled = false;
    fetchDescopeClientConfig(key)
      .then((config) => {
        if (cancelled || !config) return;
        setProjectId(config.projectId);
        setAuthDomain(config.authDomain);
      })
      .catch(() => {
        // Keep the VITE_DESCOPE_* values when the client configuration cannot be read.
      });
    return () => {
      cancelled = true;
    };
  }, [clientKey]);

  if (!projectId) {
    return (
      <Typography variant="body2" color="text.secondary">
        Enter your client key. If this client uses Descope, the sign-in form appears here.
      </Typography>
    );
  }

  return (
    // Tokens stay in memory: nothing is written to cookies or storage.
    <AuthProvider
      projectId={projectId}
      baseUrl={authDomain || undefined}
      sessionTokenViaCookie={false}
      refreshTokenViaCookie={false}
      persistTokens
      customStorage={memoryStorage}
      storeLastAuthenticatedUser={false}
    >
      <PasswordForm disabled={disabled} onTokens={onTokens} />
    </AuthProvider>
  );
}

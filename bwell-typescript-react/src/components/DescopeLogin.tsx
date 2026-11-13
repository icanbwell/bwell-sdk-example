import { useEffect, useState } from "react";
import { Alert, Box, Button, TextField } from "@mui/material";
import { AuthProvider, getRefreshToken, getSessionToken, useDescope } from "@descope/react-sdk";

import type { DescopeAuthTokens } from "@/sdk/bWellSdk";
import { fetchDescopeClientConfig } from "@/sdk/clientRecord";

const ENV_PROJECT_ID = import.meta.env.VITE_DESCOPE_PROJECT_ID ?? "";
const ENV_AUTH_DOMAIN = import.meta.env.VITE_DESCOPE_AUTH_DOMAIN ?? "";

type DescopeLoginProps = {
  clientKey?: string;
  disabled: boolean;
  onTokens: (tokens: DescopeAuthTokens) => void;
};

const fieldStyle = { minWidth: "80%", padding: "10px" };

// Keeps Descope's tokens in memory only, but lets its getters read them back.
const createMemoryStorage = () => {
  const items = new Map<string, string>();
  return {
    getItem: (key: string) => items.get(key) ?? null,
    setItem: (key: string, value: string) => void items.set(key, value),
    removeItem: (key: string) => void items.delete(key),
  };
};

const PasswordForm = ({ disabled, onTokens }: Pick<DescopeLoginProps, "disabled" | "onTokens">) => {
  const sdk = useDescope();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const signIn = async () => {
    setSubmitting(true);
    setError(null);
    try {
      const response = await sdk.password.signIn(email, password);
      if (!response.ok) {
        setError(response.error?.errorDescription || response.error?.errorMessage || "Descope sign-in failed.");
        return;
      }
      const sessionJwt = response.data?.sessionJwt || getSessionToken();
      if (!sessionJwt) {
        setError("Descope sign-in succeeded but did not return a session token.");
        return;
      }
      // The Descope session JWT is used as both the access and ID token. The refresh
      // token is usually unavailable here (HttpOnly cookie), so it is optional.
      const refreshToken = response.data?.refreshJwt || getRefreshToken() || undefined;
      onTokens({ accessToken: sessionJwt, idToken: sessionJwt, refreshToken });
    } catch {
      setError("Descope sign-in failed.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Box>
        <h2>Or Login with Descope</h2>
        <TextField
          aria-label="descope email"
          id="txtDescopeEmail"
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Email"
          value={email}
          fullWidth
          style={fieldStyle}
          disabled={disabled}
        />
        <TextField
          type="password"
          aria-label="descope password"
          id="txtDescopePassword"
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          value={password}
          fullWidth
          style={fieldStyle}
          disabled={disabled}
        />
      </Box>
      <Box>
        <Button
          id="btnSubmitDescope"
          variant="contained"
          disabled={!(email && password) || disabled || submitting}
          onClick={signIn}
        >
          Login with Descope
        </Button>
      </Box>
      {error && (
        <Alert severity="error" id="descopeError">
          {error}
        </Alert>
      )}
    </>
  );
};

/**
 * Email and password sign-in for clients that use Descope as their identity provider.
 * The Descope project and auth domain are read from the client's identity provider
 * configuration (or VITE_DESCOPE_* when it can't be read). Nothing renders for clients
 * that don't use Descope. The session and refresh tokens go to the SDK through
 * `setAuthTokens`.
 */
const DescopeLogin = ({ clientKey, disabled, onTokens }: DescopeLoginProps) => {
  const [projectId, setProjectId] = useState(ENV_PROJECT_ID);
  const [authDomain, setAuthDomain] = useState(ENV_AUTH_DOMAIN);
  const [memoryStorage] = useState(createMemoryStorage);

  useEffect(() => {
    if (!clientKey) return;
    let cancelled = false;
    fetchDescopeClientConfig(clientKey)
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

  if (!projectId) return null;

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
};

export default DescopeLogin;

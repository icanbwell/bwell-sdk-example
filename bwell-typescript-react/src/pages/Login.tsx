import { FormEvent, useState } from "react";
import { Alert, Button, Checkbox, FormControlLabel, Stack, TextField, ToggleButton, ToggleButtonGroup, Typography } from "@mui/material";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { JourneyPage } from "@/components/Page";
import { Credentials, useSession } from "@/auth/SessionContext";
import DescopeLogin, { DescopeAuthTokens } from "@/components/DescopeLogin";

type Mode = "idp" | "bwell" | "descope";

export default function Login() {
  const { status, signIn, error } = useSession();
  const navigate = useNavigate();
  // RequireSession records the page the user was sent away from, so a reload that restores the session
  // (or a sign-in) returns them there instead of starting onboarding again.
  const from = (useLocation().state as { from?: unknown } | null)?.from;
  const returnTo = typeof from === "string" && from.startsWith("/app") ? from : undefined;
  const [mode, setMode] = useState<Mode>("descope");
  const [clientKey, setClientKey] = useState("");
  const [idpToken, setIdpToken] = useState("");
  const [accessToken, setAccessToken] = useState("");
  const [idToken, setIdToken] = useState("");
  const [refreshToken, setRefreshToken] = useState("");
  const [skipOnboarding, setSkipOnboarding] = useState(false);

  // First-time users go through terms and connect-method; returning users can skip to the app.
  // (RequireTos still sends anyone who hasn't accepted the terms to /terms.)
  const destination = returnTo ?? (skipOnboarding ? "/app/connections" : "/terms");

  if (status === "signed-in") return <Navigate to={destination} replace />;

  const credentials: Credentials | null =
    mode === "idp"
      ? idpToken.trim() ? { kind: "idp", token: idpToken.trim() } : null
      : mode === "descope"
        ? null // Descope sign-in supplies its own credentials (see signInWithDescope)
        : accessToken.trim() && idToken.trim()
        ? { kind: "bwell", accessToken: accessToken.trim(), idToken: idToken.trim(), refreshToken: refreshToken.trim() || undefined }
        : null;

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (credentials && (await signIn(clientKey, credentials))) navigate(destination);
  };

  // The Descope session token is used as both the access and ID token. Without a refresh token the
  // session works until the token expires (SessionContext tolerates the failed refresh).
  const signInWithDescope = async (tokens: DescopeAuthTokens) => {
    if (await signIn(clientKey, { kind: "bwell", ...tokens })) navigate(destination);
  };

  const secret = (label: string, value: string, set: (v: string) => void) => (
    <TextField label={label} type="password" value={value} onChange={(e) => set(e.target.value)} autoComplete="off" required fullWidth />
  );

  return (
    <JourneyPage title="Sign in" subtitle="Enter your b.well client key and a user token to begin.">
      <form onSubmit={submit}>
        <Stack spacing={2.5}>
          {secret("Client key", clientKey, setClientKey)}
          <ToggleButtonGroup exclusive fullWidth size="small" color="primary" value={mode} aria-label="Token type"
            onChange={(_e, v: Mode | null) => v && setMode(v)}>
            <ToggleButton value="descope">User Credentials</ToggleButton>
            <ToggleButton value="idp">External IdP token</ToggleButton>
            <ToggleButton value="bwell">b.well tokens</ToggleButton>
          </ToggleButtonGroup>
          {mode === "idp" ? (
            secret("IdP token", idpToken, setIdpToken)
          ) : mode === "descope" ? (
            <DescopeLogin clientKey={clientKey} disabled={status === "signing-in" || !clientKey.trim()} onTokens={signInWithDescope} />
          ) : (
            <>
              <Typography variant="body2" color="text.secondary">
                Sign in to the b.well web app and copy the access and ID tokens from its session storage. The refresh
                token is optional: without it you get a limited session that works until the access token expires,
                then you'll need to sign in again.
              </Typography>
              {secret("Access token", accessToken, setAccessToken)}
              {secret("ID token", idToken, setIdToken)}
              <TextField label="Refresh token (optional)" type="password" value={refreshToken} onChange={(e) => setRefreshToken(e.target.value)} autoComplete="off" fullWidth />
            </>
          )}
          <FormControlLabel
            control={<Checkbox checked={skipOnboarding} onChange={(e) => setSkipOnboarding(e.target.checked)} />}
            label="Skip onboarding and go straight to Connect Records"
          />
          {error && <Alert severity="error">{error}</Alert>}
          {mode !== "descope" && (
            <Button type="submit" variant="contained" size="large" disabled={status === "signing-in" || !clientKey.trim() || !credentials}>
              {status === "signing-in" ? "Signing in…" : "Continue"}
            </Button>
          )}
        </Stack>
      </form>
    </JourneyPage>
  );
}

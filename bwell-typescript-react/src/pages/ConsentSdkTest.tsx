import { useMemo, useState } from "react";
import { Alert, Box, Button, Container, TextField, Typography } from "@mui/material";
import { BWellSDK } from "@icanbwell/bwell-sdk-ts";

type ConsentSummary = {
  id?: string;
  status?: string;
  provisionType?: string;
};

function extractConsents(bundle: any): any[] {
  const entries = Array.isArray(bundle?.entry) ? bundle.entry : [];
  return entries.map((e: any) => e?.resource).filter(Boolean);
}

function summarizeConsent(consent: any): ConsentSummary {
  return {
    id: consent?.id,
    status: consent?.status,
    provisionType: consent?.provision?.type,
  };
}

const DEFAULT_CLIENT_KEY = import.meta.env.VITE_DEFAULT_KEY ?? "";

const ConsentSdkTest = () => {
  const [clientKey, setClientKey] = useState(DEFAULT_CLIENT_KEY);
  const [accessToken, setAccessToken] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [logLines, setLogLines] = useState<string[]>([]);
  const [consents, setConsents] = useState<any[]>([]);

  const canRun = clientKey.trim().length > 0 && accessToken.trim().length > 0 && !loading;

  const logText = useMemo(() => logLines.join("\n"), [logLines]);

  const appendLog = (line: string) => {
    setLogLines((prev) => [...prev, `[${new Date().toISOString()}] ${line}`]);
  };

  const runGetConsents = async () => {
    const trimmedKey = clientKey.trim();
    const trimmedToken = accessToken.trim();

    setLoading(true);
    setError(null);
    setConsents([]);
    setLogLines([]);

    try {
      appendLog("Creating SDK instance");
      const sdk = new BWellSDK({ clientKey: trimmedKey });

      appendLog("Initializing SDK");
      await sdk.initialize();

      appendLog("Authenticating with access token");
      await sdk.authenticate({ token: trimmedToken });

      appendLog("Calling sdk.user.getConsents()");
      const result: any = await sdk.user.getConsents();

      if (typeof result?.hasError === "function" && result.hasError()) {
        const err = result?.error;
        console.error("getConsents failed:", err);
        appendLog("getConsents returned an error");
        setError(typeof err === "string" ? err : JSON.stringify(err, null, 2));
        return;
      }

      const data = result?.data ?? result;
      const extracted = extractConsents(data);
      setConsents(extracted);

      appendLog(`Found ${extracted.length} consent(s)`);
      extracted.forEach((c: any) => {
        const s = summarizeConsent(c);
        appendLog(`${s.id ?? "(no id)"} ${s.status ?? "(no status)"} ${s.provisionType ?? ""}`.trim());
      });
    } catch (e: any) {
      console.error("ConsentSdkTest threw:", e);
      setError(e?.message ?? "Unexpected error");
      appendLog(`Threw: ${e?.message ?? String(e)}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        Consent SDK Test
      </Typography>

      <Box display="flex" flexDirection="column" gap={2} mb={2}>
        <TextField
          label="Client Key"
          value={clientKey}
          onChange={(e) => setClientKey(e.target.value)}
          placeholder="YOUR_CLIENT_KEY"
          type="password"
          fullWidth
        />

        <TextField
          label="Access Token"
          value={accessToken}
          onChange={(e) => setAccessToken(e.target.value)}
          placeholder="YOUR_ACCESS_TOKEN"
          type="password"
          fullWidth
          multiline
          minRows={3}
        />

        <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
          <Button variant="contained" onClick={runGetConsents} disabled={!canRun}>
            {loading ? "Running..." : "Get Consents"}
          </Button>

          <Button
            variant="outlined"
            onClick={() => {
              setError(null);
              setLogLines([]);
              setConsents([]);
            }}
            disabled={loading}
          >
            Clear
          </Button>
        </Box>
      </Box>

      {error && (
        <Box mb={2}>
          <Alert severity="error" sx={{ whiteSpace: "pre-wrap" }}>
            {error}
          </Alert>
        </Box>
      )}

      {consents.length > 0 && (
        <Box mb={2}>
          <Alert severity="success">Found {consents.length} consent(s).</Alert>
        </Box>
      )}

      <Box>
        <Typography variant="h6" component="h2" gutterBottom>
          Log
        </Typography>
        <pre style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>{logText}</pre>
      </Box>

      <Box mt={3}>
        <Typography variant="h6" component="h2" gutterBottom>
          Raw Consents
        </Typography>
        <pre style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
          {JSON.stringify(consents, null, 2)}
        </pre>
      </Box>
    </Container>
  );
};

export default ConsentSdkTest;

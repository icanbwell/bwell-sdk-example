import { useMemo, useState } from "react";
import { Alert, Box, Button, Container, TextField, Typography } from "@mui/material";
import { BWellSDK, CreateConsentRequest } from "@icanbwell/bwell-sdk-ts";

const DEFAULT_CLIENT_KEY = import.meta.env.VITE_DEFAULT_KEY ?? "";

const CreateProaConsentSdkTest = () => {
  const [clientKey, setClientKey] = useState(DEFAULT_CLIENT_KEY);
  const [accessToken, setAccessToken] = useState("");
  const [organizationId, setOrganizationId] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [logLines, setLogLines] = useState<string[]>([]);
  const [createdConsent, setCreatedConsent] = useState<any>(null);

  const canRun =
    clientKey.trim().length > 0 &&
    accessToken.trim().length > 0 &&
    organizationId.trim().length > 0 &&
    !loading;
  const logText = useMemo(() => logLines.join("\n"), [logLines]);

  const appendLog = (line: string) => {
    setLogLines((prev) => [...prev, `[${new Date().toISOString()}] ${line}`]);
  };

  const runCreateProaConsent = async () => {
    const trimmedKey = clientKey.trim();
    const trimmedToken = accessToken.trim();
    const trimmedOrganizationId = organizationId.trim();

    setLoading(true);
    setError(null);
    setLogLines([]);
    setCreatedConsent(null);

    try {
      appendLog("Creating SDK instance");
      const sdk = new BWellSDK({ clientKey: trimmedKey });

      appendLog("Initializing SDK");
      await sdk.initialize();

      appendLog("Authenticating with access token");
      await sdk.authenticate({ token: trimmedToken });

      appendLog(
        'Creating request: new CreateConsentRequest({ status: "ACTIVE", provision: "PERMIT", category: "PROA_ATTESTATION", organizationId })'
      );
      const request = new CreateConsentRequest({
        status: "ACTIVE",
        provision: "PERMIT",
        category: "PROA_ATTESTATION",
        organizationId: trimmedOrganizationId,
      });

      appendLog("Calling sdk.user.createConsent(request)");
      const result = await sdk.user.createConsent(request);

      if (!result.success()) {
        const message = result.error()?.message ?? "PROA consent failed";
        console.error("PROA consent failed:", message);
        appendLog(`PROA consent failed: ${message}`);
        setError(message);
        return;
      }

      const consent = result.data();
      setCreatedConsent(consent);
      appendLog(`PROA consent created: ${consent?.id ?? "(no id)"}`);
    } catch (e: any) {
      console.error("CreateProaConsentSdkTest threw:", e);
      setError(e?.message ?? "Unexpected error");
      appendLog(`Threw: ${e?.message ?? String(e)}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        Create PROA Consent SDK Test
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

        <TextField
          label="Organization ID"
          value={organizationId}
          onChange={(e) => setOrganizationId(e.target.value)}
          placeholder="YOUR_ORGANIZATION_ID"
          fullWidth
        />

        <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
          <Button variant="contained" onClick={runCreateProaConsent} disabled={!canRun}>
            {loading ? "Running..." : "Create PROA Consent"}
          </Button>

          <Button
            variant="outlined"
            onClick={() => {
              setError(null);
              setLogLines([]);
              setCreatedConsent(null);
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

      {createdConsent && (
        <Box mb={2}>
          <Alert severity="success">PROA consent created successfully.</Alert>
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
          Created Consent
        </Typography>
        <pre style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
          {JSON.stringify(createdConsent, null, 2)}
        </pre>
      </Box>
    </Container>
  );
};

export default CreateProaConsentSdkTest;

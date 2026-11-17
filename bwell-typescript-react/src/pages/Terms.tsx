import { useState } from "react";
import { Alert, Button, Stack } from "@mui/material";
import { Navigate, useNavigate } from "react-router-dom";
import { JourneyPage } from "@/components/Page";
import { useSession } from "@/auth/SessionContext";
import TosText from "@/components/TosText";
import { TOS_HEADING } from "@/config/tos";
import { errorMessage } from "@/sdk/results";

export default function Terms() {
  const { tosAccepted, acceptTos, signOut } = useSession();
  const navigate = useNavigate();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (tosAccepted && !busy) return <Navigate to="/connect-method" replace />;

  const accept = async () => {
    setBusy(true);
    setError(null);
    try {
      await acceptTos();
      navigate("/connect-method");
    } catch (e) {
      setError(errorMessage(e, "We couldn't record your agreement. Please try again."));
      setBusy(false);
    }
  };

  return (
    <JourneyPage step="Step 1 of 2" title={TOS_HEADING} maxWidth="md">
      <div style={{ marginBottom: 24 }}><TosText /></div>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Stack direction="row" spacing={2}>
        <Button variant="contained" size="large" onClick={accept} disabled={busy}>
          {busy ? "Saving…" : "I agree"}
        </Button>
        <Button onClick={signOut} disabled={busy}>Decline and sign out</Button>
      </Stack>
    </JourneyPage>
  );
}

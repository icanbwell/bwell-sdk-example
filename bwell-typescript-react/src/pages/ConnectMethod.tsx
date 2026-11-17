import { useEffect, useState } from "react";
import { Box, Button, Card, CardActionArea, CardContent, Typography } from "@mui/material";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { useNavigate } from "react-router-dom";
import { JourneyPage, Loading } from "@/components/Page";
import { AboutSmartConnectDialog } from "@/components/ConsentDialogs";
import { useSdk } from "@/auth/SessionContext";
import { isSmartConnectEnabled, startSmartConnect } from "@/sdk/journey";
import { errorMessage } from "@/sdk/results";
import { hasAgreedToSmartConnect } from "@/sdk/smartConnectIntent";

const OPTIONS = [
  { to: "/connect/clear", title: "Find my records automatically", body: "Verify your identity with CLEAR and we'll search the network for providers that have your records.", cta: "Get Started" },
  { to: "/connect/search", title: "Search for a provider", body: "Look up your health system, clinic, lab or pharmacy by name and sign in to its patient portal.", cta: "Search providers" },
];

export default function ConnectMethod() {
  const sdk = useSdk();
  const navigate = useNavigate();
  const [aboutOpen, setAboutOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [enabled, setEnabled] = useState<boolean | null>(null); // null while checking

  // The user already agreed (e.g. they're back from CLEAR): resume without showing the dialog again.
  useEffect(() => { if (hasAgreedToSmartConnect()) navigate("/connect/clear", { replace: true }); }, [navigate]);

  // Checked before the cards render, so "Get Started" never flashes before switching to "Enabled".
  useEffect(() => {
    let cancelled = false;
    isSmartConnectEnabled(sdk).then((v) => !cancelled && setEnabled(v)).catch(() => !cancelled && setEnabled(false));
    return () => { cancelled = true; };
  }, [sdk]);

  const agree = async () => {
    setBusy(true); setError(null);
    try {
      // Either creates the consent now (verified) or leaves the agreement for the next page, which
      // offers "Verify with CLEAR".
      await startSmartConnect(sdk);
      navigate("/connect/clear");
    } catch (e) {
      setError(errorMessage(e));
      setBusy(false);
    }
  };

  if (enabled === null) return <JourneyPage step="Step 2 of 2" title="How would you like to connect?" maxWidth="md"><Loading /></JourneyPage>;

  return (
    <JourneyPage step="Step 2 of 2" title="How would you like to connect?" subtitle="You can always add more sources later." maxWidth="md">
      <Box sx={{ display: "grid", gap: 2, gridTemplateColumns: { sm: "1fr 1fr" } }}>
        {OPTIONS.map((o) => {
          const smartConnectOn = o.to === "/connect/clear" && enabled;
          const content = (
            <CardContent>
              <Typography variant="h6" gutterBottom>{o.title}</Typography>
              <Typography color="text.secondary" sx={{ mb: 2 }}>{o.body}</Typography>
              {smartConnectOn
                ? <Button variant="contained" color="success" component="span" startIcon={<CheckCircleIcon />} sx={{ pointerEvents: "none" }}>Enabled</Button>
                : <Button variant="contained" component="span">{o.cta}</Button>}
            </CardContent>
          );
          // Once Smart Connect is on there's nothing left to click, so the card isn't interactive.
          return (
            <Card key={o.to}>
              {smartConnectOn
                ? content
                : <CardActionArea onClick={() => (o.to === "/connect/clear" ? setAboutOpen(true) : navigate(o.to))} sx={{ height: "100%" }}>{content}</CardActionArea>}
            </Card>
          );
        })}
      </Box>
      <AboutSmartConnectDialog open={aboutOpen} onBack={() => setAboutOpen(false)} onAgree={agree} busy={busy} error={error} />
      <Box sx={{ mt: 3 }}><Button onClick={() => navigate("/app/connections")}>Skip for now</Button></Box>
    </JourneyPage>
  );
}

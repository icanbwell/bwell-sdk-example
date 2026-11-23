import { FormEvent, useEffect, useState } from "react";
import {
  Alert, Box, Button, Card, CardContent, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
  Link, Stack, Switch, TextField, Typography, FormControlLabel,
} from "@mui/material";
import { Marked } from "@/components/Marked";
import { Loading } from "@/components/Page";
import ExternalFlowPanel from "@/components/ExternalFlowPanel";
import { useExternalFlow } from "@/components/useExternalFlow";
import { useSdk, useSession } from "@/auth/SessionContext";
import {
  AboutSmartConnectDialog, AuthorizationDialog, SmartConnectLearnMoreDialog, VerificationRequiredDialog,
} from "@/components/ConsentDialogs";
import {
  deleteAccount, disableSmartConnect, enableSmartConnect, getLatestConsent, getProfile, isPermitted,
  getClearVerificationUrl, getVerificationState, updateProfileName, VerificationRequiredError,
} from "@/sdk/journey";
import { clearSmartConnectAgreement, hasAgreedToSmartConnect, markSmartConnectAgreed } from "@/sdk/smartConnectIntent";
import { errorMessage } from "@/sdk/results";

const Section = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <Card sx={{ mb: 3 }}><CardContent><Typography variant="h5" gutterBottom>{title}</Typography>{children}</CardContent></Card>
);

function ProfileSection() {
  const sdk = useSdk();
  const [first, setFirst] = useState("");
  const [last, setLast] = useState("");
  const [birthDate, setBirthDate] = useState<string | null>(null);
  const [loaded, setLoaded] = useState(false);
  // Saving stays off if the profile didn't load: the empty fields would otherwise overwrite the real name.
  const [loadFailed, setLoadFailed] = useState(false);
  const [msg, setMsg] = useState<{ severity: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    getProfile(sdk).then((p) => {
      const name = p.name?.find((n) => n);
      setFirst(name?.given?.[0] ?? ""); setLast(name?.family ?? ""); setBirthDate(p.birthDate);
    }).catch((e) => { setLoadFailed(true); setMsg({ severity: "error", text: errorMessage(e, "Unable to load your profile.") }); }).finally(() => setLoaded(true));
  }, [sdk]);

  const save = async (e: FormEvent) => {
    e.preventDefault();
    try { await updateProfileName(sdk, first.trim(), last.trim()); setMsg({ severity: "success", text: "Profile saved." }); }
    catch (err) { setMsg({ severity: "error", text: errorMessage(err, "Unable to save your profile.") }); }
  };

  if (!loaded) return <Loading />;
  return (
    <form onSubmit={save}>
      <Stack spacing={2} sx={{ maxWidth: 420 }}>
        <TextField label="First name" value={first} onChange={(e) => setFirst(e.target.value)} />
        <TextField label="Last name" value={last} onChange={(e) => setLast(e.target.value)} />
        <TextField label="Date of birth" value={birthDate ?? ""} disabled helperText="Contact support to change your date of birth." />
        {msg && <Alert severity={msg.severity}>{msg.text}</Alert>}
        <Button type="submit" variant="contained" disabled={loadFailed || (!first.trim() && !last.trim())} sx={{ alignSelf: "flex-start" }}>Save</Button>
      </Stack>
    </form>
  );
}

function ConsentSection() {
  const sdk = useSdk();
  const flow = useExternalFlow();
  const [smart, setSmart] = useState<boolean | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [authOpen, setAuthOpen] = useState(false);
  const [learnOpen, setLearnOpen] = useState(false);
  const [aboutOpen, setAboutOpen] = useState(false);
  const [verifyOpen, setVerifyOpen] = useState(false);
  const [verifyBusy, setVerifyBusy] = useState(false);
  const [verifyError, setVerifyError] = useState<string | null>(null);

  useEffect(() => {
    getLatestConsent(sdk, "ias:import:records").then((c) => setSmart(isPermitted(c))).catch((e) => { setError(errorMessage(e)); setSmart(false); });
  }, [sdk]);

  const turnOff = async () => {
    setError(null);
    try { await disableSmartConnect(sdk); setSmart(false); }
    catch (e) { setError(errorMessage(e, "Unable to update your preference.")); }
  };

  // Agreement is collected first; the consent itself is only created if the user is verified.
  const agreeAndEnable = async () => {
    setAboutOpen(false);
    setError(null);
    try { await enableSmartConnect(sdk); setSmart(true); }
    catch (e) {
      if (e instanceof VerificationRequiredError) { markSmartConnectAgreed(); setVerifyError(null); setVerifyOpen(true); }
      else setError(errorMessage(e, "Unable to update your preference."));
    }
  };

  const verify = async () => {
    setVerifyBusy(true); setVerifyError(null);
    try {
      const mode = await flow.start({
        getUrl: () => getClearVerificationUrl(sdk),
        // Verified: turn Smart Connect on now, as the dialog promised.
        onDone: async () => {
          if ((await getVerificationState(sdk)) !== "validated") return false;
          await enableSmartConnect(sdk);
          clearSmartConnectAgreement();
          setSmart(true);
          setVerifyOpen(false);
        },
        pendingMessage: "We haven't received your verification yet. If you just finished, give it a moment and try again.",
      });
      if (mode === "popup") setVerifyBusy(false); // otherwise the page is being redirected
    } catch (e) { setVerifyError(errorMessage(e, "Unable to start verification.")); setVerifyBusy(false); }
  };

  if (smart === null) return <Loading />;
  return (
    <Stack spacing={1}>
      <Link component="button" type="button" underline="always" onClick={() => setAuthOpen(true)} sx={{ alignSelf: "flex-start" }}>
        Review Data Intermediary Authorization
      </Link>
      <Stack direction="row" alignItems="center" flexWrap="wrap" columnGap={2}>
        <FormControlLabel
          control={<Switch checked={smart} onChange={(e) => (e.target.checked ? (hasAgreedToSmartConnect() ? void agreeAndEnable() : setAboutOpen(true)) : turnOff())} />}
          label={<><Marked text="Smart Connect" /> Enabled</>}
        />
        <Link component="button" type="button" underline="always" onClick={() => setLearnOpen(true)}>Learn more</Link>
      </Stack>
      {error && <Alert severity="error">{error}</Alert>}
      <AuthorizationDialog open={authOpen} onClose={() => setAuthOpen(false)} />
      <SmartConnectLearnMoreDialog open={learnOpen} onClose={() => setLearnOpen(false)} />
      <AboutSmartConnectDialog open={aboutOpen} onBack={() => setAboutOpen(false)} onAgree={agreeAndEnable} />
      <VerificationRequiredDialog
        open={verifyOpen}
        busy={verifyBusy}
        error={verifyError}
        onVerify={verify}
        onClose={() => { flow.cancel(); clearSmartConnectAgreement(); setVerifyOpen(false); }}
        waiting={flow.active ? (
          <ExternalFlowPanel flow={flow} doneLabel="Done verifying"
            message="Finish verifying your identity in the CLEAR window we opened. When you're done, come back here." />
        ) : undefined}
      />
    </Stack>
  );
}

function DeleteSection() {
  const sdk = useSdk();
  const { signOut } = useSession();
  const [open, setOpen] = useState(false);
  const [typed, setTyped] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const run = async () => {
    setBusy(true); setError(null);
    try { await deleteAccount(sdk); signOut(); }
    catch (e) { setError(errorMessage(e, "Unable to delete your account.")); setBusy(false); }
  };

  return (
    <>
      <Typography sx={{ mb: 2 }}>Deleting your account permanently removes your profile, connections and health records. This can't be undone.</Typography>
      <Button color="error" variant="outlined" onClick={() => setOpen(true)}>Delete my account</Button>
      <Dialog open={open} onClose={() => !busy && setOpen(false)}>
        <DialogTitle>Delete your account?</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>Type DELETE to confirm.</DialogContentText>
          <TextField autoFocus fullWidth value={typed} onChange={(e) => setTyped(e.target.value)} />
          {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)} disabled={busy}>Cancel</Button>
          <Button color="error" variant="contained" disabled={typed !== "DELETE" || busy} onClick={run}>{busy ? "Deleting…" : "Delete account"}</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default function Settings() {
  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>Settings</Typography>
      <Section title="Profile"><ProfileSection /></Section>
      <Section title="Consents"><ConsentSection /></Section>
      <Section title="Delete account"><DeleteSection /></Section>
    </Box>
  );
}

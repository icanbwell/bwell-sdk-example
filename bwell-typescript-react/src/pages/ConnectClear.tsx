import { useCallback, useEffect, useState } from "react";
import { Alert, Button, LinearProgress, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { JourneyPage, Loading } from "@/components/Page";
import ConnectList from "@/components/RecommendedConnectList";
import { usePolling } from "@/components/usePolling";
import { useSdk } from "@/auth/SessionContext";
import { AboutSmartConnectDialog } from "@/components/ConsentDialogs";
import ExternalFlowPanel from "@/components/ExternalFlowPanel";
import { useExternalFlow } from "@/components/useExternalFlow";
import {
  enableSmartConnect, getConnections, getLatestConsent, getNetworkSearchState, getOauthRedirectUrl,
  getClearVerificationUrl, getRecommendedConnections, getVerificationState, isPermitted,
  RecommendedConnection, startSmartConnect, TaskState, VerificationState,
} from "@/sdk/journey";
import { clearSmartConnectAgreement, hasAgreedToSmartConnect } from "@/sdk/smartConnectIntent";

const SEARCH_POLL_MS = 15_000;
// Most searches finish within ~90 seconds, so the bar fills over 120 seconds (one step per poll).
const EXPECTED_MS = 120_000;
const POLLS_TO_FULL = EXPECTED_MS / SEARCH_POLL_MS;
import { errorMessage } from "@/sdk/results";

type Phase = "checking" | "agree" | "verify" | "waiting-verification" | "searching" | "results";

export default function ConnectClear() {
  const sdk = useSdk();
  const navigate = useNavigate();
  const flow = useExternalFlow();
  const [phase, setPhase] = useState<Phase>("checking");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [task, setTask] = useState<TaskState>("none");
  const [polls, setPolls] = useState(0);
  const [recs, setRecs] = useState<RecommendedConnection[]>([]);
  const [connected, setConnected] = useState<Set<string>>(new Set());

  const loadResults = useCallback(async () => {
    const [r, c] = await Promise.all([getRecommendedConnections(sdk), getConnections(sdk)]);
    setRecs(r);
    setConnected(new Set(c.map((x) => x.id)));
    setPhase("results");
  }, [sdk]);

  const afterVerified = useCallback(async () => {
    // The consent is only created now that identity verification has succeeded.
    if (!isPermitted(await getLatestConsent(sdk, "ias:import:records"))) await enableSmartConnect(sdk);
    clearSmartConnectAgreement();
    setPhase("searching");
  }, [sdk]);

  const evaluate = useCallback(async (state: VerificationState) => {
    if (state === "validated") await afterVerified();
    else setPhase(state === "in-process" ? "waiting-verification" : "verify");
  }, [afterVerified]);

  const begin = useCallback(async () => {
    setPhase("checking");
    const permitted = isPermitted(await getLatestConsent(sdk, "ias:import:records"));
    // Reached directly without agreeing first (nothing is saved until they're verified).
    if (!permitted && !hasAgreedToSmartConnect()) return setPhase("agree");
    await evaluate(await getVerificationState(sdk));
  }, [sdk, evaluate]);

  useEffect(() => {
    begin().catch((e) => { setError(errorMessage(e)); setPhase("verify"); });
  }, [begin]);

  usePolling(async () => {
    try { await evaluate(await getVerificationState(sdk)); } catch (e) { setError(errorMessage(e)); }
  }, 5000, phase === "waiting-verification");

  const checkSearch = useCallback(async () => {
    try {
      const state = await getNetworkSearchState(sdk);
      setTask(state);
      if (state === "done" || state === "failed") await loadResults();
    } catch (e) { setError(errorMessage(e)); }
  }, [sdk, loadResults]);

  // Check once on arrival, then every 15 seconds until the network search task has finished.
  useEffect(() => { if (phase === "searching") { setPolls(0); void checkSearch(); } }, [phase, checkSearch]);
  usePolling(() => { setPolls((n) => n + 1); return checkSearch(); }, SEARCH_POLL_MS, phase === "searching");

  const agree = async () => {
    setBusy("agree"); setError(null);
    try {
      const next = await startSmartConnect(sdk);
      setBusy(null);
      // "needs-verification" lands on the verify step; opening CLEAR needs its own click (popups do).
      setPhase(next === "searching" ? "searching" : next === "waiting-verification" ? "waiting-verification" : "verify");
    } catch (e) { setError(errorMessage(e)); setBusy(null); }
  };

  const startVerification = async () => {
    setBusy("verify"); setError(null);
    try {
      const mode = await flow.start({
        getUrl: () => getClearVerificationUrl(sdk),
        onDone: async () => {
          const state = await getVerificationState(sdk);
          if (state === "unverified") return false;
          await evaluate(state);
        },
        pendingMessage: "We haven't received your verification yet. Finish in the CLEAR window, then try again.",
      });
      if (mode === "popup") setBusy(null); // otherwise the page is being redirected
    } catch (e) { setError(errorMessage(e)); setBusy(null); }
  };

  // Ways out of the pending-verification screens. The saved agreement is dropped first, otherwise
  // /connect-method would send the user straight back here.
  const searchProvidersInstead = () => { flow.cancel(); clearSmartConnectAgreement(); navigate("/connect/search"); };
  const skipForNow = () => { flow.cancel(); clearSmartConnectAgreement(); navigate("/app/connections"); };
  const exitActions = (
    <Stack direction="row" spacing={2} sx={{ flexWrap: "wrap" }}>
      <Button onClick={searchProvidersInstead}>Search for a provider instead</Button>
      <Button onClick={skipForNow}>Skip for now</Button>
    </Stack>
  );

  const connect = async (connectionId: string) => {
    setBusy(connectionId); setError(null);
    try {
      const mode = await flow.start({ getUrl: () => getOauthRedirectUrl(sdk, connectionId), onDone: loadResults });
      if (mode === "popup") setBusy(null);
    } catch (e) { setError(errorMessage(e)); setBusy(null); }
  };

  return (
    <JourneyPage title="Find my records" subtitle="We'll verify who you are, then search the network for your records." maxWidth="md">
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <AboutSmartConnectDialog
        open={phase === "agree"}
        onBack={() => navigate("/connect-method")}
        onAgree={agree}
        busy={busy === "agree"}
      />
      {phase === "checking" && <Loading label="Checking your verification…" />}
      {phase === "verify" && flow.active && (
        <ExternalFlowPanel flow={flow} doneLabel="Done verifying"
          message="Finish verifying your identity in the CLEAR window we opened. When you're done, come back here." />
      )}
      {phase === "verify" && !flow.active && (
        <Stack spacing={2}>
          <Typography>To protect your health information, we need to confirm your identity with CLEAR. It takes a couple of minutes.</Typography>
          <Button variant="contained" size="large" onClick={startVerification} disabled={busy === "verify"} sx={{ alignSelf: "flex-start" }}>
            {busy === "verify" ? "Opening CLEAR…" : "Verify with CLEAR"}
          </Button>
          {exitActions}
        </Stack>
      )}
      {phase === "waiting-verification" && (
        <Stack spacing={2}>
          <Typography>Your verification is being processed…</Typography>
          <LinearProgress />
          {exitActions}
        </Stack>
      )}
      {phase === "searching" && (
        <Stack spacing={2}>
          <Typography variant="h6">We're searching for your records</Typography>
          <Typography>
            This usually takes a minute or two. We check every 15 seconds and will show what we find as soon as it's
            done, so you can leave this page open.
          </Typography>
          {polls < POLLS_TO_FULL
            ? <LinearProgress variant="determinate" value={(polls / POLLS_TO_FULL) * 100} />
            : <Alert severity="info">Sorry, the network query is taking longer than expected. We're still working on locating and retrieving your data!</Alert>}
          <Button onClick={() => navigate("/connect/search")} sx={{ alignSelf: "flex-start" }}>Search for a provider instead</Button>
        </Stack>
      )}
      {phase === "results" && flow.active && (
        <ExternalFlowPanel flow={flow} doneLabel="Done connecting"
          message="Sign in to your provider in the window we opened. When you're done, come back here." />
      )}
      {phase === "results" && !flow.active && (
        <Stack spacing={2}>
          {task === "failed" && <Alert severity="warning">The network search didn't finish cleanly. You can still connect what was found, or search for a provider.</Alert>}
          {recs.length > 0
            ? <><Typography>These providers may have your records. Sign in to each one to finish connecting.</Typography>
                <ConnectList items={recs} connectedIds={connected} onConnect={connect} busyId={busy} /></>
            : <Typography>No providers need a sign-in right now. Records found automatically will appear on your connections page.</Typography>}
          <Stack direction="row" spacing={2}>
            <Button variant="contained" onClick={() => navigate("/app/connections")}>Go to my connections</Button>
            <Button onClick={() => navigate("/connect/search")}>Search for a provider instead</Button>
          </Stack>
        </Stack>
      )}
    </JourneyPage>
  );
}

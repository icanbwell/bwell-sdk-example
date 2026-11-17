import { useCallback, useEffect, useState } from "react";
import {
  Alert, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogContentText,
  DialogTitle, Stack, Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import type { MemberConnection } from "@icanbwell/bwell-sdk-ts";
import { Loading } from "@/components/Page";
import { useSdk } from "@/auth/SessionContext";
import ConnectList from "@/components/RecommendedConnectList";
import ExternalFlowPanel from "@/components/ExternalFlowPanel";
import { useExternalFlow } from "@/components/useExternalFlow";
import {
  activateDirectConnection, deleteConnection, disconnect, getConnections, isDirectConnection, getOauthRedirectUrl, getRecommendedConnections, RecommendedConnection,
} from "@/sdk/journey";
import { errorMessage } from "@/sdk/results";

const STATUS_LABEL: Record<string, { label: string; color: "success" | "warning" | "error" | "default" }> = {
  CONNECTED: { label: "Connected", color: "success" },
  EXPIRED: { label: "Needs sign-in", color: "warning" },
  DISCONNECTED: { label: "Disconnected", color: "default" },
  ACCESS_ENDED: { label: "Access ended", color: "default" },
  ERROR: { label: "Error", color: "error" },
  DELETED: { label: "Deleted", color: "default" },
};
const SYNC_LABEL: Record<string, string> = {
  PENDING: "Waiting to sync", RETRIEVING: "Syncing…", RETRIEVED: "Up to date", ERROR: "Sync problem",
  DELETING: "Removing data…", DATA_DELETED: "Data removed",
};
const NEEDS_REAUTH = new Set(["EXPIRED", "DISCONNECTED", "ACCESS_ENDED"]);

type Pending = { kind: "disconnect" | "delete"; connection: MemberConnection };

export default function Connections() {
  const sdk = useSdk();
  const navigate = useNavigate();
  const flow = useExternalFlow();
  const [items, setItems] = useState<MemberConnection[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState<Pending | null>(null);
  const [busy, setBusy] = useState(false);
  const [recommended, setRecommended] = useState<RecommendedConnection[]>([]);
  const [signingInTo, setSigningInTo] = useState<string | null>(null);

  const load = useCallback(async () => {
    try { setItems(await getConnections(sdk)); setError(null); }
    catch (e) { setError(errorMessage(e, "Unable to load your connections.")); setItems([]); }
  }, [sdk]);

  // Sources the network search found that still need the user to sign in (OAuth). A failure here
  // shouldn't hide the user's existing connections, so it's swallowed.
  const [recommendedError, setRecommendedError] = useState<string | null>(null);
  const loadRecommended = useCallback(async () => {
    try { setRecommended(await getRecommendedConnections(sdk)); setRecommendedError(null); }
    catch (e) { setRecommended([]); setRecommendedError(errorMessage(e, "Unable to load recommended connections.")); }
  }, [sdk]);
  useEffect(() => { void loadRecommended(); }, [loadRecommended]);

  // After the sign-in popup, pick up the new connection and drop it from the recommendations.
  const refreshAfterSignIn = useCallback(async () => { await Promise.all([load(), loadRecommended()]); }, [load, loadRecommended]);

  const signIn = async (connectionId: string) => {
    setSigningInTo(connectionId);
    try {
      const mode = await flow.start({ getUrl: () => getOauthRedirectUrl(sdk, connectionId), onDone: refreshAfterSignIn });
      if (mode === "popup") setSigningInTo(null);
    } catch (e) { setError(errorMessage(e)); setSigningInTo(null); }
  };

  useEffect(() => { void load(); }, [load]);

  const reconnect = async (c: MemberConnection) => {
    try {
      if (isDirectConnection(c)) { await activateDirectConnection(sdk, c.id); await load(); }
      else await flow.start({ getUrl: () => getOauthRedirectUrl(sdk, c.id), onDone: refreshAfterSignIn });
    } catch (e) { setError(errorMessage(e)); }
  };

  const confirm = async () => {
    if (!pending) return;
    setBusy(true);
    try {
      await (pending.kind === "delete" ? deleteConnection : disconnect)(sdk, pending.connection.id);
      setPending(null);
      await load();
    } catch (e) { setError(errorMessage(e)); setPending(null); }
    finally { setBusy(false); }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }} flexWrap="wrap" gap={2}>
        <Box><Typography variant="h4" component="h1">Connect Records</Typography>
          <Typography color="text.secondary">Manage the sources your health records come from.</Typography></Box>
        <Button variant="contained" onClick={() => navigate("/connect-method")}>Add a connection</Button>
      </Stack>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {flow.active && (
        <Card sx={{ mb: 3 }}><CardContent>
          <ExternalFlowPanel flow={flow} doneLabel="Done connecting"
            message="Sign in to your provider in the window we opened. When you're done, come back here." />
        </CardContent></Card>
      )}
      {items === null && <Loading />}
      {items?.length === 0 && !error && (
        <Card><CardContent><Typography>You haven't connected any records yet.</Typography>
          <Button sx={{ mt: 2 }} variant="contained" onClick={() => navigate("/connect-method")}>Connect your records</Button></CardContent></Card>
      )}
      <Stack spacing={2}>
        {items?.map((c) => {
          const status = (c.status && STATUS_LABEL[c.status]) || { label: c.status ?? "Unknown", color: "default" as const };
          return (
            <Card key={c.id}><CardContent>
              <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={2}>
                <Box>
                  <Typography variant="h6">{c.name}</Typography>
                  <Stack direction="row" spacing={1} sx={{ mt: 0.5 }} alignItems="center">
                    <Chip size="small" label={status.label} color={status.color} />
                    <Typography variant="body2" color="text.secondary">
                      {(c.syncStatus && SYNC_LABEL[c.syncStatus]) || c.syncStatus || ""}
                      {c.lastSynced ? ` · last synced ${new Date(c.lastSynced).toLocaleDateString()}` : ""}
                    </Typography>
                  </Stack>
                </Box>
                <Stack direction="row" spacing={1}>
                  {c.status && NEEDS_REAUTH.has(c.status) && <Button variant="contained" size="small" onClick={() => reconnect(c)}>Reconnect</Button>}
                  {c.status === "CONNECTED" && <Button size="small" onClick={() => setPending({ kind: "disconnect", connection: c })}>Disconnect</Button>}
                  {c.status !== "DELETED" && <Button size="small" color="error" onClick={() => setPending({ kind: "delete", connection: c })}>Delete data</Button>}
                </Stack>
              </Stack>
            </CardContent></Card>
          );
        })}
      </Stack>
      {recommendedError && <Alert severity="warning" sx={{ mt: 3 }}>Recommended connections: {recommendedError}</Alert>}
      {recommended.length > 0 && (
        <Box sx={{ mt: 4 }}>
          <Typography variant="h5" component="h2" gutterBottom>Recommended Connections</Typography>
          <Typography color="text.secondary" sx={{ mb: 1 }}>
            We found these providers may have your records. Sign in to each one to finish connecting.
          </Typography>
          <Card><ConnectList items={recommended} connectedIds={new Set((items ?? []).map((c) => c.id))} onConnect={signIn} busyId={signingInTo} /></Card>
        </Box>
      )}
      <Dialog open={!!pending} onClose={() => !busy && setPending(null)}>
        <DialogTitle>{pending?.kind === "delete" ? "Delete data from this source?" : "Disconnect this source?"}</DialogTitle>
        <DialogContent><DialogContentText>
          {pending?.kind === "delete"
            ? `This removes the records from ${pending.connection.name} and stops future collection. You can reconnect later.`
            : `We'll stop collecting new records from ${pending?.connection.name}. Records already collected stay on your account.`}
        </DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setPending(null)} disabled={busy}>Cancel</Button>
          <Button onClick={confirm} color={pending?.kind === "delete" ? "error" : "primary"} variant="contained" disabled={busy}>
            {pending?.kind === "delete" ? "Delete data" : "Disconnect"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

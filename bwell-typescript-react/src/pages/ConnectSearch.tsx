import { FormEvent, useEffect, useState } from "react";
import { Alert, Avatar, Button, List, ListItem, ListItemAvatar, ListItemText, Stack, TextField, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { JourneyPage } from "@/components/Page";
import ExternalFlowPanel from "@/components/ExternalFlowPanel";
import { useExternalFlow } from "@/components/useExternalFlow";
import { useSdk } from "@/auth/SessionContext";
import { ConnectionSearchResult, getOauthRedirectUrl, getProfile, isZipCode, searchConnections, zipFromProfile } from "@/sdk/journey";
import { errorMessage } from "@/sdk/results";

export default function ConnectSearch() {
  const sdk = useSdk();
  const navigate = useNavigate();
  const flow = useExternalFlow();
  const [term, setTerm] = useState("");
  const [zip, setZip] = useState("");
  const [zipEdited, setZipEdited] = useState(false);
  const [results, setResults] = useState<ConnectionSearchResult[] | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Prefill the zip from the profile address, unless the user has already typed one.
  useEffect(() => {
    getProfile(sdk).then((p) => {
      const fromProfile = zipFromProfile(p);
      if (fromProfile) setZip((current) => (current ? current : fromProfile));
    }).catch(() => { /* optional: the user can still type a zip */ });
  }, [sdk]);

  const zipInvalid = zipEdited && zip !== "" && !isZipCode(zip);

  const search = async (e: FormEvent) => {
    e.preventDefault();
    setBusy("search"); setError(null);
    try { setResults(await searchConnections(sdk, term.trim(), zip)); }
    catch (err) { setError(errorMessage(err, "Search failed.")); }
    finally { setBusy(null); }
  };

  const connect = async (id: string) => {
    setBusy(id); setError(null);
    try {
      const mode = await flow.start({ getUrl: () => getOauthRedirectUrl(sdk, id), onDone: async () => { navigate("/app/connections"); } });
      if (mode === "popup") setBusy(null);
    } catch (err) { setError(errorMessage(err)); setBusy(null); }
  };

  return (
    <JourneyPage title="Search for a provider" subtitle="Find your health system, clinic, lab or pharmacy." maxWidth="md">
      {flow.active && (
        <ExternalFlowPanel flow={flow} doneLabel="Done connecting"
          message="Sign in to your provider in the window we opened. When you're done, come back here." />
      )}
      {!flow.active && <form onSubmit={search}>
        <Stack direction="row" spacing={2}>
          <TextField label="Provider or health system name" value={term} onChange={(e) => setTerm(e.target.value)} fullWidth />
          <TextField
            label="Zip code"
            value={zip}
            onChange={(e) => { setZipEdited(true); setZip(e.target.value.replace(/\D/g, "").slice(0, 5)); }}
            error={zipInvalid}
            helperText={zipInvalid ? "Enter 5 digits" : "Optional"}
            inputProps={{ inputMode: "numeric", autoComplete: "postal-code" }}
            sx={{ width: 140, flexShrink: 0 }}
          />
          <Button type="submit" variant="contained" disabled={!term.trim() || zipInvalid || busy === "search"} sx={{ alignSelf: "flex-start", height: 56 }}>{busy === "search" ? "Searching…" : "Search"}</Button>
        </Stack>
      </form>}
      {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
      {!flow.active && results && results.length === 0 && <Typography sx={{ mt: 3 }} color="text.secondary">No matches. Try a shorter name.</Typography>}
      {!flow.active && results && results.length > 0 && (
        <List sx={{ mt: 2 }}>
          {results.map((r) => (
            <ListItem key={r.id} divider secondaryAction={
              r.connectionId
                ? <Button variant="outlined" size="small" disabled={busy === r.connectionId} onClick={() => connect(r.connectionId!)}>Sign in to connect</Button>
                : <Typography variant="body2" color="text.secondary">Not available yet</Typography>}>
              <ListItemAvatar><Avatar src={r.logoUrl} alt="">{r.name[0]}</Avatar></ListItemAvatar>
              <ListItemText primary={r.name} secondary={r.type} />
            </ListItem>
          ))}
        </List>
      )}
      <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
        <Button onClick={() => navigate("/connect-method")}>Back</Button>
        <Button onClick={() => navigate("/app/connections")}>Go to my connections</Button>
      </Stack>
    </JourneyPage>
  );
}

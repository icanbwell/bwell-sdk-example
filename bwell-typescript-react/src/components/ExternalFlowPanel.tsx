import { Alert, Button, Stack, Typography } from "@mui/material";
import type { ExternalFlow } from "@/components/useExternalFlow";

/** Shown in this tab while the CLEAR / provider popup is open. */
export default function ExternalFlowPanel({ flow, message, doneLabel }: {
  flow: ExternalFlow; message: string; doneLabel: string;
}) {
  return (
    <Stack spacing={2}>
      <Typography>{message}</Typography>
      {flow.notice && <Alert severity="info">{flow.notice}</Alert>}
      <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
        <Button variant="contained" onClick={() => void flow.finish()} disabled={flow.checking}>
          {flow.checking ? "Checking…" : doneLabel}
        </Button>
        <Button onClick={() => void flow.reopen()} disabled={flow.checking}>Reopen window</Button>
        <Button onClick={flow.cancel} disabled={flow.checking}>Cancel</Button>
      </Stack>
    </Stack>
  );
}

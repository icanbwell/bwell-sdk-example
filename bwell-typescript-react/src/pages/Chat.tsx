import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Card, CardContent, LinearProgress, Stack, Typography } from "@mui/material";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { useSdk } from "@/auth/SessionContext";
import { BAILEY_PROMPTS } from "@/config/app";
import { askBailey } from "@/sdk/bailey";
import { errorMessage } from "@/sdk/results";

// Styles the elements the assistant's markdown answers produce.
const MARKDOWN_SX = {
  lineHeight: 1.7,
  "& h1, & h2, & h3, & h4": { fontFamily: "inherit", fontWeight: 700, mt: 2, mb: 1, lineHeight: 1.3 },
  "& h1": { fontSize: "1.5rem" }, "& h2": { fontSize: "1.3rem" }, "& h3, & h4": { fontSize: "1.1rem" },
  "& p": { my: 1 },
  "& ul, & ol": { pl: 3, my: 1 },
  "& li": { mb: 0.5 },
  "& table": { borderCollapse: "collapse", my: 1 },
  "& th, & td": { border: "1px solid", borderColor: "divider", px: 1, py: 0.5, textAlign: "left" },
  "& > :first-child": { mt: 0 },
} as const;

/** Preset questions for the b.well health assistant. No free-form chat. */
export default function Chat() {
  const sdk = useSdk();
  const [prompt, setPrompt] = useState<string | null>(null);
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const abort = useRef<AbortController | null>(null);

  useEffect(() => () => abort.current?.abort(), []); // stop streaming if the user leaves the page

  const ask = async (question: string) => {
    abort.current?.abort();
    const controller = new AbortController();
    abort.current = controller;
    setPrompt(question); setAnswer(""); setError(null); setBusy(true);
    try {
      await askBailey(sdk, question, (text) => setAnswer((a) => a + text), controller.signal);
    } catch (e) {
      if (!controller.signal.aborted) setError(errorMessage(e, "The assistant couldn't answer that."));
    } finally {
      if (abort.current === controller) setBusy(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" component="h1">Chat</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Pick a question and our health assistant will answer using your health records.
      </Typography>
      <Stack direction="row" flexWrap="wrap" gap={1.5} sx={{ mb: 3 }}>
        {BAILEY_PROMPTS.map((p) => (
          <Button key={p} variant={p === prompt ? "contained" : "outlined"} onClick={() => ask(p)} disabled={busy && p !== prompt}>
            {p}
          </Button>
        ))}
      </Stack>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {prompt && (
        <Card>
          <CardContent>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>{prompt}</Typography>
            {busy && !answer && <LinearProgress sx={{ my: 2 }} />}
            <Box sx={MARKDOWN_SX}>
              {/* Raw HTML in the answer is not rendered (react-markdown's default), so this is safe for model output. */}
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{answer}</ReactMarkdown>
            </Box>
            {busy && answer && (
              <Button size="small" sx={{ mt: 2 }} onClick={() => abort.current?.abort()}>Stop</Button>
            )}
          </CardContent>
        </Card>
      )}
      <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 3 }}>
        This is an AI assistant and can make mistakes. It doesn't replace advice from your care team.
      </Typography>
    </Box>
  );
}

import { useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Container,
  Divider,
  Typography,
} from "@mui/material";
import { GetTasksRequest } from "@icanbwell/bwell-sdk-ts";
import { getSdk } from "@/sdk/bWellSdk";

const ACTIVITY_TYPE = "https://www.icanbwell.com/activityType";
const PERFORMER_TYPE = "https://www.icanbwell.com/performerType";

// helpers

function extractTasks(data: any): any[] {
  // Doc pattern: guard with Array.isArray before mapping
  const entries = Array.isArray(data?.entry) ? data.entry : [];
  return entries.map((e: any) => e?.resource).filter(Boolean);
}

function summarizeTask(t: any): string {
  const id = t?.id ?? "(no id)";
  const status = t?.status ?? "(no status)";
  const code = t?.code?.coding?.[0]?.code ?? "(no code)";
  const desc = t?.description ?? "";
  return `${id}  status=${status}  code=${code}  ${desc}`.trim();
}

// Doc "Reading errors from a failed task" block
function readTaskErrors(task: any): string[] {
  const errors = task?.output ?? [];
  return errors
    .map((o: any) => ({
      code: o?.valueCodeableConcept?.coding?.[0]?.code,
      display: o?.valueCodeableConcept?.coding?.[0]?.display,
      text: o?.valueString,
    }))
    .filter((e: any) => e.code || e.text)
    .map(
      (e: any) =>
        `code=${e.code ?? "-"}  display=${e.display ?? "-"}  text=${e.text ?? "-"}`
    );
}

type Scenario =
  | "all"
  | "type-single"
  | "type-multi"
  | "status-single"
  | "status-multi"
  | "ias"
  | "paginate";

const SCENARIOS: { id: Scenario; label: string; description: string }[] = [
  {
    id: "all",
    label: "Get All Tasks",
    description: "GetTasksRequest({ page: 0 }) — excludes system tasks by default",
  },
  {
    id: "type-single",
    label: "Filter by Type (single)",
    description: "code: { value: { system, code: 'care-need' } }",
  },
  {
    id: "type-multi",
    label: "Filter by Type (multiple)",
    description: "code: { values: [care-need, health-activity] }",
  },
  {
    id: "status-single",
    label: "Filter by Status (single)",
    description: "status: { value: { code: 'in-progress' } }",
  },
  {
    id: "status-multi",
    label: "Filter by Status (multiple)",
    description: "status: { values: [{ code: 'ready' }, { code: 'in-progress' }] }",
  },
  {
    id: "ias",
    label: "IAS Network Retrieval",
    description:
      "code: network-data-retrieval + performer.values override (includes system tasks)",
  },
  {
    id: "paginate",
    label: "Paginate Results",
    description: "page: 0, pageSize: 10 — reads result.data.total",
  },
];

const GetTasksSdkTest = () => {
  const [loading, setLoading] = useState(false);
  const [activeScenario, setActiveScenario] = useState<Scenario | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [logLines, setLogLines] = useState<string[]>([]);
  const [tasks, setTasks] = useState<any[]>([]);
  const [totalCount, setTotalCount] = useState<number | null>(null);

  const sdk = getSdk();
  const isReady = sdk !== undefined;

  const logText = useMemo(() => logLines.join("\n"), [logLines]);

  const appendLog = (line: string) => {
    setLogLines((prev) => [
      ...prev,
      `[${new Date().toISOString()}] ${line}`,
    ]);
  };

  const reset = () => {
    setError(null);
    setLogLines([]);
    setTasks([]);
    setTotalCount(null);
    setActiveScenario(null);
  };

  const buildRequest = (scenario: Scenario): GetTasksRequest => {
    switch (scenario) {
      // Doc block 1: Get all tasks
      case "all":
        return new GetTasksRequest({ page: 0 });

      // Doc block 2: Filter by task type (single)
      // FIX APPLIED: doc omits `page` but PagedRequestInput requires it (page: number)
      case "type-single":
        return new GetTasksRequest({
          page: 0,
          code: {
            value: {
              system: ACTIVITY_TYPE,
              code: "care-need",
            },
          },
        });

      // Doc block 3: Filter by task type (multiple)
      // FIX APPLIED: doc omits `page` but PagedRequestInput requires it
      case "type-multi":
        return new GetTasksRequest({
          page: 0,
          code: {
            values: [
              { system: ACTIVITY_TYPE, code: "care-need" },
              { system: ACTIVITY_TYPE, code: "health-activity" },
            ],
          },
        });

      // Doc block 4: Filter by status (single)
      // FIX APPLIED: doc omits `page` but PagedRequestInput requires it
      case "status-single":
        return new GetTasksRequest({
          page: 0,
          status: {
            value: { code: "in-progress" },
          },
        });

      // Doc block 5: Filter by status (multiple)
      // FIX APPLIED: doc omits `page` but PagedRequestInput requires it
      case "status-multi":
        return new GetTasksRequest({
          page: 0,
          status: {
            values: [{ code: "ready" }, { code: "in-progress" }],
          },
        });

      // Doc block 6: IAS network retrieval
      // FIX APPLIED: doc omits `page` but PagedRequestInput requires it
      case "ias":
        return new GetTasksRequest({
          page: 0,
          code: {
            value: {
              system: ACTIVITY_TYPE,
              code: "network-data-retrieval",
            },
          },
          performer: {
            values: [{ system: PERFORMER_TYPE, code: "system" }],
          },
        });

      // Doc block 7: Paginate
      case "paginate":
        return new GetTasksRequest({ page: 0, pageSize: 10 });
    }
  };

  const runScenario = async (scenario: Scenario) => {
    const currentSdk = getSdk();
    if (!currentSdk) {
      setError("SDK not initialized. Go to / and initialize + log in first.");
      return;
    }

    setLoading(true);
    setActiveScenario(scenario);
    setError(null);
    setTasks([]);
    setTotalCount(null);
    setLogLines([]);

    try {
      const request = buildRequest(scenario);
      appendLog(`Calling sdk.activity.getTasks() — scenario: "${scenario}"`);
      const result = await currentSdk.activity.getTasks(request);

      // Doc pattern: branch on result.hasError() — not result.success()
      if (result.hasError()) {
        const err = result.error;
        appendLog("getTasks returned an error");
        setError(typeof err === "string" ? err : JSON.stringify(err, null, 2));
        return;
      }

      // Doc pattern: Array.isArray guard before mapping entry
      const extracted = extractTasks(result.data);
      setTasks(extracted);

      if (scenario === "paginate") {
        // Doc block 7: read result.data.total
        const total = (result.data as any)?.total ?? 0;
        setTotalCount(total);
        const pages = Math.ceil(total / 10) || 1;
        appendLog(
          `Page 1 of ${pages}: ${extracted.length} task(s) (${total} total)`
        );
      } else if (scenario === "ias") {
        // Doc block 6: IAS-specific fields
        const [iasTask] = extracted;
        if (!iasTask) {
          appendLog(
            "No IAS task found — user has not yet provided IAS consent."
          );
        } else {
          const status = iasTask.status;
          const label =
            iasTask.businessStatus?.coding?.[0]?.display ?? "(no label)";
          appendLog(`IAS status: ${status} (${label})`);

          if (status === "failed") {
            // Doc "Reading errors from a failed task" block
            const errLines = readTaskErrors(iasTask);
            appendLog(
              `IAS retrieval errors: ${
                errLines.length > 0 ? errLines.join(" | ") : "(none)"
              }`
            );
          }
        }
      } else {
        appendLog(`Found ${extracted.length} task(s)`);
        extracted.forEach((t) => appendLog(summarizeTask(t)));
      }
    } catch (e: any) {
      console.error("GetTasksSdkTest threw:", e);
      setError(e?.message ?? "Unexpected error");
      appendLog(`Threw: ${e?.message ?? String(e)}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        getTasks SDK Test
      </Typography>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Tests every code block in the <em>getTasks — TypeScript SDK</em> doc.
        Uses the shared SDK instance — initialize and log in at{" "}
        <a href="/initialize">/initialize</a> before running scenarios.
      </Typography>

      {!isReady && (
        <Box mt={2} mb={2}>
          <Alert severity="warning">
            SDK not initialized. Go to{" "}
            <a href="/initialize">/initialize</a>, enter your client key and
            log in, then come back here.
          </Alert>
        </Box>
      )}

      {isReady && (
        <Box mt={1} mb={2}>
          <Alert severity="success">SDK is initialized and ready.</Alert>
        </Box>
      )}

      <Typography variant="h6" gutterBottom>
        Scenarios
      </Typography>
      <Box display="flex" flexDirection="column" gap={1.5} mb={3}>
        {SCENARIOS.map(({ id, label, description }) => (
          <Box key={id} display="flex" alignItems="center" gap={2}>
            <Button
              variant={activeScenario === id ? "contained" : "outlined"}
              onClick={() => runScenario(id)}
              disabled={!isReady || loading}
              sx={{ minWidth: 230, justifyContent: "flex-start" }}
            >
              {loading && activeScenario === id ? "Running..." : label}
            </Button>
            <Typography variant="body2" color="text.secondary">
              {description}
            </Typography>
          </Box>
        ))}
      </Box>

      <Button variant="text" onClick={reset} disabled={loading}>
        Clear
      </Button>

      <Divider sx={{ my: 3 }} />

      {error && (
        <Box mb={2}>
          <Alert severity="error" sx={{ whiteSpace: "pre-wrap" }}>
            {error}
          </Alert>
        </Box>
      )}

      {!error && tasks.length > 0 && (
        <Box mb={2}>
          <Alert severity="success">
            {totalCount !== null
              ? `Page returned ${tasks.length} task(s) — ${totalCount} total`
              : `Found ${tasks.length} task(s)`}
          </Alert>
        </Box>
      )}
      {!error && tasks.length === 0 && logLines.length > 0 && !loading && (
        <Box mb={2}>
          <Alert severity="info">0 tasks returned (may be expected)</Alert>
        </Box>
      )}

      <Typography variant="h6" gutterBottom>
        Log
      </Typography>
      <pre
        style={{ whiteSpace: "pre-wrap", wordBreak: "break-word", fontSize: 13 }}
      >
        {logText || "(no log yet)"}
      </pre>

      <Box mt={3}>
        <Typography variant="h6" gutterBottom>
          Raw Task Data
        </Typography>
        <pre
          style={{ whiteSpace: "pre-wrap", wordBreak: "break-word", fontSize: 12 }}
        >
          {tasks.length > 0 ? JSON.stringify(tasks, null, 2) : "(none)"}
        </pre>
      </Box>
    </Container>
  );
};

export default GetTasksSdkTest;

import { Fragment, useEffect, useState } from "react";
import {
  Alert, Box, Chip, Collapse, IconButton, Paper, Tab, Table, TableBody, TableCell, TableContainer, TableHead,
  TablePagination, TableRow, Tabs, ToggleButton, ToggleButtonGroup, Typography,
} from "@mui/material";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import { Link, Navigate, Outlet, useLocation, useParams } from "react-router-dom";
import { Loading } from "@/components/Page";
import { useSdk } from "@/auth/SessionContext";
import { HEALTH_DATA_TYPES, HEALTH_EXTRA_TABS, HealthDataTypeId } from "@/config/app";
import { fetchHealthPage, HealthPage, HealthRow, HealthView } from "@/sdk/healthData";
import { errorMessage } from "@/sdk/results";

const TABS = [
  ...HEALTH_DATA_TYPES.map((t) => ({ id: t.id, label: t.label, to: `/app/health/${t.id}` })),
  ...HEALTH_EXTRA_TABS,
];

/** Layout for "View Health Data": one tab per data type, plus any extra sub-pages (e.g. Share my records in v2). */
export function HealthLayout() {
  const { pathname } = useLocation();
  const current = TABS.find((t) => pathname.startsWith(t.to))?.to ?? false;
  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>View Health Data</Typography>
      <Tabs value={current} variant="scrollable" scrollButtons="auto" aria-label="Health data types" sx={{ mb: 3 }}>
        {TABS.map((t) => <Tab key={t.id} value={t.to} label={t.label} component={Link} to={t.to} />)}
      </Tabs>
      <Outlet />
    </Box>
  );
}

export const HealthIndexRedirect = () => <Navigate to={`/app/health/${HEALTH_DATA_TYPES[0].id}`} replace />;

const VIEW_HELP: Record<HealthView, string> = {
  refined: "Records from all your sources, combined and de-duplicated by b.well.",
  raw: "The individual records exactly as each source provided them.",
};

const formatDate = (d?: string) => {
  if (!d) return "";
  const date = new Date(d);
  return Number.isNaN(date.getTime()) ? d : date.toLocaleDateString();
};

function Row({ row, view }: { row: HealthRow; view: HealthView }) {
  const [open, setOpen] = useState(false);
  return (
    <Fragment>
      <TableRow hover>
        <TableCell padding="checkbox">
          <IconButton size="small" aria-label={open ? "Hide details" : "Show details"} aria-expanded={open} onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell sx={{ fontWeight: 600 }}>{row.title}</TableCell>
        <TableCell sx={{ whiteSpace: "nowrap" }}>{formatDate(row.date)}</TableCell>
        <TableCell>{row.detail}</TableCell>
        {view === "refined" && (
          <TableCell align="right">{row.recordCount !== undefined && <Chip size="small" label={`${row.recordCount} ${row.recordCount === 1 ? "record" : "records"}`} />}</TableCell>
        )}
      </TableRow>
      <TableRow>
        <TableCell colSpan={view === "refined" ? 5 : 4} sx={{ py: 0, borderBottom: open ? undefined : "none" }}>
          <Collapse in={open} unmountOnExit>
            <Box component="pre" sx={{ m: 2, p: 2, bgcolor: "background.default", borderRadius: 2, overflow: "auto", maxHeight: 360, fontSize: 12 }}>
              {JSON.stringify(row.json, null, 2)}
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </Fragment>
  );
}

export function HealthDataPage() {
  const sdk = useSdk();
  const { type } = useParams();
  const known = HEALTH_DATA_TYPES.find((t) => t.id === type);
  const [view, setView] = useState<HealthView>("refined");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [data, setData] = useState<HealthPage | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Each data type starts on the refined view, first page.
  useEffect(() => { setView("refined"); setPage(0); }, [type]);
  useEffect(() => { setPage(0); }, [view, pageSize]);

  useEffect(() => {
    if (!known) return;
    let cancelled = false;
    setData(null);
    setError(null);
    fetchHealthPage(sdk, known.id as HealthDataTypeId, view, { page, pageSize })
      .then((d) => {
        if (cancelled) return;
        // Paged past the end (the total was unknown and the last full page was the final one).
        if (d.rows.length === 0 && page > 0) setPage(page - 1);
        else setData(d);
      })
      .catch((e) => { if (!cancelled) { setError(errorMessage(e, `Unable to load your ${known.label.toLowerCase()}.`)); setData({ rows: [], total: 0 }); } });
    return () => { cancelled = true; };
  }, [sdk, known, view, page, pageSize]);

  if (!known) return <Navigate to="/app/health" replace />;

  // Some responses don't report a total. A short page then tells us we're at the end (so the total is
  // exact); a full page means there may be more, which MUI shows as "of more than N".
  const count = !data ? 0
    : data.total !== null ? data.total
    : data.rows.length < pageSize ? page * pageSize + data.rows.length
    : -1;

  return (
    <Box>
      <Box sx={{ display: "flex", alignItems: "center", gap: 2, flexWrap: "wrap", mb: 1 }}>
        <Typography variant="h5" component="h2" sx={{ flex: 1 }}>{known.label}</Typography>
        <ToggleButtonGroup exclusive size="small" color="primary" value={view} aria-label="Data view"
          onChange={(_e, v: HealthView | null) => v && setView(v)}>
          <ToggleButton value="refined">Refined data</ToggleButton>
          <ToggleButton value="raw">Raw data</ToggleButton>
        </ToggleButtonGroup>
      </Box>
      <Typography color="text.secondary" sx={{ mb: 2 }}>{VIEW_HELP[view]}</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {data?.warning && <Alert severity="warning" sx={{ mb: 2 }}>Some records may be missing: {data.warning}</Alert>}
      {!data && <Loading />}
      {data && !error && data.rows.length === 0 && (
        <Alert severity="info">No {known.label.toLowerCase()} yet. Once a connection finishes syncing, they'll show up here.</Alert>
      )}
      {data && data.rows.length > 0 && (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small" aria-label={`${known.label} (${view})`}>
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox" />
                  <TableCell>{view === "refined" ? "Name" : "Record"}</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>{view === "refined" ? "Details & sources" : "Details"}</TableCell>
                  {view === "refined" && <TableCell align="right">Merged from</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>{data.rows.map((r) => <Row key={r.id} row={r} view={view} />)}</TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={count}
            page={page}
            rowsPerPage={pageSize}
            rowsPerPageOptions={[10, 25, 50]}
            onPageChange={(_e, p) => setPage(p)}
            onRowsPerPageChange={(e) => setPageSize(Number(e.target.value))}
          />
        </Paper>
      )}
    </Box>
  );
}

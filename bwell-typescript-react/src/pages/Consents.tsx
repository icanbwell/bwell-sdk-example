import { useEffect, useMemo, useState } from "react";
import {
    Container,
    Box,
    Button,
    Alert,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    SelectChangeEvent,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import withAuthCheck from "@/components/withAuthCheck";
import {
    createConsent,
    getConsents,
    getErrorMessage,
    serializeForDebug,
    type SdkCallResult,
} from "@/sdk/consents";
import {
    categoryCodeValues,
    type CategoryCode,
    type ConsentProvisionType,
} from "@icanbwell/bwell-sdk-ts";

type ConsentRow = {
    id: string;
    resourceId: string;
    status: string;
    category: string;
    provisionType: string;
    dateTime: string;
    patientReference: string;
    versionId: string;
    organizationReference: string;
    raw: unknown;
};

function mapConsentBundleToRows(payload: any): ConsentRow[] {
    console.log("[consents page] mapConsentBundleToRows.payload =", payload);

    const entries = Array.isArray(payload?.entry) ? payload.entry : [];
    console.log("[consents page] mapConsentBundleToRows.entries =", entries);

    return entries.map((entry: any, index: number) => {
        const resource = entry?.resource ?? {};
        const firstCategoryCoding = resource?.category?.[0]?.coding?.[0];
        const organizationReference =
            resource?.organization?.[0]?.reference ??
            resource?.performer?.[0]?.reference ??
            "";

        const row: ConsentRow = {
            id: entry?.id ?? resource?.id ?? `consent-${index}`,
            resourceId: resource?.id ?? "",
            status: resource?.status ?? "",
            category: firstCategoryCoding?.code ?? "",
            provisionType: resource?.provision?.type ?? "",
            dateTime:
                resource?.meta?.lastUpdated ??
                resource?.dateTime ??
                resource?.provision?.period?.start ??
                "",
            patientReference: resource?.patient?.reference ?? "",
            versionId: resource?.meta?.versionId ?? "",
            organizationReference,
            raw: entry,
        };

        console.log(`[consents page] mapConsentBundleToRows.row[${index}] =`, row);

        return row;
    });
}

const Consents = () => {
    const [consents, setConsents] = useState<ConsentRow[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [rawResponse, setRawResponse] = useState<any>(null);
    const [category, setCategory] = useState<CategoryCode>("TOS");
    const [provision, setProvision] = useState<ConsentProvisionType>("PERMIT");
    const [organizationId, setOrganizationId] = useState("");

    const columns = useMemo<GridColDef<ConsentRow>[]>(
        () => [
            { field: "resourceId", headerName: "Consent ID", width: 250 },
            { field: "status", headerName: "Status", width: 120 },
            { field: "category", headerName: "Category", width: 160 },
            { field: "provisionType", headerName: "Provision Type", width: 150 },
            { field: "dateTime", headerName: "Date Time", width: 220 },
            { field: "patientReference", headerName: "Patient", width: 220 },
            { field: "versionId", headerName: "Version", width: 100 },
            { field: "organizationReference", headerName: "Organization", width: 220 },
        ],
        []
    );

    const setDebugResponse = (label: string, value: unknown) => {
        const serialized = serializeForDebug({
            label,
            at: new Date().toISOString(),
            value,
        });

        console.log(`[consents page] ${label}.serialized =`, serialized);
        setRawResponse(serialized);
    };

    const loadConsents = async () => {
        console.log("[consents page] loadConsents.start");

        setLoading(true);
        setError(null);

        try {
            const response: SdkCallResult<any> = await getConsents();

            console.log("[consents page] loadConsents.response =", response);
            console.log("[consents page] loadConsents.response.ok =", response?.ok);
            console.log("[consents page] loadConsents.response.data =", response?.data);
            console.log("[consents page] loadConsents.response.error =", response?.error);
            console.log("[consents page] loadConsents.response.shape =", response?.shape);

            setDebugResponse("getConsents", response);

            if (!response?.ok) {
                const message = getErrorMessage(response?.error);
                console.error("[consents page] loadConsents.errorMessage =", message);
                setError(message);
                setConsents([]);
                return;
            }

            const rows = mapConsentBundleToRows(response.data);

            console.log("[consents page] loadConsents.rows =", rows);

            setConsents(rows);
        } catch (err: any) {
            console.error("[consents page] loadConsents.threw =", err);
            setError(err?.message ?? "Unexpected error");
            setConsents([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadConsents();
    }, []);

    const onCreateConsent = async () => {
        console.log("[consents page] onCreateConsent.start", {
            category,
            provision,
            organizationId,
        });

        setError(null);
        setSuccess(null);

        const trimmedOrganizationId = organizationId.trim();

        const response = await createConsent({
            category,
            provision,
            organizationId: trimmedOrganizationId || undefined,
        });

        console.log("[consents page] onCreateConsent.response =", response);
        console.log("[consents page] onCreateConsent.response.ok =", response?.ok);
        console.log("[consents page] onCreateConsent.response.data =", response?.data);
        console.log("[consents page] onCreateConsent.response.error =", response?.error);
        console.log("[consents page] onCreateConsent.response.shape =", response?.shape);

        setDebugResponse("createConsent", response);

        if (!response?.ok) {
            const message = getErrorMessage(response?.error);
            console.error("[consents page] onCreateConsent.errorMessage =", message);
            setError(message);
            return;
        }

        setSuccess("Consent created successfully.");
        await loadConsents();
    };

    return (
        <Container>
            <h1>Consents</h1>

            <Box display="flex" gap={2} mb={2} alignItems="center" flexWrap="wrap">
                <FormControl sx={{ minWidth: 200 }}>
                    <InputLabel id="category-label">Category</InputLabel>
                    <Select
                        labelId="category-label"
                        value={category}
                        label="Category"
                        onChange={(event: SelectChangeEvent) =>
                            setCategory(event.target.value as CategoryCode)
                        }
                    >
                        {categoryCodeValues.map((value) => (
                            <MenuItem key={value} value={value}>
                                {value}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>

                <FormControl sx={{ minWidth: 200 }}>
                    <InputLabel id="provision-label">Provision</InputLabel>
                    <Select
                        labelId="provision-label"
                        value={provision}
                        label="Provision"
                        onChange={(event: SelectChangeEvent) =>
                            setProvision(event.target.value as ConsentProvisionType)
                        }
                    >
                        <MenuItem value="PERMIT">PERMIT</MenuItem>
                        <MenuItem value="DENY">DENY</MenuItem>
                    </Select>
                </FormControl>

                <Button variant="contained" onClick={onCreateConsent}>
                    Create Consent
                </Button>

                <Button variant="outlined" onClick={loadConsents}>
                    Refresh
                </Button>
            </Box>

            {error && (
                <Box mb={2}>
                    <Alert severity="error">{error}</Alert>
                </Box>
            )}

            {success && (
                <Box mb={2}>
                    <Alert severity="success">{success}</Alert>
                </Box>
            )}

            {loading && <div>Loading consents...</div>}

            {!loading && consents.length === 0 && !error && (
                <div>No consents found for this user.</div>
            )}

            {!loading && consents.length > 0 && (
                <Box sx={{ height: 500, width: "100%" }}>
                    <DataGrid
                        rows={consents}
                        columns={columns}
                        getRowId={(row) => row.id}
                        disableRowSelectionOnClick
                    />
                </Box>
            )}

            <Box mt={3}>
                <h2>Raw Response</h2>
                <pre style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                    {JSON.stringify(rawResponse, null, 2)}
                </pre>
            </Box>
        </Container>
    );
};

export default withAuthCheck("Consents", Consents);

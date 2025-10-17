import { useState, useRef } from "react";
import { getSdk } from "@/sdk/bWellSdk";
import { Container, TextField, Button, Box } from "@mui/material";
import { DataGrid, useGridApiRef } from "@mui/x-data-grid";
import { SearchHealthResourcesRequest, DataSourceRequest } from "@icanbwell/bwell-sdk-ts";
import { useSelector } from "react-redux";
import { RootState } from "@/store/store";

const SearchPage = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const apiRef = useGridApiRef();
  const memberConnectionsRaw = useSelector((state: RootState) => state.connections.memberConnections);
  const memberConnections = (memberConnectionsRaw && typeof memberConnectionsRaw === 'object' && 'data' in (memberConnectionsRaw as any) && Array.isArray((memberConnectionsRaw as any).data))
    ? (memberConnectionsRaw as any).data
    : [];

  const handleConnect = async (endpointName: string) => {
    try {
      const sdk = getSdk();
      if (!sdk) throw new Error("SDK not initialized");
      const req = new DataSourceRequest({ connectionId: endpointName });
      const response = await sdk.connection.getOauthUrl(req);
      const url = response?.data?.redirectUrl;
      if (url) {
        window.open(url, "_blank");
      }
    } catch (err: any) {
      // Optionally handle error
    }
  };

  const columns = [
    { field: "content", headerName: "Health Record Source", width: 450 },
    { field: "type", headerName: "Type", width: 160 },
    {
      field: "connect",
      headerName: "Connect",
      width: 200,
      renderCell: (params: any) => {
        const endpoints = params.row.endpoint;
        const connectionId = Array.isArray(endpoints) && endpoints.length > 0 && endpoints[0]?.name;
        const alreadyConnected = memberConnections.some((conn: any) => conn.id === connectionId);
        if (connectionId) {
          if (alreadyConnected) {
            return <span>Already Connected</span>;
          }
          return (
            <Button
              variant="outlined"
              size="small"
              onClick={() => handleConnect(connectionId)}
            >
              Connect
            </Button>
          );
        }
        return null;
      }
    }
  ];

  const handleSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const sdk = getSdk();
      if (!sdk) throw new Error("SDK not initialized");
      const input = new SearchHealthResourcesRequest({
        search: searchTerm,
        page: 0,
        pageSize: 10,
        // Optionally add searchLocation, filters, orderBy, etc.
      });
      const response = await sdk.search.searchHealthResources(input);
      setResults(response?.data?.results || []);
      // Scroll DataGrid to top row
      setTimeout(() => {
        apiRef.current.scrollToIndexes({ rowIndex: 0 });
      }, 0);
    } catch (err: any) {
      setError(err?.message || "Search failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <h1>Search Health Resources</h1>
      <Box display="flex" gap={2} mb={2}>
        <TextField
          label="Search Term"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          onKeyDown={e => {
            if (e.key === "Enter") {
              handleSearch();
            }
          }}
          fullWidth
        />
        <Button variant="contained" onClick={handleSearch} disabled={loading}>
          {loading ? "Searching..." : "Search"}
        </Button>
      </Box>
      {error && <Box color="red">{error}</Box>}
      <div style={{ height: 400, width: "100%", overflow: "auto" }}>
        <DataGrid
          rows={results}
          columns={columns}
          getRowId={row => row.id || row.resourceType + row.display}
          apiRef={apiRef}
        />
      </div>
    </Container>
  );
};

export default SearchPage;

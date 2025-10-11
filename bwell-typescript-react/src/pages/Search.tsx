import { useState, useRef } from "react";
import { getSdk } from "@/sdk/bWellSdk";
import { Container, TextField, Button, Box } from "@mui/material";
import { DataGrid, useGridApiRef } from "@mui/x-data-grid";
import { SearchHealthResourcesRequest } from "@icanbwell/bwell-sdk-ts";

const columns = [
  { field: "content", headerName: "Content", width: 300 },
  { field: "type", headerName: "Type", width: 160 }
];

const SearchPage = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const apiRef = useGridApiRef();

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

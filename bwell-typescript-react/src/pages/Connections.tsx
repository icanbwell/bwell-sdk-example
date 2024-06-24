import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/store/store";
import { Alert, Box, Button, Container } from "@mui/material";
import { getMemberConnections } from "@/store/connectionSlice";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { DataGrid } from "@mui/x-data-grid";

export const Connections = () => {
    const dispatch = useDispatch<AppDispatch>();

    const handleGetMemberConnections = () => {
        dispatch(getMemberConnections());
    };

    const connectionSlice = useSelector((state: RootState) => state.connection);
    const { memberConnections, connectionsError, connectionsLoading } = connectionSlice;

    const showMemberConnectionsTable = useSelector((state: RootState) => state.tableOrJsonToggle.memberConnections);

    const isLoggedIn = useSelector((state: RootState) => state.user.isLoggedIn);

    const CONNECTION_COLUMNS = [
        { field: 'id', headerName: 'ID' },
        { field: 'name', headerName: 'Name' },
        { field: 'category', headerName: 'Category' },
        { field: 'type', headerName: 'Type' },
        { field: 'status', headerName: 'Status' },
        { field: 'syncStatus', headerName: 'SyncStatus' },
        { field: 'statusUpdated', headerName: 'Status Updated', type: 'dateTime', valueGetter: (params) => new Date(params) },
        { field: 'lastSynced', headerName: 'Last Synced', type: 'dateTime', valueGetter: (params) => new Date(params) },
        { field: 'created', headerName: 'Created', type: 'dateTime', valueGetter: (params) => new Date(params) },
        { field: 'isDirect', headerName: 'Is Direct', type: 'boolean' },
    ];

    if (!isLoggedIn) {
        return (
            <Container>
                <h1>Connections</h1>
                <p>Please log in to view this page</p>
            </Container>
        );
    }

    return (
        <Container>
            <h1>Connections</h1>
            <h2>getConnections()</h2>
            <Box sx={{ padding: '5px' }}>
                <Button variant="contained" onClick={handleGetMemberConnections} disabled={connectionsLoading}>Get Connections</Button>
                {connectionsError && <Alert severity="error">{connectionsError}</Alert>}
                {connectionsLoading && <p>Loading...</p>}
            </Box>
            {memberConnections && <TableOrJsonToggle locator="memberConnections" />}
            {showMemberConnectionsTable && memberConnections &&
                <DataGrid rows={memberConnections?.data} columns={CONNECTION_COLUMNS} />
            }
            {!showMemberConnectionsTable && memberConnections &&
                <Box>
                    <pre>{JSON.stringify(memberConnections, null, 2)}</pre>
                </Box>
            }
            <h2>getDataSource()</h2>
        </Container>
    );
}
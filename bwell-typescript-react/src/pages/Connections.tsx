import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/store/store";
import { Alert, Box, Button, Container } from "@mui/material";
import { getMemberConnections } from "@/store/connectionSlice";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { DataGrid } from "@mui/x-data-grid";
import { CONNECTION_COLUMNS } from "@/column-defs";

export const Connections = () => {
    const dispatch = useDispatch<AppDispatch>();

    const handleGetMemberConnections = () => {
        dispatch(getMemberConnections());
    };

    const connectionSlice = useSelector((state: RootState) => state.connection);
    const { memberConnections, connectionsError, connectionsLoading } = connectionSlice;

    const showMemberConnectionsTable = useSelector((state: RootState) => state.tableOrJsonToggle.memberConnections);

    const isLoggedIn = useSelector((state: RootState) => state.user.isLoggedIn);

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
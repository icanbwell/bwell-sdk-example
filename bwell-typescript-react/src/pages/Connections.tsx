import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/store/store";
import { Alert, Box, Container } from "@mui/material";
import { getMemberConnections } from "@/store/connectionSlice";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { DataGrid } from "@mui/x-data-grid";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { useEffect } from "react";

const Connections = () => {
    const dispatch = useDispatch<AppDispatch>();

    const handleGetMemberConnections = () => {
        dispatch(getMemberConnections());
    };

    useEffect(handleGetMemberConnections, []);

    const connectionSlice = useSelector((state: RootState) => state.connection);
    const { memberConnections, connectionsError, connectionsLoading } = connectionSlice;

    const showMemberConnectionsTable = useSelector((state: RootState) => state.tableOrJsonToggle.memberConnections);

    return (
        <Container>
            <h1>Connections</h1>
            <h2>getConnections()</h2>
            <Box sx={{ padding: '5px' }}>
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

export default withAuthCheck('Connections', Connections);
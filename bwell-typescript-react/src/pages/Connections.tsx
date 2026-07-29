import React from "react";
import { Box, Container, Button } from "@mui/material";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { RootState } from "@/store/store";
import { useSelector, useDispatch } from "react-redux";
import { AppDispatch } from "@/store/store";
import { DataGrid } from "@mui/x-data-grid";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { getMemberConnections, getDataSource, connectionSlice } from "@/store/connectionSlice";
import { deleteConnectionById } from "@/sdk/deleteConnection";

const ManageConnections = () => {
    const dispatch = useDispatch<AppDispatch>();
    // Fetch member connections on every page load, and clear any stale data source
    // from a previous session (redux-persist rehydrates this slice on load).
    React.useEffect(() => {
        dispatch(connectionSlice.actions.resetState());
        dispatch(getMemberConnections());
    }, [dispatch]);

    const slice = useSelector((state: RootState) => state.connections);
    const memberConnections = slice.memberConnections ?? { data: [] };
    // @ts-ignore TODO: strong-type memberConnections
    const dataSource = slice.dataSource;
    const dataSourceConnectionId = slice.dataSourceConnectionId;
    // @ts-ignore TODO: strong-type memberConnections
    const showTable = useSelector((state: RootState) => state.toggle["memberConnections"] ?? true) && Array.isArray(memberConnections.data);

    // Add View Data Source and Delete button columns
    const columns = [
        ...CONNECTION_COLUMNS,
        {
            field: "viewDataSource",
            headerName: "Data Source",
            width: 160,
            renderCell: (params: any) => (
                <Button
                    variant="outlined"
                    size="small"
                    onClick={() => dispatch(getDataSource(params.row.id))}
                >
                    View Data Source
                </Button>
            )
        },
        {
            field: "delete",
            headerName: "Delete",
            width: 120,
            renderCell: (params: any) => (
                <Button
                    variant="outlined"
                    color="error"
                    size="small"
                    onClick={async () => {
                        await deleteConnectionById(params.row.id);
                        dispatch(getMemberConnections());
                    }}
                >
                    Delete
                </Button>
            )
        }
    ];

    return (
        <Container>
            <h1>Manage Connections</h1>
            { memberConnections &&
                <TableOrJsonToggle locator={"memberConnections"} />
            }
            {showTable && memberConnections &&
                // @ts-ignore TODO: strong-typing here
                <DataGrid rows={memberConnections.data} columns={columns} />
            }
            {!showTable && memberConnections &&
                <Box>
                    <pre>{JSON.stringify(memberConnections, null, 2)}</pre>
                </Box>
            }
            {dataSource &&
                <Box mt={4}>
                    <h2>Data Source: {dataSourceConnectionId}</h2>
                    <pre>{JSON.stringify(dataSource, null, 2)}</pre>
                </Box>
            }
        </Container>
    );
}

export default withAuthCheck('Manage Connections', ManageConnections);
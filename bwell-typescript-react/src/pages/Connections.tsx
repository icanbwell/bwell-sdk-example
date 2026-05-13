import React from "react";
import { Box, Container, Button } from "@mui/material";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { RootState } from "@/store/store";
import { useSelector, useDispatch } from "react-redux";
import { AppDispatch } from "@/store/store";
import { DataGrid } from "@mui/x-data-grid";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { getMemberConnections } from "@/store/connectionSlice";
import { deleteConnection } from "@/store/deleteConnectionThunk";

const ManageConnections = () => {
    const dispatch = useDispatch<AppDispatch>();
    // Fetch member connections on every page load
    React.useEffect(() => {
        dispatch(getMemberConnections());
    }, [dispatch]);

    const slice = useSelector((state: RootState) => state.connections);
    const memberConnections = slice.memberConnections ?? { data: [] };
    const deletingConnectionIds = slice.deletingConnectionIds ?? [];

    // (TIP-7050) Hide connections the backend has already marked as DELETED
    // so they don't reappear in the table after the read side catches up. The
    // optimistic remove in the slice handles the "just deleted" case; this
    // filter handles any stale DELETED rows that come back on the next fetch.
    // @ts-ignore TODO: strong-type memberConnections
    const rawRows: any[] = Array.isArray(memberConnections.data) ? memberConnections.data : [];
    const visibleRows = rawRows.filter((conn: any) => conn?.status !== "DELETED");

    // @ts-ignore TODO: strong-type memberConnections
    const showTable = useSelector((state: RootState) => state.toggle["memberConnections"] ?? true) && Array.isArray(memberConnections.data);

    // Add Delete button column
    const columns = [
        ...CONNECTION_COLUMNS,
        {
            field: "delete",
            headerName: "Delete",
            width: 120,
            renderCell: (params: any) => {
                const isDeleting = deletingConnectionIds.includes(params.row.id);
                return (
                    <Button
                        variant="outlined"
                        color="error"
                        size="small"
                        disabled={isDeleting}
                        onClick={() => {
                            // (TIP-7050) Dispatch the thunk; the slice's
                            // extraReducers optimistically remove the row on
                            // fulfilled. Do NOT refetch — an immediate refetch
                            // races with the eventually-consistent read side
                            // and undoes the optimistic remove.
                            dispatch(deleteConnection(params.row.id));
                        }}
                    >
                        {isDeleting ? "Deleting…" : "Delete"}
                    </Button>
                );
            }
        }
    ];

    return (
        <Container>
            <h1>Manage Connections</h1>
            { memberConnections &&
                <TableOrJsonToggle locator={"memberConnections"} />
            }
            {showTable && memberConnections &&
                <DataGrid rows={visibleRows} columns={columns} />
            }
            {!showTable && memberConnections &&
                <Box>
                    <pre>{JSON.stringify(memberConnections, null, 2)}</pre>
                </Box>
            }
        </Container>
    );
}

export default withAuthCheck('Manage Connections', ManageConnections);
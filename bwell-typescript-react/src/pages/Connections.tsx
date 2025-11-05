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
import { deleteConnectionById } from "@/sdk/deleteConnection";

const ManageConnections = () => {
    const dispatch = useDispatch<AppDispatch>();
    // Fetch member connections on every page load
    React.useEffect(() => {
        dispatch(getMemberConnections());
    }, [dispatch]);

    const slice = useSelector((state: RootState) => state.connections);
    const memberConnections = slice.memberConnections ?? { data: [] };
    // @ts-ignore TODO: strong-type memberConnections
    const showTable = useSelector((state: RootState) => state.toggle["memberConnections"] ?? true) && Array.isArray(memberConnections.data);

    // Add Delete button column
    const columns = [
        ...CONNECTION_COLUMNS,
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
        </Container>
    );
}

export default withAuthCheck('Manage Connections', ManageConnections);
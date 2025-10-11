import { Box, Container } from "@mui/material";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { RootState } from "@/store/store";
import { useSelector } from "react-redux";
import { DataGrid } from "@mui/x-data-grid";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";

const ManageConnections = () => {
    const slice = useSelector((state: RootState) => state.connections);

    const memberConnections = slice.memberConnections;

    // @ts-ignore TODO: strong-type memberConnections
    const showTable = useSelector((state: RootState) => state.toggle["memberConnections"] ?? true) && memberConnections.data.length > 0;

    return (
        <Container>
            <h1>Manage Connections</h1>
            { memberConnections &&
                <TableOrJsonToggle locator={"memberConnections"} />
            }
            {showTable && memberConnections &&
                // @ts-ignore TODO: strong-typing here
                slice.memberConnections && <DataGrid rows={slice.memberConnections.data} columns={CONNECTION_COLUMNS} />
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
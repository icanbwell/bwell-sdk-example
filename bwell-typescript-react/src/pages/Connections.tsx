import { Container } from "@mui/material";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { RootState } from "@/store/store";
import { useSelector } from "react-redux";
import { DataGrid } from "@mui/x-data-grid";

const Connections = () => {
    const slice = useSelector((state: RootState) => state.connections);

    return (
        <Container>
            <h1>Connections</h1>
            {
                // @ts-ignore TODO: strong-typing here
                slice.memberConnections && <DataGrid rows={slice.memberConnections.data} columns={CONNECTION_COLUMNS} />
            }
        </Container>
    );
}

export default withAuthCheck('Connections', Connections);
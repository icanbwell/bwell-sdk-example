import { Container } from "@mui/material";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import HealthDataGrid from "@/components/HealthDataGrid";
import { getMemberConnections } from "@/store/connectionSlice";

const Connections = () => {
    return (
        <Container>
            <h1>Connections</h1>
            <HealthDataGrid
                title="getConnections()"
                selector="connections"
                columns={CONNECTION_COLUMNS}
                getter={getMemberConnections}
                getRows={(memberConnections: { data: any; }) => memberConnections?.data}
                serverPagination={false}
            />
        </Container>
    );
}

export default withAuthCheck('Connections', Connections);
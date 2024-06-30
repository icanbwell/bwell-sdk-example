import { useDispatch } from "react-redux";
import { AppDispatch } from "@/store/store";
import { Container } from "@mui/material";
import { CONNECTION_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { useEffect } from "react";
import HealthDataGrid from "@/components/HealthDataGrid";
import { getMemberConnections } from "@/store/connectionSlice";

const Connections = () => {
    const dispatch = useDispatch<AppDispatch>();

    const handleGetMemberConnections = () => {
        dispatch(getMemberConnections());
    };

    useEffect(handleGetMemberConnections, []);

    //TODO: Adapt HealthDataGrid to handle Connection data
    return (
        <Container>
            <h1>Connections</h1>
            <HealthDataGrid
                title="getConnections()"
                selector="connections"
                columns={CONNECTION_COLUMNS}
                getter={getMemberConnections}
            />
        </Container>
    );
}

export default withAuthCheck('Connections', Connections);
import { MEDICATION_GROUP_COLUMNS, MEDICATION_STATEMENT_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getMedicationGroups } from "@/store/healthData/medicationGroupsSlice";
import { getMedicationStatements } from "@/store/healthData/medicationStatementsSlice";
import { getMedicationKnowledge } from "@/store/healthData/medicationKnowledgeSlice";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "@/store/store";
import { Box, Container } from "@mui/material";

const Medications = () => {
    const dispatch = useDispatch();

    const handleRowClick = ({ id }: any) => {
        dispatch(getMedicationKnowledge({ medicationStatementId: id, page: 1, pageSize: 1 }));
    }

    const { healthData } = useSelector((state: RootState) => state.medicationKnowledge);

    return (
        <>
            <h1>Medications</h1>
            <HealthDataGrid
                title="Medication Groups"
                selector="medicationGroups"
                columns={MEDICATION_GROUP_COLUMNS}
                getter={getMedicationGroups}
            />
            <HealthDataGrid
                title="Medication Statements"
                selector="medicationStatements"
                columns={MEDICATION_STATEMENT_COLUMNS}
                getter={getMedicationStatements}
                onRowClick={handleRowClick}
            />
            {healthData?.data &&
                <Container>
                    <h2>Medication Knowledge</h2>
                    <Box>
                        <pre style={{ textWrap: 'wrap' }}>{JSON.stringify(healthData, null, 2)}</pre>
                    </Box>
                </Container>
            }
        </>
    );
};

export default withAuthCheck('Medications', Medications);

import { MEDICATION_GROUP_COLUMNS, MEDICATION_STATEMENT_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getMedicationGroups } from "@/store/healthData/medicationGroupsSlice";
import { getMedicationStatements } from "@/store/healthData/medicationStatementsSlice";
import { getMedicationKnowledge, medicationKnowledgeSlice } from "@/store/healthData/medicationKnowledgeSlice";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "@/store/store";
import { DisplayKnowledge } from "@/components/DisplayKnowledge";
import { useEffect } from "react";

const Medications = () => {
    const dispatch = useDispatch();

    const handleRowClick = ({ id }: any) => {
        dispatch(getMedicationKnowledge({ medicationStatementId: id, page: 1, pageSize: 1 }));
    }

    const { healthData } = useSelector((state: RootState) => state.medicationKnowledge);

    useEffect(() => {
        dispatch(medicationKnowledgeSlice.actions.resetState());
    }, []);

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
                <DisplayKnowledge name="Medication" healthData={healthData} />
            }
        </>
    );
};

export default withAuthCheck('Medications', Medications);

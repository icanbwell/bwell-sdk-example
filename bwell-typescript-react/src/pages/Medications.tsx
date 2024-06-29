import { MEDICATION_GROUP_COLUMNS, MEDICATION_STATEMENT_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getMedicationGroups } from "@/store/healthData/medicationGroupsSlice";
import { getMedicationStatements } from "@/store/healthData/medicationStatementsSlice";

const Medications = () => {
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
                title="Medications"
                selector="medicationStatements"
                columns={MEDICATION_STATEMENT_COLUMNS}
                getter={getMedicationStatements}
            />
        </>
    );
};

export default withAuthCheck('Medications', Medications);

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
import { requestInfoSlice } from "@/store/requestInfoSlice";

const Medications = () => {
    const dispatch = useDispatch();

    const handleRowClick = ({ id }: any) => {
        // @ts-ignore no need to strong type the dispatcher here
        dispatch(getMedicationKnowledge({ labId: id, page: 1, pageSize: 1 }));
    }

    const healthDataSlice = useSelector((state: RootState) => state.labKnowledge);

    const { healthData } = healthDataSlice;

    useEffect(() => {
        dispatch(medicationKnowledgeSlice.actions.resetState());
    }, []);

    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('medicationStatements'));
        else {
            dispatch(setGroupCode({ selector: 'medicationStatements', groupCode: selection[0].coding }));
            dispatch(medicationKnowledgeSlice.actions.resetState());
        }
    }

    return (
        <>
            <h1>Medications</h1>
            <HealthDataGrid
                title="Medication Groups"
                selector="medicationGroups"
                columns={MEDICATION_GROUP_COLUMNS}
                getter={getMedicationGroups}
                onRowSelect={onRowSelect}
            />
            <HealthDataGrid
                title="Medication Statements"
                selector="medicationStatements"
                columns={MEDICATION_STATEMENT_COLUMNS}
                getter={getMedicationStatements}
                onRowClick={handleRowClick}
            />
            {// @ts-ignore TODO: export types from SDK so we can strong-type in places like this
                healthData?.data &&
                <DisplayKnowledge name="Medication" healthData={healthData} />
            }
        </>
    );
};

export default withAuthCheck('Medications', Medications);

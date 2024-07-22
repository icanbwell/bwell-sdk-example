import { ENCOUNTER_COLUMNS, ENCOUNTER_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getEncounterGroups } from "@/store/healthData/encounterGroupsSlice";
import { getEncounters } from "@/store/healthData/encountersSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { useDispatch } from "react-redux";

const Encounters = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('encounters'));
        else dispatch(setGroupCode({ selector: 'encounters', groupCode: selection[0].coding }));
    }

    return (
        <>
            <h1>Encounters</h1>
            <HealthDataGrid
                title="Encounter Groups"
                selector="encounterGroups"
                columns={ENCOUNTER_GROUP_COLUMNS}
                getter={getEncounterGroups}
                onRowSelect={onRowSelect}
            />
            <HealthDataGrid
                title="Encounters"
                selector="encounters"
                columns={ENCOUNTER_COLUMNS}
                getter={getEncounters}
            />
        </>
    );
};

export default withAuthCheck('Encounters', Encounters);

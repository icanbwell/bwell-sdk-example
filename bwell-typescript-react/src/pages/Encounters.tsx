import { ENCOUNTER_COLUMNS, ENCOUNTER_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getEncounterGroups } from "@/store/encounterGroupsSlice";
import { getEncounters } from "@/store/encountersSlice";

const Encounters = () => {
    return (
        <>
            <h1>Encounters</h1>
            <HealthDataGrid
                title="Encounter Groups"
                selector="encounterGroups"
                columns={ENCOUNTER_GROUP_COLUMNS}
                getter={getEncounterGroups}
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

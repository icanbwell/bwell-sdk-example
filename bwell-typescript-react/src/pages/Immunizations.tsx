import { IMMUNIZATION_COLUMNS, IMMUNIZATION_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getImmunizationGroups } from "@/store/immunizationGroupsSlice";
import { getImmunizations } from "@/store/immunizationsSlice";

const Immunizations = () => {
    return (
        <>
            <h1>Immunizations</h1>
            <HealthDataGrid
                title="Immunization Groups"
                selector="immunizationGroups"
                columns={IMMUNIZATION_GROUP_COLUMNS}
                getter={getImmunizationGroups}
            />
            <HealthDataGrid
                title="Immunizations"
                selector="immunizations"
                columns={IMMUNIZATION_COLUMNS}
                getter={getImmunizations}
            />
        </>
    );
};

export default withAuthCheck('Immunizations', Immunizations);

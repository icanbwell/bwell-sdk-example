import { ALLERGY_INTOLERANCE_COLUMNS, ALLERGY_INTOLERANCE_GROUP_COLUMNS } from "@/column-defs";
import { getAllergyIntolerances } from "@/store/healthData/allergyIntoleranceSlice";
import { getAllergyIntoleranceGroups } from "@/store/healthData/allergyIntoleranceGroupsSlice";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";

const AllergyIntolerances = () => {
    return (
        <>
            <h1>Allergy Intolerances</h1>
            <HealthDataGrid
                title="Allergy Intolerance Groups"
                selector="allergyIntoleranceGroups"
                columns={ALLERGY_INTOLERANCE_GROUP_COLUMNS}
                getter={getAllergyIntoleranceGroups}
            />
            <HealthDataGrid
                title="Allergy Intolerances"
                selector="allergyIntolerances"
                columns={ALLERGY_INTOLERANCE_COLUMNS}
                getter={getAllergyIntolerances}
            />
        </>
    );
};

export default withAuthCheck('Allergy Intolerances', AllergyIntolerances);

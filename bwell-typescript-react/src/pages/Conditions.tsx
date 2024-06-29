import { CONDITION_COLUMNS, CONDITION_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getConditionGroups } from "@/store/healthData/conditionGroupsSlice";
import { getConditions } from "@/store/healthData/conditionsSlice";

const Conditions = () => {
    return (
        <>
            <h1>Conditions</h1>
            <HealthDataGrid
                title="Condition Groups"
                selector="conditionGroups"
                columns={CONDITION_GROUP_COLUMNS}
                getter={getConditionGroups}
            />
            <HealthDataGrid
                title="Conditions"
                selector="conditions"
                columns={CONDITION_COLUMNS}
                getter={getConditions}
            />
        </>
    );
};

export default withAuthCheck('Conditions', Conditions);

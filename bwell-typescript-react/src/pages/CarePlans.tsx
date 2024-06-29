import { CARE_PLAN_COLUMNS, CARE_PLAN_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getCarePlanGroups } from "@/store/healthData/carePlanGroupsSlice";
import { getCarePlans } from "@/store/healthData/carePlansSlice";

const CarePlans = () => {
    return (
        <>
            <h1>Care Plans</h1>
            <HealthDataGrid
                title="Care Plan Groups"
                selector="carePlanGroups"
                columns={CARE_PLAN_GROUP_COLUMNS}
                getter={getCarePlanGroups}
            />
            <HealthDataGrid
                title="Care Plans"
                selector="carePlans"
                columns={CARE_PLAN_COLUMNS}
                getter={getCarePlans}
            />
        </>
    );
};

export default withAuthCheck('Care Plans', CarePlans);

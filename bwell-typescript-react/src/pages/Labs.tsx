import { LAB_COLUMNS, LAB_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getLabGroups } from "@/store/healthData/labGroupsSlice";
import { getLabs } from "@/store/healthData/labsSlice";

const Labs = () => {
    return (
        <>
            <h1>Labs</h1>
            <HealthDataGrid
                title="Lab Groups"
                selector="labGroups"
                columns={LAB_GROUP_COLUMNS}
                getter={getLabGroups}
            />
            <HealthDataGrid
                title="Labs"
                selector="labs"
                columns={LAB_COLUMNS}
                getter={getLabs}
            />
        </>
    );
};

export default withAuthCheck('Labs', Labs);

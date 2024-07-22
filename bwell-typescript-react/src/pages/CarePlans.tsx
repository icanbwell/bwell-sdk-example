import { CARE_PLAN_COLUMNS, CARE_PLAN_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getCarePlanGroups } from "@/store/healthData/carePlanGroupsSlice";
import { getCarePlans } from "@/store/healthData/carePlansSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { useDispatch } from "react-redux";

const CarePlans = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('carePlans'));
        else dispatch(setGroupCode({ selector: 'carePlans', groupCode: selection[0].coding }));
    }
    
    return (
        <>
            <h1>Care Plans</h1>
            <HealthDataGrid
                title="Care Plan Groups"
                selector="carePlanGroups"
                columns={CARE_PLAN_GROUP_COLUMNS}
                getter={getCarePlanGroups}
                onRowSelect={onRowSelect}
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

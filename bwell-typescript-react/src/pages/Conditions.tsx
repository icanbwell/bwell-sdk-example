import { CONDITION_COLUMNS, CONDITION_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getConditionGroups } from "@/store/healthData/conditionGroupsSlice";
import { getConditions } from "@/store/healthData/conditionsSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { useDispatch } from "react-redux";

const Conditions = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('conditions'));
        else dispatch(setGroupCode({ selector: 'conditions', groupCode: selection[0].coding }));
    }

    return (
        <>
            <h1>Conditions</h1>
            <HealthDataGrid
                title="Condition Groups"
                selector="conditionGroups"
                columns={CONDITION_GROUP_COLUMNS}
                getter={getConditionGroups}
                onRowSelect={onRowSelect}
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

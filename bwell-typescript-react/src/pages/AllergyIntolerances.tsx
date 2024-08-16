import { ALLERGY_INTOLERANCE_COLUMNS, ALLERGY_INTOLERANCE_GROUP_COLUMNS } from "@/column-defs";
import { getAllergyIntolerances } from "@/store/healthData/allergyIntoleranceSlice";
import { getAllergyIntoleranceGroups } from "@/store/healthData/allergyIntoleranceGroupsSlice";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { useDispatch } from "react-redux";
import { requestInfoSlice } from "@/store/requestInfoSlice";

const AllergyIntolerances = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('allergyIntolerances'));
        else dispatch(setGroupCode({ selector: 'allergyIntolerances', groupCode: selection[0].coding }));
    }

    return (
        <>
            <h1>Allergy Intolerances</h1>
            <HealthDataGrid
                title="Allergy Intolerance Groups"
                selector="allergyIntoleranceGroups"
                columns={ALLERGY_INTOLERANCE_GROUP_COLUMNS}
                getter={getAllergyIntoleranceGroups}
                onRowSelect={onRowSelect}
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

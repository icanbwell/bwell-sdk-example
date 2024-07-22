import { IMMUNIZATION_COLUMNS, IMMUNIZATION_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getImmunizationGroups } from "@/store/healthData/immunizationGroupsSlice";
import { getImmunizations } from "@/store/healthData/immunizationsSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { useDispatch } from "react-redux";

const Immunizations = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('immunizations'));
        else dispatch(setGroupCode({ selector: 'immunizations', groupCode: selection[0].coding }));
    }
    
    return (
        <>
            <h1>Immunizations</h1>
            <HealthDataGrid
                title="Immunization Groups"
                selector="immunizationGroups"
                columns={IMMUNIZATION_GROUP_COLUMNS}
                getter={getImmunizationGroups}
                onRowSelect={onRowSelect}
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

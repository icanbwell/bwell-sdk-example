import { VITAL_SIGN_COLUMNS, VITAL_SIGN_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getVitalSignGroups } from "@/store/healthData/vitalSignGroupsSlice";
import { getVitalSigns } from "@/store/healthData/vitalSignsSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { useDispatch } from "react-redux";

const VitalSigns = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('vitalSigns'));
        else dispatch(setGroupCode({ selector: 'vitalSigns', groupCode: selection[0].coding }));
    }

    return (
        <>
            <h1>Vital Signs</h1>
            <HealthDataGrid
                title="Vital Sign Groups"
                selector="vitalSignGroups"
                columns={VITAL_SIGN_GROUP_COLUMNS}
                getter={getVitalSignGroups}
                onRowSelect={onRowSelect}
            />
            <HealthDataGrid
                title="Vital Signs"
                selector="vitalSigns"
                columns={VITAL_SIGN_COLUMNS}
                getter={getVitalSigns}
            />
        </>
    );
};

export default withAuthCheck('Vital Signs', VitalSigns);

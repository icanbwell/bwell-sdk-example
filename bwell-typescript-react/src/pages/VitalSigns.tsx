import { VITAL_SIGN_COLUMNS, VITAL_SIGN_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getVitalSignGroups } from "@/store/healthData/vitalSignGroupsSlice";
import { getVitalSigns } from "@/store/healthData/vitalSignsSlice";

const VitalSigns = () => {
    return (
        <>
            <h1>Vital Signs</h1>
            <HealthDataGrid
                title="Vital Sign Groups"
                selector="vitalSignGroups"
                columns={VITAL_SIGN_GROUP_COLUMNS}
                getter={getVitalSignGroups}
            />
            <HealthDataGrid
                title="VitalSigns"
                selector="vitalSigns"
                columns={VITAL_SIGN_COLUMNS}
                getter={getVitalSigns}
            />
        </>
    );
};

export default withAuthCheck('Vital Signs', VitalSigns);

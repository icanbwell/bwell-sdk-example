import { HEALTH_SUMMARY_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getHealthSummary } from "@/store/healthSummarySlice";

const HealthSummary = () => {
    return (
        <>
            <h1>Health Summary</h1>
            <HealthDataGrid
                title="Health Summary"
                selector="healthSummary"
                columns={HEALTH_SUMMARY_COLUMNS}
                getter={getHealthSummary}
                rowId={"category"}
            />
        </>
    );
};

export default withAuthCheck('Health Summary', HealthSummary);

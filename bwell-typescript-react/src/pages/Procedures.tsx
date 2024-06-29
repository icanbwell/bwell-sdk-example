import { PROCEDURE_COLUMNS, PROCEDURE_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getProcedureGroups } from "@/store/healthData/procedureGroupsSlice";
import { getProcedures } from "@/store/healthData/proceduresSlice";

const Procedures = () => {
    return (
        <>
            <h1>Procedures</h1>
            <HealthDataGrid
                title="Procedure Groups"
                selector="procedureGroups"
                columns={PROCEDURE_GROUP_COLUMNS}
                getter={getProcedureGroups}
            />
            <HealthDataGrid
                title="Procedures"
                selector="procedures"
                columns={PROCEDURE_COLUMNS}
                getter={getProcedures}
            />
        </>
    );
};

export default withAuthCheck('Procedures', Procedures);

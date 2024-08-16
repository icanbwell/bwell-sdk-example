import { PROCEDURE_COLUMNS, PROCEDURE_GROUP_COLUMNS } from "@/column-defs";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getProcedureGroups } from "@/store/healthData/procedureGroupsSlice";
import { getProcedures } from "@/store/healthData/proceduresSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { useDispatch } from "react-redux";

const Procedures = () => {
    const dispatch = useDispatch();
    
    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('procedures'));
        else dispatch(setGroupCode({ selector: 'procedures', groupCode: selection[0].coding }));
    }
    
    return (
        <>
            <h1>Procedures</h1>
            <HealthDataGrid
                title="Procedure Groups"
                selector="procedureGroups"
                columns={PROCEDURE_GROUP_COLUMNS}
                getter={getProcedureGroups}
                onRowSelect={onRowSelect}
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

import { LAB_COLUMNS, LAB_GROUP_COLUMNS } from "@/column-defs";
import { DisplayKnowledge } from "@/components/DisplayKnowledge";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getLabGroups } from "@/store/healthData/labGroupsSlice";
import { getLabKnowledge, labKnowledgeSlice } from "@/store/healthData/labKnowledgeSlice";
import { requestInfoSlice } from "@/store/requestInfoSlice";
import { getLabs } from "@/store/healthData/labsSlice";
import { RootState } from "@/store/store";
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";

const Labs = () => {
    const dispatch = useDispatch();

    const handleRowClick = ({ id }: any) => {
        // @ts-ignore no need to strong type the dispatcher here
        dispatch(getLabKnowledge({ labId: id, page: 1, pageSize: 1 }));
    }

    const healthDataSlice = useSelector((state: RootState) => state.health.labKnowledge);

    const { healthData } = healthDataSlice;

    useEffect(() => {
        dispatch(labKnowledgeSlice.actions.resetState());
    }, []);

    const { setGroupCode, clearGroupCode } = requestInfoSlice.actions;

    const onRowSelect = (selection: any[]) => {
        if (!selection.length) dispatch(clearGroupCode('labs'));
        else {
            dispatch(setGroupCode({ selector: 'labs', groupCode: selection[0].coding }));
            dispatch(labKnowledgeSlice.actions.resetState());
        }
    }

    return (
        <>
            <h1>Labs</h1>
            <HealthDataGrid
                title="Lab Groups"
                selector="labGroups"
                columns={LAB_GROUP_COLUMNS}
                getter={getLabGroups}
                onRowSelect={onRowSelect}
            />
            <HealthDataGrid
                title="Labs"
                selector="labs"
                columns={LAB_COLUMNS}
                getter={getLabs}
                onRowClick={handleRowClick}
            />
            {// @ts-ignore TODO: export types from SDK so we can strong-type in places like this
                healthData?.data &&
                <DisplayKnowledge name="Lab" healthData={healthData} />
            }
        </>
    );
};

export default withAuthCheck('Labs', Labs);

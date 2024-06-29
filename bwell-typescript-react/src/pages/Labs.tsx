import { LAB_COLUMNS, LAB_GROUP_COLUMNS } from "@/column-defs";
import { DisplayKnowledge } from "@/components/DisplayKnowledge";
import HealthDataGrid from "@/components/HealthDataGrid";
import withAuthCheck from "@/components/withAuthCheck";
import { getLabGroups } from "@/store/healthData/labGroupsSlice";
import { getLabKnowledge } from "@/store/healthData/labKnowledgeSlice";
import { getLabs } from "@/store/healthData/labsSlice";
import { RootState } from "@/store/store";
import { useDispatch, useSelector } from "react-redux";

const Labs = () => {
    const dispatch = useDispatch();

    const handleRowClick = ({ id }: any) => {
        dispatch(getLabKnowledge({ labId: id, page: 1, pageSize: 1 }));
    }

    const { healthData } = useSelector((state: RootState) => state.labKnowledge);

    return (
        <>
            <h1>Labs</h1>
            <HealthDataGrid
                title="Lab Groups"
                selector="labGroups"
                columns={LAB_GROUP_COLUMNS}
                getter={getLabGroups}
            />
            <HealthDataGrid
                title="Labs"
                selector="labs"
                columns={LAB_COLUMNS}
                getter={getLabs}
                onRowClick={handleRowClick}
            />
            {healthData?.data &&
                <DisplayKnowledge name="Lab" healthData={healthData} />
            }
        </>
    );
};

export default withAuthCheck('Labs', Labs);

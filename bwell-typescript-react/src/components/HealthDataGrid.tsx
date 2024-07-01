// components/HealthDataGrid.tsx
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { DataGrid, GridEventListener, GridEventLookup, GridPaginationModel, GridRowSelectionModel } from "@mui/x-data-grid";
import { Alert, Box, Container } from "@mui/material";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { AppDispatch, RootState } from "@/store/store";

import './HealthDataGrid.css';
import { requestInfoSlice, INITIAL_REQUEST } from "@/store/requestInfoSlice";

type HealthDataGridProps = {
    title: string;
    selector: string;
    columns: any[];
    getter: Function;
    rowId?: string;
    onRowClick?: GridEventListener<keyof GridEventLookup>;
    onRowSelect?: Function;
}

const HealthDataGrid = ({
    title,
    selector,
    columns,
    getter,
    rowId,
    onRowClick,
    onRowSelect,
}: HealthDataGridProps) => {
    const dispatch = useDispatch<AppDispatch>();

    const requestInfo = useSelector((state: RootState) => state.requests[selector]) ?? INITIAL_REQUEST;
    const paginationModel = { page: requestInfo?.page ?? 0, pageSize: requestInfo?.pageSize ?? 0 };
    const { setPage, setPageSize } = requestInfoSlice.actions;

    const isGroups = selector.includes('Groups');

    const getData = () => {
        if (isGroups) dispatch(getter({ page: requestInfo.page, pageSize: requestInfo.pageSize }));
        else dispatch(getter(requestInfo));
    }

    const handlePaginationChange = async (paginationModel: GridPaginationModel) => {
        const { page, pageSize } = paginationModel;

        dispatch(setPage({ selector, page }));
        dispatch(setPageSize({ selector, pageSize }));
    }

    const slice = useSelector((state: RootState) => (state as any)[selector]);
    const { healthData, loading, error } = slice;

    const showTable = useSelector((state: RootState) => state.toggle[selector] ?? true) && healthData?.data?.resources;

    useEffect(getData, [requestInfo]);

    const getRowClassName = () => onRowClick ? 'cursor-pointer' : '';

    return (
        <Container>
            <h2>{title}</h2>
            {healthData?.data && <TableOrJsonToggle locator={selector} />}
            {loading && <p>Loading...</p>}
            {error && <Alert severity="error">{error}</Alert>}
            {showTable &&
                <DataGrid
                    rows={healthData?.data?.resources}
                    columns={columns}
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={
                        {
                            pagination: {
                                paginationModel: { pageSize: 10, page: 0 }
                            }
                        }
                    }
                    paginationMode="server"
                    paginationModel={paginationModel}
                    rowCount={healthData?.data?.paging_info?.total_items || 0}
                    onPaginationModelChange={handlePaginationChange}
                    onRowSelectionModelChange={onRowSelect}
                    getRowId={(row) => rowId ? row[rowId] : row.id}
                    onRowClick={onRowClick}
                    getRowClassName={getRowClassName}
                    rowSelection={false}
                    disableRowSelectionOnClick={false}
                />
            }
            {!showTable && healthData?.data &&
                <Box>
                    <pre>{JSON.stringify(healthData, null, 2)}</pre>
                </Box>
            }
        </Container>
    );
};

export default HealthDataGrid;

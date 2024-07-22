// components/HealthDataGrid.tsx
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { DataGrid, GridColDef, GridEventListener, GridEventLookup, GridPaginationModel } from "@mui/x-data-grid";
import { Alert, Box, Container } from "@mui/material";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { AppDispatch, RootState } from "@/store/store";

import './HealthDataGrid.css';
import { requestInfoSlice, INITIAL_REQUEST } from "@/store/requestInfoSlice";

type HealthDataGridProps = {
    title: string;
    selector: string;
    columns: GridColDef[];
    getter: Function;
    rowId?: string;
    onRowClick?: GridEventListener<keyof GridEventLookup>;
    onRowSelect?: Function;
    getRows?: Function;
    serverPagination?: boolean;
}

const HealthDataGrid = ({
    title,
    selector,
    columns,
    getter,
    rowId,
    onRowClick,
    onRowSelect,
    getRows,
    serverPagination = true
}: HealthDataGridProps) => {
    const dispatch = useDispatch<AppDispatch>();

    //get the request info for this grid; separate out pagination model
    const requestInfo = useSelector((state: RootState) => state.requests[selector]) ?? INITIAL_REQUEST;

    let paginationModel = { page: requestInfo?.page ?? 0, pageSize: requestInfo?.pageSize ?? 0 };

    //get setters for page and pageSize from requestInfo actions
    const { setPage, setPageSize } = requestInfoSlice.actions;

    //cheesy hack to figure out if we're a grid of groups or not
    const isGroups = selector.includes('Groups');

    //dispatch the getData action
    //if we are fetching groups, we need to take off the HealthDataRequest stuff,
    //else the API errors due to being given unexpected params
    const getData = () => {
        if (isGroups) dispatch(getter({ page: requestInfo.page, pageSize: requestInfo.pageSize }));
        else dispatch(getter(requestInfo));
    }

    //update the underlying request when pagination model changes
    const handlePaginationChange = async (paginationModel: GridPaginationModel) => {
        const { page, pageSize } = paginationModel;

        dispatch(setPage({ selector, page }));
        dispatch(setPageSize({ selector, pageSize }));
    }

    //get the slice of state that corresponds to the provided selector
    // @ts-ignore - we're using a dynamic selector here
    const slice = useSelector((state: RootState) => state["health"][selector]);
    const { healthData, loading, error } = slice;

    //get the rows; use a custom getter if provided
    let rows = healthData?.data?.resources || [];

    if (getRows) rows = getRows(healthData);

    //check toggle state to see if we should display a table or json
    const showTable = useSelector((state: RootState) => state.toggle[selector] ?? true) && rows?.length > 0;

    //re-fetch data when the request changes, but only if we're doing server-side pagination
    useEffect(getData, serverPagination ? [requestInfo] : []);

    //only show a pointer cursor on the rows when clicking a row does something:
    //either when a group is selected, or when a row click handler has been provided
    const getRowClassName = () => onRowClick || isGroups ? 'cursor-pointer' : '';

    //on row select, use the id to get the whole row, 
    //then call the onRowSelect callback with the row
    const handleRowSelection = (selection: any[]) => {
        if (selection?.length === 0) {
            onRowSelect?.([]);
            return;
        }

        const id = selection[0];
        const row = healthData?.data?.resources.find((row: any) => row.id === id);

        onRowSelect?.([row]);
    }

    return (
        <Container>
            <h2>{title}</h2>
            {healthData?.data && <TableOrJsonToggle locator={selector} />}
            {loading && <p>Loading...</p>}
            {error && <Alert severity="error">{error}</Alert>}
            {showTable &&
                <DataGrid
                    rows={rows}
                    columns={columns}
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={
                        {
                            pagination: {
                                paginationModel: { pageSize: 10, page: 0 }
                            }
                        }
                    }
                    paginationMode={serverPagination ? "server" : "client"}
                    paginationModel={paginationModel}
                    rowCount={serverPagination ? healthData?.data?.paging_info?.total_items || 0 : rows.length}
                    onPaginationModelChange={handlePaginationChange}
                    onRowSelectionModelChange={handleRowSelection}
                    getRowId={(row) => rowId ? row[rowId] : row.id}
                    onRowClick={onRowClick}
                    getRowClassName={getRowClassName}
                    rowSelection={isGroups}
                    disableRowSelectionOnClick={!isGroups}
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

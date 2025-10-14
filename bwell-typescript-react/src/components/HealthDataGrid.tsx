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
    let rows = [];
    if (healthData?.data?.resources) {
        rows = healthData.data.resources;
    } else if (healthData?.data?.entry) {
        rows = healthData.data.entry;
    } else if (healthData?.data?.items) {
        rows = healthData.data.items;
    } else if (healthData?.data) {
        // fallback: try to use data directly if it's an array
        if (Array.isArray(healthData.data)) {
            rows = healthData.data;
        }
    }
    // flatten allergy intolerance rows if selector is allergyIntolerances
    if (selector === 'allergyIntolerances' && rows && rows.length > 0) {
        rows = rows.map((row: any) => ({
            id: row.id,
            category: Array.isArray(row.resource?.category) ? row.resource.category.join(', ') : '',
            criticality: row.resource?.criticality ?? '',
            code: row.resource?.code?.coding?.[0]?.display ?? row.resource?.code?.text ?? '',
            onsetDateTime: row.resource?.onsetDateTime ? new Date(row.resource.onsetDateTime) : null,
            lastOccurrence: row.resource?.lastOccurrence ? new Date(row.resource.lastOccurrence) : null,
            clinicalStatus: row.resource?.clinicalStatus?.coding?.[0]?.display ?? row.resource?.clinicalStatus?.text ?? '',
            recordedDate: row.resource?.recordedDate ? new Date(row.resource.recordedDate) : null,
            sourceDisplay: row.resource?.sourceDisplay ?? '',
        }));
    }
    // flatten condition rows if selector is conditions
    if (selector === 'conditions' && rows && rows.length > 0) {
        rows = rows.map((row: any) => ({
            id: row.id,
            code: row.resource?.code?.coding?.[0]?.display ?? row.resource?.code?.text ?? '',
            severity: row.resource?.severity?.text ?? '',
            bodySite: row.resource?.bodySite?.[0]?.text ?? '',
            recordedDate: row.resource?.recordedDate ? new Date(row.resource.recordedDate) : null,
        }));
    }
    // flatten immunization rows if selector is immunizations
    if (selector === 'immunizations' && rows && rows.length > 0) {
        rows = rows.map((row: any) => ({
            id: row.id,
            vaccineCode: row.resource?.vaccineCode?.text ?? row.resource?.vaccineCode?.coding?.[0]?.display ?? '',
            site: row.resource?.site?.coding?.[0]?.display ?? row.resource?.site?.text ?? '',
            route: row.resource?.route?.coding?.[0]?.display ?? row.resource?.route?.text ?? '',
            occurrenceDateTime: row.resource?.occurrenceDateTime ? new Date(row.resource.occurrenceDateTime) : null,
        }));
    }
    // flatten procedure rows if selector is procedures
    if (selector === 'procedures' && rows && rows.length > 0) {
        rows = rows.map((row: any) => ({
            id: row.id,
            code: row.resource?.code?.text ?? row.resource?.code?.coding?.[0]?.display ?? '',
            performer: row.resource?.performer?.[0]?.actor?.name?.[0]?.text ?? '',
            outcome: row.resource?.outcome?.text ?? '',
            performedDateTime: row.resource?.performedDateTime ? new Date(row.resource.performedDateTime) : null,
        }));
    }
    // flatten vital sign rows if selector is vitalSigns
    if (selector === 'vitalSigns' && rows && rows.length > 0) {
        rows = rows.map((row: any) => ({
            id: row.id,
            code: row.resource?.code?.text ?? row.resource?.code?.coding?.[0]?.display ?? '',
            value: row.resource?.valueQuantity ? `${row.resource.valueQuantity.value ?? ''} ${row.resource.valueQuantity.unit ?? ''}` : '',
            interpretation: row.resource?.interpretation?.[0]?.text ?? '',
            note: row.resource?.note?.[0]?.text ?? '',
            effectiveDateTime: row.resource?.effectiveDateTime ? new Date(row.resource.effectiveDateTime) : null,
        }));
    }
    // flatten care plan rows if selector is carePlans
    if (selector === 'carePlans' && rows && rows.length > 0) {
        rows = rows.map((row: any, idx: number) => ({
            id: row.id ?? row.resource?.id ?? `carePlan-${idx}`,
            category: row.resource?.category?.[0]?.display ?? row.resource?.category?.[0]?.text ?? '',
            activity: row.resource?.activity,
            period: row.resource?.period,
        }));
    }
    // flatten encounter rows if selector is encounters
    if (selector === 'encounters' && rows && rows.length > 0) {
        rows = rows.map((row: any, idx: number) => ({
            id: row.id ?? row.resource?.id ?? `encounter-${idx}`,
            status: row.resource?.status ?? '',
            type: row.resource?.type?.[0]?.text ?? row.resource?.type?.[0]?.coding?.[0]?.display ?? '',
            class: row.resource?.class?.code ?? '',
            period: row.resource?.period ?? null,
            reason: row.resource?.reasonCode?.[0]?.text ?? row.resource?.reasonCode?.[0]?.coding?.[0]?.display ?? '',
            serviceProvider: row.resource?.serviceProvider?.display ?? '',
        }));
    }
    // flatten lab rows if selector is labs
    if (selector === 'labs' && rows && rows.length > 0) {
        rows = rows.map((row: any, idx: number) => ({
            id: row.id ?? row.resource?.id ?? `lab-${idx}`,
            code: row.resource?.code?.coding?.[0]?.display ?? row.resource?.code?.text ?? '',
            effectiveDateTime: row.resource?.effectiveDateTime ? new Date(row.resource.effectiveDateTime) : null,
            value: row.resource?.valueQuantity ? `${row.resource.valueQuantity.value ?? ''} ${row.resource.valueQuantity.unit ?? ''}` : '',
            referenceRange: row.resource?.referenceRange?.[0] ? `${row.resource.referenceRange[0].low?.value ?? ''} ${row.resource.referenceRange[0].low?.unit ?? ''} - ${row.resource.referenceRange[0].high?.value ?? ''} ${row.resource.referenceRange[0].high?.unit ?? ''}` : '',
            note: row.resource?.note?.[0]?.text ?? '',
        }));
    }
    if (getRows) rows = getRows(healthData);

    //check toggle state to see if we should display a table or json
    const showTable = useSelector((state: RootState) => state.toggle[selector] ?? true);

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

import { GridColDef } from "@mui/x-data-grid";

export const CONNECTION_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID' },
    { field: 'name', headerName: 'Name' },
    { field: 'category', headerName: 'Category' },
    { field: 'type', headerName: 'Type' },
    { field: 'status', headerName: 'Status' },
    { field: 'syncStatus', headerName: 'SyncStatus' },
    { field: 'statusUpdated', headerName: 'Status Updated', type: 'dateTime', valueGetter: (params) => new Date(params) },
    { field: 'lastSynced', headerName: 'Last Synced', type: 'dateTime', valueGetter: (params) => new Date(params) },
    { field: 'created', headerName: 'Created', type: 'dateTime', valueGetter: (params) => new Date(params) },
    { field: 'isDirect', headerName: 'Is Direct', type: 'boolean' },
];

export const ALLERGY_INTOLERANCE_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 300 },
];

export const ALLERGY_INTOLERANCE_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'category', headerName: 'Category', valueGetter: category => category?.length ? category[0].display : '', width: 100 },
    { field: 'criticality', headerName: 'Criticality', valueGetter: (criticality) => criticality.display },
    { field: 'onsetPeriod', headerName: 'Onset', valueGetter: (onsetPeriod) => onsetPeriod ? new Date(onsetPeriod?.start) : '', type: 'dateTime', width: 200 },
    { field: 'lastOccurrence', headerName: 'Last Occurence', valueGetter: (lastOccurrence) => lastOccurrence ? new Date(lastOccurrence) : '', type: 'dateTime', width: 200 },
    { field: 'clinicalStatus', headerName: 'Clinical Status', valueGetter: (clinicalStatus) => clinicalStatus?.coding?.length ? clinicalStatus.coding[0].display : '', width: 125 },
]
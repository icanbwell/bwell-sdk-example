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
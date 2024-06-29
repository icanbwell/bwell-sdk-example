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
    { field: 'criticality', headerName: 'Criticality', valueGetter: (criticality) => criticality.display },
    { field: 'recordedDate', headerName: 'Recorded Date', valueGetter: (recordedDate) => recordedDate ? new Date(recordedDate) : '', type: 'dateTime', width: 175 },
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const ALLERGY_INTOLERANCE_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'category', headerName: 'Category', valueGetter: category => category?.length ? category[0].display : '', width: 100 },
    { field: 'criticality', headerName: 'Criticality', valueGetter: (criticality) => criticality.display },
    { field: 'onsetPeriod', headerName: 'Onset', valueGetter: (onsetPeriod) => onsetPeriod ? new Date(onsetPeriod?.start) : '', type: 'dateTime', width: 200 },
    { field: 'lastOccurrence', headerName: 'Last Occurence', valueGetter: (lastOccurrence) => lastOccurrence ? new Date(lastOccurrence) : '', type: 'dateTime', width: 200 },
    { field: 'clinicalStatus', headerName: 'Clinical Status', valueGetter: (clinicalStatus) => clinicalStatus?.coding?.length ? clinicalStatus.coding[0].display : '', width: 125 },
]
export const HEALTH_SUMMARY_COLUMNS: GridColDef[] = [
    { field: 'category', headerName: 'Category', width: 300 },
    { field: 'total', headerName: 'Total', width: 300 },
]

export const CONDITION_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'code', headerName: 'Code', valueGetter: (code) => code?.coding?.length ? code.coding[0].display : '' },
    { field: 'subject', headerName: 'Subject Name', valueGetter: (subject) => subject?.name?.length ? subject.name[0].text : '', width: 200 },
    { field: 'severity', headerName: 'Severity', valueGetter: (severity) => severity?.coding?.length ? severity.coding[0].display : '', width: 100 },
    { field: 'bodySite', headerName: 'Body Site', valueGetter: (bodySite) => bodySite?.length && bodySite[0].coding?.length ? bodySite[0].coding[0].display : '', width: 200 },
    { field: 'recordedDate', headerName: 'Recorded Date', valueGetter: (recordedDate) => recordedDate ? new Date(recordedDate) : '', type: 'dateTime', width: 175 },
];

export const CONDITION_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 300 },
    { field: 'recordedDate', headerName: 'Recorded Date', valueGetter: (recordedDate) => recordedDate ? new Date(recordedDate) : '', type: 'dateTime', width: 150 },
];
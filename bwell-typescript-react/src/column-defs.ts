import { GridColDef } from "@mui/x-data-grid";

const monthDayYear = (dateString: String) => {
    const date = new Date(dateString);

    if (date && !isNaN(date.getDate()))
        return `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
    else
        return 'N/A';
}

const joinActivity = (activity) => {
    if (!activity?.length) return '';

    return activity.map(a => a.detail?.code?.text).join(', ');
}

const joinCoding = coding => {
    if (!coding?.length) return '';

    return coding.map(c => c.coding[0].display).join(', ');
}

const formatValue = value => `${value?.valueQuantity?.value ?? ''} ${value?.valueQuantity?.unit ?? ''}`;

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
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const LAB_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'code', headerName: 'Code', valueGetter: code => code?.coding?.length ? code.coding[0].display : '', width: 300 },
    { field: 'value', headerName: 'Value', valueGetter: value => `${value?.valueQuantity?.value} ${value?.valueQuantity?.unit}`, width: 150 },
    { field: 'referenceRange', headerName: 'Reference Range', valueGetter: referenceRange => referenceRange?.length ? `${referenceRange[0].low.value} ${referenceRange[0].low.unit} - ${referenceRange[0].high.value} ${referenceRange[0].high.unit}` : '', width: 200 },
    { field: 'note', headerName: 'Note', valueGetter: note => note?.length ? note[0].text : '', width: 400 },
];

export const LAB_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 300 },
    { field: 'recordedDate', headerName: 'Recorded Date', valueGetter: (recordedDate) => recordedDate ? new Date(recordedDate) : '', type: 'dateTime', width: 150 },
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const CARE_PLAN_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'category', headerName: 'Category', valueGetter: category => category?.length ? category[0].display : '', width: 100 },
    { field: 'activity', headerName: 'Activity', valueGetter: activity => joinActivity(activity), width: 200 },
    { field: 'period', headerName: 'Period', valueGetter: (period) => period?.start && period?.end ? `${monthDayYear(period.start)} - ${monthDayYear(period.end)}` : '', width: 150 },
];

export const CARE_PLAN_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 300 },
    { field: 'period', headerName: 'Period', valueGetter: (period) => period?.start && period?.end ? `${monthDayYear(period.start)} - ${monthDayYear(period.end)}` : '', width: 150 },
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const ENCOUNTER_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'type', headerName: 'Type', width: 300, valueGetter: type => joinCoding(type) },
    { field: 'period', headerName: 'Period', valueGetter: (period) => period?.start && period?.end ? `${monthDayYear(period.start)} - ${monthDayYear(period.end)}` : '', width: 250 },
    { field: 'reasonCode', headerName: 'Reason', valueGetter: (reasonCode) => joinCoding(reasonCode), width: 250 },
];

export const ENCOUNTER_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 300 },
    { field: 'participant', headerName: 'Participant', width: 150 },
    { field: 'date', headerName: 'Date', type: 'dateTime', valueGetter: (date) => new Date(date) },
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const IMMUNIZATION_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'vaccineCode', headerName: 'Vaccine Code', width: 300, valueGetter: vaccineCode => vaccineCode?.text, width: 150 },
    { field: 'site', headerName: 'Site', valueGetter: site => site?.coding?.length ? site.coding[0].display : '', width: 200 },
    { field: 'route', headerName: 'Route', valueGetter: route => route?.coding?.length ? route.coding[0].display : '', width: 200 },
    { field: 'occurrenceDateTime', headerName: 'Date', type: 'occurrenceDateTime', valueGetter: (occurrenceDateTime) => monthDayYear(occurrenceDateTime), width: 125 },
];

export const IMMUNIZATION_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'occurrenceDateTime', headerName: 'Date', type: 'occurrenceDateTime', valueGetter: (occurrenceDateTime) => monthDayYear(occurrenceDateTime), width: 250 },
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const PROCEDURE_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'code', headerName: 'Code', valueGetter: code => code?.text, width: 200 },
    { field: 'performer', headerName: 'Performer', valueGetter: performer => performer[0].actor.name[0].text, width: 200 },
    { field: 'outcome', headerName: 'Outcome', valueGetter: outcome => outcome?.text, width: 250 },
    { field: 'performedDateTime', headerName: 'Date', type:'date', valueGetter: (performedDateTime) => new Date(performedDateTime), width: 125 },
];

export const PROCEDURE_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'code', headerName: 'Code', valueGetter: code => code?.text, width: 200 },
    { field: 'performer', headerName: 'Performer', valueGetter: performer => performer[0].actor.name[0].text, width: 200 },
    { field: 'outcome', headerName: 'Outcome', valueGetter: outcome => outcome?.text, width: 250 },
    { field: 'performedDateTime', headerName: 'Date', type:'date', valueGetter: (performedDateTime) => new Date(performedDateTime), width: 125 },
];

export const VITAL_SIGN_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'code', headerName: 'Code', valueGetter: code => code?.text, width: 100 },
    { field: 'value', headerName: 'Value', valueGetter: value => formatValue(value), width: 100 },
    { field: 'interpretation', headerName: 'Interpretation', valueGetter: interpretation => interpretation?.length ? interpretation[0].text : '', width: 200 },
    { field: 'note', headerName: 'Note', valueGetter: note => note?.length ? note[0].text : '', width: 200 },
    { field: 'effectiveDateTime', headerName: 'Date', type: 'effectiveDateTime', valueGetter: (effectiveDateTime) => monthDayYear(effectiveDateTime), width: 150 },
];

export const VITAL_SIGN_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'value', headerName: 'Value', valueGetter: value => formatValue(value), width: 200 },
    { field: 'effectiveDateTime', headerName: 'Date', valueGetter: (effectiveDateTime) => monthDayYear(effectiveDateTime), width: 150 }
];

export const MEDICATION_GROUP_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'name', headerName: 'Name', width: 300 },
    { field: 'authoredOn', headerName: 'Date', valueGetter: (authoredOn) => authoredOn ? new Date(authoredOn) : '', type: 'dateTime', width: 175 },
    { field: 'source', headerName: 'Source', valueGetter: (source) => source?.length ? source.map(s => s).join(', ') : '', width: 200 },
];

export const MEDICATION_STATEMENT_COLUMNS: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 300 },
    { field: 'medication', headerName: 'Name', valueGetter: medication => medication?.text ?? '', width: 300 },
    { field: 'dosageInstruction', headerName: 'Instruction', valueGetter: di => di?.length ? di[0].text : '', width: 300 },
]
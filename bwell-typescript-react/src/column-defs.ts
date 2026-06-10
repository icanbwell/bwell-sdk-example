import { GridColDef } from "@mui/x-data-grid";

const monthDayYear = (dateString: string) => {
  const date = new Date(dateString);

  if (date && !isNaN(date.getDate()))
    return `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
  else return "N/A";
};

const monthDayYearTime = (dateString: string) => {
  const date = new Date(dateString);
  if (date && !isNaN(date.getDate())) {
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${month}/${day}/${year} ${hours}:${minutes}`;
  } else {
    return "N/A";
  }
};

const joinActivity = (activity: any[]) => {
  if (!activity?.length) return "";

  return activity.map((a) => a.detail?.code?.text).join(", ");
};

const joinCoding = (coding: any[]) => {
  if (!coding?.length) return "";

  return coding.map((c) => c.coding[0].display).join(", ");
};

const formatValue = (value: any) =>
  `${value?.valueQuantity?.value ?? ""} ${value?.valueQuantity?.unit ?? ""}`;

export const CONNECTION_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID" },
  { field: "name", headerName: "Name" },
  { field: "category", headerName: "Category" },
  { field: "type", headerName: "Type" },
  { field: "status", headerName: "Status" },
  { field: "syncStatus", headerName: "SyncStatus" },
  {
    field: "statusUpdated",
    headerName: "Status Updated",
    valueGetter: (params) => monthDayYear(params),
  },
  {
    field: "created",
    headerName: "Created",
    valueGetter: (params) => monthDayYear(params),
  },
  { field: "isDirect", headerName: "Is Direct", type: "boolean" },
];

export const ALLERGY_INTOLERANCE_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 300 },
  { field: "criticality", headerName: "Criticality", width: 150 },
  {
    field: "recordedDate",
    headerName: "Recorded Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 175,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const ALLERGY_INTOLERANCE_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "category", headerName: "Category", width: 100 },
  { field: "criticality", headerName: "Criticality", width: 150 },
  { field: "code", headerName: "Code", width: 200 },
  {
    field: "onsetDateTime",
    headerName: "Onset",
    valueGetter: (params) => monthDayYearTime(params),
    width: 200,
  },
  {
    field: "lastOccurrence",
    headerName: "Last Occurrence",
    valueGetter: (params) => monthDayYearTime(params),
    width: 200,
  },
  { field: "clinicalStatus", headerName: "Clinical Status", width: 125 },
  {
    field: "recordedDate",
    headerName: "Recorded Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 175,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const HEALTH_SUMMARY_COLUMNS: GridColDef[] = [
  { field: "category", headerName: "Category", width: 300 },
  { field: "total", headerName: "Total", width: 300 },
];

export const CONDITION_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "code", headerName: "Code", width: 200 },
  { field: "severity", headerName: "Severity", width: 100 },
  { field: "bodySite", headerName: "Body Site", width: 200 },
  {
    field: "recordedDate",
    headerName: "Recorded Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 175,
  },
];

export const CONDITION_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 300 },
  {
    field: "recordedDate",
    headerName: "Recorded Date",
    valueGetter: (recordedDate) => (recordedDate ? new Date(recordedDate) : ""),
    type: "dateTime",
    width: 150,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const LAB_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "code", headerName: "Code", width: 300 },
  {
    field: "effectiveDateTime",
    headerName: "Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 175,
  },
  { field: "value", headerName: "Value", width: 150 },
  { field: "referenceRange", headerName: "Reference Range", width: 200 },
  { field: "note", headerName: "Note", width: 400 },
];

export const LAB_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 300 },
  {
    field: "recordedDate",
    headerName: "Recorded Date",
    valueGetter: (recordedDate) => (recordedDate ? new Date(recordedDate) : ""),
    type: "dateTime",
    width: 150,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const CARE_PLAN_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "category", headerName: "Category", width: 100 },
  {
    field: "activity",
    headerName: "Activity",
    valueGetter: (params) => joinActivity(params),
    width: 200,
  },
  {
    field: "period",
    headerName: "Period",
    valueGetter: (params: any) =>
      params?.start && params?.end
        ? `${monthDayYear(params.start)} - ${monthDayYear(params.end)}`
        : "",
    width: 150,
  },
];

export const CARE_PLAN_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 300 },
  {
    field: "period",
    headerName: "Period",
    valueGetter: (period: any) =>
      period?.start && period?.end
        ? `${monthDayYear(period.start)} - ${monthDayYear(period.end)}`
        : "",
    width: 150,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const ENCOUNTER_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "status", headerName: "Status", width: 150 },
  { field: "type", headerName: "Type", width: 300 },
  { field: "class", headerName: "Class", width: 150 },
  {
    field: "period",
    headerName: "Period",
    valueGetter: (params: any) =>
      params?.start && params?.end
        ? `${monthDayYear(params.start)} - ${monthDayYear(params.end)}`
        : "",
    width: 250,
  },
  { field: "reason", headerName: "Reason", width: 250 },
  { field: "serviceProvider", headerName: "Service Provider", width: 250 },
];

export const ENCOUNTER_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 300 },
  { field: "participant", headerName: "Participant", width: 150 },
  {
    field: "date",
    headerName: "Date",
    type: "dateTime",
    valueGetter: (date) => new Date(date),
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const IMMUNIZATION_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "vaccineCode", headerName: "Vaccine Code", width: 300 },
  { field: "site", headerName: "Site", width: 200 },
  { field: "route", headerName: "Route", width: 200 },
  {
    field: "occurrenceDateTime",
    headerName: "Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 125,
  },
];

export const IMMUNIZATION_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 200 },
  {
    field: "occurrenceDateTime",
    headerName: "Date",
    valueGetter: (occurrenceDateTime) => monthDayYear(occurrenceDateTime),
    width: 250,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const PROCEDURE_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "code", headerName: "Code", width: 200 },
  { field: "performer", headerName: "Performer", width: 200 },
  { field: "outcome", headerName: "Outcome", width: 250 },
  {
    field: "performedDateTime",
    headerName: "Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 125,
  },
];

export const PROCEDURE_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 200 },
  {
    field: "performer",
    headerName: "Performer",
    valueGetter: (performer) => performer[0],
    width: 200,
  },
  {
    field: "performedDate",
    headerName: "Date",
    type: "date",
    valueGetter: (performedDateTime) => new Date(performedDateTime),
    width: 125,
  },
];

export const VITAL_SIGN_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "code", headerName: "Code", width: 100 },
  { field: "value", headerName: "Value", width: 100 },
  { field: "interpretation", headerName: "Interpretation", width: 200 },
  { field: "note", headerName: "Note", width: 200 },
  {
    field: "effectiveDateTime",
    headerName: "Date",
    valueGetter: (params) => monthDayYearTime(params),
    width: 150,
  },
];

export const VITAL_SIGN_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 200 },
  {
    field: "value",
    headerName: "Value",
    valueGetter: (value) => formatValue(value),
    width: 200,
  },
  {
    field: "effectiveDateTime",
    headerName: "Date",
    valueGetter: (effectiveDateTime) => monthDayYear(effectiveDateTime),
    width: 150,
  },
];

export const MEDICATION_GROUP_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "name", headerName: "Name", width: 300 },
  {
    field: "authoredOn",
    headerName: "Date",
    valueGetter: (authoredOn) => (authoredOn ? new Date(authoredOn) : ""),
    type: "dateTime",
    width: 175,
  },
  { field: "sourceDisplay", headerName: "Source", width: 200 },
];

export const MEDICATION_STATEMENT_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  {
    field: "medication",
    headerName: "Name",
    valueGetter: (medication: any) => medication?.text ?? "",
    width: 300,
  },
  {
    field: "dosageInstruction",
    headerName: "Instruction",
    valueGetter: (di: any) => (di?.length ? di[0].text : ""),
    width: 300,
  },
];

export const COVERAGE_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "status", headerName: "Status", width: 100 },
  {
    field: "type",
    headerName: "Type",
    valueGetter: (type: any) =>
      type?.coding?.[0]?.display || type?.text || "N/A",
    width: 200,
  },
  { field: "subscriberId", headerName: "Subscriber ID", width: 150 },
  {
    field: "relationship",
    headerName: "Relationship",
    valueGetter: (relationship: any) =>
      relationship?.coding?.[0]?.display || relationship?.text || "N/A",
    width: 150,
  },
  {
    field: "period",
    headerName: "Period",
    valueGetter: (period: any) =>
      period?.start && period?.end
        ? `${monthDayYear(period.start)} - ${monthDayYear(period.end)}`
        : period?.start
        ? `${monthDayYear(period.start)} - Ongoing`
        : "N/A",
    width: 250,
  },
  {
    field: "payor",
    headerName: "Payor",
    valueGetter: (payor: any) => {
      if (!payor?.length) return "N/A";
      const payorWithDisplay = payor.find((p: any) => p.display);
      return (
        payorWithDisplay?.display ||
        `Org: ${payor[0]?.reference?.split("/")[1] || "Unknown"}`
      );
    },
    width: 200,
  },
  {
    field: "class",
    headerName: "Class/Plan",
    valueGetter: (classArray: any) => {
      if (!classArray?.length) return "N/A";
      const planClass = classArray.find(
        (c: any) => c.type?.coding?.[0]?.code === "plan"
      );
      const groupClass = classArray.find(
        (c: any) => c.type?.coding?.[0]?.code === "group"
      );

      if (planClass?.name) return planClass.name;
      if (groupClass?.name) return groupClass.name;

      return classArray[0]?.name || classArray[0]?.value || "N/A";
    },
    width: 200,
  },
];

export const EXPLANATION_OF_BENEFITS_COLUMNS: GridColDef[] = [
  { field: "id", headerName: "ID", width: 300 },
  { field: "status", headerName: "Status", width: 100 },
  {
    field: "type",
    headerName: "Type",
    valueGetter: (type: any) =>
      type?.coding?.[0]?.display || type?.text || "N/A",
    width: 200,
  },
  {
    field: "use",
    headerName: "Use",
    width: 100,
  },
  {
    field: "patient",
    headerName: "Patient",
    valueGetter: (patient: any) =>
      patient?.display || patient?.reference || "N/A",
    width: 200,
  },
  {
    field: "created",
    headerName: "Created",
    valueGetter: (params) => monthDayYear(params),
    width: 150,
  },
  {
    field: "insurer",
    headerName: "Insurer",
    valueGetter: (insurer: any) =>
      insurer?.display || insurer?.reference || "N/A",
    width: 200,
  },
  {
    field: "provider",
    headerName: "Provider",
    valueGetter: (provider: any) =>
      provider?.display || provider?.reference || "N/A",
    width: 200,
  },
  {
    field: "outcome",
    headerName: "Outcome",
    width: 100,
  },
  {
    field: "total",
    headerName: "Total Amount",
    valueGetter: (total: any) => {
      if (!total?.length) return "N/A";
      const totalAmount = total.find(
        (t: any) => t.category?.coding?.[0]?.code === "total"
      );
      return totalAmount?.amount?.value
        ? `${totalAmount.amount.currency || "$"}${totalAmount.amount.value}`
        : "N/A";
    },
    width: 150,
  },
];

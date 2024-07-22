import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { HealthDataRequestInput, MedicationStatementsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "medicationStatements";

export const getMedicationStatements = createAsyncThunk(
    `${SLICE_NAME}/getMedicationStatements`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getMedicationStatements(new MedicationStatementsRequest(inputParams));
    }
);

export const medicationStatementsSlice = makeHealthDataSlice(SLICE_NAME, getMedicationStatements);
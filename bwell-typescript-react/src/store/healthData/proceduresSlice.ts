import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { HealthDataRequestInput, ProceduresRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "procedures";

export const getProcedures = createAsyncThunk(
    `${SLICE_NAME}/getProcedures`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getProcedures(new ProceduresRequest(inputParams));
    }
);

export const proceduresSlice = makeHealthDataSlice(SLICE_NAME, getProcedures);
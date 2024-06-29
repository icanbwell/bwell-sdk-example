import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ProceduresRequest, ProceduresResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "procedures";

export const getProcedures = createAsyncThunk(
    `${SLICE_NAME}/getProcedures`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getProcedures(new ProceduresRequest(inputParams));
    }
);

export const proceduresSlice = makeHealthDataSlice<BWellQueryResult<ProceduresResults>>(SLICE_NAME, getProcedures);
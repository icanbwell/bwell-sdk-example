import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { MedicationStatementsRequest, MedicationStatementsResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "medicationStatements";

export const getMedicationStatements = createAsyncThunk(
    `${SLICE_NAME}/getMedicationStatements`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getMedicationStatements(new MedicationStatementsRequest(inputParams));
    }
);

export const medicationStatementsSlice = makeHealthDataSlice<BWellQueryResult<MedicationStatementsResults>>(SLICE_NAME, getMedicationStatements);
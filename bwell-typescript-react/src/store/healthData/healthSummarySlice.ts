import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { HealthSummaryResults } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "healthSummary";

export const getHealthSummary = createAsyncThunk(
    `${SLICE_NAME}/getHealthSummary`,
    async () => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getHealthSummary();
    }
);

export const healthSummarySlice = makeHealthDataSlice<BWellQueryResult<HealthSummaryResults>>(SLICE_NAME, getHealthSummary);
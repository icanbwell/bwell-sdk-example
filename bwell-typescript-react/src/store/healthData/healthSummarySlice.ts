import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "healthSummary";

export const getHealthSummary = createAsyncThunk(
    `${SLICE_NAME}/getHealthSummary`,
    async () => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getHealthSummary();
    }
);

export const healthSummarySlice = makeHealthDataSlice(SLICE_NAME, getHealthSummary);
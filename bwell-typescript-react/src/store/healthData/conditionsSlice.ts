import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ConditionsRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "conditions";

export const getConditions = createAsyncThunk(
    `${SLICE_NAME}/getConditions`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getConditions(new ConditionsRequest(inputParams));
    }
);

export const conditionsSlice = makeHealthDataSlice(SLICE_NAME, getConditions);
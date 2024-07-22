import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { LabsRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "labs";

export const getLabs = createAsyncThunk(
    `${SLICE_NAME}/getLabs`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getLabs(new LabsRequest(inputParams));
    }
);

export const labsSlice = makeHealthDataSlice(SLICE_NAME, getLabs);
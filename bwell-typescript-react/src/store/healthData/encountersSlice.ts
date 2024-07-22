import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { EncountersRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "encounters";

export const getEncounters = createAsyncThunk(
    `${SLICE_NAME}/getEncounters`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getEncounters(new EncountersRequest(inputParams));
    }
);

export const encountersSlice = makeHealthDataSlice(SLICE_NAME, getEncounters);
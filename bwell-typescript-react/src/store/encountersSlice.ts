import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { EncountersRequest, EncountersResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "encounters";

export const getEncounters = createAsyncThunk(
    `${SLICE_NAME}/getEncounters`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getEncounters(new EncountersRequest(inputParams));
    }
);

export const encountersSlice = makeHealthDataSlice<BWellQueryResult<EncountersResults>>(SLICE_NAME, getEncounters);
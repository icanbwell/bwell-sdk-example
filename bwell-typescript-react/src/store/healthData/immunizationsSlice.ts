import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ImmunizationsRequest, ImmunizationsResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "immunizations";

export const getImmunizations = createAsyncThunk(
    `${SLICE_NAME}/getImmunizations`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getImmunizations(new ImmunizationsRequest(inputParams));
    }
);

export const immunizationsSlice = makeHealthDataSlice<BWellQueryResult<ImmunizationsResults>>(SLICE_NAME, getImmunizations);
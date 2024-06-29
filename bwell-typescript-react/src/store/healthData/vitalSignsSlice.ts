import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { VitalSignsRequest, VitalSignsResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "vitalSigns";

export const getVitalSigns = createAsyncThunk(
    `${SLICE_NAME}/getVitalSigns`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getVitalSigns(new VitalSignsRequest(inputParams));
    }
);

export const vitalSignsSlice = makeHealthDataSlice<BWellQueryResult<VitalSignsResults>>(SLICE_NAME, getVitalSigns);
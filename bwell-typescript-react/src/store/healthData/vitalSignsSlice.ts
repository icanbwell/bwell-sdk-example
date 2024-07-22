import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { HealthDataRequestInput, VitalSignsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "vitalSigns";

export const getVitalSigns = createAsyncThunk(
    `${SLICE_NAME}/getVitalSigns`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getVitalSigns(new VitalSignsRequest(inputParams));
    }
);

export const vitalSignsSlice = makeHealthDataSlice(SLICE_NAME, getVitalSigns);
import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { CarePlansRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "carePlans";

export const getCarePlans = createAsyncThunk(
    `${SLICE_NAME}/getCarePlans`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getCarePlans(new CarePlansRequest(inputParams));
    }
);

export const carePlansSlice = makeHealthDataSlice(SLICE_NAME, getCarePlans);
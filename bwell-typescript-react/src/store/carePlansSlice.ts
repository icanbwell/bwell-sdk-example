import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { CarePlansRequest, CarePlansResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "carePlans";

export const getCarePlans = createAsyncThunk(
    `${SLICE_NAME}/getCarePlans`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getCarePlans(new CarePlansRequest(inputParams));
    }
);

export const carePlansSlice = makeHealthDataSlice<BWellQueryResult<CarePlansResults>>(SLICE_NAME, getCarePlans);
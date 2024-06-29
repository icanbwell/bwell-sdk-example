import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ConditionsRequest, ConditionsResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "conditions";

export const getConditions = createAsyncThunk(
    `${SLICE_NAME}/getConditions`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getConditions(new ConditionsRequest(inputParams));
    }
);

export const conditionsSlice = makeHealthDataSlice<BWellQueryResult<ConditionsResults>>(SLICE_NAME, getConditions);
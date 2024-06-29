import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { LabsRequest, LabsResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "labs";

export const getLabs = createAsyncThunk(
    `${SLICE_NAME}/getLabs`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getLabs(new LabsRequest(inputParams));
    }
);

export const labsSlice = makeHealthDataSlice<BWellQueryResult<LabsResults>>(SLICE_NAME, getLabs);
import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { LabGroupsResults, LabGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "labGroups";

export const getLabGroups = createAsyncThunk(
    `${SLICE_NAME}/getLabGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getLabGroups(new LabGroupsRequest(inputParams));
    }
);

export const labGroupsSlice = makeHealthDataSlice<BWellQueryResult<LabGroupsResults>>(SLICE_NAME, getLabGroups);
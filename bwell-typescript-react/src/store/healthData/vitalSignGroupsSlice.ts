import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { VitalSignGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "vitalSignGroups";

export const getVitalSignGroups = createAsyncThunk(
    `${SLICE_NAME}/getVitalSignGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getVitalSignGroups(new VitalSignGroupsRequest(inputParams));
    }
);

export const vitalSignGroupsSlice = makeHealthDataSlice(SLICE_NAME, getVitalSignGroups);
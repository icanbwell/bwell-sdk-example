import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { LabGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "labGroups";

export const getLabGroups = createAsyncThunk(
    `${SLICE_NAME}/getLabGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getLabGroups(new LabGroupsRequest(inputParams));
    }
);

export const labGroupsSlice = makeHealthDataSlice(SLICE_NAME, getLabGroups);
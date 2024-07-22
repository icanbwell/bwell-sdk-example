import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ConditionGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "conditionGroups";

export const getConditionGroups = createAsyncThunk(
    `${SLICE_NAME}/getConditionGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getConditionGroups(new ConditionGroupsRequest(inputParams));
    }
);

export const conditionGroupsSlice = makeHealthDataSlice(SLICE_NAME, getConditionGroups);
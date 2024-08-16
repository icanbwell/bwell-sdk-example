import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { AllergyIntoleranceGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "allergyIntoleranceGroups";

export const getAllergyIntoleranceGroups = createAsyncThunk(
    `${SLICE_NAME}/getAllergyIntoleranceGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getAllergyIntoleranceGroups(new AllergyIntoleranceGroupsRequest(inputParams));
    }
);

export const allergyIntoleranceGroupsSlice = makeHealthDataSlice(SLICE_NAME, getAllergyIntoleranceGroups);
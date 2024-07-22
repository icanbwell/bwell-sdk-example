import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ImmunizationGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "immunizationGroups";

export const getImmunizationGroups = createAsyncThunk(
    "labGroups/getImmunizationGroups",
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getImmunizationGroups(new ImmunizationGroupsRequest(inputParams));
    }
);

export const immunizationGroupsSlice = makeHealthDataSlice(SLICE_NAME, getImmunizationGroups);
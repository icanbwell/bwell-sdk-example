import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { MedicationGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "medicationGroups";

export const getMedicationGroups = createAsyncThunk(
    `${SLICE_NAME}/getMedicationGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getMedicationGroups(new MedicationGroupsRequest(inputParams));
    }
);

export const medicationGroupsSlice = makeHealthDataSlice(SLICE_NAME, getMedicationGroups);
import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { EncounterGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "encounterGroups";

export const getEncounterGroups = createAsyncThunk(
    `${SLICE_NAME}/getEncounterGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getEncounterGroups(new EncounterGroupsRequest(inputParams));
    }
);

export const encounterGroupsSlice = makeHealthDataSlice(SLICE_NAME, getEncounterGroups);
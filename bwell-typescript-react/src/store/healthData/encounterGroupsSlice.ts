import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { EncounterGroupsResults, EncounterGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "encounterGroups";

export const getEncounterGroups = createAsyncThunk(
    `${SLICE_NAME}/getEncounterGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getEncounterGroups(new EncounterGroupsRequest(inputParams));
    }
);

export const encounterGroupsSlice = makeHealthDataSlice<BWellQueryResult<EncounterGroupsResults>>(SLICE_NAME, getEncounterGroups);
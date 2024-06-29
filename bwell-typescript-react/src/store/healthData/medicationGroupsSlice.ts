import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { MedicationGroupsRequest, MedicationGroupsResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "medicationGroups";

export const getMedicationGroups = createAsyncThunk(
    `${SLICE_NAME}/getAllergyIntolerances`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getMedicationGroups(new MedicationGroupsRequest(inputParams));
    }
);

export const medicationGroupsSlice = makeHealthDataSlice<BWellQueryResult<MedicationGroupsResults>>(SLICE_NAME, getMedicationGroups);
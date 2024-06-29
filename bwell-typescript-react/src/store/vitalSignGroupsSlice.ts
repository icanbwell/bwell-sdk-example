import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { VitalSignGroupsResults, VitalSignGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "vitalSignGroups";

export const getVitalSignGroups = createAsyncThunk(
    `${SLICE_NAME}/getVitalSignGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getVitalSignGroups(new VitalSignGroupsRequest(inputParams));
    }
);

export const vitalSignGroupsSlice = makeHealthDataSlice<BWellQueryResult<VitalSignGroupsResults>>(SLICE_NAME, getVitalSignGroups);
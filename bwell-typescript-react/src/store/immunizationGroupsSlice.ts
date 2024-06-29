import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { ImmunizationGroupsResults, ImmunizationGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "immunizationGroups";

export const getImmunizationGroups = createAsyncThunk(
    "labGroups/getImmunizationGroups",
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getImmunizationGroups(new ImmunizationGroupsRequest(inputParams));
    }
);

export const immunizationGroupsSlice = makeHealthDataSlice<BWellQueryResult<ImmunizationGroupsResults>>(SLICE_NAME, getImmunizationGroups);
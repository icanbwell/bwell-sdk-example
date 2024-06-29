import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { ConditionGroupsResults, ConditionGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "conditionGroups";

export const getConditionGroups = createAsyncThunk(
    `${SLICE_NAME}/getConditionGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getConditionGroups(new ConditionGroupsRequest(inputParams));
    }
);

export const conditionGroupsSlice = makeHealthDataSlice<BWellQueryResult<ConditionGroupsResults>>(SLICE_NAME, getConditionGroups);
import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { AllergyIntoleranceGroupsRequest, AllergyIntolerancesGroupsResults } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "allergyIntoleranceGroups";

export const getAllergyIntoleranceGroups = createAsyncThunk(
    `${SLICE_NAME}/getAllergyIntoleranceGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getAllergyIntoleranceGroups(new AllergyIntoleranceGroupsRequest(inputParams));
    }
);

export const allergyIntoleranceGroupsSlice = makeHealthDataSlice<BWellQueryResult<AllergyIntolerancesGroupsResults>>(SLICE_NAME, getAllergyIntoleranceGroups);
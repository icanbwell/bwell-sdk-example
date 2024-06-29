import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { CarePlanGroupsResults, CarePlanGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "carePlanGroups";

export const getCarePlanGroups = createAsyncThunk(
    `${SLICE_NAME}/getCarePlanGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getCarePlanGroups(new CarePlanGroupsRequest(inputParams));
    }
);

export const carePlanGroupsSlice = makeHealthDataSlice<BWellQueryResult<CarePlanGroupsResults>>(SLICE_NAME, getCarePlanGroups);
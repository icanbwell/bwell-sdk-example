import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { CarePlanGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequest, makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "carePlanGroups";

export const getCarePlanGroups = createAsyncThunk(
    `${SLICE_NAME}/getCarePlanGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getCarePlanGroups(new CarePlanGroupsRequest(inputParams));
    }
);

export const carePlanGroupsSlice = makeHealthDataSlice(SLICE_NAME, getCarePlanGroups);
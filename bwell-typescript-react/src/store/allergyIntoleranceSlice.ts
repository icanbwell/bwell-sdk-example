import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { AllergyIntolerancesRequest, AllergyIntolerancesResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";
import makeHealthDataSlice from "./healthData/makeHealthDataSlice";

const SLICE_NAME = "allergyIntolerances";

export const getAllergyIntolerances = createAsyncThunk(
    `${SLICE_NAME}/getAllergyIntolerances`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getAllergyIntolerances(new AllergyIntolerancesRequest(inputParams));
    }
);

export const allergyIntolerancesSlice = makeHealthDataSlice<BWellQueryResult<AllergyIntolerancesResults>>(SLICE_NAME, getAllergyIntolerances);
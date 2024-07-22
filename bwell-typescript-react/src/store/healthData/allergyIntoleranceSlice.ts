import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { AllergyIntolerancesRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "allergyIntolerances";

export const getAllergyIntolerances = createAsyncThunk(
    `${SLICE_NAME}/getAllergyIntolerances`,
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getAllergyIntolerances(new AllergyIntolerancesRequest(inputParams));
    }
);

export const allergyIntolerancesSlice = makeHealthDataSlice(SLICE_NAME, getAllergyIntolerances);
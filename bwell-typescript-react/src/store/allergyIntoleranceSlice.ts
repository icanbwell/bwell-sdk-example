import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { AllergyIntolerancesRequest, AllergyIntolerancesResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";

export const getAllergyIntolerances = createAsyncThunk(
    "allergyIntolerances/getAllergyIntolerances",
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        const request = new AllergyIntolerancesRequest(inputParams);
        return bWellSdk?.health.getAllergyIntolerances(request);
    }
);

export const allergyIntolerancesSlice = createSlice({
    name: "allergyIntolerances",
    initialState: {
        healthData: null as BWellQueryResult<AllergyIntolerancesResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getAllergyIntolerances.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getAllergyIntolerances.fulfilled, (state, action: PayloadAction<BWellQueryResult<AllergyIntolerancesResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getAllergyIntolerances.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.loading = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            });
            
    },
});
import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { EncountersRequest, EncountersResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";

export const getEncounters = createAsyncThunk(
    "labs/getEncounters",
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getEncounters(new EncountersRequest(inputParams));
    }
);

export const encountersSlice = createSlice({
    name: "labs",
    initialState: {
        healthData: null as BWellQueryResult<EncountersResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getEncounters.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getEncounters.fulfilled, (state, action: PayloadAction<BWellQueryResult<EncountersResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getEncounters.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.loading = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            });

    },
});
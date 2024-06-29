import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { HealthSummaryResults } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";

export const getHealthSummary = createAsyncThunk(
    "healthSummary/getHealthSummary",
    async () => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getHealthSummary();
    }
);

export const healthSummarySlice = createSlice({
    name: "healthSummary",
    initialState: {
        healthData: null as BWellQueryResult<HealthSummaryResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getHealthSummary.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getHealthSummary.fulfilled, (state, action: PayloadAction<BWellQueryResult<HealthSummaryResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }
                
                state.loading = false;
                state.error = "";
            })
            .addCase(getHealthSummary.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.loading = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            });
            
    },
});
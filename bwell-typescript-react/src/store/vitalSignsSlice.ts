import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { VitalSignsRequest, VitalSignGroupsRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";

export const getVitalSigns = createAsyncThunk(
    "vitalSigns/getVitalSigns",
    async (inputParams: HealthDataRequestInput) => {
        console.log('getVitalSigns inputParams', inputParams);
        const bWellSdk = getSdk();
        return bWellSdk?.health.getVitalSigns(new VitalSignsRequest(inputParams));
    }
);

export const vitalSignsSlice = createSlice({
    name: "vitalSigns",
    initialState: {
        healthData: null as BWellQueryResult<VitalSignGroupsRequest> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getVitalSigns.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getVitalSigns.fulfilled, (state, action: PayloadAction<BWellQueryResult<VitalSignGroupsRequest>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getVitalSigns.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.loading = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            });

    },
});
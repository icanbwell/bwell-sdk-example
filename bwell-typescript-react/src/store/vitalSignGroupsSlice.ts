import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { VitalSignGroupsResults, VitalSignGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";

export const getVitalSignGroups = createAsyncThunk(
    "vitalSigns/getVitalSignGroups",
    async (inputParams: PagedRequestInput) => {
        console.log('inputParams', inputParams);
        const bWellSdk = getSdk();
        const request = new VitalSignGroupsRequest(inputParams);
        return bWellSdk?.health.getVitalSignGroups(request);
    }
);

export const vitalSignGroupsSlice = createSlice({
    name: "vitalSignGroups",
    initialState: {
        healthData: null as BWellQueryResult<VitalSignGroupsResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getVitalSignGroups.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getVitalSignGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<VitalSignGroupsResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getVitalSignGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.healthData = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            })
    },
});
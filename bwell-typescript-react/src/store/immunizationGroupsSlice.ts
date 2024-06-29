import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { ImmunizationGroupsResults, ImmunizationGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";

export const getImmunizationGroups = createAsyncThunk(
    "labGroups/getImmunizationGroups",
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        const request = new ImmunizationGroupsRequest(inputParams);
        return bWellSdk?.health.getImmunizationGroups(request);
    }
);

export const immunizationGroupsSlice = createSlice({
    name: "carePlanGroups",
    initialState: {
        healthData: null as BWellQueryResult<ImmunizationGroupsResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getImmunizationGroups.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getImmunizationGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<ImmunizationGroupsResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getImmunizationGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.healthData = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            })
    },
});
import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { EncounterGroupsResults, EncounterGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";

export const getEncounterGroups = createAsyncThunk(
    "labGroups/getEncounterGroups",
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        const request = new EncounterGroupsRequest(inputParams);
        return bWellSdk?.health.getEncounterGroups(request);
    }
);

export const encounterGroupsSlice = createSlice({
    name: "carePlanGroups",
    initialState: {
        healthData: null as BWellQueryResult<EncounterGroupsResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getEncounterGroups.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getEncounterGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<EncounterGroupsResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getEncounterGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.healthData = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            })
    },
});
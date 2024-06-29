import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { ProcedureGroupsResults, ProcedureGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";

export const getProcedureGroups = createAsyncThunk(
    "labGroups/getProcedureGroups",
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        const request = new ProcedureGroupsRequest(inputParams);
        return bWellSdk?.health.getProcedureGroups(request);
    }
);

export const immunizationGroupsSlice = createSlice({
    name: "procedureGroups",
    initialState: {
        healthData: null as BWellQueryResult<ProcedureGroupsResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getProcedureGroups.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getProcedureGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<ProcedureGroupsResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getProcedureGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.healthData = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            })
    },
});
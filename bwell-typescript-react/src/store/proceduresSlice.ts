import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ProceduresRequest, ProceduresResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { BWellQueryResult } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/common/results";

export const getProcedures = createAsyncThunk(
    "labs/getProcedures",
    async (inputParams: HealthDataRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getProcedures(new ProceduresRequest(inputParams));
    }
);

export const proceduresSlice = createSlice({
    name: "procedures",
    initialState: {
        healthData: null as BWellQueryResult<ProceduresResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getProcedures.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getProcedures.fulfilled, (state, action: PayloadAction<BWellQueryResult<ProceduresResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getProcedures.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.loading = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            });

    },
});
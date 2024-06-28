import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { AllergyIntoleranceGroupsRequest, AllergyIntolerancesGroupsResults } from "@icanbwell/bwell-sdk-ts";
import { RootState } from "./store";


export const getAllergyIntoleranceGroups = createAsyncThunk(
    "allergyIntoleranceGroups/getAllergyIntoleranceGroups",
    async ({ page, pageSize }: { page: number, pageSize: number }, { getState }) => {
        const state = getState();
        const bWellSdk = await getSdk(state as RootState);
        const request = new AllergyIntoleranceGroupsRequest({ page, pageSize, });
        return bWellSdk.health.getAllergyIntoleranceGroups(request);
    }
);

export const allergyIntoleranceGroupsSlice = createSlice({
    name: "allergyIntoleranceGroups",
    initialState: {
        healthData: null as BWellQueryResult<AllergyIntolerancesGroupsResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getAllergyIntoleranceGroups.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getAllergyIntoleranceGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<AllergyIntolerancesGroupsResults>>) => {
                console.log('state', state);
                console.log('action', action);
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getAllergyIntoleranceGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.healthData = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            })
    },
});
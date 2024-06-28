import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { AllergyIntoleranceGroupsRequest, AllergyIntolerancesGroupsResults, AllergyIntolerancesRequest, AllergyIntolerancesResults, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";
import { RootState } from "./store";


export const getAllergyIntoleranceGroups = createAsyncThunk(
    "allergyIntolerance/getAllergyIntoleranceGroups",
    async ({ page, pageSize }: { page: number, pageSize: number }, { getState }) => {
        const state = getState();
        const bWellSdk = await getSdk(state as RootState);
        const request = new AllergyIntoleranceGroupsRequest({ page, pageSize, });
        return bWellSdk.health.getAllergyIntoleranceGroups(request);
    }
);

export const getAllergyIntolerances = createAsyncThunk(
    "allergyIntolerance/getAllergyIntolerances",
    async (inputParams: HealthDataRequestInput, { getState }) => {
        const state = getState();
        const bWellSdk = await getSdk(state as RootState);
        const request = new AllergyIntolerancesRequest(inputParams);
        return bWellSdk.health.getAllergyIntolerances(request);
    }
);

export const allergyIntoleranceGroupsSlice = createSlice({
    name: "allergyIntoleranceGroups",
    initialState: {
        allergyIntolerances: null as BWellQueryResult<AllergyIntolerancesResults> | null,
        intolerancesLoading: false,
        intolerancesError: null as string | null,
        allergyIntoleranceGroups: null as BWellQueryResult<AllergyIntolerancesGroupsResults> | null,
        groupsLoading: false,
        groupsError: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getAllergyIntoleranceGroups.pending, (state) => {
                state.groupsLoading = true;
                state.groupsError = null;
                state.allergyIntoleranceGroups = null;
            })
            .addCase(getAllergyIntoleranceGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<AllergyIntolerancesGroupsResults>>) => {
                if (action.payload.error) {
                    state.groupsError = action.payload.error.message ?? "Unknown error";
                } else {
                    state.allergyIntoleranceGroups = action.payload || [];
                }

                state.groupsLoading = false;
                state.groupsError = "";
            })
            .addCase(getAllergyIntoleranceGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.groupsLoading = true;
                } else {
                    state.groupsError = action.error.message ?? "Unknown error";
                }
            })
            .addCase(getAllergyIntolerances.pending, (state) => {
                state.intolerancesLoading = true;
                state.intolerancesError = null;
                state.allergyIntolerances = null;
            })
            .addCase(getAllergyIntolerances.fulfilled, (state, action: PayloadAction<BWellQueryResult<AllergyIntolerancesGroupsResults>>) => {
                if (action.payload.error) {
                    state.intolerancesError = action.payload.error.message ?? "Unknown error";
                } else {
                    state.allergyIntolerances = action.payload || [];
                }

                state.intolerancesLoading = false;
                state.intolerancesError = "";
            })
            .addCase(getAllergyIntolerances.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.intolerancesLoading = true;
                } else {
                    state.intolerancesError = action.error.message ?? "Unknown error";
                }
            });
            
    },
});
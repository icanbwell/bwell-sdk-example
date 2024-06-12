import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { bWellSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { AllergyIntoleranceGroupsRequest, AllergyIntolerancesGroupsResults, AllergyIntolerancesRequest, HealthDataRequestInput } from "@icanbwell/bwell-sdk-ts";

export const getAllergyIntoleranceGroups = createAsyncThunk(
    "allergyIntolerance/getAllergyIntoleranceGroups",
    async ({ page, pageSize }: { page: number, pageSize: number }) => {
        const request = new AllergyIntoleranceGroupsRequest({ page, pageSize, });
        return bWellSdk.health.getAllergyIntoleranceGroups(request);
    }
);

export const getAllergyIntolerances = createAsyncThunk(
    "allergyIntolerance/getAllergyIntolerances",
    async (inputParams: HealthDataRequestInput) => {
        const request = new AllergyIntolerancesRequest(inputParams);
        return bWellSdk.health.getAllergyIntolerances(request);
    }
);

export const allergyIntoleranceGroupsSlice = createSlice({
    name: "allergyIntoleranceGroups",
    initialState: {
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
            })
            .addCase(getAllergyIntoleranceGroups.rejected, (state, action) => {
                state.groupsError = action.error.message ?? "Unknown error";
                state.groupsLoading = false;
            });
    },
});
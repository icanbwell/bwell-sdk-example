import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { bWellSdk } from "@/sdk/bWellSdk";
import { AllergyIntolerancesGroupsResults, PagedRequest } from "@icanbwell/bwell-sdk-ts/dist/api";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";

export const getAllergyIntoleranceGroups = createAsyncThunk(
    "allergyIntolerance/getAllergyIntoleranceGroups",
    async ({ page, pageSize }: { page: number, pageSize: number }) => {
        const request = new PagedRequest({ page, pageSize, });
        return bWellSdk.health.getAllergyIntoleranceGroups(request);
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
            })
            .addCase(getAllergyIntoleranceGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<AllergyIntolerancesGroupsResults>>) => {
                state.allergyIntoleranceGroups = action.payload || [];
                state.groupsLoading = false;
            })
            .addCase(getAllergyIntoleranceGroups.rejected, (state, action) => {
                state.groupsError = action.error.message ?? "Unknown error";
                state.groupsLoading = false;
            });
    },
});
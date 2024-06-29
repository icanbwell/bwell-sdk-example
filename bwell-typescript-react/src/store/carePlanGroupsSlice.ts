import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { CarePlanGroupsResults, CarePlanGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";

export const getCarePlanGroups = createAsyncThunk(
    "labGroups/getCarePlanGroups",
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        const request = new CarePlanGroupsRequest(inputParams);
        return bWellSdk?.health.getCarePlanGroups(request);
    }
);

export const carePlanGroupsSlice = createSlice({
    name: "carePlanGroups",
    initialState: {
        healthData: null as BWellQueryResult<CarePlanGroupsResults> | null,
        loading: false,
        error: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getCarePlanGroups.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.healthData = null;
            })
            .addCase(getCarePlanGroups.fulfilled, (state, action: PayloadAction<BWellQueryResult<CarePlanGroupsResults>>) => {
                if (action.payload.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    state.healthData = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getCarePlanGroups.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.healthData = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            })
    },
});
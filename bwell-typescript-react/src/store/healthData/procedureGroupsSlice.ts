import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { BWellQueryResult } from "@icanbwell/bwell-sdk-ts/dist/common/results";
import { ProcedureGroupsResults, ProcedureGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { PagedRequestInput } from "../../.yalc/@icanbwell/bwell-sdk-ts/dist/api/base/requests/paged-request";
import makeHealthDataSlice from "./makeHealthDataSlice";

const SLICE_NAME = "procedureGroups";

export const getProcedureGroups = createAsyncThunk(
    `${SLICE_NAME}/getProcedureGroups`,
    async (inputParams: PagedRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getProcedureGroups(new ProcedureGroupsRequest(inputParams));
    }
);

export const procedureGroupsSlice = makeHealthDataSlice<BWellQueryResult<ProcedureGroupsResults>>(SLICE_NAME, getProcedureGroups);
import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { ProcedureGroupsRequest } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice, PagedRequest } from "./makeHealthDataSlice";

const SLICE_NAME = "procedureGroups";

export const getProcedureGroups = createAsyncThunk(
    `${SLICE_NAME}/getProcedureGroups`,
    async (inputParams: PagedRequest) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getProcedureGroups(new ProcedureGroupsRequest(inputParams));
    }
);

export const procedureGroupsSlice = makeHealthDataSlice(SLICE_NAME, getProcedureGroups);
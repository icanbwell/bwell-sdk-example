import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { LabKnowledgeRequest, LabKnowledgeRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "labKnowledge";

export const getLabKnowledge = createAsyncThunk(
    `${SLICE_NAME}/getLabKnowledge`,
    async (inputParams: LabKnowledgeRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getLabKnowledge(new LabKnowledgeRequest(inputParams));
    }
);

export const labKnowledgeSlice = makeHealthDataSlice(SLICE_NAME, getLabKnowledge);
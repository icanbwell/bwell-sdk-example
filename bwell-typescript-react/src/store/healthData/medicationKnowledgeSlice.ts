import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { MedicationKnowledgeRequest, MedicationKnowledgeRequestInput } from "@icanbwell/bwell-sdk-ts";
import { makeHealthDataSlice } from "./makeHealthDataSlice";

const SLICE_NAME = "medicationKnowledge";

export const getMedicationKnowledge = createAsyncThunk(
    `${SLICE_NAME}/getMedicationKnowledge`,
    async (inputParams: MedicationKnowledgeRequestInput) => {
        const bWellSdk = getSdk();
        return bWellSdk?.health.getMedicationKnowledge(new MedicationKnowledgeRequest(inputParams));
    }
);

export const medicationKnowledgeSlice = makeHealthDataSlice(SLICE_NAME, getMedicationKnowledge);
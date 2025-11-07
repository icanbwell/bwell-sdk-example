import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import {
  ExplanationOfBenefitRequestInput,
  ExplanationOfBenefitsRequest,
} from "@icanbwell/bwell-sdk-ts";
import { makeFinancialSlice } from "./makeFinancialSlice";

const SLICE_NAME = "explanationOfBenefits";

export const getExplanationOfBenefits = createAsyncThunk(
  `${SLICE_NAME}/getExplanationOfBenefits`,
  async (inputParams: ExplanationOfBenefitRequestInput) => {
    const bWellSdk = getSdk();
    const result = await bWellSdk?.financial.getExplanationOfBenefits(
      new ExplanationOfBenefitsRequest(inputParams)
    );
    return result;
  }
);

export const explanationOfBenefitsSlice = makeFinancialSlice(
  SLICE_NAME,
  getExplanationOfBenefits
);

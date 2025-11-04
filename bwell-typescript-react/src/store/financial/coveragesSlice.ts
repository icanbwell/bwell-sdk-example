import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import {
  CoverageRequestInput,
  CoveragesRequest,
} from "@icanbwell/bwell-sdk-ts";
import { makeFinancialSlice } from "./makeFinancialSlice";

const SLICE_NAME = "coverages";

export const getCoverages = createAsyncThunk(
  `${SLICE_NAME}/getCoverages`,
  async (inputParams: CoverageRequestInput) => {
    const bWellSdk = getSdk();
    return bWellSdk?.financial.getCoverages(new CoveragesRequest(inputParams));
  }
);

export const coveragesSlice = makeFinancialSlice(SLICE_NAME, getCoverages);

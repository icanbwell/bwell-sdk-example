import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { SearchHealthResourcesRequestInput, SearchHealthResourcesRequest } from "@icanbwell/bwell-sdk-ts";

export const searchHealthResources = createAsyncThunk(
  "search/searchHealthResources",
  async (input: SearchHealthResourcesRequestInput) => {
    const sdk = getSdk();
    // The SearchManager endpoint is likely under sdk.search
    return sdk?.search.searchHealthResources(new SearchHealthResourcesRequest(input));
  }
);

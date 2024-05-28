import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

import { initializeSdk, bWellSdk } from "@/sdk/bWellSdk";
import { OperationOutcome } from "@icanbwell/bwell-sdk-ts/dist/common/models/responses";

interface InitializationState {
  clientKey: string | null;
  isInitialized: boolean;
  loading: boolean;
  error: string | null;
}

const initialState: InitializationState = {
  clientKey: null,
  isInitialized: false,
  loading: false,
  error: null,
};

export const initialize = createAsyncThunk<
  string,
  { clientKey: string },
  { rejectValue: string }
>(
  "initialization/initialize",
  async ({ clientKey }, { rejectWithValue }) => {
    try {
      await initializeSdk(clientKey);
      const initializeOutcome: OperationOutcome = await bWellSdk.initialize();
      const success = initializeOutcome.success();

      if (!success)
        return rejectWithValue(initializeOutcome.message() ?? "Unknown error");

      return clientKey;
    } catch (error) {
      return rejectWithValue("Error initializing SDK");
    }
  }
);

export const initializationSlice = createSlice({
  name: "initialization",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(initialize.pending, (state) => {
        state.loading = true;
      })
      .addCase(initialize.fulfilled, (state, action) => {
        state.clientKey = action.payload;
        state.isInitialized = true;
        state.loading = false;
      })
      .addCase(initialize.rejected, (state, action) => {
        state.error = action.payload ?? "Unknown error";
        state.loading = false;
      });
  },
});

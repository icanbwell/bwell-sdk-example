import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

import { authenticateSdk, initializeSdk } from "@/sdk/bWellSdk";
import { OperationOutcome } from "@icanbwell/bwell-sdk-ts/dist/common/models/responses";

interface UserState {
  clientKey?: string;
  oauthCreds?: string;
  isRehydrated: boolean;
  isInitialized: boolean;
  isLoggedIn: boolean;
  loading: boolean;
  error: string | null;
}

export const authenticate = createAsyncThunk<
  string,
  { oauthCreds: string },
  { rejectValue: string }
>("user/authenticate", async ({ oauthCreds }, { rejectWithValue }) => {
  try {
    const authenticationOutcome: OperationOutcome = await authenticateSdk(oauthCreds);

    const success = authenticationOutcome.success();

    if (!success)
      return rejectWithValue(authenticationOutcome.message() ?? "Unknown error");

    return oauthCreds;
  } catch (error) {
    return rejectWithValue("Unhandled error logging in user.");
  }
});

export const initialize = createAsyncThunk<
  string,
  { clientKey: string },
  { rejectValue: string }
>("user/initialize", async ({ clientKey }, { rejectWithValue }) => {
  try {
    initializeSdk(clientKey);
    return clientKey;
  } catch (error) {
    if (typeof error === "string") return rejectWithValue(error);

    return rejectWithValue(error?.message);
  }
});

const initialState: UserState = {
  isLoggedIn: false,
  isRehydrated: false,
  isInitialized: false,
  loading: false,
  error: null,
};

export const userSlice = createSlice({
  name: "user",
  initialState,
  reducers: {
    resetState: (state) => {
      Object.assign(state, initialState);
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(authenticate.pending, (state) => {
        state.loading = true;
        state.error = "";
        state.isLoggedIn = false;
      })
      .addCase(authenticate.fulfilled, (state, action) => {
        state.oauthCreds = action.payload;
        state.isLoggedIn = true;
        state.loading = false;
        state.error = "";
      })
      .addCase(authenticate.rejected, (state) => {
        state.error = "Error while authenticating";
        state.loading = false;
        state.isLoggedIn = false;
      })
      .addCase(initialize.pending, (state) => {
        state.loading = true;
        state.error = "";
        state.isLoggedIn = false;
        state.isInitialized = false;
      })
      .addCase(initialize.fulfilled, (state, action) => {
        state.clientKey = action.payload;
        state.isInitialized = true;
        state.loading = false;
        state.error = "";
      })
      .addCase(initialize.rejected, (state, action) => {
        state.error = action.payload ?? "Error while initializing";
        state.loading = false;
        state.isInitialized = false;
        state.isLoggedIn = false;
        state.clientKey = undefined;
        state.oauthCreds = undefined;
      });
  },
});

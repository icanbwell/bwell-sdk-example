import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { authenticateSdk, initializeSdk } from "@/sdk/bWellSdk";

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
  { oauthCreds?: string; username?: string; password?: string },
  { rejectValue: string }
>("user/authenticate", async (params, { rejectWithValue }) => {
  try {
    let authenticationOutcome;
    if (params.oauthCreds) {
      authenticationOutcome = await authenticateSdk(params.oauthCreds);
    } else if (params.username && params.password) {
      authenticationOutcome = await authenticateSdk({
        email: params.username,
        password: params.password,
      });
    } else {
      return rejectWithValue("Missing authentication credentials");
    }
    const success = authenticationOutcome.success();
    if (!success)
      return rejectWithValue(authenticationOutcome.error().message ?? "Unknown error");
    return params.oauthCreds ?? params.username ?? "";
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
    await initializeSdk(clientKey);
    return clientKey;
  } catch (error) {
    if (typeof error === "string") return rejectWithValue(error);

    // @ts-ignore TODO: add strong typing in this block
    return rejectWithValue(error?.message ? error.message : "Unable to initialize SDK");
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
      state.isInitialized = false;
      state.isLoggedIn = false;
      state.isRehydrated = true;
      state.clientKey = undefined;
      state.oauthCreds = undefined;
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

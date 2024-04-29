import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

export class User {
  id: string;
  name: string;
  email: string;

  constructor(id: string, name: string, email: string) {
    this.id = id;
    this.name = name;
    this.email = email;
  }
}

interface UserState {
  userInfo: User | null;
  isLoggedIn: boolean;
  loading: boolean;
  error: string | null;
}

export const loginUser = createAsyncThunk<
  User,
  { oauthCreds: string },
  { rejectValue: string }
>("user/login", async ({ oauthCreds }, { rejectWithValue }) => {
  try {
    console.log(`Logging in user with OAuth credentials: ${oauthCreds}...`);

    //TODO: Replace with call to b.well SDK
    return { id: "1", name: "Kyle Wade", email: "kyle.wade@icanbwell.com" };
  } catch (error) {
    return rejectWithValue("error logging in user");
  }
});

const initialState: UserState = {
  userInfo: null,
  isLoggedIn: false,
  loading: false,
  error: null,
};

export const userSlice = createSlice({
  name: "user",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.userInfo = action.payload;
        state.isLoggedIn = true;
        state.loading = false;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.error = action.payload || "Unknown error";
        state.loading = false;
      });
  },
});

import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { RootState } from "./store";

export const getMemberConnections = createAsyncThunk(
    "connections/memberConnections",
    async (_, { getState }) => {
        const state = getState();
        const bWellSdk = await getSdk(state as RootState);
        return bWellSdk.connection.getMemberConnections();
    }
);

export const connectionSlice = createSlice({
    name: "connections",
    initialState: {
        memberConnections: null,
        connectionsLoading: false,
        connectionsError: null as string | null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getMemberConnections.pending, (state) => {
                state.connectionsLoading = true;
                state.connectionsError = null;
                state.memberConnections = null;
            })
            .addCase(getMemberConnections.fulfilled, (state, action: PayloadAction<any>) => {
                if (action.payload.error) {
                    state.connectionsError = action.payload.error.message ?? "Unknown error";
                } else {
                    state.memberConnections = action.payload || [];
                }
                state.connectionsLoading = false;
                state.connectionsError = "";
            })
            .addCase(getMemberConnections.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.connectionsLoading = true;
                } else {
                    state.connectionsError = action.error.message ?? "Unknown error";
                } 
            });
    },
});
import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";

const bWellSdk = getSdk();

export const getMemberConnections = createAsyncThunk(
    "connections/memberConnections",
    async () => {
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
            })
            .addCase(getMemberConnections.rejected, (state, action) => {
                state.connectionsError = action.error.message ?? "Unknown error";
                state.connectionsLoading = false;
            });
    },
});
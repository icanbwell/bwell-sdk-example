import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { createSlice } from "@reduxjs/toolkit";

export const getMemberConnections = createAsyncThunk(
    "connections/memberConnections",
    async () => {
        const bWellSdk = getSdk();
        return bWellSdk?.connection.getMemberConnections();
    }
);

const INITIAL_STATE = {
    memberConnections: null,
    dataSource: null,
    loading: false,
    error: null as string | null,
};

export const connectionSlice = createSlice({
    name: "connections",
    initialState: INITIAL_STATE,
    reducers: {
        resetState: (state) => {
            Object.assign(state, INITIAL_STATE);
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(getMemberConnections.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.memberConnections = null;
            })
            .addCase(getMemberConnections.fulfilled, (state, action) => {
                if (action?.payload?.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    // @ts-ignore TODO: strong-type this
                    state.memberConnections = action.payload || [];
                }

                state.loading = false;
                state.error = "";
            })
            .addCase(getMemberConnections.rejected, (state, action) => {
                if (action.error.message === "Uninitialized") {
                    state.loading = true;
                } else {
                    state.error = action.error.message ?? "Unknown error";
                }
            });
    }
});
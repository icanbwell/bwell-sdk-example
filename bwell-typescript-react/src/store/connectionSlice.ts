import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { createSlice } from "@reduxjs/toolkit";
import { DataSourceRequest } from "@icanbwell/bwell-sdk-ts";

export const getMemberConnections = createAsyncThunk(
    "connections/memberConnections",
    async () => {
        const bWellSdk = getSdk();
        return bWellSdk?.connection.getMemberConnections();
    }
);

export const getDataSource = createAsyncThunk(
    "connections/getDataSource",
    async (connectionId: string) => {
        const bWellSdk = getSdk();
        return bWellSdk?.connection.getDataSource(new DataSourceRequest({ connectionId }));
    }
);

const INITIAL_STATE = {
    memberConnections: null,
    dataSource: null,
    dataSourceConnectionId: null as string | null,
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
            })
            .addCase(getDataSource.pending, (state, action) => {
                state.error = null;
                state.dataSource = null;
                state.dataSourceConnectionId = action.meta.arg;
            })
            .addCase(getDataSource.fulfilled, (state, action) => {
                // Ignore responses for a connection that's no longer the one currently selected,
                // so an out-of-order response from a prior click can't overwrite a newer one.
                if (action.meta.arg !== state.dataSourceConnectionId) return;
                if (action?.payload?.error) {
                    state.error = action.payload.error.message ?? "Unknown error";
                } else {
                    // @ts-ignore TODO: strong-type this
                    state.dataSource = action.payload?.data ?? null;
                }
            })
            .addCase(getDataSource.rejected, (state, action) => {
                if (action.meta.arg !== state.dataSourceConnectionId) return;
                state.error = action.error.message ?? "Unknown error";
            });
    }
});
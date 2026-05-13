import { createAsyncThunk } from "@reduxjs/toolkit";
import { getSdk } from "@/sdk/bWellSdk";
import { createSlice } from "@reduxjs/toolkit";
import { deleteConnection } from "@/store/deleteConnectionThunk";

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
    deletingConnectionIds: [] as string[],
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
            // (TIP-7050) Wire the delete thunk into this slice so the UI updates
            // optimistically. The previous flow awaited deleteConnection and
            // then re-fetched getMemberConnections, but the read side is
            // eventually-consistent: the refetch often returned the just-deleted
            // connection still tagged CONNECTED, so the row never appeared to
            // disappear. We now remove the row from local state the moment the
            // delete mutation succeeds, and the UI does NOT trigger a refetch
            // on success (a refetch would undo the optimistic remove until the
            // backend caught up).
            .addCase(deleteConnection.pending, (state, action) => {
                if (!state.deletingConnectionIds.includes(action.meta.arg)) {
                    state.deletingConnectionIds.push(action.meta.arg);
                }
            })
            .addCase(deleteConnection.fulfilled, (state, action) => {
                const deletedId = action.payload;
                state.deletingConnectionIds = state.deletingConnectionIds.filter((id) => id !== deletedId);
                const memberConnections: any = state.memberConnections;
                if (memberConnections?.data && Array.isArray(memberConnections.data)) {
                    // BWellQueryResult is a class instance and Immer treats class
                    // instances as immutable leaves by default, so mutating
                    // `memberConnections.data = ...` directly is silently ignored.
                    // Replace the whole field with a plain object so Immer
                    // registers the change and consumers re-render.
                    state.memberConnections = {
                        ...memberConnections,
                        data: memberConnections.data.filter(
                            (conn: any) => conn?.id !== deletedId
                        ),
                    };
                }
            })
            .addCase(deleteConnection.rejected, (state, action) => {
                state.deletingConnectionIds = state.deletingConnectionIds.filter(
                    (id) => id !== action.meta.arg
                );
                state.error = action.error.message ?? "Failed to delete connection";
            });
    }
});
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

const INITIAL_STATE = {
    healthData: null,
    loading: false,
    error: null,
};

const makeHealthDataSlice = <T>(name: string, getter: Function) => {
    return createSlice({
        name,
        initialState: INITIAL_STATE,
        reducers: {
            resetState: (state) => {
                Object.assign(state, INITIAL_STATE);
            }
        },
        extraReducers: (builder) => {
            builder
                .addCase(getter.pending, (state) => {
                    state.loading = true;
                    state.error = null;
                    state.healthData = null;
                })
                .addCase(getter.fulfilled, (state, action: PayloadAction<T>) => {
                    if (action.payload.error) {
                        state.error = action.payload.error.message ?? "Unknown error";
                    } else {
                        state.healthData = action.payload || [];
                    }

                    state.loading = false;
                    state.error = "";
                })
                .addCase(getter.rejected, (state, action) => {
                    if (action.error.message === "Uninitialized") {
                        state.loading = true;
                    } else {
                        state.error = action.error.message ?? "Unknown error";
                    }
                });
        }
    });
}

export default makeHealthDataSlice;
import { createSlice, PayloadAction } from "@reduxjs/toolkit";

const makeHealthDataSlice = <T>(name: string, getter: Function) => {
    return createSlice({
        name,
        initialState: {
            healthData: null as T | null,
            loading: false,
            error: null as string | null,
        },
        reducers: {},
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
import { configureStore } from "@reduxjs/toolkit";
import { userSlice } from "./userSlice";
import { initializationSlice } from "./initializationSlice";

export const store = configureStore({
  reducer: {
    user: userSlice.reducer,
    initialization: initializationSlice.reducer,
  },
});

//export a dispatch type we can use when dispatching actions to the store
export type AppDispatch = typeof store.dispatch;

//export a RootState type we can use when selecting the state in the store
export type RootState = ReturnType<typeof store.getState>;
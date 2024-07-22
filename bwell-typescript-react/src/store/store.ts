import { userSlice } from "./userSlice";
import { toggleSlice } from "./toggleSlice";
import { connectionSlice } from "./connectionSlice";
import { configureStore, combineReducers } from "@reduxjs/toolkit";
import rehydrateSdk from "@/sdk/rehydrateSdk";
import { persistStore, persistReducer } from 'redux-persist';
import storage from "redux-persist/lib/storage";
import { requestInfoSlice } from "./requestInfoSlice";
import { healthReducer } from "./healthData/healthReducer";

// Setup the persist config
const persistConfig = {
  key: 'root',
  storage,
};

// Create a root reducer
const rootReducer = combineReducers({
  health: healthReducer,
  user: userSlice.reducer,
  toggle: toggleSlice.reducer,
  connections: connectionSlice.reducer,
  requests: requestInfoSlice.reducer, 
});

// Wrap the rootReducer with persistReducer so that state will automatically sync with local storage
const persistedReducer = persistReducer(persistConfig, rootReducer);

// Configure the store with the persistReducer
export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) => getDefaultMiddleware({
    serializableCheck: false,
  }).concat(rehydrateSdk),
});

// Export a persistor that we can use in the PersistGate
export const Persistor = persistStore(store);

// Export a dispatch type we can use when dispatching actions to the store
export type AppDispatch = typeof store.dispatch;

// Export a RootState type we can use when selecting the state in the store
export type RootState = ReturnType<typeof store.getState>;
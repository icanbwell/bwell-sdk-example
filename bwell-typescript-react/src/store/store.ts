import { configureStore, combineReducers } from "@reduxjs/toolkit";
import { userSlice } from "./userSlice";
import { allergyIntoleranceGroupsSlice } from "./allergyIntoleranceSlice";
import { tableOrJsonToggleSlice } from "./tableOrJsonToggleSlice";
import { connectionSlice } from "./connectionSlice";

import { persistStore, persistReducer } from 'redux-persist';
import storage from "redux-persist/lib/storage";

// Setup the persist config
const persistConfig = {
  key: 'root',
  storage,
};

// Create a root reducer
const rootReducer = combineReducers({
  user: userSlice.reducer,
  allergyIntolerance: allergyIntoleranceGroupsSlice.reducer,
  tableOrJsonToggle: tableOrJsonToggleSlice.reducer,
  connection: connectionSlice.reducer,
});

// Wrap the rootReducer with persistReducer so that state will automatically sync with local storage
const persistedReducer = persistReducer(persistConfig, rootReducer);

// Configure the store with the persistReducer
export const store = configureStore({
  reducer: persistedReducer,
});

// Export a persistor that we can use in the PersistGate
export const Persistor = persistStore(store);

// Export a dispatch type we can use when dispatching actions to the store
export type AppDispatch = typeof store.dispatch;

// Export a RootState type we can use when selecting the state in the store
export type RootState = ReturnType<typeof store.getState>;
import { configureStore, combineReducers } from "@reduxjs/toolkit";
import { userSlice } from "./userSlice";
import { allergyIntolerancesSlice } from "./allergyIntoleranceSlice";
import { tableOrJsonToggleSlice } from "./tableOrJsonToggleSlice";
import { connectionSlice } from "./connectionSlice";

import { persistStore, persistReducer } from 'redux-persist';
import storage from "redux-persist/lib/storage";

import rehydrateSdk from "@/sdk/rehydrateSdk";
import { allergyIntoleranceGroupsSlice } from "./allergyIntoleranceGroupsSlice";
import { healthSummarySlice } from "./healthSummarySlice";
import { conditionsSlice } from "./conditionsSlice";
import { conditionGroupsSlice } from "./conditionGroupsSlice";
import { labGroupsSlice } from "./labGroupsSlice";
import { labsSlice } from "./labsSlice";
import { carePlanGroupsSlice } from "./carePlanGroupsSlice";
import { carePlansSlice } from "./carePlansSlice";
import { encounterGroupsSlice } from "./encounterGroupsSlice";
import { encountersSlice } from "./encountersSlice";
import { immunizationsSlice } from "./immunizationsSlice";
import { immunizationGroupsSlice } from "./immunizationGroupsSlice";

// Setup the persist config
const persistConfig = {
  key: 'root',
  storage,
};

// Create a root reducer
const rootReducer = combineReducers({
  user: userSlice.reducer,
  healthSummary: healthSummarySlice.reducer,
  allergyIntolerances: allergyIntolerancesSlice.reducer,
  allergyIntoleranceGroups: allergyIntoleranceGroupsSlice.reducer,
  carePlans: carePlansSlice.reducer,
  carePlanGroups: carePlanGroupsSlice.reducer,
  conditions: conditionsSlice.reducer,
  conditionGroups: conditionGroupsSlice.reducer,
  encounters: encountersSlice.reducer,
  encounterGroups: encounterGroupsSlice.reducer,
  immunizations: immunizationsSlice.reducer,
  immunizationGroups: immunizationGroupsSlice.reducer,
  labs: labsSlice.reducer,
  labGroups: labGroupsSlice.reducer,
  tableOrJsonToggle: tableOrJsonToggleSlice.reducer,
  connection: connectionSlice.reducer,
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
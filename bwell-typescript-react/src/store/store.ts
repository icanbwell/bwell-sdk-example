import { userSlice } from "./userSlice";
import { toggleSlice } from "./toggleSlice";
import { connectionSlice } from "./connectionSlice";
import { allergyIntolerancesSlice } from "./healthData/allergyIntoleranceSlice";
import { allergyIntoleranceGroupsSlice } from "./healthData/allergyIntoleranceGroupsSlice";
import { healthSummarySlice } from "./healthData/healthSummarySlice";
import { conditionsSlice } from "./healthData/conditionsSlice";
import { conditionGroupsSlice } from "./healthData/conditionGroupsSlice";
import { labGroupsSlice } from "./healthData/labGroupsSlice";
import { labsSlice } from "./healthData/labsSlice";
import { carePlanGroupsSlice } from "./healthData/carePlanGroupsSlice";
import { carePlansSlice } from "./healthData/carePlansSlice";
import { encounterGroupsSlice } from "./healthData/encounterGroupsSlice";
import { encountersSlice } from "./healthData/encountersSlice";
import { immunizationsSlice } from "./healthData/immunizationsSlice";
import { immunizationGroupsSlice } from "./healthData/immunizationGroupsSlice";
import { medicationStatementsSlice } from "./healthData/medicationStatementsSlice";
import { medicationGroupsSlice } from "./healthData/medicationGroupsSlice";
import { proceduresSlice } from "./healthData/proceduresSlice";
import { vitalSignsSlice } from "./healthData/vitalSignsSlice";
import { vitalSignGroupsSlice } from "./healthData/vitalSignGroupsSlice";

import { configureStore, combineReducers } from "@reduxjs/toolkit";
import rehydrateSdk from "@/sdk/rehydrateSdk";
import { persistStore, persistReducer } from 'redux-persist';
import storage from "redux-persist/lib/storage";
import { medicationKnowledgeSlice } from "./healthData/medicationKnowledgeSlice";
import { labKnowledgeSlice } from "./healthData/labKnowledgeSlice";
import { requestInfoSlice } from "./requestInfoSlice";

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
  labKnowledge: labKnowledgeSlice.reducer,
  medicationGroups: medicationGroupsSlice.reducer,
  medicationKnowledge: medicationKnowledgeSlice.reducer,
  medicationStatements: medicationStatementsSlice.reducer,
  procedures: proceduresSlice.reducer,
  procedureGroups: proceduresSlice.reducer,
  vitalSigns: vitalSignsSlice.reducer,
  vitalSignGroups: vitalSignGroupsSlice.reducer,
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
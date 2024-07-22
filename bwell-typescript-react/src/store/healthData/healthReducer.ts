import { allergyIntolerancesSlice } from "./allergyIntoleranceSlice";
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
import { medicationStatementsSlice } from "./medicationStatementsSlice";
import { medicationGroupsSlice } from "./medicationGroupsSlice";
import { proceduresSlice } from "./proceduresSlice";
import { vitalSignsSlice } from "./vitalSignsSlice";
import { vitalSignGroupsSlice } from "./vitalSignGroupsSlice";
import { medicationKnowledgeSlice } from "./medicationKnowledgeSlice";
import { labKnowledgeSlice } from "./labKnowledgeSlice";
import { procedureGroupsSlice } from "./procedureGroupsSlice";

import { combineReducers } from "@reduxjs/toolkit";

export const healthReducer = combineReducers({
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
  procedureGroups: procedureGroupsSlice.reducer,
  vitalSigns: vitalSignsSlice.reducer,
  vitalSignGroups: vitalSignGroupsSlice.reducer,
});
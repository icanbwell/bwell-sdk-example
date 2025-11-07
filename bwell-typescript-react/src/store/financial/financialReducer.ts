import { coveragesSlice } from "./coveragesSlice";
import { explanationOfBenefitsSlice } from "./explanationOfBenefitsSlice";
import { combineReducers } from "@reduxjs/toolkit";

export const financialReducer = combineReducers({
  coverages: coveragesSlice.reducer,
  explanationOfBenefits: explanationOfBenefitsSlice.reducer,
});

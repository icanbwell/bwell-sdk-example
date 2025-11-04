import { coveragesSlice } from "./coveragesSlice";
import { combineReducers } from "@reduxjs/toolkit";

export const financialReducer = combineReducers({
  coverages: coveragesSlice.reducer,
});

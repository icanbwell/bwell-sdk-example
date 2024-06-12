import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from './store';

interface TableOrJsonToggleState {
    [locator: string]: boolean;
}

const initialState: TableOrJsonToggleState = {};

export const tableOrJsonToggleSlice = createSlice({
    name: 'tableOrJsonToggle',
    initialState,
    reducers: {
        toggleView: (state, action: PayloadAction<string>) => {
            const locator = action.payload;
            state[locator] = !state[locator];
        },
        setShowTable: (state, action: PayloadAction<{ locator: string, showTable: boolean }>) => {
            const { locator, showTable } = action.payload;
            state[locator] = showTable;
        },
    },
});

export const { toggleView, setShowTable } = tableOrJsonToggleSlice.actions;

export const selectShowTable = (state: RootState, locator: string) => state.tableOrJsonToggle[locator] ?? true;

export default tableOrJsonToggleSlice.reducer;

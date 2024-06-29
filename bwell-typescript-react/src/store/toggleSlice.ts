import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { RootState } from './store';

interface ToggleState {
    [locator: string]: boolean;
}

const initialState: ToggleState = {
    allergyIntoleranceGroups: true,
};

export const toggleSlice = createSlice({
    name: 'toggle',
    initialState,
    reducers: {
        toggleValue: (state, action: PayloadAction<string>) => {
            const locator = action.payload;

            console.log(action.payload);

            if (state[locator] === undefined) {
                state[locator] = true;
            }

            state[locator] = !state[locator];
        }
    },
});

export const { toggleValue } = toggleSlice.actions;

export const selectToggle = (state: RootState, locator: string) => state.toggle[locator] ?? true;

export default toggleSlice.reducer;

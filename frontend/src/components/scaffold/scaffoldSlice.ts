import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export interface ScaffoldState {
    isNavDrawerOpen: boolean;
}

const initialState = {
    isNavDrawerOpen: false,
} as ScaffoldState;

const slice = createSlice({
    name: 'scaffold',
    initialState: initialState,
    reducers: {
        openNavDrawer(state, _: PayloadAction) {
            state.isNavDrawerOpen = true;
        },
        closeNavDrawer(state, _: PayloadAction) {
            state.isNavDrawerOpen = false;
        },
    }
})

export const selectIsNavDrawerOpen = (state: RootState) => state.scaffold.isNavDrawerOpen;

export const {openNavDrawer, closeNavDrawer} = slice.actions

export default slice.reducer;
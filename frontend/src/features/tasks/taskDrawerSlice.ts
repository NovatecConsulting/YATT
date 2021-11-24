import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export interface TodoDrawerState {
    selectedTaskId?: string;
}

export const slice = createSlice({
    name: "taskDrawer",
    initialState: {} as TodoDrawerState,
    reducers: {
        taskSelected(state, action: PayloadAction<string>) {
            state.selectedTaskId = action.payload;
        },
        closeTaskDrawer(state, _: PayloadAction) {
            state.selectedTaskId = undefined;
        }
    }
});

export const {taskSelected, closeTaskDrawer} = slice.actions;

export const selectSelectedTaskId = (state: RootState) => state.taskDrawer.selectedTaskId;

export default slice.reducer;
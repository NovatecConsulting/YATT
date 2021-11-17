import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export interface TodoDrawerState {
    selectedTaskId?: string;
}

export const slice = createSlice({
    name: "todoDrawer",
    initialState: {} as TodoDrawerState,
    reducers: {
        taskSelected(state, action: PayloadAction<string>) {
            state.selectedTaskId = action.payload;
        },
        closeTodoDrawer(state, _: PayloadAction) {
            state.selectedTaskId = undefined;
        }
    }
});

export const {taskSelected, closeTodoDrawer} = slice.actions;

export const selectSelectedTaskId = (state: RootState) => state.todoDrawer.selectedTaskId;

export default slice.reducer;
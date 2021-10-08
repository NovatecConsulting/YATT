import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export interface Token {
    value?: string;
}

const tokenSlice = createSlice({
    name: 'token',
    initialState: {} as Token,
    reducers: {
        tokenUpdated(state, action: PayloadAction<string | undefined>) {
            state.value = action.payload;
        }
    }
})

export const selectToken = (state: RootState) => state.token.value;

export const {tokenUpdated} = tokenSlice.actions

export default tokenSlice.reducer;
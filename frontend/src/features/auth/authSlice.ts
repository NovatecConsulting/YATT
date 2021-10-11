import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export interface AuthenticationState {
    token?: string;
    isRegistered: boolean;
    isAuthenticated: boolean;
    isLoading: boolean;
}

const initialState = {
    isRegistered: false,
    isAuthenticated: false,
    isLoading: true,
} as AuthenticationState;

const authSlice = createSlice({
    name: 'auth',
    initialState: initialState,
    reducers: {
        tokenUpdated(state, action: PayloadAction<string | undefined>) {
            state.token = action.payload;
        },
        registered(state, action: PayloadAction<boolean>) {
            state.isRegistered = action.payload;
        },
        authenticated(state, action: PayloadAction<boolean>) {
            state.isAuthenticated = action.payload;
        },
        authLoading(state, action: PayloadAction<boolean>) {
            state.isLoading = action.payload;
        }
    }
})

export const selectToken = (state: RootState) => state.auth.token;
export const selectIsAuthenticated = (state: RootState) => state.auth.isAuthenticated;
export const selectIsRegistered = (state: RootState) => state.auth.isRegistered;
export const selectIsAuthLoading = (state: RootState) => state.auth.isLoading;

export const {tokenUpdated, registered, authenticated, authLoading} = authSlice.actions

export default authSlice.reducer;
import {configureStore, ThunkAction, Action} from '@reduxjs/toolkit';
import {apiSlice} from "../features/api/apiSlice";
import authSlice from "../features/auth/authSlice";
import todoDrawerSlice from "../features/tasks/todoSlice";

export const store = configureStore({
    reducer: {
        [apiSlice.reducerPath]: apiSlice.reducer,
        auth: authSlice,
        todoDrawer: todoDrawerSlice,
    },
    middleware: getDefaultMiddleware => getDefaultMiddleware().concat(apiSlice.middleware)
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<ReturnType,
    RootState,
    unknown,
    Action<string>>;

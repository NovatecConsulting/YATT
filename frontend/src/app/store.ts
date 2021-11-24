import {configureStore, ThunkAction, Action} from '@reduxjs/toolkit';
import {apiSlice} from "../features/api/apiSlice";
import authSlice from "../features/auth/authSlice";
import taskDrawerSlice from "../features/tasks/taskDrawerSlice";
import scaffoldSlice from "../components/scaffold/scaffoldSlice";

export const store = configureStore({
    reducer: {
        [apiSlice.reducerPath]: apiSlice.reducer,
        auth: authSlice,
        taskDrawer: taskDrawerSlice,
        scaffold: scaffoldSlice,
    },
    middleware: getDefaultMiddleware => getDefaultMiddleware().concat(apiSlice.middleware)
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<ReturnType,
    RootState,
    unknown,
    Action<string>>;

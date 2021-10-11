import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {selectToken} from "../auth/authSlice";
import {RootState} from "../../app/store";

export const baseUrl = 'http://localhost:8080/v2';

export const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: baseUrl,
        prepareHeaders: (headers, {getState}) => {
            const token = selectToken(getState() as RootState);
            headers.set('Authorization', `Bearer ${token}`);
            return headers;
        }
    }),
    endpoints: builder => ({})
});
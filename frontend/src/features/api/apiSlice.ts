import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";

export const baseUrl = 'http://localhost:8080/v2';

export const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: baseUrl,
    }),
    endpoints: builder => ({})
});
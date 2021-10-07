import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import keycloak from "../../keycloak";

export const baseUrl = 'http://localhost:8080/v2';

export const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: fetchBaseQuery({
        baseUrl: baseUrl,
        prepareHeaders: headers => {
            headers.set('Authorization', `Bearer ${keycloak.token}`);
            return headers;
        }
    }),
    endpoints: builder => ({})
});
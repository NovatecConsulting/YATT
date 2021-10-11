import {apiSlice} from "../api/apiSlice";

export interface User {
    identifier: string;
    externalUserId: string;
    firstname: string;
    lastname: string;
}

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getCurrentUser: builder.query<User, void>({
            query: () => '/users/current'
        })
    })
})

export const loadCurrentUser = extendedApiSlice.endpoints.getCurrentUser.initiate;


import {apiSlice} from "../api/apiSlice";
import {createSelector} from "@reduxjs/toolkit";

export interface User {
    identifier: string;
    externalUserId: string;
    firstname: string;
    lastname: string;
    email: string;
    telephone: string;
}

export interface RegisterUserDto {
    firstname: string;
    lastname: string;
    email: string;
    telephone: string;
}

export interface RenameUserDto {
    firstname: string;
    lastname: string;
}

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getCurrentUser: builder.query<User, void>({
            query: () => '/users/current',
            providesTags: ['currentUser'],
        }),
        registerUser: builder.mutation<User, RegisterUserDto>({
            query: (userDto: RegisterUserDto) => ({
                url: '/users/current',
                method: 'POST',
                body: userDto
            }),
            invalidatesTags: ['currentUser'],
        }),
        getAllUsers: builder.query<User[], void>({
            query: () => '/users'
        }),
        renameUser: builder.mutation<void, RenameUserDto>({
            query: (body) => ({
                url: '/users/current/rename',
                method: 'POST',
                body: body
            }),
            onQueryStarted(patch, api) {
                const patchResult = api.dispatch(
                    extendedApiSlice.util.updateQueryData('getCurrentUser', undefined, draft => {
                        draft.firstname = patch.firstname;
                        draft.lastname = patch.lastname;
                    })
                );

                api.queryFulfilled.catch(patchResult.undo)
            }
        }),
    })
})

export const {
    useRegisterUserMutation,
    useGetAllUsersQuery,
    useRenameUserMutation,
} = extendedApiSlice;

export const loadCurrentUser = extendedApiSlice.endpoints.getCurrentUser.initiate;

const selectCurrentUserResult = extendedApiSlice.endpoints.getCurrentUser.select();

export const selectCurrentUser = createSelector(
    selectCurrentUserResult,
    (result) => result.data,
);
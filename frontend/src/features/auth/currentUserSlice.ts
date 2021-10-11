import {apiSlice} from "../api/apiSlice";

export interface User {
    identifier: string;
    externalUserId: string;
    firstname: string;
    lastname: string;
}

export interface RegisterUserDto {
    firstname: string;
    lastname: string;
}

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getCurrentUser: builder.query<User, void>({
            query: () => '/users/current'
        }),
        registerUser: builder.mutation<User, RegisterUserDto>({
            query: (userDto: RegisterUserDto) => ({
                url: '/users/current',
                method: 'POST',
                body: userDto
            })
        })
    })
})

export const {
    useRegisterUserMutation,
} = extendedApiSlice;

export const loadCurrentUser = extendedApiSlice.endpoints.getCurrentUser.initiate;


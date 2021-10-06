import {apiSlice} from "../api/apiSlice";

export interface Project {
    identifier: string;
    version: number;
    name: string;
    plannedStartDate: string;
    deadline: string;
}

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getProjects: builder.query<Project[], void>({
            query: () => '/projects',
        })
    })
});

export const {useGetProjectsQuery} = extendedApiSlice;

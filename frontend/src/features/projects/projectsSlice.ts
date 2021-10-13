import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {subscribe} from "../../app/api";
import {CancelCallback} from "can-ndjson-stream";
import {RegisterUserDto} from "../auth/currentUserSlice";

export interface Project {
    identifier: string;
    version: number;
    name: string;
    plannedStartDate: string;
    deadline: string;
}

export interface CreateProjectDto {
    name: string;
    plannedStartDate: string;
    deadline: string;
}

const projectsAdapter = createEntityAdapter<Project>({
    selectId: model => model.identifier
});

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getProjects: builder.query<EntityState<Project>, void>({
            query: () => '/projects',
            transformResponse(response: Project[]) {
                return projectsAdapter.setAll(
                    projectsAdapter.getInitialState(),
                    response
                );
            },
            async onCacheEntryAdded(_, api): Promise<void> {
                let cancel: CancelCallback | undefined;
                try {
                    await api.cacheDataLoaded;

                    cancel = await subscribe<Project>('/projects', update => {
                        api.updateCachedData(draft => {
                            if (draft) {
                                projectsAdapter.upsertOne(draft, update);
                            }
                        });
                    });
                } catch {
                    // no-op in case `cacheEntryRemoved` resolves before `cacheDataLoaded`,
                    // in which case `cacheDataLoaded` will throw
                }
                await api.cacheEntryRemoved;
                if (cancel) {
                    await cancel("cacheEntryRemoved");
                }
            }
        }),
        createProject: builder.mutation<string, CreateProjectDto>({
            query: (projectDto: CreateProjectDto) => ({
                url: '/projects',
                method: 'POST',
                body: projectDto
            }),
        })
    })
});

export const {useGetProjectsQuery, useCreateProjectMutation} = extendedApiSlice;

const selectProjectsResult = extendedApiSlice.endpoints.getProjects.select();

const selectProjectsData = createSelector(
    selectProjectsResult,
    result => result.data
)

export const {selectAll: selectAllProjects, selectById: selectProjectById} = projectsAdapter.getSelectors<RootState>(
    state => selectProjectsData(state) ?? projectsAdapter.getInitialState()
)

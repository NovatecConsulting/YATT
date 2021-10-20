import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {subscribe} from "../../app/api";
import {CancelCallback} from "can-ndjson-stream";

export interface Project {
    identifier: string;
    version: number;
    name: string;
    startDate: string;
    deadline: string;
}

export interface ProjectDetails {
    identifier: string;
    version: number;
    name: string;
    startDate: string;
    deadline: string;
    allTasksCount: number;
    plannedTasksCount: number;
    startedTasksCount: number;
    completedTasksCount: number;
}

export interface CreateProjectDto {
    name: string;
    startDate: string;
    deadline: string;
}

export interface RescheduleProjectDto {
    identifier: string;
    version: number;
    startDate: string;
    deadline: string;
}

export interface RenameProjectDto {
    identifier: string;
    version: number;
    name: string;
}

const projectsAdapter = createEntityAdapter<Project>({
    selectId: model => model.identifier,
    sortComparer: (a, b) => {
        const comparingName = a.name.localeCompare(b.name);
        if (comparingName !== 0) {
            return comparingName
        } else {
            return a.identifier.localeCompare(b.identifier);
        }
    }
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
        }),
        rescheduleProject: builder.mutation<number, RescheduleProjectDto>({
            query: ({identifier, ...patch}) => ({
                url: `/projects/${identifier}/reschedule`,
                method: 'POST',
                body: patch
            }),
            onQueryStarted({identifier, version, ...patch}, api) {
                const patchResult = api.dispatch(
                    extendedApiSlice.util.updateQueryData('getProjects', undefined, draft => {
                        projectsAdapter.updateOne(draft, {
                            id: identifier,
                            changes: {version: version + 1, ...patch}
                        });
                    })
                );

                api.queryFulfilled.catch(patchResult.undo)
            }
        }),
        renameProject: builder.mutation<string, RenameProjectDto>({
            query: ({identifier, ...patch}) => ({
                url: `/projects/${identifier}/rename`,
                method: 'POST',
                body: patch
            }),
        }),
        getProjectDetails: builder.query<ProjectDetails, string>({
            query: (id) => `/projects/${id}/details`,
            async onCacheEntryAdded(id, api): Promise<void> {
                let cancel: CancelCallback | undefined;
                try {
                    await api.cacheDataLoaded;

                    cancel = await subscribe<ProjectDetails>(`/projects/${id}/details`, update => {
                        api.updateCachedData(draft => {
                            Object.keys(draft).forEach(key => {
                                (draft as any)[key] = (update as any)[key];
                            });
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
    })
});

export const {
    useGetProjectsQuery,
    useCreateProjectMutation,
    useRescheduleProjectMutation,
    useRenameProjectMutation,
    useGetProjectDetailsQuery,
} = extendedApiSlice;

const selectProjectsResult = extendedApiSlice.endpoints.getProjects.select();

const selectProjectsData = createSelector(
    selectProjectsResult,
    result => result.data
)

export const {selectAll: selectAllProjects, selectById: selectProjectById} = projectsAdapter.getSelectors<RootState>(
    state => selectProjectsData(state) ?? projectsAdapter.getInitialState()
)

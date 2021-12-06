import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {websocketClient} from "../../app/api";
import {QueryDefinition} from "@reduxjs/toolkit/query";
import {UseQueryStateDefaultResult} from "../../app/rtkQueryHelpers";
import {StompSubscription} from "@stomp/stompjs/esm6/stomp-subscription";

export interface Project {
    identifier: string;
    version: number;
    name: string;
    startDate: string;
    deadline: string;
    companyReference: { identifier: string, displayName: string };
    status: ProjectStatus;
    actualEndDate?: string;
}

export enum ProjectStatus {
    ON_TIME = 'ON_TIME',
    DELAYED = 'DELAYED'
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
    companyId: string;
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

export const projectsAdapter = createEntityAdapter<Project>({
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
                let subscription: StompSubscription | undefined;
                try {
                    await api.cacheDataLoaded;
                    subscription = websocketClient.subscribe("/projects", message => {
                        const update = JSON.parse(message.body);
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
                subscription?.unsubscribe()
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
                let subscription: StompSubscription | undefined;
                try {
                    await api.cacheDataLoaded;

                    subscription = websocketClient.subscribe(`/projects/${id}/details`, message => {
                        const update = JSON.parse(message.body);
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
                subscription?.unsubscribe();
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

export const selectProjectByIdFromResult = (result: UseQueryStateDefaultResult<QueryDefinition<any, any, any, EntityState<Project>>>, projectId: EntityId) => {
    const {data, ...rest} = result;
    return {
        ...rest,
        data: data ? projectsAdapter.getSelectors().selectById(data, projectId) : undefined
    };
}

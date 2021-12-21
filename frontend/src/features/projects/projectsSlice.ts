import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {QueryDefinition} from "@reduxjs/toolkit/query";
import {UseQueryStateDefaultResult} from "../../app/rtkQueryHelpers";
import {rsocket} from "../../app/rsocket";
import {Subscription} from "../../app/rsocket";

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
                let subscription: Subscription | undefined;
                try {
                    await api.cacheDataLoaded;
                    subscription = rsocket.subscribeUpdates<Project>("projects.updates", update => {
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
                subscription?.cancel();
            }
        }),
        createProject: builder.mutation<void, CreateProjectDto>({
            queryFn(command) {
                return rsocket.sendCommand("projects.create", command);
            }
        }),
        rescheduleProject: builder.mutation<void, RescheduleProjectDto>({
            queryFn(command) {
                return rsocket.sendCommand("projects.reschedule", command);
            },
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
        renameProject: builder.mutation<void, RenameProjectDto>({
            queryFn(command) {
                return rsocket.sendCommand("projects.rename", command);
            },
        }),
        getProjectDetails: builder.query<ProjectDetails, string>({
            query: (id) => `/projects/${id}/details`,
            async onCacheEntryAdded(id, api): Promise<void> {
                let subscription: Subscription | undefined;
                try {
                    await api.cacheDataLoaded;

                    subscription = rsocket.subscribeUpdates(`projects.${id}.details.updates`, update => {
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
                subscription?.cancel();
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

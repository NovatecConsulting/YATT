import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {subscribe} from "../../app/api";
import {RootState} from "../../app/store";
import {CancelCallback} from "can-ndjson-stream";

export interface Task {
    identifier: string;
    version: number;
    name: string;
    startDate: string;
    endDate: string;
    status: string;
}

export interface CreateTaskDto {
    projectId: string,
    name: string;
    startDate: string;
    endDate: string;
}

export interface RenameTaskDto {
    identifier: string;
    name: string;
}

export interface RescheduleTaskDto {
    identifier: string,
    startDate: string;
    endDate: string;
}

export const taskAdapter = createEntityAdapter<Task>({
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

export const taskApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getTasksByProject: builder.query<EntityState<Task>, string>({
            query: (projectId) => `/projects/${projectId}/tasks`,
            transformResponse(response: Task[]) {
                return taskAdapter.setAll(
                    taskAdapter.getInitialState(),
                    response
                );
            },
            async onCacheEntryAdded(projectId, api): Promise<void> {
                let cancel: CancelCallback | undefined;
                try {
                    await api.cacheDataLoaded;

                    cancel = await subscribe<Task>(`/projects/${projectId}/tasks`, update => {
                        api.updateCachedData(draft => {
                            if (draft) {
                                taskAdapter.upsertOne(draft, update);
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
        createTask: builder.mutation<string, CreateTaskDto>({
            query: (taskDto) => ({
                url: `/tasks`,
                method: 'POST',
                body: taskDto
            }),
        }),
        renameTask: builder.mutation<string, RenameTaskDto>({
            query: ({identifier, ...patch}) => ({
                url: `/tasks/${identifier}/rename`,
                method: 'POST',
                body: patch
            }),
        }),
        startTask: builder.mutation<string, string>({
            query: (taskId) => ({
                url: `/tasks/${taskId}/start`,
                method: 'POST',
            }),
        }),
        completeTask: builder.mutation<string, string>({
            query: (taskId) => ({
                url: `/tasks/${taskId}/complete`,
                method: 'POST',
            }),
        }),
        rescheduleTask: builder.mutation<string, RescheduleTaskDto>({
            query: ({identifier, ...patch}) => ({
                url: `/tasks/${identifier}/reschedule`,
                method: 'POST',
                body: patch,
            }),
        }),
    })
});

export const {
    useGetTasksByProjectQuery,
    useCreateTaskMutation,
    useRenameTaskMutation,
    useStartTaskMutation,
    useCompleteTaskMutation,
    useRescheduleTaskMutation,
} = taskApiSlice;

const selectGetTasksByProjectResult
    = (state: RootState, projectId: EntityId) => taskApiSlice.endpoints.getTasksByProject.select(projectId.toString())(state)

const selectGetTasksByProjectData = createSelector(
    selectGetTasksByProjectResult,
    result => result.data
)

const getSelectors = (projectId: EntityId) => taskAdapter.getSelectors<RootState>(
    state => selectGetTasksByProjectData(state, projectId) ?? taskAdapter.getInitialState())

export const selectTaskIdsByProjectId =
    (state: RootState, projectId: EntityId) => getSelectors(projectId).selectIds(state)

export const selectTaskEntitiesByProjectId =
    (state: RootState, projectId: EntityId) => getSelectors(projectId).selectAll(state)

export const selectTaskByProjectIdAndTaskId =
    (state: RootState, projectId: EntityId, taskId: EntityId) => getSelectors(projectId).selectById(state, taskId)

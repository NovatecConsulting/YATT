import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {Subscription, websocketClient} from "../../app/api";
import {RootState} from "../../app/store";

export interface Task {
    identifier: string;
    version: number;
    name: string;
    startDate: string;
    endDate: string;
    status: string;
    todos: Todo[],
}

export interface Todo {
    todoId: string;
    description: string;
    isDone: boolean;
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

export interface TaskAndTodoId {
    taskId: string;
    todoId: string;
}

export interface AddTodoDto {
    taskId: string;
    description: string;
}

export const taskAdapter = createEntityAdapter<Task>({
    selectId: model => model.identifier,
    sortComparer: (a, b) => a.identifier.localeCompare(b.identifier)
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
                let subscription: Subscription | undefined;
                try {
                    await api.cacheDataLoaded;

                    subscription = websocketClient.subscribe(`/projects/${projectId}/tasks`, message => {
                        const update = JSON.parse(message.body);
                        api.updateCachedData(draft => {
                            if (draft) {
                                taskAdapter.upsertMany(draft, update);
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
        addTodo: builder.mutation<string, AddTodoDto>({
            query: ({taskId, ...body}) => ({
                url: `/tasks/${taskId}/todos`,
                method: 'POST',
                body: body
            }),
        }),
        markTodoAsDone: builder.mutation<string, TaskAndTodoId>({
            query: ({taskId, todoId}) => ({
                url: `/tasks/${taskId}/todos/${todoId}/markDone`,
                method: 'POST',
            }),
        }),
        removeTodo: builder.mutation<string, TaskAndTodoId>({
            query: ({taskId, todoId}) => ({
                url: `/tasks/${taskId}/todos/${todoId}`,
                method: 'DELETE',
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
    useMarkTodoAsDoneMutation,
    useAddTodoMutation,
    useRemoveTodoMutation
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

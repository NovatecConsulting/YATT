import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {rsocket, Subscription} from "../../app/rsocket";

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

                    subscription = rsocket.subscribeUpdates<Task>(`projects.${projectId}.tasks`, update => {
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
                subscription?.cancel()
            }
        }),
        createTask: builder.mutation<void, CreateTaskDto>({
            queryFn(command) {
                return rsocket.sendCommand("tasks.create", command);
            }
        }),
        renameTask: builder.mutation<void, RenameTaskDto>({
            queryFn(command) {
                return rsocket.sendCommand("tasks.rename", command);
            }
        }),
        startTask: builder.mutation<void, string>({
            queryFn(taskId) {
                return rsocket.sendCommand(`tasks.${taskId}.start`, null);
            }
        }),
        completeTask: builder.mutation<void, string>({
            queryFn(taskId) {
                return rsocket.sendCommand(`tasks.${taskId}.complete`, null);
            }
        }),
        rescheduleTask: builder.mutation<void, RescheduleTaskDto>({
            queryFn(command) {
                return rsocket.sendCommand("tasks.reschedule", command);
            }
        }),
        addTodo: builder.mutation<void, AddTodoDto>({
            queryFn(command) {
                return rsocket.sendCommand("tasks.todos.add", command);
            }
        }),
        markTodoAsDone: builder.mutation<void, TaskAndTodoId>({
            queryFn(command) {
                return rsocket.sendCommand("tasks.todos.markDone", command);
            }
        }),
        removeTodo: builder.mutation<void, TaskAndTodoId>({
            queryFn(command) {
                return rsocket.sendCommand("tasks.todos.remove", command);
            }
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

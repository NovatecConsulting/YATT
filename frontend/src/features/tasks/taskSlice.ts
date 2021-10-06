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

export const taskAdapter = createEntityAdapter<Task>({
    selectId: model => model.identifier
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
        })
    })
});

export const {useGetTasksByProjectQuery} = taskApiSlice;

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

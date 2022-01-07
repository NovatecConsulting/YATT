import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {rsocket, Subscription} from "../../app/rsocket";

export interface Participant {
    identifier: string;
    version: number;
    projectId: string;
    companyId: string;
    companyName?: string;
    userId: string;
    userFirstName?: string;
    userLastName?: string;
}

export interface CreateParticipantDto {
    projectId: string;
    companyId: string;
    userId: string;
}

export const entityAdapter = createEntityAdapter<Participant>({
    selectId: model => model.identifier,
    sortComparer: (a, b) => {
        const comparingName = a.userLastName?.localeCompare(b.userLastName ?? '') ?? 0;
        if (comparingName !== 0) {
            return comparingName
        } else {
            return a.identifier.localeCompare(b.identifier);
        }
    }
});

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getParticipantsByProject: builder.query<EntityState<Participant>, string>({
            query: (projectId) => `/projects/${projectId}/participants`,
            transformResponse(response: Participant[]) {
                return entityAdapter.setAll(
                    entityAdapter.getInitialState(),
                    response
                );
            },
            async onCacheEntryAdded(projectId, api): Promise<void> {
                let subscription: Subscription | undefined;
                try {
                    await api.cacheDataLoaded;

                    subscription = rsocket.subscribeUpdates<Participant>(`projects.${projectId}.participants`, update => {
                        api.updateCachedData(draft => {
                            if (draft) {
                                entityAdapter.upsertOne(draft, update);
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
        createParticipant: builder.mutation<void, CreateParticipantDto>({
            queryFn(command) {
                return rsocket.sendCommand("participants.create", command);
            }
        }),
    })
});

export const {useGetParticipantsByProjectQuery, useCreateParticipantMutation} = extendedApiSlice;

const selectGetParticipantsByProjectResult
    = (state: RootState, projectId: EntityId) => extendedApiSlice.endpoints.getParticipantsByProject.select(projectId.toString())(state)

const selectGetParticipantsByProjectData = createSelector(
    selectGetParticipantsByProjectResult,
    result => result.data
)

const getSelectors = (projectId: EntityId) => entityAdapter.getSelectors<RootState>(
    state => selectGetParticipantsByProjectData(state, projectId) ?? entityAdapter.getInitialState())

export const selectParticipantIdsByProjectId =
    (state: RootState, projectId: EntityId) => getSelectors(projectId).selectIds(state)

export const selectParticipantEntitiesByProjectId =
    (state: RootState, projectId: EntityId) => getSelectors(projectId).selectAll(state)

export const selectParticipantByProjectIdAndParticipantId =
    (state: RootState, projectId: EntityId, participantId: EntityId) => getSelectors(projectId).selectById(state, participantId)

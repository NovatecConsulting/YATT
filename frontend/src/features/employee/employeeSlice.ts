import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {CancelCallback} from "can-ndjson-stream";
import {subscribe} from "../../app/api";

export interface Employee {
    identifier: string;
    version: number;
    companyId: string;
    userId: string;
    userFirstName: string;
    userLastName: string;
    isAdmin: boolean;
    isProjectManager: boolean;
}

export interface CreateEmployeeDto {
    companyId: string;
    userId: string;
}

const employeesAdapter = createEntityAdapter<Employee>({
    selectId: model => model.identifier,
    sortComparer: (a, b) => {
        const comparingName = a.userLastName.localeCompare(b.userLastName);
        if (comparingName !== 0) {
            return comparingName
        } else {
            return a.identifier.localeCompare(b.identifier);
        }
    }
});

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getEmployeesByCompany: builder.query<EntityState<Employee>, string>({
            query: (companyId) => `/companies/${companyId}/employees`,
            transformResponse(response: Employee[]) {
                return employeesAdapter.setAll(
                    employeesAdapter.getInitialState(),
                    response
                );
            },
            async onCacheEntryAdded(companyId, api): Promise<void> {
                let cancel: CancelCallback | undefined;
                try {
                    await api.cacheDataLoaded;

                    cancel = await subscribe<Employee>(`/companies/${companyId}/employees`, update => {
                        api.updateCachedData(draft => {
                            if (draft) {
                                employeesAdapter.upsertOne(draft, update);
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
        createEmployee: builder.mutation<string, CreateEmployeeDto>({
            query: (dto) => ({
                url: `/employees`,
                method: 'POST',
                body: dto,
            })
        }),
        grantAdminPermission: builder.mutation<string, string>({
            query: (id) => ({
                url: `/employees/${id}/permission/admin/grant`,
                method: 'POST',
            }),
            onQueryStarted(id, api) {
                const patchResult = api.dispatch(
                    extendedApiSlice.util.updateQueryData('getEmployeesByCompany', id, draft => {
                        employeesAdapter.updateOne(draft, {
                            id: id,
                            changes: {version: draft.entities[id]!.version + 1, isAdmin: true}
                        });
                    })
                );
                api.queryFulfilled.catch(patchResult.undo)
            }
        }),
        removeAdminPermission: builder.mutation<string, string>({
            query: (id) => ({
                url: `/employees/${id}/permission/admin/remove`,
                method: 'POST',
            }),
            onQueryStarted(id, api) {
                const patchResult = api.dispatch(
                    extendedApiSlice.util.updateQueryData('getEmployeesByCompany', id, draft => {
                        employeesAdapter.updateOne(draft, {
                            id: id,
                            changes: {version: draft.entities[id]!.version + 1, isAdmin: false}
                        });
                    })
                );
                api.queryFulfilled.catch(patchResult.undo)
            }
        }),
        grantProjectManagerPermission: builder.mutation<string, string>({
            query: (id) => ({
                url: `/employees/${id}/permission/projectmanager/grant`,
                method: 'POST',
            }),
            onQueryStarted(id, api) {
                const patchResult = api.dispatch(
                    extendedApiSlice.util.updateQueryData('getEmployeesByCompany', id, draft => {
                        employeesAdapter.updateOne(draft, {
                            id: id,
                            changes: {version: draft.entities[id]!.version + 1, isProjectManager: true}
                        });
                    })
                );
                api.queryFulfilled.catch(patchResult.undo)
            }
        }),
        removeProjectManagerPermission: builder.mutation<string, string>({
            query: (id) => ({
                url: `/employees/${id}/permission/projectmanager/remove`,
                method: 'POST',
            }),
            onQueryStarted(id, api) {
                const patchResult = api.dispatch(
                    extendedApiSlice.util.updateQueryData('getEmployeesByCompany', id, draft => {
                        employeesAdapter.updateOne(draft, {
                            id: id,
                            changes: {version: draft.entities[id]!.version + 1, isProjectManager: false}
                        });
                    })
                );
                api.queryFulfilled.catch(patchResult.undo)
            }
        }),
        getAllEmployees: builder.query<Employee[], void>({
            query: () => '/employees'
        }),
    })
})

export const {
    useGetEmployeesByCompanyQuery,
    useCreateEmployeeMutation,
    useGrantAdminPermissionMutation,
    useRemoveAdminPermissionMutation,
    useGrantProjectManagerPermissionMutation,
    useRemoveProjectManagerPermissionMutation,
    useGetAllEmployeesQuery,
} = extendedApiSlice;

const selectGetEmployeesByCompanyResult
    = (state: RootState, companyId: EntityId) => extendedApiSlice.endpoints.getEmployeesByCompany.select(companyId.toString())(state)

const selectGetEmployeesByCompanyData = createSelector(
    selectGetEmployeesByCompanyResult,
    result => result.data
)

const getSelectors = (companyId: EntityId) => employeesAdapter.getSelectors<RootState>(
    state => selectGetEmployeesByCompanyData(state, companyId) ?? employeesAdapter.getInitialState())

export const selectEmployeesByCompanyId =
    (state: RootState, companyId: EntityId) => getSelectors(companyId).selectIds(state)

export const selectEmployeesByCompanyIdAndEmployeeId =
    (state: RootState, companyId: EntityId, employeeId: EntityId) => getSelectors(companyId).selectById(state, employeeId)
import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

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
        return a.userLastName.localeCompare(b.userLastName);
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
        }),
        createEmployee: builder.mutation<string, CreateEmployeeDto>({
            query: (dto) => ({
                url: `/employees`,
                method: 'POST',
                body: dto,
            })
        })
    })
})

export const {useGetEmployeesByCompanyQuery, useCreateEmployeeMutation} = extendedApiSlice;

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

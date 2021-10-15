import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityState} from "@reduxjs/toolkit";
import {Project} from "../projects/projectsSlice";
import {RootState} from "../../app/store";

export interface Company {
    identifier: string;
    version: number;
    name: string;
}

export interface CreateCompanyDto {
    name: string;
}

const companiesAdapter = createEntityAdapter<Company>({
    selectId: model => model.identifier,
    sortComparer: (a, b) => {
        return a.name.localeCompare(b.name);
    }
});

export const extendedApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getCompanies: builder.query<EntityState<Company>, void>({
            query: () => '/companies',
            transformResponse(response: Company[]) {
                return companiesAdapter.setAll(
                    companiesAdapter.getInitialState(),
                    response
                );
            },
        }),
        createCompany: builder.mutation<string,CreateCompanyDto>({
            query: (companyDto) => ({
                url: `/companies`,
                method: 'POST',
                body: companyDto
            }),
        })
    })
})

export const {useGetCompaniesQuery,useCreateCompanyMutation} = extendedApiSlice;

const selectCompaniesResult = extendedApiSlice.endpoints.getCompanies.select();

const selectCompaniesData = createSelector(
    selectCompaniesResult,
    result => result.data
)

export const {selectById: selectCompanyById} = companiesAdapter.getSelectors<RootState>(
    state => selectCompaniesData(state) ?? companiesAdapter.getInitialState()
)
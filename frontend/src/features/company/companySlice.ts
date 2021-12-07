import {apiSlice} from "../api/apiSlice";
import {createEntityAdapter, createSelector, EntityId, EntityState} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {websocketClient} from "../../app/api";
import {UseQueryStateDefaultResult} from "../../app/rtkQueryHelpers";
import {QueryDefinition} from "@reduxjs/toolkit/query";
import {StompSubscription} from "@stomp/stompjs/esm6/stomp-subscription";

export interface Company {
    identifier: string;
    version: number;
    name: string;
}

export interface CreateCompanyDto {
    name: string;
}

export const companiesAdapter = createEntityAdapter<Company>({
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
            async onCacheEntryAdded(_, api): Promise<void> {
                let subscription: StompSubscription | undefined;
                try {
                    await api.cacheDataLoaded;

                    subscription = websocketClient.subscribe(`/companies`, message => {
                        const update = JSON.parse(message.body);
                        api.updateCachedData(draft => {
                            if (draft) {
                                companiesAdapter.upsertOne(draft, update);
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
        createCompany: builder.mutation<string, CreateCompanyDto>({
            query: (companyDto) => ({
                url: `/companies`,
                method: 'POST',
                body: companyDto
            }),
        })
    })
})

export const {useGetCompaniesQuery, useCreateCompanyMutation} = extendedApiSlice;

const selectCompaniesResult = extendedApiSlice.endpoints.getCompanies.select();

const selectCompaniesData = createSelector(
    selectCompaniesResult,
    result => result.data
)

export const {selectById: selectCompanyById} = companiesAdapter.getSelectors<RootState>(
    state => selectCompaniesData(state) ?? companiesAdapter.getInitialState()
)

export const selectCompanyByIdFromResult = (result: UseQueryStateDefaultResult<QueryDefinition<any, any, any, EntityState<Company>>>, companyId: EntityId) => {
    const {data, ...rest} = result;
    return {
        ...rest,
        data: data ? companiesAdapter.getSelectors().selectById(data, companyId) : undefined
    };
}
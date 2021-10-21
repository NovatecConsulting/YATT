import {QueryDefinition, QueryStatus} from "@reduxjs/toolkit/query";
import {Id, Override} from "@reduxjs/toolkit/dist/query/tsHelpers";
import {QuerySubState} from "@reduxjs/toolkit/dist/query/core/apiState";
import {createEntityAdapter, EntityState} from "@reduxjs/toolkit";

export function selectIdsFromResult<T>(result: UseQueryStateDefaultResult<QueryDefinition<any, any, any, EntityState<T>>>) {
    const {data: entityState, ...rest} = result;
    return ({
        ...rest,
        data: entityState?.ids
    });
}

export function selectEntitiesFromResult<T>(result: UseQueryStateDefaultResult<QueryDefinition<any, any, any, EntityState<T>>>) {
    const {data: entityState, ...rest} = result;
    return ({
        ...rest,
        data: entityState ? createEntityAdapter<T>().getSelectors().selectAll(entityState) : undefined
    });
}

//source: https://github.com/reduxjs/redux-toolkit/blob/master/packages/toolkit/src/query/react/buildHooks.ts
type UseQueryStateBaseResult<D extends QueryDefinition<any, any, any, any>> =
    QuerySubState<D> & {
    /**
     * Query has not started yet.
     */
    isUninitialized: false
    /**
     * Query is currently loading for the first time. No data yet.
     */
    isLoading: false
    /**
     * Query is currently fetching, but might have data from an earlier request.
     */
    isFetching: false
    /**
     * Query has data from a successful load.
     */
    isSuccess: false
    /**
     * Query is currently in "error" state.
     */
    isError: false
}

type UseQueryStateDefaultResult<D extends QueryDefinition<any, any, any, any>> =
    Id<| Override<Extract<UseQueryStateBaseResult<D>,
        { status: QueryStatus.uninitialized }>,
        { isUninitialized: true }>
        | Override<UseQueryStateBaseResult<D>,
        | { isLoading: true; isFetching: boolean; data: undefined }
        | ({
        isSuccess: true
        isFetching: boolean
        error: undefined
    } & Required<Pick<UseQueryStateBaseResult<D>, 'data' | 'fulfilledTimeStamp'>>)
        | ({ isError: true } & Required<Pick<UseQueryStateBaseResult<D>, 'error'>>)>> & {
    /**
     * @deprecated will be removed in the next version
     * please use the `isLoading`, `isFetching`, `isSuccess`, `isError`
     * and `isUninitialized` flags instead
     */
    status: QueryStatus
}
import {createAsyncThunk, createEntityAdapter, createSlice} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import {subscriptionQuery} from "../../app/api";

export interface Project {
    identifier: string;
    version: number;
    name: string;
    plannedStartDate: string;
    deadline: string;
}

const projectsAdapter = createEntityAdapter<Project>({
    selectId: model => model.identifier
});

const initialState = projectsAdapter.getInitialState();

export const subscribeAllProjects = createAsyncThunk<void, void, { state: RootState }>(
    'projects/subscribeAllProjects',
    (_, thunkAPI) => {
        const dispatchUpdateAction = (project: Project) => thunkAPI.dispatch(projectsSlice.actions.upsertOne(project));
        return subscriptionQuery('projects', dispatchUpdateAction, dispatchUpdateAction);
    }
);

export const projectsSlice = createSlice({
        name: 'projects',
        initialState,
        reducers: {
            upsertOne(state, action) {
                projectsAdapter.upsertOne(state, action.payload as Project);
            }
        },
    }
);

export const projectSelectors = projectsAdapter.getSelectors<RootState>(state => state.projects)

export default projectsSlice.reducer;
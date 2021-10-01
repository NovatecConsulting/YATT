import {createAsyncThunk, createEntityAdapter, createSlice} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";
import ndjsonStream from "can-ndjson-stream";

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

export const fetchAllProjects = createAsyncThunk<Project[], void, { state: RootState }>(
    'projects/fetchAllProjects',
    async (_, thunkAPI) => {
        const response = await fetch('http://localhost:8080/v2/projects', {
            headers: new Headers({
                'Accept': 'application/x-ndjson',
            })
        });
        if (response.body) {
            const reader = ndjsonStream(response.body).getReader();
            while (true) {
                const {value, done} = await reader.read();
                if (done) break;
                console.log('Received', value);
                thunkAPI.dispatch(projectsSlice.actions.upsertOne(value));
                // not working:
                // projectsAdapter.setOne(thunkAPI.getState().projects, value as Project);
            }
        }

        // const response = await fetch('http://localhost:8080/v1/projects');
        // return (await response.json()) as Array<Project>;
        return [] as Array<Project>;
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
        extraReducers(builder) {
            // builder.addCase(fetchAllProjects.fulfilled, (state, action) => {
            //     projectsAdapter.setAll(state, action.payload);
            // });
        }
    }
);

export const projectSelectors = projectsAdapter.getSelectors<RootState>(state => state.projects)

export default projectsSlice.reducer;
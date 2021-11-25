import {Scaffold} from "../../components/scaffold/Scaffold";
import {Chart} from "react-google-charts";
import {useHistory, useParams} from "react-router-dom";
import {useAppDispatch, useWindowDimensions} from "../../app/hooks";
import {selectProjectByIdFromResult, useGetProjectsQuery} from "../projects/projectsSlice";
import {useGetTasksByProjectQuery} from "./taskSlice";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Card, CardContent,
    CircularProgress,
} from "@mui/material";
import {TaskDrawer} from "./TaskDrawer";
import React from "react";
import {taskSelected} from "./taskDrawerSlice";
import {TableToolbar} from "../../components/TableToolbar";
import {ReactGoogleChartEvent} from "react-google-charts/dist/types";
import {parseDate} from "../../app/utils";

export function TasksGanttChart() {
    const history = useHistory();
    const dispatch = useAppDispatch();
    const {id: projectId} = useParams<{ id: string }>();
    // TODO quick workaround to keep subscribed to query
    const {data: project} = useGetProjectsQuery(undefined, {
        selectFromResult: (result) => selectProjectByIdFromResult(result, projectId)
    });
    const {width} = useWindowDimensions();

    const navigateToTaskCreateForm = () => history.push(`/projects/${projectId}/tasks/new`)

    const {
        data: tasks,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetTasksByProjectQuery(projectId, {selectFromResult: selectEntitiesFromResult});

    const chartEvents: ReactGoogleChartEvent[] = [
        {
            eventName: "select",
            callback: (args) => {
                const selectedIndex = args.chartWrapper.getChart().getSelection()[0].row;
                const selectedTask = tasks![selectedIndex];
                dispatch(taskSelected(selectedTask.identifier))
            }
        }
    ];

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && tasks) {
        const legendHeight = 40;
        const trackHeight = 30;
        const heightOfMouseOverPopup = 122;
        content = (
            <Card sx={{width: '100%'}}>
                <TableToolbar
                    title={`Tasks for Project "${project?.name}"`}
                    tooltip={"Create Task"}
                    onClick={navigateToTaskCreateForm}
                />
                <CardContent sx={{p: 0}} key={`card-content-${tasks.length}-${width}`}>
                    {
                        tasks.length === 0 ? null : (<Chart
                            width={'100%'}
                            height={Math.max(tasks.length * trackHeight + legendHeight, heightOfMouseOverPopup)}
                            chartType="Gantt"
                            loader={<CircularProgress/>}
                            chartEvents={chartEvents}
                            data={[
                                [
                                    {type: 'string', label: 'Task ID'},
                                    {type: 'string', label: 'Task Name'},
                                    {type: 'date', label: 'Start Date'},
                                    {type: 'date', label: 'End Date'},
                                    {type: 'number', label: 'Duration'},
                                    {type: 'number', label: 'Percent Complete'},
                                    {type: 'string', label: 'Dependencies'},
                                ],
                                ...tasks.map((task) => {
                                    const todoCount = task.todos.length;
                                    const completedTodoCount = task.todos.filter(todo => todo.isDone).length
                                    const todosCompletedPercent = task.status === 'COMPLETED' ? 100 : todoCount !== 0 ? 100 * completedTodoCount / todoCount : 0;
                                    return [
                                        task.identifier,
                                        task.name,
                                        parseDate(task.startDate),
                                        parseDate(task.endDate),
                                        null,
                                        todosCompletedPercent.toFixed(2),
                                        null
                                    ];
                                })
                            ]}
                            options={{
                                gantt: {
                                    trackHeight: trackHeight,
                                    criticalPathEnabled: false
                                },
                            }}
                        />)
                    }
                </CardContent>
            </Card>
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold alignItems='start' aside={<TaskDrawer/>}>
            {content}
        </Scaffold>
    );
}
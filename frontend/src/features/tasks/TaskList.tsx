import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import {
    selectTaskByProjectIdAndTaskId, Task,
    useGetTasksByProjectQuery,
    useRenameTaskMutation, useRescheduleTaskMutation,
} from "./taskSlice";
import {useHistory, useParams} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/scaffold/Scaffold";
import React, {useEffect} from "react";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {
    selectProjectByIdFromResult,
    useGetProjectsQuery
} from "../projects/projectsSlice";
import {EditableText} from "../../components/EditableText";
import {EditableDateTableCells} from "../../components/EditableDatesTableCell";
import {TaskDrawer} from "./TaskDrawer";
import {closeTaskDrawer, taskSelected} from "./taskDrawerSlice";
import {UpdateTaskStatusButton} from "./components/UpdateTaskStatusButton";

export function TaskList() {
    const history = useHistory();
    const dispatch = useAppDispatch();
    const {projectId} = useParams<{ projectId: string }>();
    // TODO quick workaround to keep subscribed to query
    const {data: project} = useGetProjectsQuery(undefined, {
        selectFromResult: (result) => selectProjectByIdFromResult(result, projectId)
    });

    const {
        data: taskIds,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetTasksByProjectQuery(projectId, {selectFromResult: selectIdsFromResult});

    useEffect(() => {
        return () => {
            dispatch(closeTaskDrawer());
        };
    })

    const navigateToTaskCreateForm = () => history.push(`/projects/${projectId}/tasklist/new`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && taskIds) {
        content = (
            <TableContainer sx={{maxWidth: 1000}} component={Paper}>
                <TableToolbar
                    title={`Tasks for Project "${project?.name}"`}
                    tooltip={'Create Task'}
                    onClick={navigateToTaskCreateForm}
                />
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell width={300}>Name</TableCell>
                            <TableCell>Start Date</TableCell>
                            <TableCell>End Date</TableCell>
                            <TableCell>Status</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {
                            taskIds.map(
                                (taskId: EntityId) => <TaskListRow key={taskId} projectId={projectId} taskId={taskId}/>
                            )
                        }
                    </TableBody>
                </Table>
            </TableContainer>
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
            {content}
            <TaskDrawer/>
        </Scaffold>
    );
}

function TaskListRow({projectId, taskId}: { projectId: EntityId, taskId: EntityId }) {
    const task = useAppSelector((state) => selectTaskByProjectIdAndTaskId(state, projectId, taskId));
    const dispatch = useAppDispatch();

    const [rescheduleTask] = useRescheduleTaskMutation();

    const onSave = async (startDate: string, endDate: string) => {
        await rescheduleTask({
            identifier: task!.identifier,
            startDate: startDate,
            endDate: endDate,
        }).unwrap();
    }

    const showTodos = () => dispatch(taskSelected(task!.identifier))

    if (task) {
        const canEditDates = task.status !== 'COMPLETED';
        return (
            <React.Fragment>
                <TableRow hover onClick={showTodos}>
                    <TaskNameCell task={task}/>
                    <EditableDateTableCells
                        canEdit={canEditDates}
                        onSave={onSave}
                        startDate={task.startDate}
                        endDate={task.endDate}
                    />
                    <TaskStatusCell taskStatus={task.status} taskId={task.identifier}/>
                </TableRow>
            </React.Fragment>
        );
    } else {
        return null;
    }
}

function TaskStatusCell({taskStatus, taskId}: { taskStatus: string, taskId: EntityId }) {
    return (
        <TableCell>
            {taskStatus}
            <UpdateTaskStatusButton sx={{ml: 2}} taskId={taskId} taskStatus={taskStatus}/>
        </TableCell>
    );
}

function TaskNameCell({task}: { task: Task }) {
    const [saveName] = useRenameTaskMutation();

    const canEditName = task.status !== 'COMPLETED';

    const onSave = async (name: string) => {
        await saveName({
            identifier: task.identifier.toString(),
            name: name,
        }).unwrap();
    };

    return (
        <TableCell>
            <EditableText initialValue={task.name} label={'Name'} canEdit={canEditName} onSave={onSave}/>
        </TableCell>
    );
}
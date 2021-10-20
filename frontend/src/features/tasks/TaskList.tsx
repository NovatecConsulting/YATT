import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Button,
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
    selectTaskByProjectIdAndTaskId, Task, useCompleteTaskMutation,
    useGetTasksByProjectQuery,
    useRenameTaskMutation, useRescheduleTaskMutation,
    useStartTaskMutation
} from "./taskSlice";
import {useHistory, useParams} from "react-router-dom";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/Scaffold";
import React from "react";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {selectProjectById} from "../projects/projectsSlice";
import {useStore} from "react-redux";
import {EditableTableCell} from "../../components/EditableTableCell";
import {EditableDateTableCells} from "../../components/EditableDatesTableCell";

export function TaskList() {
    const history = useHistory();
    const {id: projectId} = useParams<{ id: string }>();
    const store = useStore();
    const project = selectProjectById(store.getState(), projectId)

    const {
        data: taskIds,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetTasksByProjectQuery(projectId, {selectFromResult: selectIdsFromResult});

    const navigateToTaskCreateForm = () => history.push(`/projects/${projectId}/tasks/new`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && taskIds) {
        content = (
            <TableContainer sx={{maxWidth: 1000}} component={Paper}>
                <TableToolbar
                    title={`Task for Project "${project?.name}"`}
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
        </Scaffold>
    );
}

function TaskListRow({projectId, taskId}: { projectId: EntityId, taskId: EntityId }) {
    const task = useAppSelector((state) => selectTaskByProjectIdAndTaskId(state, projectId, taskId));

    const [rescheduleTask] = useRescheduleTaskMutation();

    const onSave = async (startDate: string, endDate: string) => {
        await rescheduleTask({
            identifier: task!.identifier,
            startDate: startDate,
            endDate: endDate,
        }).unwrap();
    }

    if (task) {
        const canEditDates = task.status !== 'COMPLETED';
        return (
            <React.Fragment>
                <TableRow hover>
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
    const [startTask, {isLoading: isLoadingStart}] = useStartTaskMutation();
    const [completeTask, {isLoading: isLoadingComplete}] = useCompleteTaskMutation();
    const isLoading = isLoadingStart || isLoadingComplete;

    let handleClick = () => {
    };
    let buttonTitle = '';
    let showButton = true;
    switch (taskStatus) {
        case 'PLANNED':
            buttonTitle = 'Start Task';
            handleClick = () => startTask(taskId.toString());
            break;
        case 'STARTED':
            buttonTitle = 'Complete Task';
            handleClick = () => completeTask(taskId.toString());
            break;
        case 'COMPLETED':
        default:
            showButton = false;
            break;
    }

    return (
        <TableCell>
            {taskStatus}
            {
                showButton ? (
                    <Button
                        sx={{ml: 2}}
                        onClick={handleClick}
                        disabled={isLoading}
                    >
                        {buttonTitle}
                    </Button>
                ) : null
            }

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
        <EditableTableCell initialValue={task.name} label={'Name'} canEdit={canEditName} onSave={onSave}/>
    );
}
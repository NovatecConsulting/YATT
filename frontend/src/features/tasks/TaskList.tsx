import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Box,
    CircularProgress, IconButton,
    Paper,
} from "@mui/material";
import {
    Task,
    useGetTasksByProjectQuery,
    useRenameTaskMutation, useRescheduleTaskMutation,
} from "./taskSlice";
import {useHistory, useParams} from "react-router-dom";
import {useAppDispatch} from "../../app/hooks";
import {Scaffold} from "../../components/scaffold/Scaffold";
import React, {useEffect, useState} from "react";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {
    selectProjectByIdFromResult,
    useGetProjectsQuery
} from "../projects/projectsSlice";
import {EditableText} from "../../components/EditableText";
import {RescheduleDialog} from "../../components/EditableDatesTableCell";
import {TaskDrawer} from "./TaskDrawer";
import {closeTaskDrawer, taskSelected} from "./taskDrawerSlice";
import {UpdateTaskStatusButton} from "./components/UpdateTaskStatusButton";
import {VirtualizedTable} from "../../components/VirtualizedTable";
import dayjs from "dayjs";
import {Edit} from "@mui/icons-material";

export function TaskList() {
    const history = useHistory();
    const dispatch = useAppDispatch();
    const {projectId} = useParams<{ projectId: string }>();
    // TODO quick workaround to keep subscribed to query
    const {data: project} = useGetProjectsQuery(undefined, {
        selectFromResult: (result) => selectProjectByIdFromResult(result, projectId)
    });

    const {
        data: tasks,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetTasksByProjectQuery(projectId, {selectFromResult: selectEntitiesFromResult});

    useEffect(() => {
        return () => {
            dispatch(closeTaskDrawer());
        };
    }, [dispatch])

    const [taskToReschedule, setTaskToReschedule] = useState<Task | undefined>(undefined);

    const closeRescheduleDialog = () => setTaskToReschedule(undefined);

    const navigateToTaskCreateForm = () => history.push(`/projects/${projectId}/tasklist/new`)

    const showTodos = (taskId: string) => dispatch(taskSelected(taskId))

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && tasks) {
        content = (
            <Paper sx={{width: 1000, height: 500, display: "flex", flexFlow: "column", flex: "1 1 auto"}}>
                <TableToolbar
                    title={`Tasks for Project "${project?.name}"`}
                    tooltip={'Create Task'}
                    onClick={navigateToTaskCreateForm}
                />
                <Box sx={{flex: "1 1 auto"}}>
                    <VirtualizedTable
                        rowHeight={64}
                        rowCount={tasks.length}
                        rowGetter={(index) => tasks[index.index]}
                        onRowClick={(row) => showTodos((row.rowData as Task).identifier)}
                        columns={[
                            {
                                width: 120,
                                flexGrow: 1,
                                label: "Name",
                                dataKey: "name",
                                cellRenderer: (cellProps) => <TaskNameCell task={cellProps.rowData as Task}/>
                            },
                            {
                                width: 150,
                                label: "Start Date",
                                dataKey: "startDate",
                                cellRenderer: (cellProps) => <DateCell task={cellProps.rowData as Task}
                                                                       isStartDate={true}
                                                                       onEdit={setTaskToReschedule}/>
                            },
                            {
                                width: 150,
                                label: "End Date",
                                dataKey: "endDate",
                                cellRenderer: (cellProps) => <DateCell task={cellProps.rowData as Task}
                                                                       onEdit={setTaskToReschedule}/>
                            },
                            {
                                width: 150,
                                label: "Assignee",
                                dataKey: "assignee",
                                cellRenderer: (cellProps) => <AssigneeCell task={cellProps.rowData as Task}/>
                            },
                            {
                                width: 300,
                                label: "Status",
                                dataKey: "status",
                                cellRenderer: (cellProps) => <TaskStatusCell task={cellProps.rowData as Task}/>
                            }
                        ]}
                    />
                </Box>
            </Paper>
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
            <TaskRescheduleDialog
                key={taskToReschedule?.identifier}
                task={taskToReschedule}
                handleClose={closeRescheduleDialog}
            />
        </Scaffold>
    );
}

function AssigneeCell({task} : {task: Task}) {
    const assigneeName = (task.assigneeFirstName ?? "") + " " + (task.assigneeLastName ?? "")
    return (<React.Fragment>{assigneeName.trim() === "" ? "-" : assigneeName}</React.Fragment>);
}

function DateCell({task, isStartDate, onEdit}: { task: Task, isStartDate?: boolean, onEdit: (task: Task) => void }) {
    const canEdit = task.status !== 'COMPLETED';

    return (
        <React.Fragment>
            {dayjs(isStartDate ? task.startDate : task.endDate).format('L')}
            {
                canEdit ? (
                    <IconButton size='small' sx={{ml: 1}} onClick={(event) => {
                        event.stopPropagation();
                        onEdit(task);
                    }}>
                        <Edit fontSize="inherit"/>
                    </IconButton>
                ) : null
            }
        </React.Fragment>
    );
}

function TaskStatusCell({task}: { task: Task }) {
    return (
        <React.Fragment>
            {task.status}
            <UpdateTaskStatusButton sx={{ml: 2}} taskId={task.identifier} taskStatus={task.status} assigned={task.assigneeCompanyName != null}/>
        </React.Fragment>
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
        <EditableText initialValue={task.name} label={'Name'} canEdit={canEditName} onSave={onSave}/>
    );
}

interface TaskRescheduleDialogProps {
    task?: Task;
    handleClose: () => void;
}

function TaskRescheduleDialog({task, handleClose}: TaskRescheduleDialogProps) {
    const [rescheduleTask] = useRescheduleTaskMutation();
    const isOpen = !!task;

    const onSave = async (startDate: string, endDate: string) => {
        await rescheduleTask({
            identifier: task!.identifier,
            startDate: startDate,
            endDate: endDate,
        }).unwrap();
    }

    return (
        <RescheduleDialog initialStartDate={task?.startDate ?? ""}
                          initialEndDate={task?.endDate ?? ""}
                          open={isOpen}
                          onClose={handleClose}
                          onSave={onSave}/>
    );
}
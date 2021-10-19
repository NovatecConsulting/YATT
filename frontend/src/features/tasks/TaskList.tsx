import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Box,
    Button,
    CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
    IconButton,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow, TextField, TextFieldProps, Typography
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
import React, {useState} from "react";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {selectProjectById} from "../projects/projectsSlice";
import {useStore} from "react-redux";
import dayjs from "dayjs";
import {Check, Clear, Edit} from "@mui/icons-material";
import {useFormik} from "formik";
import DatePicker from "@mui/lab/DatePicker";

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

    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    if (task)
        return (
            <React.Fragment>
                <TableRow hover>
                    <TaskNameCell task={task}/>
                    <TaskDateCell task={task} date={task.startDate} openDialog={handleClickOpen}/>
                    <TaskDateCell task={task} date={task.endDate} openDialog={handleClickOpen}/>
                    <TaskStatusCell taskStatus={task.status} taskId={task.identifier}/>
                </TableRow>
                <RescheduleDialog task={task} open={open} onClose={handleClose}/>
            </React.Fragment>
        );
    else {
        return null;
    }
}

interface TaskDateCellProps {
    date: string;
    task: Task;
    openDialog: () => void;
}

function TaskDateCell(props: TaskDateCellProps) {
    const canEditDates = props.task.status !== 'COMPLETED';

    return (
        <TableCell>
            {dayjs(props.date).format('L')}
            {
                canEditDates ? (
                    <IconButton size='small' sx={{ml: 1}} onClick={props.openDialog}>
                        <Edit fontSize="inherit"/>
                    </IconButton>
                ) : null
            }
        </TableCell>
    );
}

interface RescheduleDialogProps {
    open: boolean;
    onClose: () => void;
    task: Task;
}

function RescheduleDialog(props: RescheduleDialogProps) {
    const [rescheduleTask] = useRescheduleTaskMutation();
    const formik = useFormik({
        initialValues: {
            startDate: dayjs(props.task.startDate),
            endDate: dayjs(props.task.endDate),
        },
        validateOnChange: true,
        validate: values => {
            if (values.startDate > values.endDate) {
                return {
                    endDate: `Can't be before Start Date`
                }
            }
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await rescheduleTask({
                    identifier: props.task.identifier,
                    startDate: values.startDate.format("YYYY-MM-DD"),
                    endDate: values.endDate.format("YYYY-MM-DD"),
                }).unwrap();
                props.onClose();
            } catch (e) {
                // TODO error handling
                console.log("task creation failed");
            }
        },
    });

    return (
        <Dialog open={props.open} onClose={props.onClose}>
            <DialogTitle>Update Start and End Date</DialogTitle>
            <DialogContent>
                <DatePicker
                    label="Start Date"
                    value={formik.values.startDate}
                    mask={"__.__.____"}
                    inputFormat={"DD.MM.YYYY"}
                    onChange={(newValue) => {
                        formik.getFieldHelpers('startDate').setValue(newValue, true)
                    }}
                    renderInput={(params: TextFieldProps) => <TextField
                        {...params}
                        required
                        fullWidth
                        id="startDate"
                        name="startDate"
                        error={params.error || (formik.touched.startDate && Boolean(formik.errors.startDate))}
                        helperText={formik.touched.startDate && formik.errors.startDate}
                    />}
                />
                <DatePicker
                    minDate={formik.values.startDate}
                    label="End Date"
                    value={formik.values.endDate}
                    mask={"__.__.____"}
                    inputFormat={"DD.MM.YYYY"}
                    onChange={(newValue) => {
                        formik.getFieldHelpers('endDate').setValue(newValue, true)
                    }}
                    renderInput={(params: TextFieldProps) => <TextField
                        {...params}
                        required
                        fullWidth
                        id="endDate"
                        name="endDate"
                        error={params.error || (formik.touched.endDate && Boolean(formik.errors.endDate))}
                        helperText={formik.touched.endDate && formik.errors.endDate}
                    />}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={props.onClose}>Cancel</Button>
                <Button onClick={formik.submitForm} disabled={formik.isSubmitting || !formik.isValid}>Save</Button>
            </DialogActions>
        </Dialog>
    );
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
    const [isEditing, setIsEditing] = useState(false);

    const handleEditing = () => setIsEditing(!isEditing);

    const [saveName, {isLoading}] = useRenameTaskMutation();

    const canEditName = task.status !== 'COMPLETED';

    const formik = useFormik({
        initialValues: {
            name: task.name,
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await saveName({
                    identifier: task.identifier.toString(),
                    name: values.name,
                }).unwrap();
                handleEditing();
            } catch (e) {
                // TODO error handling
                console.log("task rename failed");
            }
        },
    });

    return (
        <TableCell>
            {!isEditing ? (
                <React.Fragment>
                    {task.name}
                    {
                        canEditName ? (
                            <IconButton size='small' sx={{ml: 1}} onClick={handleEditing}>
                                <Edit fontSize="inherit"/>
                            </IconButton>
                        ) : null
                    }

                </React.Fragment>
            ) : (
                <React.Fragment>
                    <TextField
                        margin='none'
                        size='small'
                        id="name"
                        name="name"
                        label="Name"
                        value={formik.values.name}
                        onChange={formik.handleChange}
                        error={formik.touched.name && Boolean(formik.errors.name)}
                        helperText={formik.touched.name && formik.errors.name}
                    />
                    <IconButton size='small' sx={{ml: 1}} onClick={formik.submitForm} disabled={isLoading}>
                        <Check/>
                    </IconButton>
                    <IconButton size='small' sx={{ml: 1}} onClick={handleEditing}>
                        <Clear/>
                    </IconButton>
                </React.Fragment>
            )}
        </TableCell>
    );
}
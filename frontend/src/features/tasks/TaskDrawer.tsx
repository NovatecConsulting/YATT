import {
    Box, Button,
    Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, Divider,
    Drawer, FormControl, IconButton,
    InputLabel,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    ListSubheader, MenuItem, Select, TextField,
    Toolbar, Tooltip, Typography
} from "@mui/material";
import {useParams} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {
    selectTaskByProjectIdAndTaskId, Task, Todo,
    useAddTodoMutation,
    useAssignTaskMutation,
    useMarkTodoAsDoneMutation,
    useRemoveTodoMutation, useRenameTaskMutation, useRescheduleTaskMutation, useUnassignTaskMutation
} from "./taskSlice";
import AddIcon from "@mui/icons-material/Add";
import React, {useState} from "react";
import {closeTaskDrawer, selectSelectedTaskId} from "./taskDrawerSlice";
import {useFormik} from "formik";
import {useSnackbar} from "notistack";
import {Close, Delete, Edit} from "@mui/icons-material";
import dayjs from "dayjs";
import {RescheduleDialog} from "../../components/EditableDatesTableCell";
import {UpdateTaskStatusButton} from "./components/UpdateTaskStatusButton";
import {EditableText} from "../../components/EditableText";
import {Participant, useGetParticipantsByProjectQuery} from "../participants/participantSlice";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";

const drawerWidth = 350;

export function TaskDrawer() {
    const {projectId} = useParams<{ projectId: string }>();
    const taskId = useAppSelector(selectSelectedTaskId);
    const isTodosDrawerOpen = !!taskId;
    const task = useAppSelector((state) => taskId ? selectTaskByProjectIdAndTaskId(state, projectId, taskId) : undefined);

    const [isAddTodoDialogOpen, setAddTodoDialogOpen] = React.useState(false);

    const openAddTodoDialog = (event: any) => {
        event.stopPropagation();
        setAddTodoDialogOpen(true);
    };

    const closeAddTodoDialog = () => {
        setAddTodoDialogOpen(false);
    };

    return (
        <Drawer
            anchor="right"
            variant="persistent"
            open={isTodosDrawerOpen}
            sx={{
                width: isTodosDrawerOpen ? drawerWidth : 0,
                flexShrink: 0,
                [`& .MuiDrawer-paper`]: {width: drawerWidth, boxSizing: 'border-box'},
            }}
        >
            <Toolbar/>
            {
                task ? (
                    <Box sx={{overflow: 'auto'}}>
                        <List subheader={<TaskDetailsHeader task={task}/>}>
                            <StatusListItem key={"task-status"} task={task}/>
                            <DatesListItem key={"task-dates"} task={task}/>
                            <AssigneeListItem key={"task-assignee"} task={task}/>
                        </List>
                        <Divider/>
                        <List
                            subheader={<TodoListHeader task={task} handleOpenAddTodoDialog={openAddTodoDialog}/>}
                            dense={true}
                        >
                            {
                                task.todos.map((todo) => <TodoListItem key={todo.todoId} todo={todo} task={task}/>)
                            }
                        </List>
                    </Box>
                ) : null
            }
            <AddTodoDialog open={isAddTodoDialogOpen} onClose={closeAddTodoDialog}/>
        </Drawer>
    );
}

interface TodoListHeaderProps {
    task: Task;
    handleOpenAddTodoDialog: (event: any) => void;
}

function TodoListHeader({task, handleOpenAddTodoDialog}: TodoListHeaderProps) {
    return (
        <ListSubheader>{
            <Toolbar disableGutters={true}>
                <Box sx={{flex: '1 1 100%', wordBreak: "break-word", boxSizing: "border-box"}}>
                    <Typography
                        color="inherit"
                        variant="inherit"
                        component="span"
                    >
                        {`Todos for Task "${task.name}"`}
                    </Typography>
                </Box>
                {
                    task.status === 'COMPLETED' ? null : (
                        <Tooltip title={"Add Todo"}>
                            <IconButton edge='end' onClick={handleOpenAddTodoDialog}>
                                <AddIcon/>
                            </IconButton>
                        </Tooltip>
                    )
                }
            </Toolbar>
        }</ListSubheader>
    );
}

function TaskDetailsHeader({task}: { task: Task }) {
    const dispatch = useAppDispatch();
    const [saveName] = useRenameTaskMutation();
    const [isMouseEntered, setIsMouseEntered] = useState(false);

    const canEditName = task.status !== 'COMPLETED';

    const onSave = async (name: string) => {
        await saveName({
            identifier: task.identifier.toString(),
            name: name,
        }).unwrap();
    };

    return (
        <ListSubheader onMouseEnter={() => setIsMouseEntered(true)}
                       onMouseLeave={() => setIsMouseEntered(false)}>{
            <Toolbar disableGutters={true}>
                <Box sx={{flexGrow: 1}}>
                    <EditableText
                        typographyProps={{
                            color: "inherit",
                            variant: "h5"
                        }}
                        initialValue={task.name}
                        label={'Name'}
                        canEdit={canEditName && isMouseEntered}
                        onSave={onSave}
                    />
                </Box>
                <Tooltip title={"Close"}>
                    <IconButton edge='end' onClick={() => dispatch(closeTaskDrawer())}>
                        <Close/>
                    </IconButton>
                </Tooltip>
            </Toolbar>
        }</ListSubheader>
    );
}


interface TodoListItemProps {
    todo: Todo;
    task: Task;
}

function TodoListItem({todo, task}: TodoListItemProps) {
    const [markDone] = useMarkTodoAsDoneMutation();
    const [removeTodo] = useRemoveTodoMutation();
    const [isMouseEntered, setIsMouseEntered] = useState(false);

    return (
        <ListItem
            secondaryAction={
                task.status === 'COMPLETED' || !isMouseEntered ? null : (
                    <Tooltip title={"Remove Todo"}>
                        <IconButton edge="end" aria-label="delete" onClick={() => removeTodo({
                            taskId: task.identifier,
                            todoId: todo.todoId
                        })}>
                            <Delete sx={{color: "red"}}/>
                        </IconButton>
                    </Tooltip>
                )
            }
            onMouseEnter={() => setIsMouseEntered(true)}
            onMouseLeave={() => setIsMouseEntered(false)}
        >
            <ListItemIcon>
                <Checkbox
                    edge="start"
                    checked={todo.isDone}
                    onClick={todo.isDone ? undefined : () => markDone({
                        todoId: todo.todoId,
                        taskId: task.identifier
                    })}
                    tabIndex={-1}
                    disabled={todo.isDone}
                />
            </ListItemIcon>
            <ListItemText primary={todo.description}/>
        </ListItem>
    );
}

interface AddTodoDialogProps {
    open: boolean;
    onClose: () => void;
}

function AddTodoDialog(props: AddTodoDialogProps) {
    const {enqueueSnackbar} = useSnackbar();
    const taskId = useAppSelector(selectSelectedTaskId)!;
    const [addTodo] = useAddTodoMutation();

    const formik = useFormik({
        initialValues: {
            description: '',
        },
        validateOnChange: true,
        validate: values => {
            if (values.description.trim().length <= 0) {
                return {
                    description: "Description can't be empty"
                };
            }
        },
        onSubmit: async (values, _) => {
            try {
                await addTodo({
                    description: values.description,
                    taskId: taskId
                }).unwrap();
                props.onClose();
                formik.resetForm();
            } catch (e) {
                enqueueSnackbar("Failed saving todo")
                console.log("saving todo failed");
            }
        },
    });

    return (
        <Dialog open={props.open} onClose={props.onClose} onClick={event => event.stopPropagation()}>
            <DialogTitle>Add Todo</DialogTitle>
            <DialogContent>
                <TextField
                    required
                    fullWidth
                    id="description"
                    name="description"
                    label="Description"
                    value={formik.values.description}
                    onChange={formik.handleChange}
                    error={formik.touched.description && Boolean(formik.errors.description)}
                    helperText={formik.touched.description && formik.errors.description}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={props.onClose}>Cancel</Button>
                <Button onClick={formik.submitForm} disabled={formik.isSubmitting || !formik.isValid}>Save</Button>
            </DialogActions>
        </Dialog>
    );
}

function StatusListItem(props: { task: Task }) {
    const [isMouseEntered, setIsMouseEntered] = useState(false);

    return <ListItem
        secondaryAction={
            isMouseEntered ? <UpdateTaskStatusButton sx={{ml: 2}}
                                                     taskId={props.task.identifier}
                                                     taskStatus={props.task.status}/> : null
        }
        onMouseEnter={() => setIsMouseEntered(true)}
        onMouseLeave={() => setIsMouseEntered(false)}
    >
        <ListItemText
            primary={"Status"} primaryTypographyProps={{variant: "caption", color: "#00000099"}}
            secondary={props.task.status}
            secondaryTypographyProps={{variant: "body1", color: "black"}}
        />
    </ListItem>;
}

function DatesListItem({task}: { task: Task }) {
    const [isDialogOpen, setDialogOpen] = React.useState(false);
    const [rescheduleTask] = useRescheduleTaskMutation();
    const [isMouseEntered, setIsMouseEntered] = useState(false);
    const canEditDates = task.status !== 'COMPLETED';

    const onSave = async (startDate: string, endDate: string) => {
        await rescheduleTask({
            identifier: task.identifier,
            startDate: startDate,
            endDate: endDate,
        }).unwrap();
    };

    const openDialog = () => {
        setDialogOpen(true);
    };

    const closeDialog = () => {
        setDialogOpen(false);
    };

    return (
        <React.Fragment>
            <ListItem
                secondaryAction={
                    canEditDates && isMouseEntered ? (
                        <IconButton edge={"end"} sx={{ml: 1}} onClick={openDialog}>
                            <Edit/>
                        </IconButton>
                    ) : null
                }
                onMouseEnter={() => setIsMouseEntered(true)}
                onMouseLeave={() => setIsMouseEntered(false)}
            >
                <ListItemText
                    primary={"Start/End Date"}
                    primaryTypographyProps={{variant: "caption", color: "#00000099"}}
                    secondary={`${dayjs(task.startDate).format('L')}  -  ${dayjs(task.endDate).format('L')}`}
                    secondaryTypographyProps={{variant: "body1", color: "black"}}
                />
            </ListItem>
            <RescheduleDialog key={task.identifier}
                              initialStartDate={task.startDate}
                              initialEndDate={task.endDate}
                              open={isDialogOpen}
                              onClose={closeDialog}
                              onSave={onSave}/>
        </React.Fragment>
    );
}

function AssigneeListItem({task}: { task: Task }) {
    const [isDialogOpen, setDialogOpen] = React.useState(false);
    const [assignTask] = useAssignTaskMutation();
    const [unassignTask] = useUnassignTaskMutation();
    const [isMouseEntered, setIsMouseEntered] = useState(false);
    const canAssignTask = task.status !== 'COMPLETED';
    const canUnassignTask = task.status === 'PLANNED';
    const assigneeName = (task.assigneeFirstName ?? "") + " " + (task.assigneeLastName ?? "")

    const onSave = async (assigneeId?: string) => {
        assigneeId = assigneeId?.trim() ?? ""
        if ((assigneeId ?? "") != (task.participantId ?? "")) {
            if (assigneeId === "") {
                unassignTask({taskId: task.identifier});
            } else {
                assignTask({taskId: task.identifier, assignee: assigneeId})
            }
        }
    };

    const openDialog = () => {
        setDialogOpen(true);
    };

    const closeDialog = () => {
        setDialogOpen(false);
    };

    return (
        <React.Fragment>
            <ListItem
                secondaryAction={
                    canAssignTask && isMouseEntered ? (
                        <IconButton edge={"end"} sx={{ml: 1}} onClick={openDialog}>
                            <Edit/>
                        </IconButton>
                    ) : null
                }
                onMouseEnter={() => setIsMouseEntered(true)}
                onMouseLeave={() => setIsMouseEntered(false)}
            >
                <ListItemText
                    primary={"Assignee"} primaryTypographyProps={{variant: "caption", color: "#00000099"}}
                    secondary={assigneeName.trim() === "" ? "-" : assigneeName}
                    secondaryTypographyProps={{variant: "body1", color: "black"}}
                />
            </ListItem>
            <AssigneeSelectionDialog key={task.identifier}
                                     task={task}
                                     open={isDialogOpen}
                                     onClose={closeDialog}
                                     onSave={onSave}/>
        </React.Fragment>
    );
}


export interface AssigneSelectionDialogProps {
    open: boolean;
    onClose: () => void;
    task: Task;
    onSave: (selectedAssignee?: string) => Promise<void>;
}

export function AssigneeSelectionDialog(props: AssigneSelectionDialogProps) {
    const {projectId} = useParams<{ projectId: string }>();
    const participants = useGetParticipantsByProjectQuery(projectId, {selectFromResult: selectEntitiesFromResult});
    const canUnassign = props.task.status === 'PLANNED'

    const formik = useFormik({
        initialValues: {
            assigneeId: props.task.participantId ?? "",
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await props.onSave(values.assigneeId);
                props.onClose();
            } catch (e) {
                // TODO error handling
                console.log("assigning task failed");
            }
        },
    });

    const noAssignee = {
        identifier: "",
        version: 0,
        projectId: "",
        companyId: "",
        userId: "",
        userFirstName: "-"
    } as Participant;

    if (participants.isSuccess) {
        return (
            <Dialog open={props.open} onClose={props.onClose} onClick={event => event.stopPropagation()}>
                <DialogTitle>Assign Participant to Task "{props.task.name}"</DialogTitle>
                <DialogContent>
                    <Box component="form" onSubmit={formik.handleSubmit} sx={{p: 1}}>
                        <FormControl fullWidth>
                            <InputLabel id="assigneeLabel">Assignee</InputLabel>
                            <Select
                                required
                                labelId="assigneeLabel"
                                id="assigneeId"
                                name="assigneeId"
                                value={formik.values.assigneeId}
                                label="Assignee"
                                onChange={formik.handleChange}
                            >
                                {(canUnassign ? [noAssignee] : []).concat(participants.data!!).map(participant => (
                                    <MenuItem key={participant.identifier}
                                              value={participant.identifier}>
                                        {`${participant.userFirstName ?? ""} ${participant.userLastName ?? ""}`}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose}>Cancel</Button>
                    <Button onClick={formik.submitForm} disabled={formik.isSubmitting || !formik.isValid}>Save</Button>
                </DialogActions>
            </Dialog>
        );
    } else {
        return null;
    }
}


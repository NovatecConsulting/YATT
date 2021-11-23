import {
    Box, Button,
    Checkbox, Dialog, DialogActions, DialogContent, DialogTitle,
    Drawer, IconButton,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    ListSubheader, TextField,
    Toolbar, Tooltip, Typography
} from "@mui/material";
import {useParams} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {
    selectTaskByProjectIdAndTaskId, Task, Todo,
    useAddTodoMutation,
    useMarkTodoAsDoneMutation,
    useRemoveTodoMutation
} from "./taskSlice";
import AddIcon from "@mui/icons-material/Add";
import React from "react";
import {closeTodoDrawer, selectSelectedTaskId} from "./todoSlice";
import {useFormik} from "formik";
import {useSnackbar} from "notistack";
import {ChevronRight, Delete} from "@mui/icons-material";

const drawerWidth = 350;

export function TodosDrawer() {
    const {id: projectId} = useParams<{ id: string }>();
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
                        <List
                            subheader={<TodoListHeader task={task} handleOpenAddTodoDialog={openAddTodoDialog}/>}
                            dense={true}
                        >
                            {
                                task.todos.map((todo) => <TodoListItem todo={todo} task={task}/>)
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
    const dispatch = useAppDispatch();

    return (
        <ListSubheader>{
            <Toolbar disableGutters={true}>
                <Tooltip title={"Close Drawer"}>
                    <IconButton edge='start' onClick={() => dispatch(closeTodoDrawer())}>
                        <ChevronRight/>
                    </IconButton>
                </Tooltip>
                <Typography
                    sx={{flex: '1 1 100%'}}
                    color="inherit"
                    variant="inherit"
                    component="div"
                >
                    {`Todos for Task "${task.name}"`}
                </Typography>
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


interface TodoListItemProps {
    todo: Todo;
    task: Task;
}

function TodoListItem({todo, task}: TodoListItemProps) {
    const [markDone] = useMarkTodoAsDoneMutation();
    const [removeTodo] = useRemoveTodoMutation();

    return (
        <ListItem key={todo.todoId} secondaryAction={
            task.status === 'COMPLETED' ? null : (
                <IconButton edge="end" aria-label="delete" onClick={() => removeTodo({
                    taskId: task.identifier,
                    todoId: todo.todoId
                })}>
                    <Delete/>
                </IconButton>)
        }>
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
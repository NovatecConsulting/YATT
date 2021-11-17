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
import {useAppSelector} from "../../app/hooks";
import {selectTaskByProjectIdAndTaskId, useAddTodoMutation, useMarkTodoAsDoneMutation} from "./taskSlice";
import AddIcon from "@mui/icons-material/Add";
import React from "react";
import {selectSelectedTaskId} from "./todoSlice";
import {useFormik} from "formik";
import {useSnackbar} from "notistack";

const drawerWidth = 350;

export function TodosDrawer() {
    const {id: projectId} = useParams<{ id: string }>();
    const taskId = useAppSelector(selectSelectedTaskId);
    const isTodosDrawerOpen = !!taskId;
    const task = useAppSelector((state) => selectTaskByProjectIdAndTaskId(state, projectId, taskId ?? ''));
    const [markDone] = useMarkTodoAsDoneMutation();
    const [addTodo] = useAddTodoMutation();

    const saveTodo = async (description: string) => {
        await addTodo({
            description: description,
            taskId: taskId!
        }).unwrap();
    };

    const [isTodoDialogOpen, setAddTodoDialog] = React.useState(false);

    const openAddTodoDialog = (event: any) => {
        event.stopPropagation();
        setAddTodoDialog(true);
    };

    const handleCloseAddTodoDialog = () => {
        setAddTodoDialog(false);
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
                            subheader={<ListSubheader>{
                                <Toolbar disableGutters={true}>
                                    <Typography
                                        sx={{flex: '1 1 100%'}}
                                        color="inherit"
                                        variant="inherit"
                                        component="div"
                                    >
                                        {`Todos for Task "${task.name}"`}
                                    </Typography>
                                    <Tooltip title={"Add Todo"}>
                                        <IconButton onClick={openAddTodoDialog}>
                                            <AddIcon fontSize={"large"}/>
                                        </IconButton>
                                    </Tooltip>
                                </Toolbar>
                            }</ListSubheader>}
                            dense={true}
                        >
                            {
                                task.todos.map((todo) => {
                                    return (
                                        <ListItem key={todo.todoId}>
                                            <ListItemIcon>
                                                <Checkbox
                                                    edge="start"
                                                    checked={todo.isDone}
                                                    onClick={todo.isDone ? undefined : () => markDone({
                                                        todoId: todo.todoId,
                                                        taskId: taskId!
                                                    })}
                                                    tabIndex={-1}
                                                    disabled={todo.isDone}
                                                />
                                            </ListItemIcon>
                                            <ListItemText primary={todo.description}/>
                                        </ListItem>
                                    );
                                })
                            }
                        </List>
                    </Box>
                ) : null
            }
            <AddTodoDialog open={isTodoDialogOpen}
                           onClose={handleCloseAddTodoDialog}
                           onSave={saveTodo}/>
        </Drawer>
    );
}


interface AddTodoDialogProps {
    open: boolean;
    onClose: () => void;
    onSave: (description: string) => Promise<void>;
}

function AddTodoDialog(props: AddTodoDialogProps) {
    const {enqueueSnackbar} = useSnackbar();

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
        onSubmit: async (values, formikHelpers) => {
            try {
                await props.onSave(
                    values.description
                );
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
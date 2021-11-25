import {useHistory, useParams} from "react-router-dom";
import {useFormik} from "formik";
import {Scaffold} from "../../components/scaffold/Scaffold";
import {Box, Button, Card, CardContent, TextField, TextFieldProps} from "@mui/material";
import DatePicker from '@mui/lab/DatePicker';
import React from "react";
import {useSnackbar} from "notistack";
import {useCreateTaskMutation} from "./taskSlice";
import dayjs, {Dayjs} from "dayjs";

interface Values {
    name: string;
    startDate: Dayjs;
    endDate: Dayjs;
}

export function CreateTaskForm() {
    const {id: projectId} = useParams<{ id: string }>()

    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveTask] = useCreateTaskMutation();

    const formik = useFormik({
        initialValues: {
            name: '',
            startDate: dayjs(),
            endDate: dayjs().add(1, "day"),
        } as Values,
        validateOnChange: true,
        validate: values => {
            if (values.startDate > values.endDate) {
                return {
                    endDate: `Can't be before Planned Start Date`
                }
            }
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await saveTask({
                    projectId: projectId,
                    name: values.name,
                    endDate: values.endDate.toISOString(),
                    startDate: values.startDate.toISOString(),
                }).unwrap();
                enqueueSnackbar(`Task "${values.name}" created successfully`)
                history.goBack();
            } catch (e) {
                enqueueSnackbar(`Failed creating task.`);
                // TODO error handling
                console.log("task creation failed");
            }
        },
    });

    return (
        <Scaffold>
            <Card><CardContent>
                <Box component="form" onSubmit={formik.handleSubmit}>
                    <TextField
                        required
                        fullWidth
                        id="name"
                        name="name"
                        label="Name"
                        value={formik.values.name}
                        onChange={formik.handleChange}
                        error={formik.touched.name && Boolean(formik.errors.name)}
                        helperText={formik.touched.name && formik.errors.name}
                    />
                    <DatePicker
                        label="Planned Start Date"
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
                        minDate={formik.values.startDate.add(1, "day")}
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
                    <Button
                        variant="contained"
                        fullWidth
                        type="submit"
                        disabled={formik.isSubmitting || !formik.isValid}
                        sx={{mt: 2}}
                    >
                        Submit
                    </Button>
                </Box>
            </CardContent></Card>
        </Scaffold>
    );
}
import {useHistory} from "react-router-dom";
import {useFormik} from "formik";
import {Scaffold} from "../../components/Scaffold";
import {Box, Button, Card, CardContent, TextField, TextFieldProps} from "@mui/material";
import DatePicker from '@mui/lab/DatePicker';
import {useCreateProjectMutation} from "./projectsSlice";
import React from "react";
import {useSnackbar} from "notistack";
import dayjs, {Dayjs} from "dayjs";

interface Values {
    name: string;
    startDate: Dayjs;
    deadline: Dayjs;
}

export function CreateProjectForm() {
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveProject] = useCreateProjectMutation();

    const formik = useFormik({
        initialValues: {
            name: '',
            startDate: dayjs(),
            deadline: dayjs(),
        } as Values,
        validateOnChange: true,
        validate: values => {
            if (values.startDate.isAfter(values.deadline)) {
                return {
                    deadline: `Can't be before Planned Start Date`
                }
            }
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await saveProject({
                    name: values.name,
                    deadline: values.deadline.toISOString(),
                    startDate: values.startDate.toISOString(),
                }).unwrap();
                enqueueSnackbar(`Project "${values.name}" created successfully`)
                history.goBack();
            } catch (e) {
                // TODO error handling
                console.log(e)
                console.log("project creation failed");
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
                        minDate={formik.values.startDate}
                        label="Deadline"
                        value={formik.values.deadline}
                        mask={"__.__.____"}
                        inputFormat={"DD.MM.YYYY"}
                        onChange={(newValue) => {
                            formik.getFieldHelpers('deadline').setValue(newValue, true)
                        }}
                        renderInput={(params: TextFieldProps) => <TextField
                            {...params}
                            required
                            fullWidth
                            id="deadline"
                            name="deadline"
                            error={params.error || (formik.touched.deadline && Boolean(formik.errors.deadline))}
                            helperText={formik.touched.deadline && formik.errors.deadline}
                        />}
                    />
                    <Button variant="contained" fullWidth type="submit"
                            disabled={formik.isSubmitting || !formik.isValid}>
                        Submit
                    </Button>
                </Box>
            </CardContent></Card>
        </Scaffold>
    );
}
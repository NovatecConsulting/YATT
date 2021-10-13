import {useHistory} from "react-router-dom";
import {useFormik} from "formik";
import {Scaffold} from "../../components/Scaffold";
import {Box, Button, Card, CardContent, Paper, Snackbar, TextField, TextFieldProps} from "@mui/material";
import DatePicker from '@mui/lab/DatePicker';
import {useCreateProjectMutation} from "./projectsSlice";
import React from "react";
import {useSnackbar} from "notistack";

interface Values {
    name: string;
    plannedStartDate: Date;
    deadline: Date;
}

export function CreateProjectForm() {
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveProject] = useCreateProjectMutation();

    const formik = useFormik({
        initialValues: {
            name: '',
            plannedStartDate: new Date(),
            deadline: new Date(),
        } as Values,
        validateOnChange: true,
        validate: values => {
            if (values.plannedStartDate > values.deadline) {
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
                    plannedStartDate: values.plannedStartDate.toISOString(),
                }).unwrap();
                enqueueSnackbar(`Project "${values.name}" created successfully`)
                history.goBack();
            } catch (e) {
                // TODO error handling
                console.log("project creation failed");
            }
        },
    });

    return (
        <Scaffold title={"Create Project"}>
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
                        value={formik.values.plannedStartDate}
                        onChange={(newValue) => {
                            formik.getFieldHelpers('plannedStartDate').setValue(newValue, true)
                        }}
                        renderInput={(params: TextFieldProps) => <TextField
                            {...params}
                            required
                            fullWidth
                            id="deadline"
                            name="deadline"
                            error={params.error || (formik.touched.plannedStartDate && Boolean(formik.errors.plannedStartDate))}
                            helperText={formik.touched.plannedStartDate && formik.errors.plannedStartDate}
                        />}
                    />
                    <DatePicker
                        minDate={formik.values.plannedStartDate}
                        label="Deadline"
                        value={formik.values.deadline}
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
import {useHistory} from "react-router-dom";
import {useFormik} from "formik";
import {Scaffold} from "../../components/scaffold/Scaffold";
import {
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    TextField,
    TextFieldProps
} from "@mui/material";
import DatePicker from '@mui/lab/DatePicker';
import {useCreateProjectMutation} from "./projectsSlice";
import React from "react";
import {useSnackbar} from "notistack";
import dayjs, {Dayjs} from "dayjs";
import {useGetCompaniesQuery} from "../company/companySlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";

interface Values {
    name: string;
    startDate: Dayjs;
    deadline: Dayjs;
    companyId: string;
}

export function CreateProjectForm() {
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveProject] = useCreateProjectMutation();
    const companiesResult = useGetCompaniesQuery(undefined, {selectFromResult: selectEntitiesFromResult});

    const formik = useFormik({
        initialValues: {
            name: '',
            startDate: dayjs(),
            deadline: dayjs(),
            companyId: ''
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
                    companyId: values.companyId
                }).unwrap();
                enqueueSnackbar(`Project "${values.name}" created successfully`)
                history.goBack();
            } catch (e) {
                enqueueSnackbar(`Failed creating project.`);
                // TODO error handling
                console.log(e)
                console.log("project creation failed");
            }
        },
    });

    let content: ReactJSXElement;
    if (companiesResult.isLoading) {
        content = <CircularProgress/>;
    } else if (companiesResult.isSuccess && companiesResult.data) {
        content = (
            <Card><CardContent>
                <Box component="form" onSubmit={formik.handleSubmit}>
                    <FormControl fullWidth>
                        <InputLabel id="companyIdLabel">Company</InputLabel>
                        <Select
                            required
                            labelId="companyIdLabel"
                            id="companyId"
                            name="companyId"
                            value={formik.values.companyId}
                            label="Company"
                            onChange={formik.handleChange}
                        >
                            {companiesResult.data.map(company => (
                                <MenuItem key={company.identifier}
                                          value={company.identifier}>
                                    {`${company.name}`}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
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
                    <Button
                        variant="contained"
                        fullWidth
                        type="submit"
                        sx={{mt: 2}}
                        disabled={formik.isSubmitting || !formik.isValid}
                    >
                        Submit
                    </Button>
                </Box>
            </CardContent></Card>
        );
    } else if (companiesResult.isError) {
        content = <div>{companiesResult.error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
            {content}
        </Scaffold>
    );
}
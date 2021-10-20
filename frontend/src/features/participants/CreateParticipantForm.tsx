import React from "react";
import {useHistory, useParams} from "react-router-dom";
import {useSnackbar} from "notistack";
import {useFormik} from "formik";
import {Scaffold} from "../../components/Scaffold";
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
} from "@mui/material";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {useCreateParticipantMutation} from "./participantSlice";
import {useGetAllEmployeesQuery} from "../employee/employeeSlice";
import { FetchBaseQueryError } from "@reduxjs/toolkit/dist/query/react";

interface Values {
    employeeId: string;
}

export function CreateParticipantForm() {
    const {id: projectId} = useParams<{ id: string }>()

    const employeesResult = useGetAllEmployeesQuery(); // TODO filter out users who are already participant
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveParticipant] = useCreateParticipantMutation();

    const formik = useFormik({
        initialValues: {
            employeeId: '',
        } as Values,
        validateOnChange: true,
        onSubmit: async (values, formikHelpers) => {
            const employee = employeesResult.data?.find(employee => employee.identifier === values.employeeId);
            try {
                if (employee) {
                    await saveParticipant({
                        projectId: projectId,
                        companyId: employee.companyId,
                        userId: employee.userId,
                    }).unwrap();

                    enqueueSnackbar(`Participant "${employee.userFirstName} ${employee.userLastName}" created successfully`)
                    history.goBack();
                }
            } catch (e) {
                enqueueSnackbar(`Failed creating participant.`);
                // TODO error handling
                console.log("participant creation failed");
            }
        },
    });

    let content: ReactJSXElement;
    if (employeesResult.isLoading) {
        content = <CircularProgress/>;
    } else if (employeesResult.isSuccess) {
        content = (
            <Card><CardContent>
                <Box component="form" onSubmit={formik.handleSubmit}>
                    <FormControl fullWidth>
                        <InputLabel id="employeeIdLabel">User</InputLabel>
                        <Select
                            required
                            labelId="employeeIdLabel"
                            id="employeeId"
                            name="employeeId"
                            value={formik.values.employeeId}
                            label="User"
                            onChange={formik.handleChange}
                        >
                            {employeesResult.data.map(employee => (
                                <MenuItem key={employee.identifier}
                                          value={employee.identifier}>
                                    {`${employee.userFirstName} ${employee.userLastName}`}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
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
        );
    } else if (employeesResult.isError) {
        content = <div>{employeesResult.error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
            {content}
        </Scaffold>
    );
}
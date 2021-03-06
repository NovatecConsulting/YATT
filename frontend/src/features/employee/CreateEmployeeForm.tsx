import React from "react";
import {useHistory, useParams} from "react-router-dom";
import {useSnackbar} from "notistack";
import {useCreateEmployeeMutation} from "./employeeSlice";
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
} from "@mui/material";
import {useGetAllUsersQuery} from "../auth/usersSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";

interface Values {
    userId: string;
}

export function CreateEmployeeForm() {
    const {companyId} = useParams<{ companyId: string }>()

    const usersResult = useGetAllUsersQuery(); // TODO filter out users who are already employee
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveEmployee] = useCreateEmployeeMutation();

    const formik = useFormik({
        initialValues: {
            userId: '',
        } as Values,
        validateOnChange: true,
        onSubmit: async (values, formikHelpers) => {
            try {
                await saveEmployee({
                    companyId: companyId,
                    userId: values.userId,
                }).unwrap();
                const user = usersResult.data?.find(user => user.identifier === values.userId);
                enqueueSnackbar(`Employee "${user?.firstname} ${user?.lastname}" created successfully`)
                history.goBack();
            } catch (e) {
                enqueueSnackbar(`Failed creating employee.`);
                // TODO error handling
                console.log("employee creation failed");
            }
        },
    });

    let content: ReactJSXElement;
    if (usersResult.isLoading) {
        content = <CircularProgress/>;
    } else if (usersResult.isSuccess) {
        content = (
            <Card><CardContent>
                <Box component="form" onSubmit={formik.handleSubmit}>
                    <FormControl fullWidth>
                        <InputLabel id="userIdLabel">User</InputLabel>
                        <Select
                            required
                            labelId="userIdLabel"
                            id="userId"
                            name="userId"
                            value={formik.values.userId}
                            label="User"
                            onChange={formik.handleChange}
                        >
                            {usersResult.data.map(user => (
                                <MenuItem key={user.identifier}
                                          value={user.identifier}>{`${user.firstname} ${user.lastname}`}</MenuItem>
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
    } else if (usersResult.isError) {
        content = <div>{usersResult.error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
            {content}
        </Scaffold>
    );
}
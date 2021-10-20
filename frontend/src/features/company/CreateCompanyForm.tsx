import {useSnackbar} from "notistack";
import {useHistory} from "react-router-dom";
import {useFormik} from "formik";
import {Scaffold} from "../../components/Scaffold";
import {Box, Button, Card, CardContent, TextField} from "@mui/material";
import React from "react";
import {useCreateCompanyMutation} from "./companySlice";

interface Values {
    name: string;
}

export function CreateCompanyForm() {
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const [saveCompany] = useCreateCompanyMutation();

    const formik = useFormik({
        initialValues: {
            name: '',
        } as Values,
        validateOnChange: true,
        onSubmit: async (values, formikHelpers) => {
            try {
                await saveCompany({
                    name: values.name,
                }).unwrap();
                enqueueSnackbar(`Company "${values.name}" created successfully`)
                history.goBack();
            } catch (e) {
                enqueueSnackbar(`Failed creating company.`);
                // TODO error handling
                console.log(e)
                console.log("company creation failed");
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
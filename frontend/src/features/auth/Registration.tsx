import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {registered, selectIsRegistered} from "./authSlice";
import {Redirect, useHistory} from "react-router-dom";
import {Scaffold} from "../../components/Scaffold";
import {useFormik} from "formik";
import {Button, Card, CardContent, TextField} from "@mui/material";
import {RegisterUserDto, useRegisterUserMutation} from "./usersSlice";

export function Registration() {
    const history = useHistory();
    const dispatch = useAppDispatch();
    const [registerUser] = useRegisterUserMutation();
    const formik = useFormik({
        initialValues: {
            firstname: '',
            lastname: '',
        } as RegisterUserDto,
        onSubmit: async (userDto, formikHelpers) => {
            try {
                await registerUser(userDto).unwrap();
                dispatch(registered(true));
                history.replace("/");
            } catch (e) {
                // TODO error handling
                console.log("registration failed");
            }
        },
    });

    const isRegistered = useAppSelector(selectIsRegistered);
    if (isRegistered) {
        return <Redirect to={"/"}/>;
    }

    return (
        <Scaffold title={"Registration"}>
            <Card><CardContent>
                <form onSubmit={formik.handleSubmit}>
                    <TextField
                        required
                        fullWidth
                        id="firstname"
                        name="firstname"
                        label="Firstname"
                        value={formik.values.firstname}
                        onChange={formik.handleChange}
                        error={formik.touched.firstname && Boolean(formik.errors.firstname)}
                        helperText={formik.touched.firstname && formik.errors.firstname}
                    />
                    <TextField
                        required
                        fullWidth
                        id="lastname"
                        name="lastname"
                        label="Lastname"
                        value={formik.values.lastname}
                        onChange={formik.handleChange}
                        error={formik.touched.lastname && Boolean(formik.errors.lastname)}
                        helperText={formik.touched.lastname && formik.errors.lastname}
                    />
                    <Button variant="contained" fullWidth type="submit" disabled={formik.isSubmitting}>
                        Submit
                    </Button>
                </form>
            </CardContent></Card>
        </Scaffold>
    );
}
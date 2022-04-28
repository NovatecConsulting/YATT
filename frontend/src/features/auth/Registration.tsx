import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {registered, selectIsRegistered} from "./authSlice";
import {Redirect, useHistory} from "react-router-dom";
import {Scaffold} from "../../components/scaffold/Scaffold";
import {useFormik} from "formik";
import {Button, Card, CardContent, TextField} from "@mui/material";
import {RegisterUserDto, useRegisterUserMutation} from "./usersSlice";
import {useSnackbar} from "notistack";

export function Registration() {
    const {enqueueSnackbar} = useSnackbar();
    const history = useHistory();
    const dispatch = useAppDispatch();
    const [registerUser] = useRegisterUserMutation();
    const formik = useFormik({
        initialValues: {
            firstname: '',
            lastname: '',
            email: '',
            telephone: ''
        } as RegisterUserDto,
        onSubmit: async (userDto, formikHelpers) => {
            try {
                await registerUser(userDto).unwrap();
                dispatch(registered(true));
                history.replace("/");
            } catch (e) {
                enqueueSnackbar(`Registration failed`);
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
        <Scaffold title={"Registration"} showNav={false} alignItems="center">
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
                    <TextField
                        required
                        fullWidth
                        id="email"
                        name="email"
                        label="email"
                        value={formik.values.email}
                        onChange={formik.handleChange}
                        error={formik.touched.email && Boolean(formik.errors.email)}
                        helperText={formik.touched.email && formik.errors.email}
                    />
                    <TextField
                        required
                        fullWidth
                        id="telephone"
                        name="telephone"
                        label="telephone"
                        value={formik.values.telephone}
                        onChange={formik.handleChange}
                        error={formik.touched.telephone && Boolean(formik.errors.telephone)}
                        helperText={formik.touched.telephone && formik.errors.telephone}
                    />
                    <Button
                        variant="contained"
                        fullWidth
                        type="submit"
                        disabled={formik.isSubmitting}
                        sx={{mt: 2}}
                    >
                        Submit
                    </Button>
                </form>
            </CardContent></Card>
        </Scaffold>
    );
}
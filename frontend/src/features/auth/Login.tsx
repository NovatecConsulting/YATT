import {Box, Button} from "@mui/material";
import keycloak from "../../keycloak";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {selectIsAuthenticated, authLoading} from "./authSlice";
import {Redirect} from "react-router-dom";

export function Login() {
    const dispatch = useAppDispatch();

    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    if (isAuthenticated) {
        return <Redirect to={"/"}/>;
    }

    return (
        <Box component="main" sx={{p: 3}}>
            <Button
                onClick={() => {
                    dispatch(authLoading(true));
                    keycloak.login();
                }}
                variant="contained"
            >Login</Button>
        </Box>
    );
}
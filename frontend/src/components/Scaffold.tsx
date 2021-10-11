import React from "react";
import {AppBar, Box, IconButton, Toolbar, Typography} from "@mui/material";
import {Logout} from "@mui/icons-material";
import {useAppDispatch} from "../app/hooks";
import {logout} from "../features/auth/authSlice";
import {useKeycloak} from "@react-keycloak/web";

interface Props {
    title: string;
}

export function Scaffold(props: React.PropsWithChildren<Props>) {
    const dispatch = useAppDispatch();
    const {keycloak} = useKeycloak();
    return (
        <Box className={"centerColumn fullViewPort"}>
            <AppBar>
                <Toolbar>
                    <Typography variant="h6" component="div">
                        {props.title}
                    </Typography>
                    <Box sx={{flexGrow: 1}}/>
                    <IconButton
                        size="large"
                        edge="end"
                        aria-label="logout"
                        onClick={() => {
                            dispatch(logout());
                            keycloak.logout()
                        }}
                        color="inherit"
                    >
                        <Logout/>
                    </IconButton>
                </Toolbar>
            </AppBar>
            <Toolbar style={{flex: 0}}/>
            {props.children}
        </Box>
    );
}
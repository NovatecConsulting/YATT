import React from 'react';
import {
    BrowserRouter as Router,
    Switch
} from "react-router-dom";
import {ReactKeycloakProvider} from '@react-keycloak/web'
import {ProjectsList} from "./features/projects/ProjectsList";
import {AppBar, Box, CircularProgress, Toolbar, Typography} from "@mui/material";
import keycloak from "./keycloak";
import PrivateRoute from "./components/PrivateRoute";
import {AuthClientError, AuthClientEvent} from "@react-keycloak/core/lib/types";
import {useAppDispatch} from "./app/hooks";
import {tokenUpdated} from "./features/api/tokenSlice";

function App() {
    const dispatch = useAppDispatch();

    const initOptions = {
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
    }

    const handleOnEvent = async (event: AuthClientEvent, error?: AuthClientError) => {
        dispatch(tokenUpdated(keycloak.token));
    }

    return (
        <ReactKeycloakProvider
            authClient={keycloak}
            initOptions={initOptions}
            LoadingComponent={<CircularProgress/>}
            onEvent={handleOnEvent}
        >
            <Router>
                <Box style={{
                    display: "flex",
                    flexDirection: "column",
                    height: "100vh",
                    width: "100vw",
                    alignItems: "center",
                    justifyContent: "center"
                }}>
                    <AppBar>
                        <Toolbar>
                            <Typography variant="h6" component="div">
                                Projects
                            </Typography>
                        </Toolbar>
                    </AppBar>
                    <Toolbar style={{flex: 0}}/>
                    <Switch>
                        <PrivateRoute exact path={"/"} component={ProjectsList}/>
                    </Switch>
                </Box>
            </Router>
        </ReactKeycloakProvider>
    );
}

export default App;

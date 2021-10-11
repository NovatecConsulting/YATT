import React from 'react';
import {
    BrowserRouter as Router, Route,
    Switch
} from "react-router-dom";
import {ReactKeycloakProvider} from '@react-keycloak/web'
import {ProjectsList} from "./features/projects/ProjectsList";
import {Box, CircularProgress} from "@mui/material";
import keycloak from "./keycloak";
import PrivateRoute from "./components/PrivateRoute";
import {AuthClientError, AuthClientEvent, AuthClientTokens} from "@react-keycloak/core/lib/types";
import {
    authenticated, authLoading, registered,
    selectIsAuthLoading,
    tokenUpdated
} from "./features/auth/authSlice";
import {Login} from "./features/auth/Login";
import {Home} from "./components/Home";
import {Registration} from "./features/auth/Registration";
import {useAppDispatch} from "./app/hooks";
import {loadCurrentUser} from "./features/auth/currentUserSlice";
import {useStore} from "react-redux";

function App() {
    const store = useStore();
    const dispatch = useAppDispatch();

    const initOptions = {
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
    }

    const handleOnEvent = async (event: AuthClientEvent, error?: AuthClientError) => {
        if (event === 'onAuthSuccess') {
            if (keycloak.authenticated) {
                dispatch(tokenUpdated(keycloak.token));
                dispatch(authenticated(true));
                const {data: currentUser} = await dispatch(loadCurrentUser());
                if (currentUser) {
                    dispatch(registered(true));
                }
                dispatch(authLoading(false));
                keycloak.onAuthRefreshSuccess?.call(undefined);// workaround to trigger isLoadingCheck
            }
        } else if (event === 'onReady') {
            if (!keycloak.authenticated) {
                dispatch(authLoading(false));
                keycloak.onAuthRefreshSuccess?.call(undefined);// workaround to trigger isLoadingCheck
            }
        }
    }

    const isLoadingCheck = () => selectIsAuthLoading(store.getState());

    const handleOnTokens = (tokens: AuthClientTokens) => dispatch(tokenUpdated(tokens.token))

    return (
        <Box className={"centerColumn fullViewPort"}>
            <ReactKeycloakProvider
                authClient={keycloak}
                initOptions={initOptions}
                isLoadingCheck={isLoadingCheck}
                LoadingComponent={<CircularProgress/>}
                onEvent={handleOnEvent}
                onTokens={handleOnTokens}
            >
                <Router>
                    <Switch>
                        <Route exact path={"/"} component={Home}/>
                        <Route exact path={"/login"} component={Login}/>
                        <PrivateRoute exact path={"/registration"} component={Registration}
                                      allowUnregistered={true}/>
                        <PrivateRoute exact path={"/projects"} component={ProjectsList}/>
                    </Switch>
                </Router>
            </ReactKeycloakProvider>
        </Box>
    );
}

export default App;

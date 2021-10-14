import React from 'react';
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {ReactKeycloakProvider} from '@react-keycloak/web'
import {ProjectsList} from "./features/projects/ProjectsList";
import {Box, CircularProgress, ThemeProvider} from "@mui/material";
import keycloak from "./keycloak";
import PrivateRoute from "./components/PrivateRoute";
import {AuthClientError, AuthClientEvent, AuthClientTokens} from "@react-keycloak/core/lib/types";
import {authenticated, authLoading, registered, selectIsAuthLoading, tokenUpdated} from "./features/auth/authSlice";
import {Login} from "./features/auth/Login";
import {Home} from "./components/Home";
import {Registration} from "./features/auth/Registration";
import {useAppDispatch} from "./app/hooks";
import {loadCurrentUser} from "./features/auth/currentUserSlice";
import {useStore} from "react-redux";
import {TaskList} from "./features/tasks/TaskList";
import {CreateProjectForm} from "./features/projects/CreateProjectForm";
import {LocalizationProvider} from "@mui/lab";
import DateAdapter from '@mui/lab/AdapterDayjs';
import {theme} from "./theme";
import {SnackbarProvider} from "notistack";
import locale from "dayjs/locale/de.js";
import {CreateTaskForm} from "./features/tasks/CreateTaskForm";

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
                // TODO error handling if server not reachable
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
        <ThemeProvider theme={theme}>
            <LocalizationProvider dateAdapter={DateAdapter} locale={locale}>
                <SnackbarProvider maxSnack={3} autoHideDuration={2000}>
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
                                    <PrivateRoute exact path={"/projects/create"} component={CreateProjectForm}/>
                                    <PrivateRoute exact path={"/projects/:id/tasks"} component={TaskList}/>
                                    <PrivateRoute exact path={"/projects/:id/tasks/create"} component={CreateTaskForm}/>
                                </Switch>
                            </Router>
                        </ReactKeycloakProvider>
                    </Box>
                </SnackbarProvider>
            </LocalizationProvider>
        </ThemeProvider>
    );
}

export default App;

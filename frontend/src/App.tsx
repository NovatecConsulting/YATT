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
import {loadCurrentUser} from "./features/auth/usersSlice";
import {useStore} from "react-redux";
import {TaskList} from "./features/tasks/TaskList";
import {CreateProjectForm} from "./features/projects/CreateProjectForm";
import {LocalizationProvider} from "@mui/lab";
import DateAdapter from '@mui/lab/AdapterDayjs';
import {theme} from "./theme";
import {SnackbarProvider} from "notistack";
import locale from "dayjs/locale/de.js";
import {CreateTaskForm} from "./features/tasks/CreateTaskForm";
import dayjs from "dayjs";
import localizedFormat from "dayjs/plugin/localizedFormat";
import {CompanyList} from "./features/company/CompanyList";
import {CreateCompanyForm} from "./features/company/CreateCompanyForm";
import {EmployeeList} from "./features/employee/EmployeeList";
import {CreateEmployeeForm} from "./features/employee/CreateEmployeeForm";
import {ProjectDetailsPage} from "./features/projects/ProjectDetailsPage";
import {ParticipantList} from "./features/participants/ParticipantList";
import {CreateParticipantForm} from "./features/participants/CreateParticipantForm";
import {ProfilePage} from "./features/auth/ProfilePage";
import {TasksGanttChart} from "./features/tasks/TasksGanttChart";


function App() {
    const store = useStore();
    const dispatch = useAppDispatch();

    const initOptions = {
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
    }

    dayjs.extend(localizedFormat);
    dayjs.locale(locale);

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

    const loadingComponent = (
        <Box component="main" sx={{p: 3}}>
            <CircularProgress/>
        </Box>
    );

    return (
        <ThemeProvider theme={theme}>
            <LocalizationProvider dateAdapter={DateAdapter} locale={locale}>
                <SnackbarProvider maxSnack={3} autoHideDuration={2000}>
                    <ReactKeycloakProvider
                        authClient={keycloak}
                        initOptions={initOptions}
                        isLoadingCheck={isLoadingCheck}
                        LoadingComponent={loadingComponent}
                        onEvent={handleOnEvent}
                        onTokens={handleOnTokens}
                    >
                        <Router>
                            <Switch>
                                <Route exact path={"/"} component={Home}/>
                                <Route exact path={"/login"} component={Login}/>
                                <PrivateRoute exact path={"/registration"} component={Registration}
                                              allowUnregistered={true}/>
                                <PrivateRoute exact path={"/profile"} component={ProfilePage}/>
                                <PrivateRoute exact path={"/projects"} component={ProjectsList}/>
                                <PrivateRoute exact path={"/projects/new"} component={CreateProjectForm}/>
                                <PrivateRoute exact path={"/projects/:projectId"} component={ProjectDetailsPage}/>
                                <PrivateRoute exact path={"/projects/:projectId/participants"} component={ParticipantList}/>
                                <PrivateRoute exact path={"/projects/:projectId/participants/new"} component={CreateParticipantForm}/>
                                <PrivateRoute exact path={"/projects/:projectId/tasks"} component={TaskList}/>
                                <PrivateRoute exact path={"/projects/:projectId/tasks/gantt-chart"} component={TasksGanttChart}/>
                                <PrivateRoute exact path={"/projects/:projectId/tasks/new"} component={CreateTaskForm}/>
                                <PrivateRoute exact path={"/companies"} component={CompanyList}/>
                                <PrivateRoute exact path={"/companies/new"} component={CreateCompanyForm}/>
                                <PrivateRoute exact path={"/companies/:companyId/employees"} component={EmployeeList}/>
                                <PrivateRoute exact path={"/companies/:companyId/employees/new"}
                                              component={CreateEmployeeForm}/>
                            </Switch>
                        </Router>
                    </ReactKeycloakProvider>
                </SnackbarProvider>
            </LocalizationProvider>
        </ThemeProvider>
    );
}

export default App;

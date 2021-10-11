import * as React from 'react';
import {
    Redirect,
    Route,
    RouteProps,
} from 'react-router-dom';
import {useAppSelector} from "../app/hooks";
import {selectIsAuthenticated, selectIsRegistered} from "../features/auth/authSlice";

export interface CustomRouteProps {
    allowUnregistered?: boolean;
}

function PrivateRoute(props: RouteProps & CustomRouteProps) {
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const isRegistered = useAppSelector(selectIsRegistered);
    const allowUnregistered = props.allowUnregistered ?? false;

    const {component: Component, ...rest} = props;
    if (!Component) return null;

    return (
        <Route
            {...rest}
            render={props => (
                isAuthenticated && (isRegistered || allowUnregistered) ? <Component {...props} /> : <Redirect to={"/"}/>
            )}
        />
    )
}

export default PrivateRoute
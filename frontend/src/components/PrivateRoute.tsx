import * as React from 'react';
import {
    Route,
    RouteProps,
} from 'react-router-dom';
import {useKeycloak} from "@react-keycloak/web";

function PrivateRoute(props: RouteProps) {
    const {keycloak} = useKeycloak()
    const {component: Component, ...rest} = props;
    if (!Component) return null;

    return (
        <Route
            {...rest}
            render={props => (
                keycloak?.authenticated ? <Component {...props} /> : keycloak.login()
            )}
        />
    )
}

export default PrivateRoute
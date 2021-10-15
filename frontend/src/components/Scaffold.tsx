import React from "react";
import {AppBar, Box, Breadcrumbs, IconButton, Link, LinkProps, Toolbar, Typography} from "@mui/material";
import {Logout} from "@mui/icons-material";
import {useAppDispatch} from "../app/hooks";
import {logout} from "../features/auth/authSlice";
import {useKeycloak} from "@react-keycloak/web";
import {Link as RouterLink, useLocation, useParams} from 'react-router-dom';

const breadcrumbNameMap: { [key: string]: string } = {
    '/projects': 'Projects',
    '/projects/new': 'New',
    '/projects/tasks': 'Tasks',
    '/projects/tasks/new': 'New',
    '/companies': 'Companies',
    '/companies/new': 'New',
};

interface LinkRouterProps extends LinkProps {
    to: string;
    replace?: boolean;
}

const LinkRouter = (props: LinkRouterProps) => (
    <Link {...props} component={RouterLink as any}/>
);

interface Props {
    title?: string;
}

export function Scaffold(props: React.PropsWithChildren<Props>) {
    const {id: projectId} = useParams<{ id: string }>()
    const location = useLocation();
    const dispatch = useAppDispatch();
    const {keycloak} = useKeycloak();
    const pathnames = location.pathname.split('/').filter((path) => path);

    return (
        <Box className={"centerColumn fullViewPort"}>
            <AppBar>
                <Toolbar>
                    {props.title ? (
                        <Typography variant="h6" color="primary.contrastText" component="div">
                            {props.title}
                        </Typography>
                    ) : (
                        <Breadcrumbs
                            color={"primary.contrastText"}
                            separator={<Typography variant="h6" component="div">/</Typography>}
                        >
                            {pathnames.map((value, index) => {
                                const last = index === pathnames.length - 1;
                                const to = `/${pathnames.slice(0, index + 1).join('/')}`;
                                const text = breadcrumbNameMap[to.replace(`/${projectId}`, '')];

                                if (value === projectId) return null;
                                return last ? (
                                    <Typography variant="h6" color="primary.contrastText" component="div" key={to}>
                                        {text}
                                    </Typography>
                                ) : (
                                    <LinkRouter underline="hover" color="primary.contrastText" to={to} key={to}>
                                        <Typography variant="h6" component="div">
                                            {text}
                                        </Typography>
                                    </LinkRouter>
                                );
                            })}
                        </Breadcrumbs>
                    )}
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
            {props.children}
        </Box>
    );
}
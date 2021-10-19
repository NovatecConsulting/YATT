import React from "react";
import {
    AppBar,
    Box,
    Breadcrumbs,
    Divider,
    Drawer,
    IconButton,
    Link,
    LinkProps,
    List,
    ListItem,
    ListItemText,
    Toolbar,
    Typography
} from "@mui/material";
import {Logout} from "@mui/icons-material";
import {useAppDispatch} from "../app/hooks";
import {logout} from "../features/auth/authSlice";
import {useKeycloak} from "@react-keycloak/web";
import {Link as RouterLink, useHistory, useLocation, useParams} from 'react-router-dom';
import {Property} from "csstype";

const breadcrumbNameMap: { [key: string]: string } = {
    '/projects': 'Projects',
    '/projects/new': 'New',
    '/projects/tasks': 'Tasks',
    '/projects/participants': 'Participants',
    '/projects/tasks/new': 'New',
    '/companies': 'Companies',
    '/companies/new': 'New',
    '/companies/employees': 'Employees',
    '/companies/employees/new': 'New',
};

const topLevelDestinations = [
    {
        title: 'Projects',
        path: '/projects',
    },
    {
        title: 'Companies',
        path: '/companies',
    }
]

interface LinkRouterProps extends LinkProps {
    to: string;
    replace?: boolean;
}

const LinkRouter = (props: LinkRouterProps) => (
    <Link {...props} component={RouterLink as any}/>
);

interface Props {
    title?: string;
    alignItems?: Property.AlignItems;
}

const drawerWidth = 240;

export function Scaffold(props: React.PropsWithChildren<Props>) {
    const {id: id} = useParams<{ id: string }>()
    const location = useLocation();
    const dispatch = useAppDispatch();
    const history = useHistory();
    const {keycloak} = useKeycloak();
    const pathnames = location.pathname.split('/').filter((path) => path);

    return (
        <Box sx={{display: 'flex', width: '100vw', height: '100vh'}}>
            <AppBar
                position="fixed"
                sx={{width: `calc(100% - ${drawerWidth}px)`, ml: `${drawerWidth}px`}}
            >
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
                                let text = breadcrumbNameMap[to.replace(`/${id}`, '')];
                                if (value === id && to.startsWith('/projects')) {
                                    text = value;
                                } else if(value === id) {
                                    return null;
                                }
                                if (text === undefined) {
                                    text = value.charAt(0).toUpperCase() + value.slice(1);
                                }

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
            <Drawer
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                    },
                }}
                variant="permanent"
                anchor="left"
            >
                <Toolbar/>
                <Divider/>
                <List>
                    {topLevelDestinations.map((destination, index) => (
                        <ListItem button key={destination.path} onClick={() => history.push(destination.path)}>
                            <ListItemText primary={destination.title}/>
                        </ListItem>
                    ))}
                </List>
            </Drawer>
            <Box
                component="main"
                sx={{flexGrow: 1, p: 3, alignItems: props.alignItems}}
            >
                <Toolbar/>
                {props.children}
            </Box>
        </Box>
    );
}
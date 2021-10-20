import React from "react";
import {
    AppBar, Avatar,
    Box,
    Breadcrumbs,
    Divider,
    Drawer,
    IconButton,
    Link,
    LinkProps,
    List,
    ListItem, ListItemIcon,
    ListItemText, Menu, MenuItem,
    Toolbar, Tooltip,
    Typography
} from "@mui/material";
import {Logout, Settings} from "@mui/icons-material";
import {useAppDispatch, useAppSelector} from "../app/hooks";
import {logout} from "../features/auth/authSlice";
import {useKeycloak} from "@react-keycloak/web";
import {Link as RouterLink, useHistory, useLocation, useParams} from 'react-router-dom';
import {Property} from "csstype";
import {selectCurrentUser} from "../features/auth/usersSlice";

const breadcrumbNameMap: { [key: string]: string } = {
    '/profile': 'My Profile',
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
    showNav?: boolean;
}

const drawerWidth = 240;

export function Scaffold(props: React.PropsWithChildren<Props>) {
    const history = useHistory();
    const {showNav = true} = props;

    return (
        <Box sx={{display: 'flex', width: '100vw', height: '100vh'}}>
            <AppBar
                position="fixed"
                sx={{width: `calc(100% - ${showNav ? drawerWidth : 0}px)`, ml: `${showNav ? drawerWidth : 0}px`}}
            >
                <Toolbar>
                    <CustomBreadcrumbs title={props.title}/>
                    <Box sx={{flexGrow: 1}}/>
                    <AccountAvatar/>
                </Toolbar>
            </AppBar>
            {
                showNav ? (
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
                ) : null
            }
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

function CustomBreadcrumbs({title}: { title?: string }) {
    const {id} = useParams<{ id: string }>();
    const location = useLocation();
    const pathnames = location.pathname.split('/').filter((path) => path);

    if (title) {
        return (
            <Typography variant="h6" color="primary.contrastText" component="div">
                {title}
            </Typography>
        );
    } else {
        return (
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
                    } else if (value === id) {
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
        );
    }
}

function AccountAvatar() {
    const dispatch = useAppDispatch();
    const {keycloak} = useKeycloak();
    const history = useHistory();

    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };
    const handleLogout = () => {
        dispatch(logout());
        keycloak.logout();
    };
    const navigateToProfile = () => {
        history.push('/profile');
    }

    const currentUser = useAppSelector(selectCurrentUser);
    let initials: string | undefined;
    if (currentUser) {
        initials = `${currentUser.firstname.charAt(0)}${currentUser.lastname.charAt(0)}`;
    }
    let fullName: string | undefined;
    if (currentUser) {
        fullName = `${currentUser.firstname} ${currentUser.lastname}`;
    }

    return (
        <React.Fragment>
            <Tooltip title="Account settings">
                <IconButton
                    size="large"
                    edge="end"
                    aria-label="logout"
                    onClick={handleClick}
                    color="inherit"
                >
                    <Avatar>{initials}</Avatar>
                </IconButton>
            </Tooltip>
            <Menu
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                onClick={handleClose}
                PaperProps={{
                    elevation: 0,
                    sx: {
                        overflow: 'visible',
                        filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
                        mt: 1.5,
                        '& .MuiAvatar-root': {
                            width: 32,
                            height: 32,
                            ml: -0.5,
                            mr: 1,
                        },
                        '&:before': {
                            content: '""',
                            display: 'block',
                            position: 'absolute',
                            top: 0,
                            right: 14,
                            width: 10,
                            height: 10,
                            bgcolor: 'background.paper',
                            transform: 'translateY(-50%) rotate(45deg)',
                            zIndex: 0,
                        },
                    },
                }}
                transformOrigin={{horizontal: 'right', vertical: 'top'}}
                anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
            >
                <MenuItem onClick={navigateToProfile}>
                    <Avatar/>
                    <ListItemText primary="Profile" secondary={fullName}/>
                </MenuItem>
                <Divider/>
                <MenuItem>
                    <ListItemIcon>
                        <Settings fontSize="small"/>
                    </ListItemIcon>
                    Settings
                </MenuItem>
                <MenuItem onClick={handleLogout}>
                    <ListItemIcon>
                        <Logout fontSize="small"/>
                    </ListItemIcon>
                    Logout
                </MenuItem>
            </Menu>
        </React.Fragment>
    );
}
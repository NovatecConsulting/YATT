import {
    Button,
    CircularProgress, IconButton,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow, Toolbar, Tooltip,
    Typography
} from "@mui/material";
import AddIcon from '@mui/icons-material/Add';
import {selectProjectById, useGetProjectsQuery, useRescheduleProjectMutation} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/Scaffold";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import React from "react";
import {useHistory} from "react-router-dom";

export function ProjectsList() {
    const {
        data: projectIds,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetProjectsQuery(undefined, {selectFromResult: selectIdsFromResult});

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && projectIds) {
        content = (
            <Paper>
                <TableToolbar/>
                <TableContainer sx={{minWidth: 1000}}>
                    <Table stickyHeader>
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell>Planned Start Date</TableCell>
                                <TableCell>Deadline</TableCell>
                                <TableCell/>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {
                                projectIds.map(
                                    (projectId: EntityId) => <ProjectRow key={projectId} projectId={projectId}/>
                                )
                            }
                        </TableBody>
                    </Table>
                </TableContainer>
            </Paper>
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold title={"Projects"}>
            {content}
        </Scaffold>
    );
}

interface ProjectRowProps {
    projectId: EntityId;
}

function ProjectRow(props: ProjectRowProps) {
    const project = useAppSelector(state => selectProjectById(state, props.projectId))
    const history = useHistory()
    const [rescheduleProject, {isLoading}] = useRescheduleProjectMutation();

    if (project) {
        return (
            <TableRow hover onClick={() => history.push(`/projects/${project.identifier}/tasks`)}>
                <TableCell>{project.name}</TableCell>
                <TableCell>{project.plannedStartDate}</TableCell>
                <TableCell>{project.deadline}</TableCell>
                <TableCell align="right">
                    <Button
                        disabled={isLoading}
                        onClick={event => {
                            event.stopPropagation();
                            const date = new Date(project.deadline);
                            date.setDate(date.getDate() + 1);
                            rescheduleProject({
                               identifier: project.identifier,
                               aggregateVersion: project.version,
                                newStartDate: project.plannedStartDate,
                                newDeadline: date.toISOString(),
                            });
                        }}
                    >Postpone Deadline</Button>
                </TableCell>
            </TableRow>
        );
    } else {
        return null;
    }
}

function TableToolbar() {
    const history = useHistory();
    return (
        <Toolbar
            sx={{
                pl: {sm: 2},
                pr: {xs: 1, sm: 1},
            }}
        >
            <Typography
                sx={{flex: '1 1 100%'}}
                color="inherit"
                variant="subtitle1"
                component="div"
            >
                All Projects
            </Typography>
            <Tooltip title="Create Project">
                <IconButton
                    onClick={() => history.push('/projects/create')}
                >
                    <AddIcon fontSize={"large"}/>
                </IconButton>
            </Tooltip>
        </Toolbar>
    );
};
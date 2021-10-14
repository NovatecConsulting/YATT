import {
    Button,
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
} from "@mui/material";
import {selectProjectById, useGetProjectsQuery, useRescheduleProjectMutation} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/Scaffold";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import React from "react";
import {useHistory} from "react-router-dom";
import {TableToolbar} from "../../components/TableToolbar";

export function ProjectsList() {
    const history = useHistory();
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
            <TableContainer sx={{minWidth: 1000}} component={Paper}>
                <TableToolbar
                    title={'All Projects'}
                    tooltip={'Create Project'}
                    onClick={() => history.push(`/projects/new`)}
                />
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
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
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
    const [rescheduleProject] = useRescheduleProjectMutation();

    if (project) {
        return (
            <TableRow hover onClick={() => history.push(`/projects/${project.identifier}/tasks`)}>
                <TableCell>{project.name}</TableCell>
                <TableCell>{new Date(project.startDate).toLocaleDateString()}</TableCell>
                <TableCell>{new Date(project.deadline).toLocaleDateString()}</TableCell>
                <TableCell align="right">
                    <Button
                        onClick={event => {
                            event.stopPropagation();
                            const date = new Date(project.deadline);
                            date.setDate(date.getDate() + 1);
                            rescheduleProject({
                                identifier: project.identifier,
                                version: project.version,
                                startDate: project.startDate,
                                deadline: date.toISOString(),
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

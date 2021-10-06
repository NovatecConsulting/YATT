import {CircularProgress, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {selectProjectById, useGetProjectsQuery} from "./projectsSlice";
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
            <TableContainer sx={{minWidth: 1000, borderRadius: '10px'}} component={Paper}>
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Planned Start Date</TableCell>
                            <TableCell>Deadline</TableCell>
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

    if (project) {
        return (
            <TableRow hover onClick={() => history.push(`/projects/${project.identifier}/tasks`)}>
                <TableCell>{project.name}</TableCell>
                <TableCell>{project.plannedStartDate}</TableCell>
                <TableCell>{project.deadline}</TableCell>
            </TableRow>
        );
    } else {
        return null;
    }
}
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
import {
    Project,
    selectProjectById,
    useGetProjectsQuery,
    useRenameProjectMutation,
    useRescheduleProjectMutation
} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/Scaffold";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import React from "react";
import {useHistory} from "react-router-dom";
import {TableToolbar} from "../../components/TableToolbar";
import dayjs from "dayjs";
import {Task, useRenameTaskMutation, useRescheduleTaskMutation} from "../tasks/taskSlice";
import {EditableTableCell} from "../../components/EditableTableCell";
import {EditableDateTableCells} from "../../components/EditableDatesTableCell";

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
            <TableContainer sx={{maxWidth: 1000}} component={Paper}>
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

    const navigateToProjectDetailsPage = () => history.push(`/projects/${project?.identifier}`)

    const onSave = async (startDate: string, endDate: string) => {
        await rescheduleProject({
            identifier: project!.identifier,
            version: project!.version,
            startDate: startDate,
            deadline: endDate,
        }).unwrap();
    }

    if (project) {
        return (
            <TableRow hover onClick={navigateToProjectDetailsPage}>
                <ProjectNameCell project={project} />
                <EditableDateTableCells
                    canEdit={true}
                    onSave={onSave}
                    startDate={project.startDate}
                    endDate={project.deadline}
                />
            </TableRow>
        );
    } else {
        return null;
    }
}

function ProjectNameCell({project}: { project: Project }) {
    const [saveName] = useRenameProjectMutation();

    const onSave = async (name: string) => {
        await saveName({
            identifier: project.identifier.toString(),
            version: project.version,
            name: name,
        }).unwrap();
    };

    return (
        <EditableTableCell initialValue={project.name} label={'Name'} canEdit={true} onSave={onSave}/>
    );
}

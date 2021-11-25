import {
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
    ProjectStatus,
    selectProjectById,
    useGetProjectsQuery,
    useRenameProjectMutation,
    useRescheduleProjectMutation
} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/scaffold/Scaffold";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import React from "react";
import {useHistory} from "react-router-dom";
import {TableToolbar} from "../../components/TableToolbar";
import {EditableText} from "../../components/EditableText";
import {EditableDateTableCells} from "../../components/EditableDatesTableCell";
import dayjs from "dayjs";

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
                            <TableCell>Company</TableCell>
                            <TableCell>Planned Start Date</TableCell>
                            <TableCell>Deadline</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Actual End Date</TableCell>
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
                <ProjectNameCell project={project}/>
                <TableCell>{project.companyReference.displayName}</TableCell>
                <EditableDateTableCells
                    canEdit={true}
                    onSave={onSave}
                    startDate={project.startDate}
                    endDate={project.deadline}
                />
                <ProjectStatusCell status={project.status}/>
                <TableCell>{project.actualEndDate ? dayjs(project.actualEndDate).format('DD.MM.YYYY') : '-'}</TableCell>
            </TableRow>
        );
    } else {
        return null;
    }
}

function ProjectStatusCell({status}: { status: ProjectStatus }) {
    let color: string;
    let text: string;
    switch (status) {
        case ProjectStatus.DELAYED:
            text = 'Delayed'
            color = 'red';
            break;
        case ProjectStatus.ON_TIME:
            text = 'On Time'
            color = 'green';
            break;
    }

    return <TableCell sx={{color: color}}>{text}</TableCell>
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
        <TableCell>
            <EditableText initialValue={project.name} label={'Name'} canEdit={true} onSave={onSave}/>
        </TableCell>
    );
}

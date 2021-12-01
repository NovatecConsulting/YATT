import {
    Box,
    CircularProgress, IconButton,
    Paper,
    Typography,
} from "@mui/material";
import {
    Project,
    ProjectStatus,
    useGetProjectsQuery,
    useRenameProjectMutation,
    useRescheduleProjectMutation
} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {Scaffold} from "../../components/scaffold/Scaffold";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";
import React, {useState} from "react";
import {useHistory} from "react-router-dom";
import {TableToolbar} from "../../components/TableToolbar";
import {EditableText} from "../../components/EditableText";
import {RescheduleDialog} from "../../components/EditableDatesTableCell";
import dayjs from "dayjs";
import {VirtualizedTable} from "../../components/VirtualizedTable";
import {Edit} from "@mui/icons-material";

export function ProjectsList() {
    const history = useHistory();
    const {
        data: projects,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetProjectsQuery(undefined, {selectFromResult: selectEntitiesFromResult});

    const [projectToReschedule, setProjectToReschedule] = useState<Project | undefined>(undefined);

    const closeRescheduleDialog = () => setProjectToReschedule(undefined);

    const navigateToCreateProjectForm = () => history.push(`/projects/new`);

    const navigateToProjectDetailsPage = (projectId: string) => history.push(`/projects/${projectId}`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && projects) {
        content = (
            <Paper sx={{width: 1000, height: 500, display: "flex", flexFlow: "column", flex: "1 1 auto"}}>
                <TableToolbar
                    title={'All Projects'}
                    tooltip={'Create Project'}
                    onClick={navigateToCreateProjectForm}
                />
                <Box sx={{flex: "1 1 auto"}}>
                    <VirtualizedTable
                        rowHeight={64}
                        rowCount={projects.length}
                        rowGetter={(index) => projects[index.index]}
                        onRowClick={(row) => navigateToProjectDetailsPage((row.rowData as Project).identifier)}
                        columns={[
                            {
                                width: 120,
                                label: "Name",
                                dataKey: "name",
                                cellRenderer: (cellProps) => <ProjectNameCell project={cellProps.rowData as Project}/>
                            },
                            {
                                width: 120,
                                label: "Company",
                                dataKey: "companyReference",
                                cellRenderer: (cellProps) => <CompanyNameCell project={cellProps.rowData as Project}/>
                            },
                            {
                                width: 120,
                                label: "Planned Start Date",
                                dataKey: "startDate",
                                cellRenderer: (cellProps) => <DateCell project={cellProps.rowData as Project}
                                                                       isStartDate={true}
                                                                       onEdit={setProjectToReschedule}/>
                            },
                            {
                                width: 120,
                                label: "Deadline",
                                dataKey: "deadline",
                                cellRenderer: (cellProps) => <DateCell project={cellProps.rowData as Project}
                                                                       onEdit={setProjectToReschedule}/>
                            },
                            {
                                width: 120,
                                label: "Status",
                                dataKey: "status",
                                cellRenderer: (cellProps) => <ProjectStatusCell project={cellProps.rowData as Project}/>
                            },
                            {
                                width: 120,
                                label: "Actual End Date",
                                dataKey: "actualEndDate",
                                cellRenderer: (cellProps) => <ActualEndDateCell project={cellProps.rowData as Project}/>
                            },
                        ]}
                    />
                </Box>
            </Paper>
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
            {content}
            <ProjectRescheduleDialog
                key={projectToReschedule?.identifier}
                project={projectToReschedule}
                handleClose={closeRescheduleDialog}
            />
        </Scaffold>
    );
}

function ProjectStatusCell({project}: { project: Project }) {
    let color: string;
    let text: string;
    switch (project.status) {
        case ProjectStatus.DELAYED:
            text = 'Delayed'
            color = 'red';
            break;
        case ProjectStatus.ON_TIME:
            text = 'On Time'
            color = 'green';
            break;
    }

    return <Typography variant={"body2"} sx={{color: color}}>{text}</Typography>
}

function CompanyNameCell({project}: { project: Project }) {
    return <Typography variant={"body2"}>{project.companyReference.displayName}</Typography>;
}

function ActualEndDateCell({project}: { project: Project }) {
    return <Typography
        variant={"body2"}>{project.actualEndDate ? dayjs(project.actualEndDate).format('DD.MM.YYYY') : '-'}</Typography>;
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
        <EditableText initialValue={project.name} label={'Name'} canEdit={true} onSave={onSave}/>
    );
}

interface ProjectRescheduleDialogProps {
    project?: Project;
    handleClose: () => void;
}

function ProjectRescheduleDialog({project, handleClose}: ProjectRescheduleDialogProps) {
    const [rescheduleProject] = useRescheduleProjectMutation();
    const isOpen = !!project;

    const onSave = async (startDate: string, endDate: string) => {
        await rescheduleProject({
            identifier: project!.identifier,
            version: project!.version,
            startDate: startDate,
            deadline: endDate,
        }).unwrap();
    }

    return (
        <RescheduleDialog initialStartDate={project?.startDate ?? ""}
                          initialEndDate={project?.deadline ?? ""}
                          open={isOpen}
                          onClose={handleClose}
                          onSave={onSave}/>
    );
}

function DateCell({
                      project,
                      isStartDate,
                      onEdit
                  }: { project: Project, isStartDate?: boolean, onEdit: (project: Project) => void }) {
    return (
        <React.Fragment>
            {dayjs(isStartDate ? project.startDate : project.deadline).format('L')}
            <IconButton size='small' sx={{ml: 1}} onClick={(event) => {
                event.stopPropagation();
                onEdit(project);
            }}>
                <Edit fontSize="inherit"/>
            </IconButton>
        </React.Fragment>
    );
}

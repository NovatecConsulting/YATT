import {CircularProgress, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {Project, useGetProjectsQuery} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";

export function ProjectsList() {
    const {
        data: projects = [],
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetProjectsQuery();

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess) {
        content = (
            <TableContainer sx={{flex: 1, maxWidth: 900}}>
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell>Id</TableCell>
                            <TableCell>Name</TableCell>
                            <TableCell>Planned Start Date</TableCell>
                            <TableCell>Deadline</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {projects.map(project => <ProjectRow key={project.identifier} project={project}/>)}
                    </TableBody>
                </Table>
            </TableContainer>
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return content;
}

function ProjectRow({project}: { project: Project }) {
    return (
        <TableRow hover>
            <TableCell>{project.identifier}</TableCell>
            <TableCell>{project.name}</TableCell>
            <TableCell>{project.plannedStartDate}</TableCell>
            <TableCell>{project.deadline}</TableCell>
        </TableRow>
    );
}
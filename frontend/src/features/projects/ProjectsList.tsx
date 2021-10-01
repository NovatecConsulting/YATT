import {useAppSelector} from "../../app/hooks";

import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {projectSelectors} from "./projectsSlice";
import {EntityId} from "@reduxjs/toolkit";

export function ProjectsList() {
    const projectIds = useAppSelector(projectSelectors.selectIds);

    return (
        <section>
            <TableContainer>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Id</TableCell>
                            <TableCell>Name</TableCell>
                            <TableCell>Planned Start Date</TableCell>
                            <TableCell>Deadline</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {projectIds.map(projectId => <ProjectRow key={projectId} projectId={projectId}/>)}
                    </TableBody>
                </Table>
            </TableContainer>
        </section>
    )
}

function ProjectRow({projectId}: { projectId: EntityId }) {
    const project = useAppSelector(state => projectSelectors.selectById(state, projectId));

    if (project) {
        return (
            <TableRow>
                <TableCell>{project.identifier}</TableCell>
                <TableCell>{project.name}</TableCell>
                <TableCell>{project.plannedStartDate}</TableCell>
                <TableCell>{project.deadline}</TableCell>
            </TableRow>
        );
    }
    return null;
}
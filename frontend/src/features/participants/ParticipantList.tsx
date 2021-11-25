import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {CircularProgress, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import {
    selectParticipantByProjectIdAndParticipantId,
    useGetParticipantsByProjectQuery,
} from "./participantSlice";
import {useHistory, useParams} from "react-router-dom";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/scaffold/Scaffold";
import React from "react";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {selectProjectByIdFromResult, useGetProjectsQuery} from "../projects/projectsSlice";

export function ParticipantList() {
    const history = useHistory();
    const {id: projectId} = useParams<{ id: string }>();
    // TODO quick workaround to keep subscribed to query
    const {data: project} = useGetProjectsQuery(undefined, {
        selectFromResult: (result) => selectProjectByIdFromResult(result, projectId)
    });

    const {
        data: ids,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetParticipantsByProjectQuery(projectId, {selectFromResult: selectIdsFromResult});

    const navigateToCreateFrom = () => history.push(`/projects/${projectId}/participants/new`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && ids) {
        content = (
            <TableContainer sx={{maxWidth: 1000}} component={Paper}>
                <TableToolbar
                    title={`Participants for Project "${project?.name}"`}
                    tooltip={'Create Participant'}
                    onClick={navigateToCreateFrom}
                />
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell>Company</TableCell>
                            <TableCell>First Name</TableCell>
                            <TableCell>Last Name</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {
                            ids.map(
                                (id: EntityId) => <ParticipantRow key={id} projectId={projectId} participantId={id}/>
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

interface ParticipantRowProps {
    projectId: EntityId;
    participantId: EntityId;
}

function ParticipantRow(props: ParticipantRowProps) {
    const participant = useAppSelector((state) => selectParticipantByProjectIdAndParticipantId(state, props.projectId, props.participantId))
    if (participant)
        return (
            <TableRow hover>
                <TableCell>{participant.companyName}</TableCell>
                <TableCell>{participant.userFirstName}</TableCell>
                <TableCell>{participant.userLastName}</TableCell>
            </TableRow>
        );
    else {
        return null;
    }
}
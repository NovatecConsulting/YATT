import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Box,
    CircularProgress,
    Paper,
} from "@mui/material";
import {
    useGetParticipantsByProjectQuery,
} from "./participantSlice";
import {useHistory, useParams} from "react-router-dom";
import {Scaffold} from "../../components/scaffold/Scaffold";
import React from "react";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {selectProjectByIdFromResult, useGetProjectsQuery} from "../projects/projectsSlice";
import {VirtualizedTable} from "../../components/VirtualizedTable";

export function ParticipantList() {
    const history = useHistory();
    const {projectId} = useParams<{ projectId: string }>();
    // TODO quick workaround to keep subscribed to query
    const {data: project} = useGetProjectsQuery(undefined, {
        selectFromResult: (result) => selectProjectByIdFromResult(result, projectId)
    });

    const {
        data: participants,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetParticipantsByProjectQuery(projectId, {selectFromResult: selectEntitiesFromResult});

    const navigateToParticipantCreateFrom = () => history.push(`/projects/${projectId}/participants/new`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && participants) {
        content = (
            <Paper sx={{width: 1000, display: "flex", flexFlow: "column", flex: "1 1 auto"}}>
                <TableToolbar
                    title={`Participants for Project "${project?.name}"`}
                    tooltip={'Create Participant'}
                    onClick={navigateToParticipantCreateFrom}
                />
                <Box sx={{flex: "1 1 auto"}}>
                    <VirtualizedTable
                        rowHeight={64}
                        rowCount={participants.length}
                        rowGetter={(index) => participants[index.index]}
                        columns={[
                            {
                                width: 120,
                                flexGrow: 1,
                                label: "Company",
                                dataKey: "companyName",
                            },
                            {
                                width: 120,
                                flexGrow: 1,
                                label: "First Name",
                                dataKey: "userFirstName",
                            },
                            {
                                width: 120,
                                flexGrow: 1,
                                label: "Last Name",
                                dataKey: "userLastName",
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
        </Scaffold>
    );
}

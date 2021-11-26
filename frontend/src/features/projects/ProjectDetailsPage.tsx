import {Scaffold} from "../../components/scaffold/Scaffold";
import {Box, Button, Card, CardActions, CardContent, CardHeader, Skeleton, Typography} from "@mui/material";
import {useHistory, useParams} from "react-router-dom";
import {useGetProjectDetailsQuery} from "./projectsSlice";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import React from "react";

export function ProjectDetailsPage() {
    const history = useHistory();
    const {projectId} = useParams<{ projectId: string }>();

    const result = useGetProjectDetailsQuery(projectId);
    const navigateToTaskList = () => history.push(`/projects/${projectId}/tasks`)
    const navigateToTaskGanttChart = () => history.push(`/projects/${projectId}/tasks/gantt-chart`)
    const navigateToParticipantList = () => history.push(`/projects/${projectId}/participants`)

    let taskCardContent: ReactJSXElement | null = null;
    if (result.isLoading || result.isSuccess) {
        taskCardContent = (
            <React.Fragment>
                <Typography>
                    {result.isSuccess ? `# Overall Tasks: ${result.data.allTasksCount}` : <Skeleton variant="text"/>}
                </Typography>
                <Typography>
                    {result.isSuccess ? `# Planned Tasks: ${result.data.plannedTasksCount}` :
                        <Skeleton variant="text"/>}
                </Typography>
                <Typography>
                    {result.isSuccess ? `# Started Tasks: ${result.data.startedTasksCount}` :
                        <Skeleton variant="text"/>}
                </Typography>
                <Typography>
                    {result.isSuccess ? `# Completed Tasks: ${result.data.completedTasksCount}` :
                        <Skeleton variant="text"/>}
                </Typography>
            </React.Fragment>
        );
    } else if (result.isError) {
        taskCardContent = <div>{result.error}</div>;
    }

    return (
        <Scaffold>
            <Box className='row'>
                <Card sx={{width: 300, display: 'flex', flexDirection: 'column'}}>
                    <CardHeader title='Tasks'/>
                    <CardContent sx={{flexGrow: 1}}>
                        {taskCardContent}
                    </CardContent>
                    <CardActions>
                        <Button onClick={navigateToTaskList}>View Task List</Button>
                        <Button onClick={navigateToTaskGanttChart}>View Gantt Chart</Button>
                    </CardActions>
                </Card>
                <Card sx={{width: 300, display: 'flex', flexDirection: 'column'}}>
                    <CardHeader title='Participants'/>
                    <CardContent sx={{flexGrow: 1}}>
                        <Typography>
                            <Skeleton variant="text"/>
                        </Typography>
                        <Typography>
                            <Skeleton variant="text"/>
                        </Typography>
                        <Typography>
                            <Skeleton variant="text"/>
                        </Typography>
                        <Typography>
                            <Skeleton variant="text"/>
                        </Typography>
                    </CardContent>
                    <CardActions>
                        <Button onClick={navigateToParticipantList}>View Participants</Button>
                    </CardActions>
                </Card>
            </Box>
        </Scaffold>
    );
}
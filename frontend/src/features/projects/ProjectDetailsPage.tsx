import {Scaffold} from "../../components/Scaffold";
import {Box, Button, Card, CardActions, CardContent, CardHeader, Skeleton} from "@mui/material";
import {useHistory, useParams} from "react-router-dom";

export function ProjectDetailsPage() {
    const history = useHistory();
    const {id: projectId} = useParams<{ id: string }>();

    const navigateToTaskList = () => history.push(`/projects/${projectId}/tasks`)
    const navigateToParticipantList = () => history.push(`/projects/${projectId}/participants`)

    return (
        <Scaffold alignItems='start'>
            <Box className='row'>
                <Card>
                    <CardHeader title='Tasks'/>
                    <CardContent>
                        <Skeleton variant="text" width={300}/>
                        <Skeleton variant="text"/>
                        <Skeleton variant="text"/>
                        <Skeleton variant="text"/>
                    </CardContent>
                    <CardActions>
                        <Button onClick={navigateToTaskList}>View Tasks</Button>
                    </CardActions>
                </Card>
                <Card>
                    <CardHeader title='Participants'/>
                    <CardContent>
                        <Skeleton variant="text" width={300}/>
                        <Skeleton variant="text"/>
                        <Skeleton variant="text"/>
                        <Skeleton variant="text"/>
                    </CardContent>
                    <CardActions>
                        <Button onClick={navigateToParticipantList}>View Participants</Button>
                    </CardActions>
                </Card>
            </Box>
        </Scaffold>
    );
}
import {Box, Button, Card, CardContent, CardHeader, Typography} from "@mui/material";
import keycloak from "../../keycloak";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {selectIsAuthenticated, authLoading} from "./authSlice";
import {Redirect} from "react-router-dom";

export function Login() {
    const dispatch = useAppDispatch();

    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    if (isAuthenticated) {
        return <Redirect to={"/"}/>;
    }

    return (
        <Box component="main" sx={{mt: '20px'}}>
            <Typography
                variant='h4'
                color='#ededed'
                sx={{
                    p: '62px 10px 20px',
                    fontWeight: 400,
                    fontSize: '29px',
                    textAlign: 'center',
                    letterSpacing: '3px',
                    mb: '40px'
                }}
            >
                EVENTSOURCING-WITH-AXON
            </Typography>
            <Card sx={{
                borderRadius: 0,
                borderTop: '4px solid',
                borderColor: '#0066cc',
                width: '500px',
                padding: '20px 40px 30px 40px',
                boxSizing: 'border-box'
            }}>
                <Typography
                    variant='h6'
                    color='#363636'
                    sx={{mb: 4, fontWeight: 300, fontSize: '24px', textAlign: 'center'}}>
                    You need to Sign In
                </Typography>
                <Button
                    fullWidth
                    sx={{
                        borderRadius: 0,
                        m: 0,
                        backgroundColor: '#0066cc',
                        height: '30px',
                        fontSize: '14px',
                        textTransform: 'none'
                    }}
                    onClick={() => {
                        dispatch(authLoading(true));
                        keycloak.login();
                    }}
                    variant="contained"
                >
                    Sign In
                </Button>
            </Card>
        </Box>
    );
}
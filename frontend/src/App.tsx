import React from 'react';
import {ProjectsList} from "./features/projects/ProjectsList";
import {AppBar, Box, Toolbar, Typography} from "@mui/material";

function App() {
    return (
        <Box style={{
            display: "flex",
            flexDirection: "column",
            height: "100vh",
            width: "100vw",
            alignItems: "center",
            justifyContent: "center"
        }}>
            <AppBar>
                <Toolbar>
                    <Typography variant="h6" component="div">
                        Projects
                    </Typography>
                </Toolbar>
            </AppBar>
            <Toolbar style={{flex: 0}}/>
            <ProjectsList/>
        </Box>
    );
}

export default App;

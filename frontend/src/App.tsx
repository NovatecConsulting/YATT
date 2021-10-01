import React from 'react';
import {ProjectsList} from "./features/projects/ProjectsList";
import {AppBar, Box, Toolbar, Typography} from "@mui/material";

function App() {
    return (
        <React.Fragment>
            <Box sx={{flexGrow: 1}}>
                <AppBar position="static">
                    <Toolbar>
                        <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                            Projects
                        </Typography>
                    </Toolbar>
                </AppBar>
            </Box>
            <Box component="main">
                <ProjectsList/>
            </Box>
        </React.Fragment>
    );
}

export default App;

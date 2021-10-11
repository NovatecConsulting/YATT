import React from "react";
import {AppBar, Box, Toolbar, Typography} from "@mui/material";

interface Props {
    title: string;
}

export function Scaffold(props: React.PropsWithChildren<Props>) {
    return (
        <Box className={"centerColumn fullViewPort"}>
            <AppBar>
                <Toolbar>
                    <Typography variant="h6" component="div">
                        {props.title}
                    </Typography>
                </Toolbar>
            </AppBar>
            <Toolbar style={{flex: 0}}/>
            {props.children}
        </Box>
    );
}
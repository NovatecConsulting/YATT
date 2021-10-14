import React from "react";
import {IconButton, Toolbar, Tooltip, Typography} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

interface TableToolbarProps {
    title: string;
    tooltip: string;
    onClick: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
}

export function TableToolbar(props: TableToolbarProps) {
    return (
        <Toolbar
            sx={{
                pl: {sm: 2},
                pr: {xs: 1, sm: 1},
            }}
        >
            <Typography
                sx={{flex: '1 1 100%'}}
                color="inherit"
                variant="subtitle1"
                component="div"
            >
                {props.title}
            </Typography>
            <Tooltip title={props.tooltip}>
                <IconButton onClick={props.onClick}>
                    <AddIcon fontSize={"large"}/>
                </IconButton>
            </Tooltip>
        </Toolbar>
    );
};
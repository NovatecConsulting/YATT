import {EntityId} from "@reduxjs/toolkit";
import {SxProps} from "@mui/system";
import {Theme} from "@mui/material/styles";
import {useCompleteTaskMutation, useStartTaskMutation} from "../taskSlice";
import React from "react";
import {Button} from "@mui/material";

export interface UpdateTaskStatusButtonProps {
    taskStatus: string;
    taskId: EntityId;
    assigned: boolean;
    sx?: SxProps<Theme>;
}

export function UpdateTaskStatusButton({taskStatus, taskId, assigned, sx}: UpdateTaskStatusButtonProps) {
    const [startTask, {isLoading: isLoadingStart}] = useStartTaskMutation();
    const [completeTask, {isLoading: isLoadingComplete}] = useCompleteTaskMutation();
    const isLoading = isLoadingStart || isLoadingComplete;

    let taskStateChangeAction: (taskId: string) => void;
    const handleClick = (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
        event.stopPropagation();
        taskStateChangeAction(taskId.toString());
    };
    let buttonTitle = '';
    let showButton = assigned;
    switch (taskStatus) {
        case 'PLANNED':
            buttonTitle = 'Start Task';
            taskStateChangeAction = startTask;
            break;
        case 'STARTED':
            buttonTitle = 'Complete Task';
            taskStateChangeAction = completeTask;
            break;
        case 'COMPLETED':
        default:
            showButton = false;
            break;
    }

    return showButton ? (
        <Button sx={sx} onClick={handleClick} disabled={isLoading}>
            {buttonTitle}
        </Button>
    ) : null;
}
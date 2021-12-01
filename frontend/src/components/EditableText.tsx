import React, {useState} from "react";
import {useFormik} from "formik";
import {Box, IconButton, TextField, Typography} from "@mui/material";
import {Check, Clear, Edit} from "@mui/icons-material";
import {TypographyProps} from "@mui/material/Typography/Typography";

export interface EditableTextProps {
    initialValue: string;
    label: string;
    canEdit: boolean;
    onSave: (text: string) => Promise<void>;
    typographyProps?: TypographyProps & { component: string };
}

export function EditableText(props: EditableTextProps) {
    const [isEditing, setIsEditing] = useState(false);

    const handleEditing = () => setIsEditing(!isEditing);

    const formik = useFormik({
        initialValues: {
            name: props.initialValue,
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await props.onSave(values.name);
                handleEditing();
            } catch (e) {
                // TODO error handling
                console.log("edit failed");
            }
        },
        enableReinitialize: true,
    });

    return !isEditing ? (
        <Box>
            <Typography variant={"body2"} component={"span"} {...props.typographyProps}>
                {props.initialValue}
            </Typography>
            {
                props.canEdit ? (
                    <IconButton size='small' sx={{ml: 1}} onClick={event => {
                        event.stopPropagation();
                        handleEditing();
                    }}>
                        <Edit fontSize="inherit"/>
                    </IconButton>
                ) : null
            }

        </Box>
    ) : (
        <React.Fragment>
            <TextField
                margin='none'
                size='small'
                id='name'
                name='name'
                label={props.label}
                value={formik.values.name}
                onChange={formik.handleChange}
                error={formik.touched.name && Boolean(formik.errors.name)}
                helperText={formik.touched.name && formik.errors.name}
                onClick={event => event.stopPropagation()}
            />
            <IconButton size='small' sx={{ml: 1}} onClick={event => {
                event.stopPropagation();
                formik.submitForm();
            }} disabled={formik.isSubmitting}>
                <Check/>
            </IconButton>
            <IconButton size='small' sx={{ml: 1}} onClick={event => {
                event.stopPropagation();
                handleEditing();
            }}>
                <Clear/>
            </IconButton>
        </React.Fragment>
    );
}
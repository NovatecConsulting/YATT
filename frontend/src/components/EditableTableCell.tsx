import React, {useState} from "react";
import {useFormik} from "formik";
import {IconButton, TableCell, TextField} from "@mui/material";
import {Check, Clear, Edit} from "@mui/icons-material";

export interface EditableTableCellProps {
    initialValue: string;
    label: string;
    canEdit: boolean;
    onSave: (text: string) => Promise<void>;
}

export function EditableTableCell(props: EditableTableCellProps) {
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
    });

    return (
        <TableCell>
            {!isEditing ? (
                <React.Fragment>
                    {props.initialValue}
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

                </React.Fragment>
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
            )}
        </TableCell>
    );
}
import React from "react";
import {
    Button,
    Dialog, DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    TableCell,
    TextField,
    TextFieldProps
} from "@mui/material";
import dayjs from "dayjs";
import {Edit} from "@mui/icons-material";
import {useFormik} from "formik";
import DatePicker from "@mui/lab/DatePicker";

export interface EditableDateTableCellsProps {
    startDate: string;
    endDate: string;
    canEdit: boolean;
    onSave: (startDate: string, endDate: string) => Promise<void>;
}

export function EditableDateTableCells(props: EditableDateTableCellsProps) {
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = (event: any) => {
        event.stopPropagation();
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };


    return (
        <React.Fragment>
            <TableCell>
                {dayjs(props.startDate).format('L')}
                {
                    props.canEdit ? (
                        <IconButton size='small' sx={{ml: 1}} onClick={handleClickOpen}>
                            <Edit fontSize="inherit"/>
                        </IconButton>
                    ) : null
                }
            </TableCell>
            <TableCell>
                {dayjs(props.endDate).format('L')}
                {
                    props.canEdit ? (
                        <IconButton size='small' sx={{ml: 1}} onClick={handleClickOpen}>
                            <Edit fontSize="inherit"/>
                        </IconButton>
                    ) : null
                }
            </TableCell>
            <RescheduleDialog initialStartDate={props.startDate}
                              initialEndDate={props.endDate}
                              open={open}
                              onClose={handleClose}
                              onSave={props.onSave}/>
        </React.Fragment>
    );
}

interface RescheduleDialogProps {
    open: boolean;
    onClose: () => void;
    initialStartDate: string;
    initialEndDate: string;
    onSave: (startDate: string, endDate: string) => Promise<void>;
}

function RescheduleDialog(props: RescheduleDialogProps) {
    const formik = useFormik({
        initialValues: {
            startDate: dayjs(props.initialStartDate),
            endDate: dayjs(props.initialEndDate),
        },
        validateOnChange: true,
        validate: values => {
            if (values.startDate > values.endDate) {
                return {
                    endDate: `Can't be before Start Date`
                }
            }
        },
        onSubmit: async (values, formikHelpers) => {
            try {
                await props.onSave(
                    values.startDate.format("YYYY-MM-DD"),
                    values.endDate.format("YYYY-MM-DD"),
                );
                props.onClose();
            } catch (e) {
                // TODO error handling
                console.log("rescheduling failed");
            }
        },
    });

    return (
        <Dialog open={props.open} onClose={props.onClose} onClick={event => event.stopPropagation()}>
            <DialogTitle>Update Start and End Date</DialogTitle>
            <DialogContent>
                <DatePicker
                    label="Start Date"
                    value={formik.values.startDate}
                    mask={"__.__.____"}
                    inputFormat={"DD.MM.YYYY"}
                    onChange={(newValue) => {
                        formik.getFieldHelpers('startDate').setValue(newValue, true)
                    }}
                    renderInput={(params: TextFieldProps) => <TextField
                        {...params}
                        required
                        fullWidth
                        id="startDate"
                        name="startDate"
                        error={params.error || (formik.touched.startDate && Boolean(formik.errors.startDate))}
                        helperText={formik.touched.startDate && formik.errors.startDate}
                    />}
                />
                <DatePicker
                    minDate={formik.values.startDate}
                    label="End Date"
                    value={formik.values.endDate}
                    mask={"__.__.____"}
                    inputFormat={"DD.MM.YYYY"}
                    onChange={(newValue) => {
                        formik.getFieldHelpers('endDate').setValue(newValue, true)
                    }}
                    renderInput={(params: TextFieldProps) => <TextField
                        {...params}
                        required
                        fullWidth
                        id="endDate"
                        name="endDate"
                        error={params.error || (formik.touched.endDate && Boolean(formik.errors.endDate))}
                        helperText={formik.touched.endDate && formik.errors.endDate}
                    />}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={props.onClose}>Cancel</Button>
                <Button onClick={formik.submitForm} disabled={formik.isSubmitting || !formik.isValid}>Save</Button>
            </DialogActions>
        </Dialog>
    );
}
import {Scaffold} from "../../components/scaffold/Scaffold";
import { useAppSelector} from "../../app/hooks";
import {
    RenameUserDto,
    selectCurrentUser,
    useRenameUserMutation
} from "./usersSlice";
import {Avatar,  Button, Card, CardContent, TextField, Typography} from "@mui/material";
import {useState} from "react";
import {useFormik} from "formik";
import React from "react";

export function ProfilePage() {
    const currentUser = useAppSelector(selectCurrentUser);
    const [isEditing, setIsEditing] = useState(false);

    const handleEditing = () => {
        setIsEditing(!isEditing);
    }

    return (
        <Scaffold>
            <Avatar sx={{width: 128, height: 128}}/>
            {
                isEditing ? (
                    <ProfileForm onClose={handleEditing}/>
                ) : (
                    <React.Fragment>
                        <Button sx={{m: 2}} onClick={handleEditing}>Edit Profile</Button>
                        <Typography color={'white'}>
                            {`${currentUser?.firstname} ${currentUser?.lastname}`}
                        </Typography>
                    </React.Fragment>
                )
            }
        </Scaffold>
    );
}

interface ProfileFormProps {
    onClose?: () => void;
}

function ProfileForm(props: ProfileFormProps) {
    const currentUser = useAppSelector(selectCurrentUser);
    const [renameUser] = useRenameUserMutation();

    const formik = useFormik({
        initialValues: {
            firstname: currentUser?.firstname ?? '',
            lastname: currentUser?.lastname ?? '',
        } as RenameUserDto,
        onSubmit: async (userDto, formikHelpers) => {
            try {
                await renameUser(userDto).unwrap();
                if (props.onClose) {
                    props.onClose();
                }
            } catch (e) {
                // TODO error handling
                console.log("rename failed");
            }
        },
    });

    return (
        <Card sx={{m: 2}}><CardContent>
            <form onSubmit={formik.handleSubmit}>
                <TextField
                    required
                    fullWidth
                    id="firstname"
                    name="firstname"
                    label="Firstname"
                    value={formik.values.firstname}
                    onChange={formik.handleChange}
                    error={formik.touched.firstname && Boolean(formik.errors.firstname)}
                    helperText={formik.touched.firstname && formik.errors.firstname}
                />
                <TextField
                    required
                    fullWidth
                    id="lastname"
                    name="lastname"
                    label="Lastname"
                    value={formik.values.lastname}
                    onChange={formik.handleChange}
                    error={formik.touched.lastname && Boolean(formik.errors.lastname)}
                    helperText={formik.touched.lastname && formik.errors.lastname}
                />
                <Button variant="contained" fullWidth type="submit" disabled={formik.isSubmitting} sx={{mt: 3}}>
                    Save
                </Button>
                <Button fullWidth disabled={formik.isSubmitting} onClick={props.onClose} sx={{mt: 2}}>
                    Cancel
                </Button>
            </form>
        </CardContent></Card>
    );
}
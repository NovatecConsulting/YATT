import {createTheme} from "@mui/material";

export const theme = createTheme({
    shape: {
        borderRadius: 10,
    },
    components: {
        MuiTextField: {
            defaultProps: {
                margin: 'normal'
            },
        },
    },
});
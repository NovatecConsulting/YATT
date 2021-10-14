import {createTheme} from "@mui/material";
import {deDE} from "@mui/material/locale";

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
}, deDE);
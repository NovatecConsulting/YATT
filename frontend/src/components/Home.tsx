import {Redirect} from "react-router-dom";
import {useAppSelector} from "../app/hooks";
import {selectIsAuthenticated, selectIsRegistered} from "../features/auth/authSlice";

export function Home() {
    const isAuthenticated = useAppSelector(selectIsAuthenticated);
    const isRegistered = useAppSelector(selectIsRegistered);

    if (isRegistered) {
        return <Redirect to={"/projects"}/>
    } else if (isAuthenticated) {
        return <Redirect to={"/registration"}/>
    } else {
        return <Redirect to={"/login"}/>
    }
}
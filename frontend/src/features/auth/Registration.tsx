import {useAppSelector} from "../../app/hooks";
import {selectIsRegistered} from "./authSlice";
import {Redirect} from "react-router-dom";
import {Scaffold} from "../../components/Scaffold";

export function Registration() {
    const isRegistered = useAppSelector(selectIsRegistered);
    if (isRegistered) {
        return <Redirect to={"/"}/>;
    }

    return (
        <Scaffold title={"Registration"}>
            <div>Registration</div>
        </Scaffold>
    );
}
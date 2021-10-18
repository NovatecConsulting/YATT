import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    CircularProgress,
    Paper,
    Switch,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import {useHistory, useParams} from "react-router-dom";
import {useAppSelector} from "../../app/hooks";
import {EntityId} from "@reduxjs/toolkit";
import {Scaffold} from "../../components/Scaffold";
import React from "react";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {
    selectEmployeesByCompanyIdAndEmployeeId,
    useGetEmployeesByCompanyQuery,
    useGrantAdminPermissionMutation,
    useGrantProjectManagerPermissionMutation,
    useRemoveAdminPermissionMutation,
    useRemoveProjectManagerPermissionMutation
} from "./employeeSlice";

export function EmployeeList() {
    const history = useHistory();
    const {id: companyId} = useParams<{ id: string }>();

    const {
        data: employeeIds,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetEmployeesByCompanyQuery(companyId, {selectFromResult: selectIdsFromResult});

    const navigateToEmployeeCreateForm = () => history.push(`/companies/${companyId}/employees/new`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && employeeIds) {
        content = (
            <TableContainer sx={{maxWidth: 1000}} component={Paper}>
                <TableToolbar
                    title={``}
                    tooltip={'Create Employee'}
                    onClick={navigateToEmployeeCreateForm}
                />
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell>First Name</TableCell>
                            <TableCell>Last Name</TableCell>
                            <TableCell>Is Admin</TableCell>
                            <TableCell>Is Project Manager</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {
                            employeeIds.map(
                                (employeeId: EntityId) => <EmployeeRow key={employeeId} companyId={companyId}
                                                                       employeeId={employeeId}/>
                            )
                        }
                    </TableBody>
                </Table>
            </TableContainer>
        );
    } else if (isError) {
        content = <div>{error}</div>;
    } else {
        return null;
    }

    return (
        <Scaffold>
            {content}
        </Scaffold>
    );
}

interface EmployeeRowProps {
    companyId: EntityId;
    employeeId: EntityId;
}

function EmployeeRow(props: EmployeeRowProps) {
    const employee = useAppSelector((state) => selectEmployeesByCompanyIdAndEmployeeId(state, props.companyId, props.employeeId))
    const [grantAdminPermission] = useGrantAdminPermissionMutation();
    const [removeAdminPermission] = useRemoveAdminPermissionMutation();
    const [grantProjectMangerPermission] = useGrantProjectManagerPermissionMutation();
    const [removeProjectMangerPermission] = useRemoveProjectManagerPermissionMutation();

    if (employee)
        return (
            <TableRow hover>
                <TableCell>{employee.userFirstName}</TableCell>
                <TableCell>{employee.userLastName}</TableCell>
                <TableCell>
                    {employee.isAdmin ? 'yes' : 'no'}
                    <Switch checked={employee.isAdmin} onChange={(event) => {
                        if (event.target.checked) {
                            grantAdminPermission(employee.identifier);
                        } else {
                            removeAdminPermission(employee.identifier)
                        }
                    }}/>
                </TableCell>
                <TableCell>
                    {employee.isProjectManager ? 'yes' : 'no'}
                    <Switch checked={employee.isProjectManager} onChange={(event) => {
                        if (event.target.checked) {
                            grantProjectMangerPermission(employee.identifier);
                        } else {
                            removeProjectMangerPermission(employee.identifier)
                        }
                    }}/></TableCell>
            </TableRow>
        );
    else {
        return null;
    }
}
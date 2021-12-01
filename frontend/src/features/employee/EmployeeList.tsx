import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Box,
    CircularProgress,
    Paper,
    Switch,
} from "@mui/material";
import {useHistory, useParams} from "react-router-dom";
import {Scaffold} from "../../components/scaffold/Scaffold";
import React from "react";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {
    Employee,
    useGetEmployeesByCompanyQuery,
    useGrantAdminPermissionMutation,
    useGrantProjectManagerPermissionMutation,
    useRemoveAdminPermissionMutation,
    useRemoveProjectManagerPermissionMutation
} from "./employeeSlice";
import {VirtualizedTable} from "../../components/VirtualizedTable";

export function EmployeeList() {
    const history = useHistory();
    const {companyId} = useParams<{ companyId: string }>();

    const {
        data: employees,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetEmployeesByCompanyQuery(companyId, {selectFromResult: selectEntitiesFromResult});

    const navigateToEmployeeCreateForm = () => history.push(`/companies/${companyId}/employees/new`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && employees) {
        content = (
            <Paper sx={{width: 1000, display: "flex", flexFlow: "column", flex: "1 1 auto"}}>
                <TableToolbar
                    title={'Employees'}
                    tooltip={'Create Employee'}
                    onClick={navigateToEmployeeCreateForm}
                />
                <Box sx={{flex: "1 1 auto"}}>
                    <VirtualizedTable
                        rowHeight={64}
                        rowCount={employees.length}
                        rowGetter={(index) => employees[index.index]}
                        columns={[
                            {
                                width: 120,
                                label: "First Name",
                                dataKey: "userFirstName",
                            },
                            {
                                width: 120,
                                label: "Last Name",
                                dataKey: "userLastName",
                            },
                            {
                                width: 120,
                                label: "Is Admin",
                                dataKey: "isAdmin",
                                cellRenderer: (cellProps) => <IsAdminCell employee={cellProps.rowData as Employee}/>
                            },
                            {
                                width: 120,
                                label: "Is Project Manager",
                                dataKey: "isProjectManager",
                                cellRenderer: (cellProps) => <IsProjectManagerCell
                                    employee={cellProps.rowData as Employee}/>
                            },
                        ]}
                    />
                </Box>
            </Paper>
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

function IsAdminCell({employee}: { employee: Employee }) {
    const [grantAdminPermission] = useGrantAdminPermissionMutation();
    const [removeAdminPermission] = useRemoveAdminPermissionMutation();

    return (
        <React.Fragment>
            {employee.isAdmin ? 'yes' : 'no'}
            <Switch checked={employee.isAdmin} onChange={(event) => {
                if (event.target.checked) {
                    grantAdminPermission(employee.identifier);
                } else {
                    removeAdminPermission(employee.identifier)
                }
            }}/>
        </React.Fragment>
    );
}

function IsProjectManagerCell({employee}: { employee: Employee }) {
    const [grantProjectMangerPermission] = useGrantProjectManagerPermissionMutation();
    const [removeProjectMangerPermission] = useRemoveProjectManagerPermissionMutation();

    return (
        <React.Fragment>
            {employee.isProjectManager ? 'yes' : 'no'}
            <Switch checked={employee.isProjectManager} onChange={(event) => {
                if (event.target.checked) {
                    grantProjectMangerPermission(employee.identifier);
                } else {
                    removeProjectMangerPermission(employee.identifier)
                }
            }}/>
        </React.Fragment>
    );
}

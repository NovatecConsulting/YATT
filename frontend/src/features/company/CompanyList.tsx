import {Company, useGetCompaniesQuery} from "./companySlice";
import {Scaffold} from "../../components/scaffold/Scaffold";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    Box,
    CircularProgress,
    Paper,
} from "@mui/material";
import React from "react";
import {selectEntitiesFromResult} from "../../app/rtkQueryHelpers";
import {TableToolbar} from "../../components/TableToolbar";
import {useHistory} from "react-router-dom";
import {VirtualizedTable} from "../../components/VirtualizedTable";

export function CompanyList() {
    const history = useHistory();
    const {
        data: companies,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetCompaniesQuery(undefined, {selectFromResult: selectEntitiesFromResult});

    const navigateToCreateCompanyForm = () => history.push(`/companies/new`);

    const navigateToEmployeeList = (companyId: string) => history.push(`/companies/${companyId}/employees`)

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && companies) {
        content = (
            <Paper sx={{width: 1000, display: "flex", flexFlow: "column", flex: "1 1 auto"}}>
                <TableToolbar
                    title={'All Companies'}
                    tooltip={'Create Company'}
                    onClick={navigateToCreateCompanyForm}
                />
                <Box sx={{flex: "1 1 auto"}}>
                    <VirtualizedTable
                        rowHeight={64}
                        rowCount={companies.length}
                        rowGetter={(index) => companies[index.index]}
                        onRowClick={(row) => navigateToEmployeeList((row.rowData as Company).identifier)}
                        columns={[
                            {
                                width: 1000,
                                label: "Name",
                                dataKey: "name",
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

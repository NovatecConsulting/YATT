import {Company, selectCompanyById, useGetCompaniesQuery} from "./companySlice";
import {Scaffold} from "../../components/Scaffold";
import {ReactJSXElement} from "@emotion/react/types/jsx-namespace";
import {
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import React from "react";
import {selectIdsFromResult} from "../../app/rtkQueryHelpers";
import {EntityId} from "@reduxjs/toolkit";
import {useAppSelector} from "../../app/hooks";
import {TableToolbar} from "../../components/TableToolbar";
import {useHistory} from "react-router-dom";

export function CompanyList() {
    const history = useHistory();
    const {
        data: companyIds,
        isLoading,
        isSuccess,
        isError,
        error
    } = useGetCompaniesQuery(undefined, {selectFromResult: selectIdsFromResult});

    let content: ReactJSXElement;
    if (isLoading) {
        content = <CircularProgress/>;
    } else if (isSuccess && companyIds) {
        content = (
            <TableContainer sx={{minWidth: 1000}} component={Paper}>
                <TableToolbar
                    title={'All Companies'}
                    tooltip={'Create Company'}
                    onClick={() => history.push(`/companies/new`)}
                />
                <Table stickyHeader>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {
                            companyIds.map((companyId) => (
                                <CompanyRow key={companyId} companyId={companyId}/>
                            ))
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

interface CompanyRowProps {
    companyId: EntityId;
}

function CompanyRow(props: CompanyRowProps) {
    const history = useHistory();
    const company = useAppSelector(state => selectCompanyById(state, props.companyId))

    const navigateToEmployeeList = (company: Company) => history.push(`/companies/${company.identifier}/employees`)

    if (company) {
        return (
            <TableRow hover onClick={() => navigateToEmployeeList(company)}>
                <TableCell>{company.name}</TableCell>
            </TableRow>
        );
    } else {
        return null;
    }
}
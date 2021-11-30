import * as React from 'react';
import clsx from 'clsx';
import {withStyles, WithStyles} from '@mui/styles';
import {Theme, createTheme, styled} from '@mui/material/styles';
import TableCell from '@mui/material/TableCell';
import {
    AutoSizer,
    Column, Index,
    Table,
    TableCellRenderer,
    TableHeaderProps,
} from 'react-virtualized';

const styles = (theme: Theme) =>
    ({
        flexContainer: {
            display: 'flex',
            alignItems: 'center',
            boxSizing: 'border-box',
        },
        tableRow: {
            cursor: 'pointer',
        },
        tableRowHover: {
            '&:hover': {
                backgroundColor: theme.palette.grey[200],
            },
        },
        tableCell: {
            flex: 1,
        },
        noClick: {
            cursor: 'initial',
        },
    } as const);

interface ColumnData {
    dataKey: string;
    label: string;
    numeric?: boolean;
    width: number;
    cellRenderer?: TableCellRenderer;
}

interface RowMouseEventHandlerParams<T> {
    rowData: T;
    index: number;
    event: React.MouseEvent<any>;
}

interface MuiVirtualizedTableProps<T> extends WithStyles<typeof styles> {
    columns: readonly ColumnData[];
    headerHeight?: number;
    onRowClick?: (params: RowMouseEventHandlerParams<T>) => void;
    rowCount: number;
    rowGetter: (index: Index) => T;
    rowHeight?: number;
}

class MuiVirtualizedTable<T> extends React.PureComponent<MuiVirtualizedTableProps<T>> {
    static defaultProps = {
        headerHeight: 48,
        rowHeight: 48,
    };

    getRowClassName = ({index}: Index) => {
        const {classes, onRowClick} = this.props;

        return clsx(classes.tableRow, classes.flexContainer, {
            [classes.tableRowHover]: index !== -1 && onRowClick != null,
        });
    };

    cellRenderer: TableCellRenderer = (tableCellProps) => {
        const {columns, classes, rowHeight, onRowClick} = this.props;
        const {cellData, columnIndex} = tableCellProps;
        return (
            <TableCell
                component="div"
                className={clsx(classes.tableCell, classes.flexContainer, {
                    [classes.noClick]: onRowClick == null,
                })}
                variant="body"
                style={{height: rowHeight}}
                align={
                    (columnIndex != null && columns[columnIndex].numeric) || false
                        ? 'right'
                        : 'left'
                }
            >
                {columns[columnIndex].cellRenderer ? columns[columnIndex].cellRenderer!(tableCellProps) : cellData}
            </TableCell>
        );
    };

    headerRenderer = ({label, columnIndex}: TableHeaderProps & { columnIndex: number }) => {
        const {headerHeight, columns, classes} = this.props;

        return (
            <TableCell
                component="div"
                className={clsx(classes.tableCell, classes.flexContainer, classes.noClick)}
                variant="head"
                style={{height: headerHeight}}
                align={columns[columnIndex].numeric || false ? 'right' : 'left'}
            >
                <span>{label}</span>
            </TableCell>
        );
    };

    render() {
        const {classes, columns, rowHeight, headerHeight, ...tableProps} = this.props;
        return (
            <AutoSizer>
                {({height, width}) => (
                    <Table
                        height={height}
                        width={width}
                        rowHeight={rowHeight!}
                        gridStyle={{direction: 'inherit'}}
                        headerHeight={headerHeight!}
                        {...tableProps}
                        rowClassName={this.getRowClassName}
                    >
                        {columns.map(({dataKey, ...other}, index) => {
                            return (
                                <Column
                                    flexGrow={1}
                                    key={dataKey}
                                    headerRenderer={(headerProps) =>
                                        this.headerRenderer({
                                            ...headerProps,
                                            columnIndex: index,
                                        })
                                    }
                                    className={classes.flexContainer}
                                    dataKey={dataKey}
                                    {...other}
                                    cellRenderer={this.cellRenderer}
                                />
                            );
                        })}
                    </Table>
                )}
            </AutoSizer>
        );
    }
}

const defaultTheme = createTheme();
export const VirtualizedTable = withStyles(styles, {defaultTheme})(MuiVirtualizedTable);



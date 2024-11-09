import React, { useState } from 'react';
import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';

const EnhancedTable = ({ rows, columns }) => {
    const [currentPage, setCurrentPage] = useState(0);
    const [sortConfig, setSortConfig] = useState({ key: null, direction: null });
    const rowsPerPage = 10;

    // Sorting logic
    const sortedRows = React.useMemo(() => {
        if (!sortConfig.key) return rows;

        return [...rows].sort((a, b) => {
            if (a[sortConfig.key] < b[sortConfig.key]) {
                return sortConfig.direction === 'asc' ? -1 : 1;
            }
            if (a[sortConfig.key] > b[sortConfig.key]) {
                return sortConfig.direction === 'asc' ? 1 : -1;
            }
            return 0;
        });
    }, [rows, sortConfig]);

    // Pagination
    const pageCount = Math.ceil(sortedRows.length / rowsPerPage);
    const paginatedRows = sortedRows.slice(
        currentPage * rowsPerPage,
        (currentPage + 1) * rowsPerPage
    );

    const requestSort = (key) => {
        let direction = 'asc';
        if (sortConfig.key === key && sortConfig.direction === 'asc') {
            direction = 'desc';
        }
        setSortConfig({ key, direction });
    };

    const getSortIcon = (columnKey) => {
        if (sortConfig.key !== columnKey) {
            return <ChevronsUpDown className="w-4 h-4 text-gray-400" />;
        }
        return sortConfig.direction === 'asc' ? (
            <ChevronUp className="w-4 h-4 text-blue-500" />
        ) : (
            <ChevronDown className="w-4 h-4 text-blue-500" />
        );
    };

    return (
        <div className="w-full bg-[#1a1a1a] rounded-lg overflow-hidden border border-gray-700">
            <div className="overflow-x-auto">
                <table className="w-full">
                    <thead>
                    <tr className="bg-[#252526] border-b border-gray-700">
                        {columns.map((column) => (
                            <th
                                key={column.field}
                                className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-[#2d2d2d]"
                                onClick={() => requestSort(column.field)}
                            >
                                <div className="flex items-center space-x-1">
                                    <span>{column.headerName}</span>
                                    {getSortIcon(column.field)}
                                </div>
                            </th>
                        ))}
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                    {paginatedRows.map((row, rowIndex) => (
                        <tr
                            key={row.id || rowIndex}
                            className="hover:bg-[#2d2d2d] transition-colors duration-150"
                        >
                            {columns.map((column) => (
                                <td
                                    key={column.field}
                                    className="px-6 py-4 text-sm text-gray-300 whitespace-nowrap"
                                >
                                    {row[column.field]}
                                </td>
                            ))}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            <div className="bg-[#252526] px-4 py-3 border-t border-gray-700 flex items-center justify-between">
                <div className="text-sm text-gray-400">
                    Showing {currentPage * rowsPerPage + 1} to{' '}
                    {Math.min((currentPage + 1) * rowsPerPage, sortedRows.length)} of{' '}
                    {sortedRows.length} results
                </div>
                <div className="flex space-x-2">
                    <button
                        onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                        disabled={currentPage === 0}
                        className="px-3 py-1 rounded bg-[#333333] text-gray-300 disabled:bg-[#2d2d2d] disabled:text-gray-500 hover:bg-[#404040]"
                    >
                        Previous
                    </button>
                    <button
                        onClick={() => setCurrentPage(Math.min(pageCount - 1, currentPage + 1))}
                        disabled={currentPage >= pageCount - 1}
                        className="px-3 py-1 rounded bg-[#333333] text-gray-300 disabled:bg-[#2d2d2d] disabled:text-gray-500 hover:bg-[#404040]"
                    >
                        Next
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EnhancedTable;
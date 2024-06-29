// components/HealthDataGrid.tsx
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { DataGrid, GridPaginationModel } from "@mui/x-data-grid";
import { Alert, Box, Container } from "@mui/material";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { RootState } from "@/store/store";

type HealthDataGridProps = {
    title: string;
    selector: string;
    columns: any[];
    getter: Function;
    rowId?: string
}

const HealthDataGrid = ({
    title,
    selector,
    columns,
    getter,
    rowId,
}: HealthDataGridProps) => {
    const dispatch = useDispatch();

    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
        page: 0,
        pageSize: 10,
    });

    const getData = (paginationModel: GridPaginationModel) => {
        dispatch(getter(paginationModel));
    }

    const handlePaginationChange = (paginationModel: GridPaginationModel) => {
        setPaginationModel(paginationModel);
        getData(paginationModel);
    }

    const slice = useSelector((state: RootState) => (state as any)[selector]);
    const { healthData, loading, error } = slice;

    const showTable = useSelector((state: RootState) => state.tableOrJsonToggle[selector] ?? true);

    useEffect(() => {
        getData({ page: 0, pageSize: 10 });
    }, []);

    return (
        <Container>
            <h2>{title}</h2>
            {healthData?.data && <TableOrJsonToggle locator={selector} />}
            {loading && <p>Loading...</p>}
            {error && <Alert severity="error">{error}</Alert>}
            {showTable && healthData?.data &&
                <DataGrid
                    rows={healthData?.data?.resources}
                    columns={columns}
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={
                        {
                            pagination: {
                                paginationModel: { pageSize: 10, page: 0 }
                            }
                        }
                    }
                    paginationMode="server"
                    paginationModel={paginationModel}
                    rowCount={healthData?.data?.paging_info?.total_items || 0}
                    onPaginationModelChange={handlePaginationChange}
                    getRowId={(row) => rowId ? row[rowId] : row.id}
                />
            }
            {!showTable && healthData?.data &&
                <Box>
                    <pre>{JSON.stringify(healthData, null, 2)}</pre>
                </Box>
            }
        </Container>
    );
};

export default HealthDataGrid;

import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getAllergyIntoleranceGroups, getAllergyIntolerances } from "@/store/allergyIntoleranceSlice";
import { AppDispatch, RootState } from "@/store/store";
import { Alert, Box, Container } from "@mui/material";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { DataGrid, GridPaginationModel } from "@mui/x-data-grid";
import { ALLERGY_INTOLERANCE_COLUMNS, ALLERGY_INTOLERANCE_GROUP_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";

const AllergyIntolerances = () => {
    const dispatch = useDispatch<AppDispatch>();

    const handleGetAllergyIntoleranceGroups = (paginationModel: GridPaginationModel) => {
        const { page, pageSize } = paginationModel;
        dispatch(getAllergyIntoleranceGroups({ page, pageSize }));
    };

    const handleGetAllergyIntolerances = (paginationModel: GridPaginationModel) => {
        const { page, pageSize } = paginationModel;
        dispatch(getAllergyIntolerances({ page, pageSize }));
    };

    const allergyIntoleranceSlice = useSelector((state: RootState) => state.allergyIntolerance);
    const { allergyIntoleranceGroups, groupsError, groupsLoading, allergyIntolerances, intolerancesError, intolerancesLoading } = allergyIntoleranceSlice;

    const showAllergyIntoleranceGroupsTable = useSelector((state: RootState) => state.tableOrJsonToggle.allergyIntoleranceGroups);
    const showAllergyIntolerancesTable = useSelector((state: RootState) => state.tableOrJsonToggle.allergyIntolerances);

    const handleGroupsPaginationChange = (paginationModel: any) => {
        handleGetAllergyIntoleranceGroups(paginationModel);
    }

    const handleIntolerancesPaginationChange = (paginationModel: any) => {
        handleGetAllergyIntolerances(paginationModel);
    }

    useEffect(() => {
        handleGetAllergyIntoleranceGroups({ page: 0, pageSize: 10 });
        handleGetAllergyIntolerances({ page: 0, pageSize: 10 });
    }, []);

    return (
        <Container>
            <h1>Allergy Intolerances</h1>
            <h2>getAllergyIntoleranceGroups()</h2>
            {allergyIntoleranceGroups?.data && <TableOrJsonToggle locator="allergyIntoleranceGroups" />}
            {groupsLoading && <p>Loading...</p>}
            {groupsError && <Alert severity="error" id="allergyIntoleranceGroupsError">{groupsError}</Alert>}
            {
                showAllergyIntoleranceGroupsTable && allergyIntoleranceGroups?.data &&
                <DataGrid
                    rows={allergyIntoleranceGroups?.data?.resources}
                    columns={ALLERGY_INTOLERANCE_GROUP_COLUMNS}
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={{
                        pagination: {
                            paginationModel: { pageSize: 10, page: 0 }
                        }
                    }
                    }
                    paginationMode="server"
                    rowCount={allergyIntoleranceGroups?.data?.paging_info?.total_items || 0}
                    onPaginationModelChange={handleGroupsPaginationChange}
                />
            }
            {!showAllergyIntoleranceGroupsTable && allergyIntoleranceGroups?.data &&
                <Box>
                    <pre>{JSON.stringify(allergyIntoleranceGroups, null, 2)}</pre>
                </Box>
            }
            <h2>getAllergyIntolerances()</h2>
            {allergyIntolerances?.data && <TableOrJsonToggle locator="allergyIntolerances" />}
            {intolerancesLoading && <p>Loading...</p>}
            {intolerancesError && <Alert severity="error" id="allergyIntolerancesError">{intolerancesError}</Alert>}
            {
                showAllergyIntolerancesTable && allergyIntolerances?.data &&
                <DataGrid
                    rows={allergyIntolerances?.data?.resources}
                    columns={ALLERGY_INTOLERANCE_COLUMNS}
                    pageSizeOptions={[10, 25, 50, 100]}
                    initialState={{
                        pagination: {
                            paginationModel: { pageSize: 10, page: 0 }
                        }
                    }
                    }
                    paginationMode="server"
                    rowCount={allergyIntolerances?.data?.paging_info?.total_items || 0}
                    onPaginationModelChange={handleIntolerancesPaginationChange}
                />
            }
            {!showAllergyIntolerancesTable && allergyIntolerances?.data &&
                <Box>
                    <pre>{JSON.stringify(allergyIntolerances, null, 2)}</pre>
                </Box>
            }
        </Container>
    );
};

export default withAuthCheck('Allergy Intolerances', AllergyIntolerances);
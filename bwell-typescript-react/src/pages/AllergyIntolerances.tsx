import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getAllergyIntoleranceGroups } from "@/store/allergyIntoleranceSlice";
import { AppDispatch, RootState } from "@/store/store";
import { Alert, Box, Button, Container } from "@mui/material";
import PaginationForm from "@/components/PaginationForm";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { DataGrid } from "@mui/x-data-grid";
import { ALLERGY_INTOLERANCE_GROUP_COLUMNS } from "@/column-defs";

const AllergyIntolerances = () => {
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);

    const dispatch = useDispatch<AppDispatch>();

    const handleGetAllergyIntoleranceGroups = () => {
        dispatch(getAllergyIntoleranceGroups({ page, pageSize }));
    };

    const allergyIntoleranceSlice = useSelector((state: RootState) => state.allergyIntolerance);
    const { allergyIntoleranceGroups, groupsError, groupsLoading } = allergyIntoleranceSlice;

    const isLoggedIn = useSelector((state: RootState) => state.user.isLoggedIn);

    const showAllergyIntoleranceGroupsTable = useSelector((state: RootState) => state.tableOrJsonToggle.allergyIntoleranceGroups);

    if (!isLoggedIn) {
        return (
            <Container>
                <h1>Allergy Intolerances</h1>
                <p>Please log in to view this page</p>
            </Container>
        );
    }

    return (
        <Container>
            <h1>Allergy Intolerances</h1>
            <h2>getAllergyIntoleranceGroups()</h2>
            <PaginationForm page={page} pageSize={pageSize} onPageChange={setPage} onPageSizeChange={setPageSize} />
            <Box sx={{ padding: '5px' }}>
                <Button variant="contained" onClick={handleGetAllergyIntoleranceGroups}>
                    Get Allergy Intolerance Groups
                </Button>
            </Box>
            {allergyIntoleranceGroups?.data && <TableOrJsonToggle locator="allergyIntoleranceGroups" />}
            {groupsLoading && <p>Loading...</p>}
            {groupsError && <Alert severity="error" id="allergyIntoleranceGroupsError">{groupsError}</Alert>}
            {
                showAllergyIntoleranceGroupsTable && allergyIntoleranceGroups?.data &&
                <DataGrid rows={allergyIntoleranceGroups?.data?.resources} columns={ALLERGY_INTOLERANCE_GROUP_COLUMNS} />
            }
            {!showAllergyIntoleranceGroupsTable && allergyIntoleranceGroups?.data &&
                <Box>
                    <pre>{JSON.stringify(allergyIntoleranceGroups.data, null, 2)}</pre>
                </Box>
            }
            <h2>getAllergyIntolerances()</h2>
        </Container>
    );
};

export default AllergyIntolerances;
import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getAllergyIntoleranceGroups } from "@/store/allergyIntoleranceSlice";
import { AppDispatch, RootState } from "@/store/store";
import { Alert, Box, Button, Container, Table, TableBody, TableCell, TableHead, TableRow, TextField, ToggleButton, ToggleButtonGroup } from "@mui/material";
import PaginationForm from "@/components/PaginationForm";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";

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
            <TableOrJsonToggle locator="allergyIntoleranceGroups" />
            {groupsLoading && <p>Loading...</p>}
            {groupsError && <Alert severity="error" id="allergyIntoleranceGroupsError">{groupsError}</Alert>}
            {
                showAllergyIntoleranceGroupsTable && allergyIntoleranceGroups?.data &&
                <Box>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>ID</TableCell>
                                <TableCell>Name</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {allergyIntoleranceGroups.data.resources.map((group) => (
                                <TableRow key={group.id}>
                                    <TableCell>{group.id}</TableCell>
                                    <TableCell>{group.name}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Box>
            }
            {!showAllergyIntoleranceGroupsTable && allergyIntoleranceGroups?.data &&
                <Box>
                    <pre>{JSON.stringify(allergyIntoleranceGroups.data, null, 2)}</pre>
                </Box>
            }
        </Container>
    );
};

export default AllergyIntolerances;
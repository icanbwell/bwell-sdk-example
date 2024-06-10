import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getAllergyIntoleranceGroups } from "@/store/allergyIntoleranceSlice";
import { AppDispatch, RootState } from "@/store/store";
import { useEffect } from "react";
import { Box, Button, Container, Table, TableBody, TableCell, TableHead, TableRow, TextField } from "@mui/material";

const AllergyIntolerances = () => {
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const dispatch = useDispatch<AppDispatch>();
    const allergyIntoleranceGroups = useSelector((state: RootState) => state.allergyIntolerance.allergyIntoleranceGroups);
    const groupsLoading = useSelector((state: RootState) => state.allergyIntolerance.groupsLoading);
    const groupsError = useSelector((state: RootState) => state.allergyIntolerance.groupsError);
    const isLoggedIn = useSelector((state: RootState) => state.user.isLoggedIn);

    useEffect(() => {
        dispatch(getAllergyIntoleranceGroups({ page, pageSize }));
    }, [dispatch]);

    if (!isLoggedIn) {
        return (
            <Container>
                <h1>Allergy Intolerances</h1>
                <p>Please log in to view this page</p>
            </Container>
        );
    }

    const handlePageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setPage(Number(event.target.value));
    };

    const handlePageSizeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setPageSize(Number(event.target.value));
    };

    const handleGetAllergyIntoleranceGroups = () => {
        dispatch(getAllergyIntoleranceGroups({ page, pageSize }));
    };

    const [showTable, setShowTable] = useState(true);

    const handleToggleView = () => {
        setShowTable(!showTable);
    };

    return (
        <Container>
            <h1>Allergy Intolerances</h1>
            <h2>getAllergyIntoleranceGroups()</h2>
            <Box sx={{ padding: '5px' }}>
                <TextField
                    label="Page"
                    type="number"
                    value={page}
                    onChange={handlePageChange}
                />
                <TextField
                    label="Page Size"
                    type="number"
                    value={pageSize}
                    onChange={handlePageSizeChange}
                />
            </Box>
            <Box sx={{ padding: '5px' }}>
                <Button variant="contained" onClick={handleGetAllergyIntoleranceGroups}>
                    Get Allergy Intolerance Groups
                </Button>
                <Button variant="contained" onClick={handleToggleView}>
                    Show {showTable ? "JSON" : "Table"}
                </Button>
            </Box>
            {groupsLoading && <p>Loading...</p>}
            {groupsError && <p>Error: {groupsError}</p>}
            {showTable ? (
                allergyIntoleranceGroups && allergyIntoleranceGroups.data && (
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
                )
            ) : (
                allergyIntoleranceGroups && allergyIntoleranceGroups.data && (
                    <Box>
                        <pre>{JSON.stringify(allergyIntoleranceGroups.data, null, 2)}</pre>
                    </Box>
                )
            )}
        </Container>
    );
};

export default AllergyIntolerances;
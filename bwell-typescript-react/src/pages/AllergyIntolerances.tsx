import { useDispatch, useSelector } from "react-redux";
import { getAllergyIntoleranceGroups } from "@/store/allergyIntoleranceSlice";
import { AppDispatch, RootState } from "@/store/store";
import { useEffect } from "react";
import { Container } from "@mui/material";

const AllergyIntolerances = () => {
    const dispatch = useDispatch<AppDispatch>();
    const allergyIntoleranceGroups = useSelector((state: RootState) => state.allergyIntolerance.allergyIntoleranceGroups);
    const groupsLoading = useSelector((state: RootState) => state.allergyIntolerance.groupsLoading);
    const groupsError = useSelector((state: RootState) => state.allergyIntolerance.groupsError);
    const isLoggedIn = useSelector((state: RootState) => state.user.isLoggedIn);

    useEffect(() => {
        dispatch(getAllergyIntoleranceGroups({ page: 1, pageSize: 10 }));
    }, [dispatch]);

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
            {groupsLoading && <p>Loading...</p>}
            {groupsError && <p>Error: {groupsError}</p>}
            {allergyIntoleranceGroups && (
                <pre>{JSON.stringify(allergyIntoleranceGroups, null, 2)}</pre>
            )}
        </Container>
    );
};

export default AllergyIntolerances;
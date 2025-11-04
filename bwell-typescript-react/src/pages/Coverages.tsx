import React from "react";
import { Box, Container } from "@mui/material";
import { COVERAGE_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { RootState } from "@/store/store";
import { useSelector, useDispatch } from "react-redux";
import { AppDispatch } from "@/store/store";
import { DataGrid } from "@mui/x-data-grid";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { getCoverages } from "@/store/healthData/coveragesSlice";

const Coverages = () => {
  const dispatch = useDispatch<AppDispatch>();

  // Fetch coverages on every page load
  React.useEffect(() => {
    dispatch(getCoverages({ page: 0, pageSize: 10 }));
  }, [dispatch]);

  const slice = useSelector((state: RootState) => state.health.coverages);
  const coveragesData = slice.healthData ?? { data: { entry: [] } };
  const showTable =
    useSelector((state: RootState) => state.toggle["coverages"] ?? true) &&
    Array.isArray(coveragesData.data?.entry);

  // Extract resources from FHIR bundle format
  const rows =
    coveragesData.data?.entry?.map((entry: any) => entry.resource) || [];

  return (
    <Container>
      <h1>Coverage</h1>
      {coveragesData.data && <TableOrJsonToggle locator={"coverages"} />}
      {showTable && coveragesData.data && (
        <DataGrid rows={rows} columns={COVERAGE_COLUMNS} />
      )}
      {!showTable && coveragesData.data && (
        <Box>
          <pre>{JSON.stringify(coveragesData, null, 2)}</pre>
        </Box>
      )}
    </Container>
  );
};

export default withAuthCheck("Coverage", Coverages);

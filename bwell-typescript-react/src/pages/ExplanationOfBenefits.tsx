import React from "react";
import { Box, Container } from "@mui/material";
import { EXPLANATION_OF_BENEFITS_COLUMNS } from "@/column-defs";
import withAuthCheck from "@/components/withAuthCheck";
import { RootState } from "@/store/store";
import { useSelector, useDispatch } from "react-redux";
import { AppDispatch } from "@/store/store";
import { DataGrid } from "@mui/x-data-grid";
import TableOrJsonToggle from "@/components/TableOrJsonToggle";
import { getExplanationOfBenefits } from "@/store/financial/explanationOfBenefitsSlice";

const ExplanationOfBenefits = () => {
  const dispatch = useDispatch<AppDispatch>();

  // Fetch explanation of benefits on every page load
  React.useEffect(() => {
    dispatch(
      getExplanationOfBenefits({
        page: 0,
        pageSize: 20,
        sort: [{ field: "id", order: "desc" }],
      })
    );
  }, [dispatch]);

  const slice = useSelector(
    (state: RootState) => state.financial.explanationOfBenefits
  );
  console.log("ExplanationOfBenefits slice:", slice.data);
  const eobData = slice.data ?? { data: { entry: [] } };
  const showTable =
    useSelector(
      (state: RootState) => state.toggle["explanationOfBenefits"] ?? true
    ) && Array.isArray(eobData.data?.entry);

  // Extract resources from FHIR bundle format
  const rows = eobData.data?.entry?.map((entry: any) => entry.resource) || [];

  console.log("EOB Data:", eobData);

  return (
    <Container>
      <h1>Explanation of Benefits</h1>
      {eobData.data && <TableOrJsonToggle locator={"explanationOfBenefits"} />}
      {showTable && eobData.data && (
        <DataGrid rows={rows} columns={EXPLANATION_OF_BENEFITS_COLUMNS} />
      )}
      {!showTable && eobData.data && (
        <Box>
          <pre>{JSON.stringify(eobData, null, 2)}</pre>
        </Box>
      )}
    </Container>
  );
};

export default withAuthCheck("Explanation of Benefits", ExplanationOfBenefits);

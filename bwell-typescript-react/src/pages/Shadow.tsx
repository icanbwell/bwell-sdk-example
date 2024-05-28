import ShadowComponent from "@/components/ShadowComponent.tsx";

import { Container, Grid, CircularProgress, Button } from "@mui/material";
import { useEffect, useState } from "react";

const Shadow = () => {
  const [loading, setLoading] = useState(false);

  const startSpinner = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 3000);
  };

  //start spinner on page load
  useEffect(() => {
    startSpinner();
  }, []);

  return (
    <Container>
      <h1>Shadow</h1>
      <Grid direction="column" container spacing={2}>
        <Grid item>{loading ? <CircularProgress id="cpShadow" /> : <ShadowComponent />}</Grid>
        <Grid item>
          <Button id="btnStartSpinner" onClick={startSpinner}>Start Spinner</Button>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Shadow;

import { useState } from "react";
import { Card, Container, Grid } from "@mui/material";
import { bWellSdk } from "@/sdk/bWellSdk";

const Hello = () => {
  const [helloResponse, setHelloResponse] = useState<string>("");

  const handleClick = () => {
    setHelloResponse(bWellSdk.hello());
  };

  return (
    <Container>
      <Grid direction="column" container spacing={2}>
        <Grid item>
          <Grid container direction="row" spacing={2}>
            <button onClick={handleClick}>Hello</button>
            <button onClick={() => setHelloResponse("")}>Clear</button>
          </Grid>
        </Grid>
        <Grid item>
          <Card variant="outlined">
            <pre style={{ padding: "10px" }}>{helloResponse}</pre>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Hello;

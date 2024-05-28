import { useState } from "react";
import { Card, Container, Grid } from "@mui/material";

const Hello = () => {
  const [helloResponse, setHelloResponse] = useState<string>("");

  const handleClick = () => {
    setHelloResponse("World!");
  };

  return (
    <Container>
      <h1>Hello</h1>
      <h2>A page for testing the SDK hello() function</h2>
      <Grid direction="column" container spacing={2}>
        <Grid item>
          <button id="btnHello" onClick={handleClick}>Hello</button>
          <button id="btnClear" onClick={() => setHelloResponse("")}>Clear</button>
        </Grid>
        <Grid item>
          <Card variant="outlined">
            <pre id="preHelloResponse" style={{ padding: "10px" }}>{helloResponse}</pre>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Hello;

import { useState } from "react";
import { Alert, Box, Button, Grid, TextField } from "@mui/material";
import { useDispatch, useSelector } from "react-redux";

import { authenticate, initialize, userSlice } from "@/store/userSlice";
import { AppDispatch, RootState } from "@/store/store";

const DEFAULT_KEY = import.meta.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = import.meta.env.VITE_DEFAULT_OAUTH_CREDS ?? "";

const Initialize = () => {
  const [key, setKey] = useState<string>(DEFAULT_KEY);
  const [oauthCreds, setOauthCreds] = useState<string>(DEFAULT_OAUTH_CREDS);

  const dispatch = useDispatch<AppDispatch>();

  const initializationError = useSelector(
    (state: RootState) => state.user.error
  );

  const user = useSelector((state: RootState) => state.user)

  const { error: authenticationError, isInitialized, isLoggedIn } = user;

  const initializeWithProvidedKey = () => dispatch(initialize({ clientKey: key }));

  const loginWithProvidedOAuthCreds = () => dispatch(authenticate({ oauthCreds }));

  const CenteredGridItem = ({ children }) => (
    <Grid item>
      <Box textAlign={"center"}>{children}</Box>
    </Grid>
  );

  return (
    <Grid
      container
      alignItems="stretch"
      direction="column"
      justifyContent="flex-start"
      spacing={2}
    >
      <CenteredGridItem>
        <h1>b.well Typescript SDK Example Web App</h1>
      </CenteredGridItem>
      <CenteredGridItem>
        <h2>Client Key</h2>
        <TextField
          aria-label="client key"
          id="txtKey"
          onChange={(e) => setKey(e.target.value)}
          placeholder="Enter a valid b.well client key here"
          disabled={isInitialized}
          fullWidth
          type="password"
          value={key}
        />
      </CenteredGridItem>
      <Grid item>
        <Box textAlign={"center"}>
          <Button
            id="btnInitialize"
            variant="contained"
            disabled={!key}
            onClick={initializeWithProvidedKey}
            disabled={isInitialized}
          >
            Initialize
          </Button>
        </Box>
      </Grid>
      {initializationError && (
        <Grid item>
          <Box textAlign={"center"}>
            <Alert severity="error" id="initializationError">{initializationError.message}</Alert>
          </Box>
        </Grid>
      )}
      {isInitialized && (
        <>
          <Grid item>
            <Box textAlign={"center"}>
              <h2>OAuth Creds</h2>
              <TextField
                type="password"
                aria-label="oauth creds"
                id="txtOauthCreds"
                onChange={(e) => setOauthCreds(e.target.value)}
                placeholder="Enter valid a b.well OAuth JWT here"
                value={oauthCreds}
                fullWidth
                style={{ minWidth: "80%", padding: "10px" }}
                disabled={isLoggedIn}
              />
            </Box>
          </Grid>
          <Grid item>
            <Box textAlign={"center"}>
              <Button
                id="btnSubmit"
                variant="contained"
                disabled={!(key && oauthCreds) || isLoggedIn}
                onClick={loginWithProvidedOAuthCreds}
              >
                Login
              </Button>
            </Box>
          </Grid>
          {authenticationError && (
            <Grid item>
              <Box textAlign={"center"}>
                <Alert severity="error" id="authenticationError">{authenticationError}</Alert>
              </Box>
            </Grid>
          )}
          {isLoggedIn && (
            <Grid item>
              <Box textAlign={"center"}>
                <Alert severity="info" id="authenticationInfo">User successfully logged in.</Alert>
              </Box>
            </Grid>
          )}

          {isLoggedIn && (
            <Grid item>
              <Box textAlign={"center"}>
                <Button variant="contained" onClick={() => dispatch(userSlice.actions.resetState())}>Log Out</Button>
              </Box>
            </Grid>
          )}
        </>
      )}
    </Grid>
  );
};

export default Initialize;

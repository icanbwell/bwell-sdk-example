import { useState } from "react";
import { Alert, Box, Button, Grid, TextField } from "@mui/material";
import { useDispatch, useSelector } from "react-redux";

import { authenticate, initialize, userSlice } from "@/store/userSlice";
import { AppDispatch, RootState } from "@/store/store";

const DEFAULT_KEY = import.meta.env.VITE_DEFAULT_KEY ?? "";
const DEFAULT_OAUTH_CREDS = import.meta.env.VITE_DEFAULT_OAUTH_CREDS ?? "";

const CenteredGridItem = ({ children }: { children: React.ReactNode }) => (
  <Grid item>
    <Box textAlign={"center"}>{children}</Box>
  </Grid>
);

const Initialize = () => {
  const [key, setKey] = useState<string>(DEFAULT_KEY);
  const [oauthCreds, setOauthCreds] = useState<string>(DEFAULT_OAUTH_CREDS);

  const dispatch = useDispatch<AppDispatch>();

  const user = useSelector((state: RootState) => state.user)

  const { error, isInitialized, isLoggedIn } = user;

  const initializeWithProvidedKey = () => dispatch(initialize({ clientKey: key }));

  const loginWithProvidedOAuthCreds = () => dispatch(authenticate({ oauthCreds }));

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
      <CenteredGridItem>
        <Button
          id="btnInitialize"
          variant="contained"
          onClick={initializeWithProvidedKey}
          disabled={isInitialized || !key}
        >
          Initialize
        </Button>
      </CenteredGridItem>
      {
        isInitialized && (
          <>
            <CenteredGridItem>
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
            </CenteredGridItem>
            <CenteredGridItem>
              <Button
                id="btnSubmit"
                variant="contained"
                disabled={!(key && oauthCreds) || isLoggedIn}
                onClick={loginWithProvidedOAuthCreds}
              >
                Login
              </Button>
            </CenteredGridItem>
            {
              isLoggedIn && (
                <CenteredGridItem>
                  <Alert severity="info" id="authenticationInfo">User successfully logged in.</Alert>
                </CenteredGridItem>
              )
            }

            {
              isLoggedIn && (
                <CenteredGridItem>
                  <Button variant="contained" onClick={() => dispatch(userSlice.actions.resetState())}>Log Out</Button>
                </CenteredGridItem>
              )
            }
          </>
        )
      }
      {
        error && (
          <CenteredGridItem>
            <Alert severity="error" id="authenticationError">{error}</Alert>
          </CenteredGridItem>
        )
      }
    </Grid >
  );
};

export default Initialize;

import { useState } from "react";
import { Box, Button, Grid, TextareaAutosize } from "@mui/material";
import { useDispatch, useSelector } from "react-redux";

import { initialize } from "@/store/initializationSlice";
import { loginUser } from "@/store/userSlice";
import { AppDispatch, RootState } from "@/store/store";

const DEFAULT_KEY =
  "eyJyIjoiMTVoaGcwOHpybjgyOW8zenV3ZHgiLCJlbnYiOiJkZXYiLCJraWQiOiJzYW1zdW5nLWRldiJ9";

const DEFAULT_OAUTH_CREDS =
  "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJ3ZWxsLXRlc3QifQ.eyJndWlkIjoiYndlbGwtdGVzdF9IM3FUSmNoeWJQUXlRT015M29qZS9nPT0iLCJvdGlkIjpmYWxzZSwiZXhwIjoyNjk4MjM0MzcxLCJpYXQiOjE3MDYxMTA3NzF9.3D-4HYfsZFYfyL_QsDEmjBgxyajg7XrIXda48xm_PbkmrbhURG7Pa_ijOGKHIfGSMJ7sWkZDkbx5nsLu-pKgjg";

const Initialize = () => {
  const [key, setKey] = useState<string>(DEFAULT_KEY);
  const [oauthCreds, setOauthCreds] = useState<string>(DEFAULT_OAUTH_CREDS);

  const dispatch = useDispatch<AppDispatch>();

  const isInitialized = useSelector(
    (state: RootState) => state.initialization.isInitialized
  );

  const initializeWithProvidedKey = () => {
    console.log("dispatching initialize action...");
    dispatch(initialize({ clientKey: key }));
  };

  const loginWithProvidedOAuthCreds = () => {
    console.log("dispatching login action...");
    dispatch(loginUser({ oauthCreds }));
  }

  return (
    <Grid
      container
      alignItems="stretch"
      direction="column"
      justifyContent="flex-start"
      spacing={2}
    >
      <Grid item>
        <Box textAlign={"center"}>
          <h1>b.well Typescript SDK Example Web App</h1>
        </Box>
      </Grid>
      <Grid item>
        <Box textAlign={"center"}>
          <h2>Client Key</h2>
          <TextareaAutosize
            aria-label="client key"
            id="txtKey"
            minRows={5}
            onChange={(e) => setKey(e.target.value)}
            placeholder="Enter a valid b.well client key here"
            value={key}
            style={{ minWidth: "80%", padding: "10px" }}
          />
        </Box>
      </Grid>
      <Grid item>
        <Box textAlign={"center"}>
          <Button
            id="btnSubmit"
            variant="contained"
            disabled={!(key && oauthCreds)}
            onClick={initializeWithProvidedKey}
          >
            Initialize
          </Button>
        </Box>
      </Grid>
      {isInitialized && (
        <>
          <Grid item>
            <Box textAlign={"center"}>
              <h2>OAuth Creds</h2>
              <TextareaAutosize
                aria-label="oauth creds"
                id="txtOauthCreds"
                minRows={5}
                onChange={(e) => setOauthCreds(e.target.value)}
                placeholder="Enter valid a b.well OAuth JWT here"
                value={oauthCreds}
                style={{ minWidth: "80%", padding: "10px" }}
              />
            </Box>
          </Grid>
          <Grid item>
            <Box textAlign={"center"}>
              <Button
                id="btnSubmit"
                variant="contained"
                disabled={!(key && oauthCreds)}
                onClick={loginWithProvidedOAuthCreds}
              >
                Login
              </Button>
            </Box>
          </Grid>
        </>
      )}
    </Grid>
  );
};

export default Initialize;
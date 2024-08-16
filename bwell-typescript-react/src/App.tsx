import { createTheme, CssBaseline, ThemeProvider } from "@mui/material";
import { Provider } from "react-redux";
import { RouterProvider } from "react-router-dom";
import { Persistor, store } from "@/store/store";

import Router from "@/Router";
import { PersistGate } from "redux-persist/integration/react";

const theme = createTheme({
  typography: {
    fontFamily: '"Helvetica", "Arial", sans-serif',
  },
});

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <Provider store={store}>
        <PersistGate loading={null} persistor={Persistor}>
          <CssBaseline />
          <RouterProvider router={Router} />
        </PersistGate>
      </Provider>
    </ThemeProvider>
  );
};

export default App;

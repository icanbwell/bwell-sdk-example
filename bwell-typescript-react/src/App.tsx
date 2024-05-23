import { createTheme, CssBaseline, ThemeProvider } from "@mui/material";
import { Provider } from "react-redux";
import { RouterProvider } from "react-router-dom";
import { store } from "@/store/store";

import Router from "@/Router";

const theme = createTheme({
  typography: {
    fontFamily: '"Helvetica", "Arial", sans-serif',
  },
});

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <Provider store={store}>
        <CssBaseline />
        <RouterProvider router={Router} />
      </Provider>
    </ThemeProvider>
  );
};

export default App;

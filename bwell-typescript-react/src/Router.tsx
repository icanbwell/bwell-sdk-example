import Initialize from "@/pages/Initialize";
import Hello from "@/pages/Hello";
import Page from "@/pages/Page";

import { createBrowserRouter } from "react-router-dom";

const Router = createBrowserRouter([
  {
    path: "/",
    element: (
      <Page>
        <Initialize />
      </Page>
    ),
  },
  {
    path: "/initialize",
    element: (
      <Page>
        <Initialize />
      </Page>
    ),
  },
  {
    path: "/hello",
    element: (
      <Page>
        <Hello />
      </Page>
    ),
  },
]);

export default Router;

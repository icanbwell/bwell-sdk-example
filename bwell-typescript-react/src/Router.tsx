import Initialize from "@/pages/Initialize";
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
  }
]);

export default Router;

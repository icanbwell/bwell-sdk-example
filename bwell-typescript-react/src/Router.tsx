import Initialize from "@/pages/Initialize";
import Hello from "@/pages/Hello";
import Page from "@/pages/Page";
import Shadow from "@/pages/Shadow";
import AllergyIntolerances from "@/pages/AllergyIntolerances";

import { createBrowserRouter } from "react-router-dom";

const makePageRoute = (path: string, element: JSX.Element) => ({
  path,
  element: <Page>{element}</Page>,
});

const Router = createBrowserRouter([
  makePageRoute("/", <Initialize />),
  makePageRoute("/initialize", <Initialize />),
  makePageRoute("/hello", <Hello />),
  makePageRoute("/shadow", <Shadow />),
  makePageRoute("/allergyIntolerances", <AllergyIntolerances />),
]);

export default Router;

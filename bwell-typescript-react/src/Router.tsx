import Initialize from "@/pages/Initialize";
import Page from "@/pages/Page";
import AllergyIntolerances from "@/pages/AllergyIntolerances";
import Connections from "@/pages/Connections";
import { createBrowserRouter } from "react-router-dom";
import HealthSummary from "./pages/HealthSummary";

const makePageRoute = (path: string, element: JSX.Element) => ({
  path,
  element: <Page>{element}</Page>,
});

const Router = createBrowserRouter([
  makePageRoute("/", <Initialize />),
  makePageRoute("/initialize", <Initialize />),
  makePageRoute("/healthSummary", <HealthSummary />),
  makePageRoute("/allergyIntolerances", <AllergyIntolerances />),
  makePageRoute("/connections", <Connections />),
]);

export default Router;

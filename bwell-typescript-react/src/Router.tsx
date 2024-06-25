import Initialize from "@/pages/Initialize";
import Page from "@/pages/Page";
import AllergyIntolerances from "@/pages/AllergyIntolerances";
import Connections from "@/pages/Connections";
import HealthData from "@/pages/HealthData";
import { createBrowserRouter } from "react-router-dom";

const makePageRoute = (path: string, element: JSX.Element) => ({
  path,
  element: <Page>{element}</Page>,
});

const Router = createBrowserRouter([
  makePageRoute("/", <Initialize />),
  makePageRoute("/initialize", <Initialize />),
  makePageRoute("/allergyIntolerances", <AllergyIntolerances />),
  makePageRoute("/healthData", <HealthData />),
  makePageRoute("/connections", <Connections />),
]);

export default Router;

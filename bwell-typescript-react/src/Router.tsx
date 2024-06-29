import Initialize from "@/pages/Initialize";
import Page from "@/pages/Page";
import AllergyIntolerances from "@/pages/AllergyIntolerances";
import Connections from "@/pages/Connections";
import { createBrowserRouter } from "react-router-dom";
import HealthSummary from "./pages/HealthSummary";
import Conditions from "./pages/Conditions";
import Labs from "./pages/Labs";
import CarePlans from "./pages/CarePlans";
import Encounters from "./pages/Encounters";

const makePageRoute = (path: string, element: JSX.Element) => ({
  path,
  element: <Page>{element}</Page>,
});

const Router = createBrowserRouter([
  makePageRoute("/", <Initialize />),
  makePageRoute("/initialize", <Initialize />),
  makePageRoute("/healthSummary", <HealthSummary />),
  makePageRoute("/allergyIntolerances", <AllergyIntolerances />),
  makePageRoute("/carePlans", <CarePlans />),
  makePageRoute("/conditions", <Conditions />),
  makePageRoute("/encounters", < Encounters />),
  makePageRoute("/labs", <Labs />),
  makePageRoute("/connections", <Connections />),  
])


export default Router;

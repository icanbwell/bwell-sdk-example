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
import Immunizations from "./pages/Immunizations";
import Medications from "./pages/Medications";
import Procedures from "./pages/Procedures";
import VitalSigns from "./pages/VitalSigns";
import ProfilePage from "./pages/Profile";

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
  makePageRoute("/encounters", <Encounters />),
  makePageRoute("/immunizations", <Immunizations />),
  makePageRoute("/labs", <Labs />),
  makePageRoute("/medications", <Medications />),
  makePageRoute("/procedures", <Procedures />),
  makePageRoute("/vitalSigns", <VitalSigns />),
  makePageRoute("/connections", <Connections />),
  makePageRoute("/profile", <ProfilePage />),
]);

export default Router;

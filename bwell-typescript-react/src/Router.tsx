import Initialize from "@/pages/Initialize";
import Page from "@/pages/Page";
import AllergyIntolerances from "@/pages/AllergyIntolerances";
import ManageConnections from "@/pages/Connections";
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
import SearchPage from "./pages/Search";
import ConsentSdkTest from "./pages/ConsentSdkTest";
import TosConsentSdkTest from "./pages/TosConsentSdkTest";
import CreateTosConsentSdkTest from "./pages/CreateTosConsentSdkTest";
import CreateProaConsentSdkTest from "./pages/CreateProaConsentSdkTest";

const makePageRoute = (path: string, element: JSX.Element) => ({
  path,
  element: <Page>{element}</Page>,
});

const Router = createBrowserRouter([
  makePageRoute("/", <Initialize />),
  makePageRoute("/initialize", <Initialize />),
  makePageRoute("/profile", <ProfilePage />),
  makePageRoute("/search", <SearchPage />),
  makePageRoute("/connections", <ManageConnections />),
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
  makePageRoute("/consentSdkTest", <ConsentSdkTest />),
  makePageRoute("/tosConsentSdkTest", <TosConsentSdkTest />),
  makePageRoute("/createTosConsentSdkTest", <CreateTosConsentSdkTest />),
  makePageRoute("/createProaConsentSdkTest", <CreateProaConsentSdkTest />)
]);

export default Router;

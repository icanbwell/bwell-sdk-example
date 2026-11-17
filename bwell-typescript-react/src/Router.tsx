import { createBrowserRouter, Navigate, Outlet } from "react-router-dom";
import { SessionProvider } from "@/auth/SessionContext";
import { RequireSession, RequireTos } from "@/auth/Guards";
import AppShell from "@/components/AppShell";
import Login from "@/pages/Login";
import Terms from "@/pages/Terms";
import ConnectMethod from "@/pages/ConnectMethod";
import ConnectClear from "@/pages/ConnectClear";
import ConnectSearch from "@/pages/ConnectSearch";
import OAuthReturn from "@/pages/OAuthReturn";
import Connections from "@/pages/Connections";
import { HealthDataPage, HealthIndexRedirect, HealthLayout } from "@/pages/Health";
import Settings from "@/pages/Settings";
import Chat from "@/pages/Chat";

// The session lives in the router so the OAuth/CLEAR return page can sit outside it.
const SessionRoot = () => <SessionProvider><Outlet /></SessionProvider>;

export const router = createBrowserRouter([
  // The popup lands here after CLEAR or a provider sign-in; it must not restore the session.
  { path: "/oauth/return", element: <OAuthReturn /> },
  {
    element: <SessionRoot />,
    children: [
      { path: "/", element: <Navigate to="/login" replace /> },
      { path: "/login", element: <Login /> },
      {
        element: <RequireSession />,
        children: [
          { path: "/terms", element: <Terms /> },
          {
            element: <RequireTos />,
            children: [
              { path: "/connect-method", element: <ConnectMethod /> },
              { path: "/connect/clear", element: <ConnectClear /> },
              { path: "/connect/search", element: <ConnectSearch /> },
              {
                path: "/app",
                element: <AppShell />,
                children: [
                  { index: true, element: <Navigate to="connections" replace /> },
                  { path: "connections", element: <Connections /> },
                  {
                    path: "health",
                    element: <HealthLayout />,
                    children: [
                      { index: true, element: <HealthIndexRedirect /> },
                      // v2: add { path: "share", element: <ShareMyRecords /> } before the :type route.
                      { path: ":type", element: <HealthDataPage /> },
                    ],
                  },
                  { path: "chat", element: <Chat /> },
                  { path: "settings", element: <Settings /> },
                ],
              },
            ],
          },
        ],
      },
      { path: "*", element: <Navigate to="/login" replace /> },
    ],
  },
]);

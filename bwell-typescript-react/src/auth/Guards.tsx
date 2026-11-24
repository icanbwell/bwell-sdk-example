import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useSession } from "@/auth/SessionContext";
import { Loading } from "@/components/Page";

/** Journey order: signed in → ToS accepted → rest of the app. */
export function RequireSession() {
  const { status } = useSession();
  const location = useLocation();
  if (status === "restoring" || status === "signing-in") return <Loading label="Signing you in…" />;
  return status === "signed-in" ? <Outlet /> : <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
}

export function RequireTos() {
  const { tosAccepted } = useSession();
  return tosAccepted ? <Outlet /> : <Navigate to="/terms" replace />;
}

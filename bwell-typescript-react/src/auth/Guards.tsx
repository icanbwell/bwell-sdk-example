import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useSession } from "@/auth/SessionContext";
import { Loading } from "@/components/Page";

/** Journey order: signed in → ToS accepted → rest of the app. */
export function RequireSession() {
  const { status, restoreFailed } = useSession();
  const location = useLocation();
  if (status === "restoring" || status === "signing-in") return <Loading label="Signing you in…" />;
  if (status === "signed-in") return <Outlet />;
  // Remember the page only when the session couldn't be restored, so a reload brings the user back to it.
  // An explicit sign-out leaves no return path: the next sign-in starts from the sign-in form's choices.
  return <Navigate to="/login" replace state={restoreFailed ? { from: location.pathname + location.search } : undefined} />;
}

export function RequireTos() {
  const { tosAccepted } = useSession();
  return tosAccepted ? <Outlet /> : <Navigate to="/terms" replace />;
}

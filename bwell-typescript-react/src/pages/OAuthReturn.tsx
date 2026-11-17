import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { JourneyPage } from "@/components/Page";
import { announceExternalReturn } from "@/sdk/externalFlow";

// Where the user lands after signing in to a provider portal or finishing CLEAR. Register this
// path as the OAuth redirect for the environment in CCS (and use it for VITE_CLEAR_CALLBACK_URL).
//
// This route sits outside SessionProvider on purpose: the popup shouldn't try to restore the
// session (and refresh tokens) while the main tab is still using it.
//
// In the popup it hands control back to the main tab and closes. If no tab answers (the popup was
// blocked, so the whole page was redirected), it carries on to the connections page itself.
export default function OAuthReturn() {
  const navigate = useNavigate();
  const [handedOff, setHandedOff] = useState(false);
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return; // StrictMode runs effects twice in dev
    started.current = true;
    void announceExternalReturn().then((acknowledged) => {
      if (!acknowledged) return navigate("/app/connections", { replace: true });
      setHandedOff(true);
      window.close(); // only works for windows the app opened; otherwise the message below stays
    });
  }, [navigate]);

  return handedOff
    ? <JourneyPage title="You're all set" subtitle="You can close this window and return to the app.">{null}</JourneyPage>
    : <JourneyPage title="Finishing up…">{null}</JourneyPage>;
}

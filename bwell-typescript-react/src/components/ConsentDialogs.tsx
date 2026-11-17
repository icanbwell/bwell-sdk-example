import { ReactNode } from "react";
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, Link, Stack, Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import PauseCircleOutlineIcon from "@mui/icons-material/PauseCircleOutline";
import TuneIcon from "@mui/icons-material/Tune";
import { Marked } from "@/components/Marked";
import TosText from "@/components/TosText";
import { TOS_HEADING } from "@/config/tos";
import {
  PATIENT_RIGHTS_URL, SMART_CONNECT_ABOUT_INTRO, SMART_CONNECT_ABOUT_TITLE, SMART_CONNECT_BULLETS,
  SMART_CONNECT_LEARN_MORE_AFTER, SMART_CONNECT_LEARN_MORE_BEFORE, SMART_CONNECT_LEARN_MORE_TERM,
  SMART_CONNECT_LEARN_MORE_TITLE, SMART_CONNECT_TERMS_URL,
} from "@/config/smartConnect";

const ICONS = { pause: PauseCircleOutlineIcon, lock: LockOutlinedIcon, controls: TuneIcon } as const;

function TitleWithClose({ children, onClose }: { children: ReactNode; onClose: () => void }) {
  return (
    <DialogTitle sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 2 }}>
      {children}
      <IconButton aria-label="Close" onClick={onClose} edge="end"><CloseIcon /></IconButton>
    </DialogTitle>
  );
}

const PolicyLink = ({ href, children }: { href: string; children: ReactNode }) =>
  href ? <Link href={href} target="_blank" rel="noopener noreferrer" underline="always" color="inherit" fontWeight={600}>{children}</Link>
       : <Box component="span" fontWeight={600} sx={{ textDecoration: "underline" }}>{children}</Box>;

/** Explains automatic record search and collects the user's agreement. Nothing is saved on agree. */
export function AboutSmartConnectDialog({ open, onAgree, onBack, busy = false, error = null }: {
  open: boolean; onAgree: () => void; onBack: () => void; busy?: boolean; error?: string | null;
}) {
  return (
    <Dialog open={open} onClose={busy ? undefined : onBack} fullWidth maxWidth="xs" aria-labelledby="about-sc-title">
      <TitleWithClose onClose={onBack}><span id="about-sc-title"><Marked text={SMART_CONNECT_ABOUT_TITLE} /></span></TitleWithClose>
      <DialogContent>
        <Typography sx={{ mb: 2 }}><Marked text={SMART_CONNECT_ABOUT_INTRO} /></Typography>
        <Stack spacing={1.5} sx={{ mb: 3 }}>
          {SMART_CONNECT_BULLETS.map((b) => {
            const Icon = ICONS[b.icon];
            return (
              <Stack key={b.text} direction="row" spacing={1.5} alignItems="center">
                <Icon color="primary" /><Typography>{b.text}</Typography>
              </Stack>
            );
          })}
        </Stack>
        <Typography variant="body2" color="text.secondary">
          Read our <PolicyLink href={PATIENT_RIGHTS_URL}>Patient Rights and Requests</PolicyLink> policy. See the full{" "}
          <PolicyLink href={SMART_CONNECT_TERMS_URL}><Marked text="Smart Connect Terms" /></PolicyLink> for more.
        </Typography>
        {error && <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>}
      </DialogContent>
      <DialogActions sx={{ flexDirection: "column", gap: 1, p: 3, pt: 0 }}>
        <Button variant="contained" size="large" fullWidth onClick={onAgree} disabled={busy}>
          {busy ? "One moment…" : "Agree and continue"}
        </Button>
        <Button onClick={onBack} disabled={busy}>Back</Button>
      </DialogActions>
    </Dialog>
  );
}

/** Longer description of Smart Connect, opened from the "Learn more" link. */
export function SmartConnectLearnMoreDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="learn-sc-title">
      <TitleWithClose onClose={onClose}><span id="learn-sc-title"><Marked text={SMART_CONNECT_LEARN_MORE_TITLE} /></span></TitleWithClose>
      <DialogContent>
        <Typography sx={{ lineHeight: 1.7 }}>
          <Marked text={SMART_CONNECT_LEARN_MORE_BEFORE} /><strong>{SMART_CONNECT_LEARN_MORE_TERM}</strong><Marked text={SMART_CONNECT_LEARN_MORE_AFTER} />
        </Typography>
      </DialogContent>
      <DialogActions sx={{ p: 3, pt: 0 }}><Button onClick={onClose}>Close</Button></DialogActions>
    </Dialog>
  );
}

/** Shown when Smart Connect is requested before IAL2 identity verification. */
export function VerificationRequiredDialog({ open, busy, error, onVerify, onClose, waiting }: {
  open: boolean; busy: boolean; error: string | null; onVerify: () => void; onClose: () => void;
  /** Replaces the dialog body while the CLEAR window is open. */
  waiting?: ReactNode;
}) {
  return (
    <Dialog open={open} onClose={busy ? undefined : onClose} fullWidth maxWidth="xs" aria-labelledby="verify-req-title">
      <TitleWithClose onClose={onClose}><span id="verify-req-title">Verify your identity</span></TitleWithClose>
      {waiting ? <DialogContent sx={{ pb: 3 }}>{waiting}</DialogContent> : <>
      <DialogContent>
        <Typography sx={{ mb: 2 }}>
          <Marked text="Smart Connect searches the network for your records, so we need to confirm it's really you first. Verifying with CLEAR takes a couple of minutes." />
        </Typography>
        <Typography><Marked text="Once you're verified, Smart Connect will be turned on automatically." /></Typography>
        {error && <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>}
      </DialogContent>
      <DialogActions sx={{ flexDirection: "column", gap: 1, p: 3, pt: 0 }}>
        <Button variant="contained" size="large" fullWidth onClick={onVerify} disabled={busy}>
          {busy ? "Opening CLEAR…" : "Verify with CLEAR"}
        </Button>
        <Button onClick={onClose} disabled={busy}>Not now</Button>
      </DialogActions>
      </>}
    </Dialog>
  );
}

/** Read-only review of the data intermediary authorization the user accepted. */
export function AuthorizationDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md" aria-labelledby="auth-title">
      <TitleWithClose onClose={onClose}><span id="auth-title">{TOS_HEADING}</span></TitleWithClose>
      <DialogContent><TosText /></DialogContent>
      <DialogActions sx={{ p: 3, pt: 0 }}><Button onClick={onClose}>Close</Button></DialogActions>
    </Dialog>
  );
}

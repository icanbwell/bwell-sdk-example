import { Link, Typography } from "@mui/material";
import { PRIVACY_STATEMENT_URL } from "@/config/app";
import { TOS_AFTER_LINK, TOS_BEFORE_LINK, TOS_LINK_TEXT } from "@/config/tos";

export default function TosText() {
  return (
    <Typography sx={{ lineHeight: 1.7 }}>
      {TOS_BEFORE_LINK}
      <Link href={PRIVACY_STATEMENT_URL} target="_blank" rel="noopener noreferrer">{TOS_LINK_TEXT}</Link>
      {TOS_AFTER_LINK}
    </Typography>
  );
}

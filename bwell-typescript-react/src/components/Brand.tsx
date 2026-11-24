import { Box } from "@mui/material";

// The logo is a JPG, which can't be transparent. Multiplying it with the page behind it turns its white
// background into that page's colour (white stays white, the navy and green are unchanged). Replace with an
// official transparent PNG or SVG if one is available, and drop the blend mode.
export const Logo = ({ height = 36 }: { height?: number }) => (
  <Box component="img" src="/brand/bwell-logo.jpg" alt="b.well" sx={{ height, display: "block", mixBlendMode: "multiply" }} />
);

export const PoweredBy = ({ height = 28 }: { height?: number }) => (
  <Box component="img" src="/brand/powered-by-bwell.png" alt="Powered by b.well" sx={{ height, display: "block" }} />
);

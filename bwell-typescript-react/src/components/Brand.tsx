import { Box } from "@mui/material";

export const Logo = ({ height = 36 }: { height?: number }) => (
  <Box component="img" src="/brand/bwell-logo.jpg" alt="b.well" sx={{ height, display: "block" }} />
);

export const PoweredBy = ({ height = 28 }: { height?: number }) => (
  <Box component="img" src="/brand/powered-by-bwell.png" alt="Powered by b.well" sx={{ height, display: "block" }} />
);

import { createTheme } from "@mui/material/styles";
import "@fontsource/quicksand/400.css";
import "@fontsource/quicksand/600.css";
import "@fontsource/quicksand/700.css";
import "@fontsource/open-sans/400.css";
import "@fontsource/open-sans/600.css";
import "@fontsource/open-sans/700.css";

// 2026 b.well brand book.
export const brand = {
  blue: "#2E3586",
  green: "#6CC63F",
  darkBlue: "#191D4D",
  lilac: "#6C60ED",
  orange: "#F56345",
  yellow: "#FFE249",
  darkGray: "#494949",
  midGray: "#7F7F7F",
  lightGray: "#F7F7F7",
  blue25: "#CBCCE1",
  green25: "#DAF1CF",
} as const;

const headingFont = '"Quicksand", "Open Sans", sans-serif';
const bodyFont = '"Open Sans", sans-serif';

export const theme = createTheme({
  palette: {
    primary: { main: brand.blue, dark: brand.darkBlue, contrastText: "#fff" },
    secondary: { main: brand.green, contrastText: brand.darkBlue },
    error: { main: brand.orange },
    warning: { main: brand.yellow, contrastText: brand.darkBlue },
    info: { main: brand.lilac },
    success: { main: brand.green, contrastText: brand.darkBlue },
    text: { primary: brand.darkGray, secondary: brand.midGray },
    background: { default: brand.lightGray, paper: "#fff" },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: bodyFont,
    h1: { fontFamily: headingFont, fontWeight: 700, color: brand.blue },
    h2: { fontFamily: headingFont, fontWeight: 700, color: brand.blue },
    h3: { fontFamily: headingFont, fontWeight: 700, color: brand.blue },
    h4: { fontFamily: headingFont, fontWeight: 700, color: brand.blue },
    h5: { fontFamily: headingFont, fontWeight: 600, color: brand.blue },
    h6: { fontFamily: headingFont, fontWeight: 600, color: brand.blue },
    button: { fontFamily: headingFont, fontWeight: 700, textTransform: "none", letterSpacing: 0.2 },
  },
  components: {
    MuiButton: { defaultProps: { disableElevation: true }, styleOverrides: { root: { borderRadius: 999, paddingInline: 24 } } },
    MuiCard: { defaultProps: { variant: "outlined" }, styleOverrides: { root: { borderColor: brand.blue25 } } },
  },
});

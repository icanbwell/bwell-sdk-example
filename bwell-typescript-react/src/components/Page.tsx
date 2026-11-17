import { ReactNode } from "react";
import { Box, Container, Paper, Typography } from "@mui/material";
import { Logo, PoweredBy } from "@/components/Brand";

/** Centered single-card layout for the pre-app journey (login, terms, connect). */
export function JourneyPage({ title, subtitle, children, step, maxWidth = "sm" }: {
  title: string; subtitle?: string; children: ReactNode; step?: string; maxWidth?: "sm" | "md";
}) {
  return (
    <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column", py: 4 }}>
      <Container maxWidth={maxWidth} sx={{ flex: 1 }}>
        <Box sx={{ display: "flex", justifyContent: "center", mb: 4 }}><Logo height={44} /></Box>
        <Paper sx={{ p: { xs: 3, sm: 5 } }} elevation={0}>
          {step && <Typography variant="overline" color="text.secondary">{step}</Typography>}
          <Typography variant="h4" component="h1" gutterBottom>{title}</Typography>
          {subtitle && <Typography color="text.secondary" sx={{ mb: 3 }}>{subtitle}</Typography>}
          {children}
        </Paper>
      </Container>
      <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}><PoweredBy /></Box>
    </Box>
  );
}

export const Loading = ({ label = "Loading…" }: { label?: string }) => (
  <Typography color="text.secondary" role="status" sx={{ py: 3 }}>{label}</Typography>
);

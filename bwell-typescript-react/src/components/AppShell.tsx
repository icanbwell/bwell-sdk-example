import { AppBar, Box, Button, Container, Toolbar } from "@mui/material";
import { NavLink, Outlet } from "react-router-dom";
import { Logo, PoweredBy } from "@/components/Brand";
import { MAIN_NAV } from "@/config/app";
import { useSession } from "@/auth/SessionContext";
import { brand } from "@/theme/theme";
import { Marked } from "@/components/Marked";

export default function AppShell() {
  const { signOut } = useSession();
  return (
    <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <AppBar position="static" color="inherit" elevation={0} sx={{ borderBottom: `1px solid ${brand.blue25}` }}>
        <Toolbar sx={{ gap: 2, flexWrap: "wrap" }}>
          <Logo height={32} />
          <Box component="nav" aria-label="Main" sx={{ display: "flex", gap: 1, flex: 1, ml: { sm: 4 }, flexWrap: "wrap" }}>
            {MAIN_NAV.map((item) => (
              <Button
                key={item.id}
                component={NavLink}
                to={item.to}
                color="primary"
                sx={{ "&.active": { bgcolor: "primary.main", color: "#fff" } }}
              >
                <Marked text={item.label} />
              </Button>
            ))}
          </Box>
          <Button color="primary" onClick={signOut}>Sign out</Button>
        </Toolbar>
      </AppBar>
      <Container sx={{ flex: 1, py: 4 }}><Outlet /></Container>
      <Box sx={{ display: "flex", justifyContent: "center", py: 3 }}><PoweredBy /></Box>
    </Box>
  );
}

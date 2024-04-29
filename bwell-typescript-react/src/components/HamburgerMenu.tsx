import { useState } from "react";
import { Drawer, IconButton } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import { Link, RemixRouter, RouteObject } from "react-router-dom";

type HamburgerMenuProps = {
  router: RemixRouter;
};

const HamburgerMenu = ({ router }: HamburgerMenuProps) => {
  const [open, setOpen] = useState(false);

  const getRouteLabel = (path: string) => {
    const escapedPath = path.replace("/", "");

    if (escapedPath === "") return "Home";
    else return escapedPath.charAt(0).toUpperCase() + escapedPath.slice(1);
  };

  return (
    <div style={{ padding: "10px" }}>
      <IconButton
        color="inherit"
        aria-label="open drawer"
        edge="start"
        onClick={() => setOpen(true)}
        sx={{ mr: 2 }}
      >
        <MenuIcon />
      </IconButton>
      <Drawer
        anchor="left"
        open={open}
        onClose={() => setOpen(false)}
        PaperProps={{ sx: { minWidth: "10%", padding:"15px" }}}
      >
        <h2>Menu</h2>
        {router.routes
          .filter((r: RouteObject) => r.path !== "/")
          .map((route: RouteObject, i: number) => {
            return route.path ? (
              <div style={{ padding: "5px" }} key={i}>
                <Link
                  key={route.path}
                  to={route.path}
                  onClick={() => setOpen(false)}
                >
                  {getRouteLabel(route.path)}
                </Link>
              </div>
            ) : null;
          })}
      </Drawer>
    </div>
  );
};

export default HamburgerMenu;

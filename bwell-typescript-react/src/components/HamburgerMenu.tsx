import { useState } from "react";
import { Drawer, IconButton } from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import { Link, RemixRouter, RouteObject } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "@/store/store";
import { userSlice } from "@/store/userSlice";

type HamburgerMenuProps = {
  menuId: string;
  router: RemixRouter;
};

function addSpaceBeforeUppercase(pascalCaseString: string) {
  return pascalCaseString.replace(/([A-Z])/g, ' $1').trim();
}

const HamburgerMenu = ({ menuId, router }: HamburgerMenuProps) => {
  const [open, setOpen] = useState(false);

  const getRouteLabel = (path: string) => {
    const escapedPath = path.replace("/", "");

    if (escapedPath === "") return "Home";
    else return addSpaceBeforeUppercase(escapedPath.charAt(0).toUpperCase() + escapedPath.slice(1));
  };

  const user = useSelector((state: RootState) => state.user)

  const { isLoggedIn } = user;

  const dispatch = useDispatch<AppDispatch>();

  return (
    <div style={{ padding: "10px" }}>
      <IconButton
        color="inherit"
        aria-label="open drawer"
        edge="start"
        onClick={() => setOpen(true)}
        sx={{ mr: 2 }}
        id={menuId}
      >
        <MenuIcon />
      </IconButton>
      <Drawer
        anchor="left"
        open={open}
        onClose={() => setOpen(false)}
        PaperProps={{ sx: { minWidth: "10%", padding: "15px" } }}
      >
        <h2>Menu</h2>
        {router.routes
          .filter((r: RouteObject) => r.path !== "/")
          .map((route: RouteObject, i: number) => {
            return route.path ? (
              <div key={i}>
                <Link
                  key={route.path}
                  to={route.path}
                  onClick={() => setOpen(false)}
                  id={route.path.replace("/", "") + "MenuItem"}
                >
                  {getRouteLabel(route.path)}
                </Link>
              </div>
            ) : null;
          })
        }
        <hr />
        {
          isLoggedIn && (
            <Link id="logoutMenuItem" onClick={() => {
              dispatch(userSlice.actions.resetState());
              setOpen(false);
            }} to={"/initialize"}>Log Out</Link>
          )
        }
      </Drawer>
    </div>
  );
};

export default HamburgerMenu;

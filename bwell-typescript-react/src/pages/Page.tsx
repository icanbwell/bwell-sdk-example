import { ReactNode } from "react";

import { Box } from "@mui/material";

import HamburgerMenu from "@/components/HamburgerMenu";

import Router from "@/Router";

type PageProps = {
  children: ReactNode;
};

const Page = ({ children }: PageProps) => {
  return (
    <Box>
      <HamburgerMenu menuId="btnMainMenu" router={Router} />
      {children}
    </Box>
  );
};

export default Page;

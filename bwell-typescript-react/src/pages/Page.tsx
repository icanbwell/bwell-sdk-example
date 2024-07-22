import { ReactNode } from "react";

import { Container } from "@mui/material";

import HamburgerMenu from "@/components/HamburgerMenu";

import Router from "@/Router";

type PageProps = {
  children: ReactNode;
};

const Page = ({ children }: PageProps) => {
  return (
    <Container sx={{ marginBottom: '35px;' }}>
      <HamburgerMenu menuId="btnMainMenu" router={Router} />
      {children}
    </Container>
  );
};

export default Page;

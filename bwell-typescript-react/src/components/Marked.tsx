import { Fragment } from "react";

// Product names that carry a trademark. Add new ones here and every <Marked> picks them up.
const TRADEMARKED = /(Smart Connect)/;

/** Renders text with a superscript "TM" after each trademarked product name. */
export function Marked({ text }: { text: string }) {
  return (
    <>
      {text.split(TRADEMARKED).map((part, i) =>
        // split() with a capture group puts the matches at odd indexes.
        i % 2 === 1 ? (
          <Fragment key={i}>{part}<sup style={{ fontSize: "0.6em", lineHeight: 0 }}>TM</sup></Fragment>
        ) : (
          <Fragment key={i}>{part}</Fragment>
        ),
      )}
    </>
  );
}

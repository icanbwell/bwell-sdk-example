import { describe, expect, test } from "vitest";
import { formatFhirDate } from "@/sdk/formatDate";

describe("formatFhirDate", () => {
  // Run with TZ=America/Los_Angeles (see package.json) so a UTC-parsing regression would show up.
  test.each([
    ["a year", "2015", "2015"],
    ["a year and month", "2015-03", "Mar 2015"],
    ["a full date", "2015-03-07", "3/7/2015"],
    ["a date-time", "2015-03-07T12:00:00Z", "3/7/2015"],
    ["nothing", undefined, ""],
    ["text that isn't a date", "unknown", "unknown"],
  ])("formats %s", (_label, input, expected) => {
    expect(formatFhirDate(input, "en-US")).toBe(expected);
  });
});

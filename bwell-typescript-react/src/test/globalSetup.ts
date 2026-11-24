// Runs once before the tests, in every shell. A fixed non-UTC timezone makes the date tests catch
// code that parses date-only strings as UTC (they would show the previous day/month/year).
export default function setup() {
  process.env.TZ = "America/Los_Angeles";
}

/**
 * Formats a FHIR date for display. FHIR dates can be partial ("2015", "2015-03", "2015-03-07"). Those are
 * shown with only the precision they have, and built in local time: JavaScript would parse them as UTC
 * midnight, which shows as the previous year/month/day west of UTC. Full date-times are shown as a date.
 * Anything unparseable is returned as is.
 */
export function formatFhirDate(d?: string | null, locale?: string): string {
  if (!d) return "";
  const partial = /^(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?$/.exec(d);
  if (partial) {
    const [, year, month, day] = partial;
    if (!month) return year;
    const date = new Date(Number(year), Number(month) - 1, Number(day ?? 1));
    if (Number.isNaN(date.getTime())) return d;
    return day
      ? date.toLocaleDateString(locale)
      : date.toLocaleDateString(locale, { year: "numeric", month: "short" });
  }
  const date = new Date(d);
  return Number.isNaN(date.getTime()) ? d : date.toLocaleDateString(locale);
}

// Human-relative formatting for expiry timestamps, e.g. "expires in 4h" or
// "expired 2h ago". Intentionally simple (no i18n) — good enough for a demo.

export function formatRelativeExpiry(isoTimestamp: string): string {
  const target = new Date(isoTimestamp).getTime();
  if (Number.isNaN(target)) {
    return "unknown";
  }

  const diffMs = target - Date.now();
  const isPast = diffMs < 0;
  const absDiffMs = Math.abs(diffMs);

  const minutes = Math.round(absDiffMs / (60 * 1000));
  const hours = Math.round(absDiffMs / (60 * 60 * 1000));
  const days = Math.round(absDiffMs / (24 * 60 * 60 * 1000));

  let magnitude: string;
  if (minutes < 1) {
    magnitude = "less than a minute";
  } else if (minutes < 60) {
    magnitude = `${minutes}m`;
  } else if (hours < 24) {
    magnitude = `${hours}h`;
  } else {
    magnitude = `${days}d`;
  }

  return isPast ? `expired ${magnitude} ago` : `expires in ${magnitude}`;
}

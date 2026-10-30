export function parseJwtPayload(token: string): Record<string, unknown> {
  const segments = token.split(".");
  if (segments.length < 2) {
    throw new Error("Invalid JWT");
  }
  const base64 = segments[1].replace(/-/g, "+").replace(/_/g, "/");
  const json = atob(base64);
  return JSON.parse(json) as Record<string, unknown>;
}

export function claimString(payload: Record<string, unknown>, name: string): string | null {
  const value = payload[name];
  return typeof value === "string" ? value : null;
}

import type { LoginResponse, UserRole } from "../types";
import { apiRequest, ApiError } from "./client";

const LEGACY_LOGIN =
  (import.meta.env.VITE_LEGACY_LOGIN as string | undefined) === "true";

function keycloakUsername(role: UserRole, username: string): string {
  const lower = username.toLowerCase();
  if (
    lower.includes("provider") ||
    lower.includes("ngo") ||
    lower.includes("coordinator") ||
    lower.includes("admin")
  ) {
    return username;
  }
  if (role === "PROVIDER") {
    return "provider1";
  }
  if (role === "COORDINATOR") {
    return "coordinator1";
  }
  return "ngo1";
}

/**
 * Login via API gateway (proxies Keycloak password grant server-side).
 * Avoids browser → Keycloak CORS failures and keeps one API origin.
 */
export async function login(
  username: string,
  password: string,
  role: UserRole,
): Promise<LoginResponse> {
  const resolvedUsername = LEGACY_LOGIN ? username : keycloakUsername(role, username);

  try {
    return await apiRequest<LoginResponse>("/api/auth/login", {
      method: "POST",
      body: {
        username: resolvedUsername,
        password,
        role,
      },
    });
  } catch (err) {
    if (err instanceof ApiError) {
      throw new Error(err.message);
    }
    if (err instanceof Error) {
      throw err;
    }
    throw new Error("Login failed");
  }
}

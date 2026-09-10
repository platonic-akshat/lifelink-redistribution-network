import { apiRequest } from "./client";
import type { LoginRequest, LoginResponse } from "../types";

// Starter/demo auth flow: the gateway's stub auth endpoint accepts any
// non-blank username/password and issues a JWT. There is no real signup or
// credential verification here — swap for a real identity provider (see
// ARCHITECTURE.md, api-gateway notes) before production use.
export async function login(
  username: string,
  password: string,
): Promise<LoginResponse> {
  const body: LoginRequest = { username, password, role: "PROVIDER" };
  return apiRequest<LoginResponse>("/api/auth/login", {
    method: "POST",
    body,
  });
}

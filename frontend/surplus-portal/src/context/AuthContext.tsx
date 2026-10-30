import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  clearAuthToken,
  getAuthToken,
  setAuthToken as persistAuthToken,
} from "../api/client";
import { login as loginRequest } from "../api/auth";
import { claimString, parseJwtPayload } from "../api/jwt";
import type { UserRole } from "../types";

const PROVIDER_ID_KEY = "lifelink.providerId";
const PROVIDER_NAME_KEY = "lifelink.providerName";
const REQUESTER_ID_KEY = "lifelink.requesterId";
const REQUESTER_NAME_KEY = "lifelink.requesterName";
const COORDINATOR_ID_KEY = "lifelink.coordinatorId";
const ROLE_KEY = "lifelink.role";
const USER_SUB_KEY = "lifelink.userSub";

/** Dev Keycloak realm organization IDs (see infra/keycloak/lifelink-realm.json). */
export const DEV_PROVIDER_ORG_ID = "11111111-1111-1111-1111-111111111111";
export const DEV_NGO_ORG_ID = "22222222-2222-2222-2222-222222222222";

function getOrCreateId(key: string): string {
  let id = localStorage.getItem(key);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(key, id);
  }
  return id;
}

interface AuthContextValue {
  token: string | null;
  role: UserRole | null;
  isAuthenticated: boolean;
  providerId: string;
  providerName: string;
  requesterId: string;
  requesterName: string;
  coordinatorId: string;
  setProviderName: (name: string) => void;
  setRequesterName: (name: string) => void;
  login: (username: string, password: string, role: UserRole) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getAuthToken());
  const [role, setRole] = useState<UserRole | null>(() => {
    const stored = localStorage.getItem(ROLE_KEY);
    return stored === "PROVIDER" || stored === "NGO" || stored === "COORDINATOR" ? stored : null;
  });
  const [providerId, setProviderIdState] = useState<string>(
    () => localStorage.getItem(PROVIDER_ID_KEY) ?? DEV_PROVIDER_ORG_ID,
  );
  const [requesterId, setRequesterIdState] = useState<string>(
    () => localStorage.getItem(REQUESTER_ID_KEY) ?? DEV_NGO_ORG_ID,
  );
  const [coordinatorId, setCoordinatorIdState] = useState<string>(
    () => localStorage.getItem(COORDINATOR_ID_KEY) ?? getOrCreateId(COORDINATOR_ID_KEY),
  );
  const [providerName, setProviderNameState] = useState<string>(
    () => localStorage.getItem(PROVIDER_NAME_KEY) ?? "",
  );
  const [requesterName, setRequesterNameState] = useState<string>(
    () => localStorage.getItem(REQUESTER_NAME_KEY) ?? "",
  );

  const setProviderName = useCallback((name: string) => {
    localStorage.setItem(PROVIDER_NAME_KEY, name);
    setProviderNameState(name);
  }, []);

  const setRequesterName = useCallback((name: string) => {
    localStorage.setItem(REQUESTER_NAME_KEY, name);
    setRequesterNameState(name);
  }, []);

  const login = useCallback(
    async (username: string, password: string, loginRole: UserRole) => {
      const { token: newToken } = await loginRequest(username, password, loginRole);
      persistAuthToken(newToken);
      localStorage.setItem(ROLE_KEY, loginRole);

      const payload = parseJwtPayload(newToken);
      const orgId = claimString(payload, "organization_id");
      const sub = claimString(payload, "sub");
      if (orgId) {
        if (loginRole === "PROVIDER") {
          localStorage.setItem(PROVIDER_ID_KEY, orgId);
          setProviderIdState(orgId);
        } else {
          localStorage.setItem(REQUESTER_ID_KEY, orgId);
          setRequesterIdState(orgId);
        }
      }
      if (sub) {
        localStorage.setItem(USER_SUB_KEY, sub);
        localStorage.setItem(COORDINATOR_ID_KEY, sub);
        setCoordinatorIdState(sub);
      }

      setToken(newToken);
      setRole(loginRole);
    },
    [],
  );

  const logout = useCallback(() => {
    clearAuthToken();
    localStorage.removeItem(ROLE_KEY);
    setToken(null);
    setRole(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      role,
      isAuthenticated: token !== null,
      providerId,
      providerName,
      requesterId,
      requesterName,
      coordinatorId,
      setProviderName,
      setRequesterName,
      login,
      logout,
    }),
    [
      token,
      role,
      providerId,
      providerName,
      requesterId,
      requesterName,
      coordinatorId,
      setProviderName,
      setRequesterName,
      login,
      logout,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

export function homePathForRole(role: UserRole | null): string {
  if (role === "NGO") {
    return "/needs";
  }
  if (role === "COORDINATOR") {
    return "/matches";
  }
  return "/postings";
}

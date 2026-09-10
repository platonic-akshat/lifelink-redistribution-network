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

const PROVIDER_ID_KEY = "lifelink.providerId";
const PROVIDER_NAME_KEY = "lifelink.providerName";

function getOrCreateProviderId(): string {
  let id = localStorage.getItem(PROVIDER_ID_KEY);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(PROVIDER_ID_KEY, id);
  }
  return id;
}

interface AuthContextValue {
  token: string | null;
  isAuthenticated: boolean;
  providerId: string;
  providerName: string;
  setProviderName: (name: string) => void;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getAuthToken());
  const [providerId] = useState<string>(() => getOrCreateProviderId());
  const [providerName, setProviderNameState] = useState<string>(
    () => localStorage.getItem(PROVIDER_NAME_KEY) ?? "",
  );

  const setProviderName = useCallback((name: string) => {
    localStorage.setItem(PROVIDER_NAME_KEY, name);
    setProviderNameState(name);
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const { token: newToken } = await loginRequest(username, password);
    persistAuthToken(newToken);
    setToken(newToken);
  }, []);

  const logout = useCallback(() => {
    clearAuthToken();
    setToken(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      isAuthenticated: token !== null,
      providerId,
      providerName,
      setProviderName,
      login,
      logout,
    }),
    [token, providerId, providerName, setProviderName, login, logout],
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

import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth, homePathForRole } from "../context/AuthContext";
import { ApiError } from "../api/client";
import type { UserRole } from "../types";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("PROVIDER");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!username.trim() || !password.trim()) {
      setError("Username and password are both required.");
      return;
    }

    setSubmitting(true);
    try {
      await login(username.trim(), password.trim(), role);
      navigate(homePathForRole(role), { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Login failed.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-page">
      <aside className="login-hero">
        <div className="login-hero-inner">
          <p className="brand-wordmark">LifeLink</p>
          <p className="tagline">
            Connect hospitals, blood banks, and food donors with NGOs and patients
            in need — in near real time, before good surplus goes to waste.
          </p>
          <div className="login-stats">
            <div className="login-stat">
              <strong>Medicine · Blood · Food</strong>
              <span>One coordination network</span>
            </div>
            <div className="login-stat">
              <strong>Match → Approve → Deliver</strong>
              <span>Full audit trail</span>
            </div>
          </div>
        </div>
      </aside>
      <main className="login-panel">
        <div className="login-card-wrap">
          <h2>Welcome back</h2>
          <p className="login-hint">
            Sign in through the API gateway (port 8080). Use{" "}
            <code>provider1</code>, <code>ngo1</code>, or <code>coordinator1</code> with the same
            password. Requires Keycloak from <code>docker compose up -d</code> and a running{" "}
            <code>api-gateway</code>.
          </p>
          <form className="card" onSubmit={handleSubmit}>
            <label className="field">
              <span>I am signing in as</span>
              <select value={role} onChange={(e) => setRole(e.target.value as UserRole)}>
                <option value="PROVIDER">Provider / donor</option>
                <option value="NGO">NGO (needs only)</option>
                <option value="COORDINATOR">Coordinator (matches & logistics)</option>
              </select>
            </label>
            <label className="field">
              <span>Username</span>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. provider1"
                autoComplete="username"
                autoFocus
              />
            </label>
            <label className="field">
              <span>Password</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            {error && <p className="error">{error}</p>}
            <button type="submit" disabled={submitting}>
              {submitting ? "Signing in…" : "Sign in to LifeLink"}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

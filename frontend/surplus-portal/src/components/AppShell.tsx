import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { UserRole } from "../types";

interface NavItem {
  to: string;
  label: string;
  roles: UserRole[];
}

const NAV: NavItem[] = [
  { to: "/postings", label: "My surplus", roles: ["PROVIDER"] },
  { to: "/postings/new", label: "Create surplus", roles: ["PROVIDER"] },
  { to: "/explore", label: "Explore nearby", roles: ["PROVIDER", "NGO", "COORDINATOR"] },
  { to: "/needs", label: "Needs", roles: ["NGO"] },
  { to: "/needs/new", label: "Create need", roles: ["NGO"] },
  { to: "/matches", label: "Match review", roles: ["COORDINATOR"] },
  { to: "/logistics", label: "Logistics", roles: ["COORDINATOR"] },
];

const ROLE_LABEL: Record<UserRole, string> = {
  PROVIDER: "Provider",
  NGO: "NGO",
  COORDINATOR: "Coordinator",
};

export default function AppShell() {
  const { role, providerName, requesterName, logout } = useAuth();
  const orgLabel =
    role === "NGO" || role === "COORDINATOR"
      ? requesterName || "Your organization"
      : providerName || "Your organization";

  const visibleNav = NAV.filter((item) => role && item.roles.includes(role));

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand-row">
          <div className="brand-icon" aria-hidden>
            LL
          </div>
          <span className="sidebar-brand">LifeLink</span>
        </div>
        <p className="sidebar-tagline">Redirect surplus before it expires</p>
        {role && <span className="role-chip">{ROLE_LABEL[role]}</span>}
        <nav className="sidebar-nav" aria-label="Main">
          {visibleNav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive ? "nav-link nav-link-active" : "nav-link"
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <p className="sidebar-footer">Surplus → need network</p>
      </aside>
      <div className="shell-main">
        <header className="topbar">
          <div>
            <span className="topbar-org-label">Signed in as</span>
            <span className="topbar-org">{orgLabel}</span>
          </div>
          <button type="button" className="link-button" onClick={logout}>
            Sign out
          </button>
        </header>
        <main className="shell-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

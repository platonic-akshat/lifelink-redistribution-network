import { Navigate, Route, Routes } from "react-router-dom";
import "./App.css";
import { AuthProvider, homePathForRole, useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AppShell from "./components/AppShell";
import LoginPage from "./pages/LoginPage";
import PostingsPage from "./pages/PostingsPage";
import NewPostingPage from "./pages/NewPostingPage";
import NeedsPage from "./pages/NeedsPage";
import NewNeedPage from "./pages/NewNeedPage";
import MatchesPage from "./pages/MatchesPage";
import LogisticsPage from "./pages/LogisticsPage";
import ExplorePage from "./pages/ExplorePage";

function HomeRedirect() {
  const { role, isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <Navigate to={homePathForRole(role)} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<HomeRedirect />} />
          <Route
            path="/postings"
            element={
              <ProtectedRoute allowedRoles={["PROVIDER"]}>
                <PostingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/postings/new"
            element={
              <ProtectedRoute allowedRoles={["PROVIDER"]}>
                <NewPostingPage />
              </ProtectedRoute>
            }
          />
          <Route path="/explore" element={<ExplorePage />} />
          <Route
            path="/needs"
            element={
              <ProtectedRoute allowedRoles={["NGO"]}>
                <NeedsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/needs/new"
            element={
              <ProtectedRoute allowedRoles={["NGO"]}>
                <NewNeedPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/matches"
            element={
              <ProtectedRoute allowedRoles={["COORDINATOR"]}>
                <MatchesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/logistics"
            element={
              <ProtectedRoute allowedRoles={["COORDINATOR"]}>
                <LogisticsPage />
              </ProtectedRoute>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
}

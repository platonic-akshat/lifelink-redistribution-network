import { Navigate, Route, Routes } from "react-router-dom";
import "./App.css";
import { useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import PostingsPage from "./pages/PostingsPage";
import NewPostingPage from "./pages/NewPostingPage";

function NavBar() {
  const { isAuthenticated, logout } = useAuth();
  if (!isAuthenticated) {
    return null;
  }
  return (
    <nav className="navbar">
      <span className="brand">LifeLink Surplus Portal</span>
      <button className="link-button" onClick={logout}>
        Log out
      </button>
    </nav>
  );
}

export default function App() {
  return (
    <>
      <NavBar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/postings"
          element={
            <ProtectedRoute>
              <PostingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/postings/new"
          element={
            <ProtectedRoute>
              <NewPostingPage />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/postings" replace />} />
        <Route path="*" element={<Navigate to="/postings" replace />} />
      </Routes>
    </>
  );
}

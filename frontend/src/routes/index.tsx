import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { LoginPage } from "../pages/LoginPage";
import { RegisterPage } from "../pages/RegisterPage";
import { AnamnesisPage } from "../pages/AnamnesisPage";
import { LandingPage } from "../pages/LandingPage";
import { VaccinesPage } from "../pages/VaccinesPage";
import { useAuth } from "../context/AuthContext";
import type { ReactNode } from "react";

function PrivateRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

export function AppRoutes() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
        <Route
          path="/anamnese"
          element={
            <PrivateRoute>
              <AnamnesisPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/vacinas"
          element={
            <PrivateRoute>
              <VaccinesPage />
            </PrivateRoute>
          }
        />
      </Routes>
    </Router>
  );
}

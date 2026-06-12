import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { LoginPage } from "../pages/LoginPage";
import { RegisterPage } from "../pages/RegisterPage";
import { AnamnesisPage } from "../pages/AnamnesisPage";
import { LandingPage } from "../pages/LandingPage";
import { VaccinesPage } from "../pages/VaccinesPage";
import { ConsultasPage } from "../pages/ConsultasPage";
import { ReportsPage } from "../pages/ReportsPage";
import { SharingPage } from "../pages/SharingPage";
import { DoctorPatientsPage } from "../pages/DoctorPatientsPage";
import { DashboardPage } from "../pages/DashboardPage";
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
          path="/dashboard"
          element={
            <PrivateRoute>
              <DashboardPage />
            </PrivateRoute>
          }
        />
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
        <Route
          path="/consultas"
          element={
            <PrivateRoute>
              <ConsultasPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/relatorios"
          element={
            <PrivateRoute>
              <ReportsPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/compartilhamento"
          element={
            <PrivateRoute>
              <SharingPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/meus-pacientes"
          element={
            <PrivateRoute>
              <DoctorPatientsPage />
            </PrivateRoute>
          }
        />
      </Routes>
    </Router>
  );
}

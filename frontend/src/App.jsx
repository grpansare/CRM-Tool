import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import LandingPage from "./pages/LandingPage.jsx";
import TenantRegistration from "./pages/TenantRegistration.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import AcceptInvitation from "./pages/AcceptInvitation.jsx";
import { AuthProvider } from "./contexts/AuthContext.jsx";

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/register" element={<TenantRegistration />} />
            <Route path="/login" element={<Login />} />
            <Route path="/join" element={<AcceptInvitation />} />
            <Route path="/dashboard/*" element={<Dashboard />} />
          </Routes>
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: "#363636",
                color: "#fff",
              },
            }}
          />
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;

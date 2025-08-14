import React, { createContext, useContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import api from "../services/api.js";

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(localStorage.getItem("token"));
  const navigate = useNavigate();

  useEffect(() => {
    if (token) {
      validateToken();
    } else {
      setLoading(false);
    }
  }, [token]);

  const validateToken = async () => {
    try {
      const response = await api.post("/auth/validate", null, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.data.success) {
        const userResponse = await api.get("/auth/me", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUser(userResponse.data.data);
      } else {
        logout();
      }
    } catch (error) {
      console.error("Token validation failed:", error);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      const response = await api.post("/auth/login", {
        email,
        password,
        rememberMe: true, // Assuming rememberMe is always true for simplicity
      });

      if (response.data.success) {
        const { accessToken, user: userData } = response.data.data;
        setToken(accessToken);
        setUser(userData);
        console.log(user);

        localStorage.setItem("token", accessToken);
        alert(userData.email);
        toast.success("Login successful!");
        navigate("/dashboard");
        return { success: true };
      } else {
        console.error("Login failed:", response.data.message);
        toast.error(response.data.message || "Login failed");
        return { success: false, message: response.data.message };
      }
    } catch (error) {
      console.error("Login error:", error);

      toast.error("Login failed. Please try again.");
      alert("njskfk");
      return { success: false, message: "Login failed" };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("token");
    navigate("/");
    toast.success("Logged out successfully");
  };

  const registerTenant = async (registrationData) => {
    console.log("Starting tenant registration with data:", registrationData);

    try {
      console.log("Making API call to /api/v1/tenants/register");
      const response = await api.post("/tenants/register", registrationData, {
        headers: {
          "Content-Type": "application/json",
        },
      });

      console.log("Registration response received:", response);
      console.log("Registration response data:", response.data);

      if (response.data && response.data.success) {
        console.log("Registration successful, navigating to login");
        toast.success(
          "Registration successful! Please check your email to verify your account."
        );

        return { success: true };
      } else {
        console.log("Registration failed with response:", response.data);
        toast.error(response.data?.message || "Registration failed");
        return { success: false, message: response.data?.message };
      }
    } catch (error) {
      console.error("Registration error caught:", error);
      console.error("Error details:", {
        message: error.message,
        response: error.response,
        status: error.response?.status,
        data: error.response?.data,
      });

      const message =
        error.response?.data?.message ||
        "Registration failed. Please try again.";
      toast.error(message);
      return { success: false, message };
    }
  };

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    registerTenant,
    isAuthenticated: !!user && !!token,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

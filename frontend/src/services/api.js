import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api/v1/",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    console.log("=== API REQUEST DEBUG ===");
    console.log("Request URL:", config.url);
    console.log("Request Method:", config.method);
    console.log("Token exists:", !!token);
    console.log("Token length:", token ? token.length : 0);
    console.log("Request headers before:", config.headers);
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log("Added Authorization header:", config.headers.Authorization?.substring(0, 20) + "...");
    } else {
      console.log("No token found - request will be unauthenticated");
    }
    
    console.log("Final request headers:", config.headers);
    return config;
  },
  (error) => {
    console.error("Request interceptor error:", error);
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => {
    console.log("=== API RESPONSE DEBUG ===");
    console.log("Response URL:", response.config?.url);
    console.log("Response Status:", response.status);
    console.log("Response Data:", response.data);
    console.log("Response Headers:", response.headers);
    return response;
  },
  (error) => {
    console.log("=== API ERROR DEBUG ===");
    console.log("Error URL:", error.config?.url);
    console.log("Error Method:", error.config?.method);
    console.log("Error Status:", error.response?.status);
    console.log("Error Status Text:", error.response?.statusText);
    console.log("Error Data:", error.response?.data);
    console.log("Request Data:", error.config?.data);
    console.log("Error Headers:", error.response?.headers);
    console.log("Request Headers:", error.config?.headers);
    
    // Log 401 errors for debugging
    if (error.response?.status === 401) {
      console.log("=== 401 UNAUTHORIZED ANALYSIS ===");
      console.log("URL that failed:", error.config?.url);
      console.log("Authorization header sent:", error.config?.headers?.Authorization?.substring(0, 30) + "...");
      console.log("Response body:", error.response?.data);
      console.log("Response headers:", error.response?.headers);
    }
    
    // Only redirect on 401 if it's not a token validation request
    // Let AuthContext handle token validation failures gracefully
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/validate')) {
     console.log("401 Unauthorized - TEMPORARILY NOT REDIRECTING FOR DEBUG");
     console.log("Check backend console for JWT filter debug logs");
     // Temporarily disabled for debugging
     // localStorage.removeItem("token");
     // window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;

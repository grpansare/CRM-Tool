import React, { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import api from "../services/api.js";
import toast from "react-hot-toast";

const AcceptInvitation = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const [token, setToken] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  useEffect(() => {
    const t = params.get("token");
    if (t) setToken(t);
  }, [params]);

  const submit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post("tenants/accept-invitation", {
        token,
        firstName,
        lastName,
        username,
        password,
      });
      if (res.data?.success) {
        toast.success("Invitation accepted. You can now log in.");
        navigate("/login");
      } else {
        toast.error(res.data?.message || "Failed to accept invitation");
      }
    } catch (e) {
      toast.error("Failed to accept invitation");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <form
        onSubmit={submit}
        className="bg-white shadow rounded p-6 w-full max-w-md space-y-4"
      >
        <h1 className="text-xl font-semibold">Accept Invitation</h1>
        <input
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          placeholder="First name"
          className="border px-3 py-2 rounded w-full"
          required
        />
        <input
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          placeholder="Last name"
          className="border px-3 py-2 rounded w-full"
          required
        />
        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Username"
          className="border px-3 py-2 rounded w-full"
          required
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          className="border px-3 py-2 rounded w-full"
          required
        />
        <button
          type="submit"
          className="w-full bg-primary-600 text-white py-2 rounded"
        >
          Create Account
        </button>
      </form>
    </div>
  );
};

export default AcceptInvitation;

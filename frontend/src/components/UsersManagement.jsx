import React, { useEffect, useState } from "react";
import api from "../services/api.js";

const UsersManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState("SALES_REP");
  const [pending, setPending] = useState([]);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const res = await api.get("tenant-admin/users", {
          params: { page: 0, size: 50 },
        });
        if (res.data?.success && Array.isArray(res.data?.data)) {
          setUsers(res.data.data);
        }
      } catch (e) {
        // ignore
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
    const fetchPending = async () => {
      try {
        const res = await api.get("tenant-admin/invitations/pending");
        if (res.data?.success && Array.isArray(res.data?.data)) {
          setPending(res.data.data);
        }
      } catch {}
    };
    fetchPending();
  }, []);

  const invite = async () => {
    if (!inviteEmail) return;
    try {
      await api.post("tenant-admin/users/invite", {
        email: inviteEmail,
        role: inviteRole,
      });
      setInviteEmail("");
      const res = await api.get("tenant-admin/invitations/pending");
      if (res.data?.success && Array.isArray(res.data?.data))
        setPending(res.data.data);
    } catch {}
  };

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">
        Users Management
      </h1>
      <div className="card">
        {loading ? (
          <p className="text-gray-600">Loading users...</p>
        ) : users.length === 0 ? (
          <p className="text-gray-600">No users found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Email
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Role
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {users.map((u) => (
                  <tr key={u.userId}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {u.firstName} {u.lastName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">{u.email}</td>
                    <td className="px-6 py-4 whitespace-nowrap">{u.role}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {u.isActive ? "Active" : "Inactive"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-8">
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Invite User
          </h2>
          <div className="flex items-center space-x-3">
            <input
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
              placeholder="email@example.com"
              className="border px-3 py-2 rounded w-full"
            />
            <select
              value={inviteRole}
              onChange={(e) => setInviteRole(e.target.value)}
              className="border px-3 py-2 rounded"
            >
              <option value="SALES_MANAGER">SALES_MANAGER</option>
              <option value="SALES_REP">SALES_REP</option>
              <option value="SUPPORT_AGENT">SUPPORT_AGENT</option>
              <option value="READ_ONLY">READ_ONLY</option>
            </select>
            <button
              onClick={invite}
              className="px-4 py-2 bg-primary-600 text-white rounded"
            >
              Send Invite
            </button>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Pending Invitations
          </h2>
          {pending.length === 0 ? (
            <p className="text-gray-600">No pending invitations.</p>
          ) : (
            <ul className="divide-y">
              {pending.map((p) => (
                <li
                  key={p.invitationId}
                  className="py-3 flex items-center justify-between"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {p.email}
                    </p>
                    <p className="text-xs text-gray-500">
                      {p.role} • Expires {p.expiresAt}
                    </p>
                  </div>
                  <div className="space-x-2">
                    <button
                      onClick={async () => {
                        await api.post(
                          `tenant-admin/invitations/${p.invitationId}/resend`
                        );
                        const r = await api.get(
                          "tenant-admin/invitations/pending"
                        );
                        if (r.data?.success) setPending(r.data.data);
                      }}
                      className="px-3 py-1 border rounded"
                    >
                      Resend
                    </button>
                    <button
                      onClick={async () => {
                        await api.delete(
                          `tenant-admin/invitations/${p.invitationId}`
                        );
                        const r = await api.get(
                          "tenant-admin/invitations/pending"
                        );
                        if (r.data?.success) setPending(r.data.data);
                      }}
                      className="px-3 py-1 border rounded text-red-600"
                    >
                      Cancel
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

export default UsersManagement;

import React, { useState, useEffect } from 'react';
import { X, Mail, User, Shield } from 'lucide-react';
import api from '../../services/api';
import toast from 'react-hot-toast';

const InviteUserModal = ({ isOpen, onClose, onSuccess }) => {
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState("SALES_REP");
  const [inviteManagerId, setInviteManagerId] = useState("");
  const [availableManagers, setAvailableManagers] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      fetchManagers();
    }
  }, [isOpen]);

  const fetchManagers = async () => {
    try {
      const res = await api.get("tenant-admin/managers");
      if (res.data?.success && Array.isArray(res.data?.data)) {
        setAvailableManagers(res.data.data);
      }
    } catch (e) {
      console.error("Failed to fetch managers:", e);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!inviteEmail) {
      toast.error("Please enter an email address");
      return;
    }
    
    // Validate manager selection for SALES_REP
    if (inviteRole === "SALES_REP" && !inviteManagerId) {
      toast.error("Please select a manager for the sales rep");
      return;
    }
    
    setLoading(true);

    try {
      const payload = {
        email: inviteEmail,
        role: inviteRole,
      };
      
      // Add managerId only for SALES_REP role
      if (inviteRole === "SALES_REP" && inviteManagerId) {
        payload.managerId = parseInt(inviteManagerId);
      }
      
      await api.post("tenant-admin/users/invite", payload);
      
      toast.success("User invitation sent successfully!");
      
      // Reset form
      setInviteEmail("");
      setInviteRole("SALES_REP");
      setInviteManagerId("");
      
      onSuccess && onSuccess();
      onClose();
    } catch (error) {
      console.error("Failed to send invitation:", error);
      const errorMessage = error.response?.data?.message || "Failed to send invitation. Please try again.";
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = (e) => {
    setInviteRole(e.target.value);
    if (e.target.value !== "SALES_REP") {
      setInviteManagerId("");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Invite New User</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
              Email Address *
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              <input
                type="email"
                id="email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                required
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="user@example.com"
              />
            </div>
          </div>

          <div>
            <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-1">
              Role *
            </label>
            <div className="relative">
              <Shield className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              <select
                id="role"
                value={inviteRole}
                onChange={handleRoleChange}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="SALES_MANAGER">Sales Manager</option>
                <option value="SALES_REP">Sales Rep</option>
                <option value="SUPPORT_AGENT">Support Agent</option>
                <option value="READ_ONLY">Read Only</option>
              </select>
            </div>
          </div>

          {inviteRole === "SALES_REP" && (
            <div>
              <label htmlFor="manager" className="block text-sm font-medium text-gray-700 mb-1">
                Assign Manager *
              </label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                <select
                  id="manager"
                  value={inviteManagerId}
                  onChange={(e) => setInviteManagerId(e.target.value)}
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                >
                  <option value="">Select Manager</option>
                  {availableManagers.map((manager) => (
                    <option key={manager.userId} value={manager.userId}>
                      {manager.firstName} {manager.lastName} ({manager.email})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "Sending..." : "Send Invite"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default InviteUserModal;

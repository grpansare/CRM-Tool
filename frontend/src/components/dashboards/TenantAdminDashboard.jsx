import React, { useState, useEffect } from "react";
import api from "../../services/api.js";
import {
  Users,
  BarChart3,
  Building,
  Settings,
  DollarSign,
  TrendingUp,
  UserPlus,
  Activity,
} from "lucide-react";

const TenantAdminDashboard = () => {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalDeals: 0,
    totalContacts: 0,
    totalRevenue: 0,
    activeUsers: 0,
    pendingInvitations: 0,
  });
  const [activity, setActivity] = useState([]);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await api.get("tenant-admin/dashboard/stats");
        if (res.data?.success && res.data?.data) {
          const d = res.data.data;
          setStats({
            totalUsers: d.totalUsers || 0,
            totalDeals: d.totalDeals || 0,
            totalContacts: d.totalContacts || 0,
            totalRevenue: Number(d.totalRevenue || 0),
            activeUsers: d.activeUsers || 0,
            pendingInvitations: d.pendingInvitations || 0,
          });
        }
      } catch (e) {
        // keep defaults on error
      }
    };
    fetchStats();
    const fetchActivity = async () => {
      try {
        const res = await api.get("tenant-admin/dashboard/recent-activity", {
          params: { limit: 5 },
        });
        if (res.data?.success && Array.isArray(res.data?.data)) {
          setActivity(res.data.data);
        }
      } catch (e) {
        // ignore
      }
    };
    fetchActivity();
  }, []);

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Organization Dashboard
        </h1>
        <p className="text-gray-600">
          Manage your organization and monitor performance
        </p>
      </div>

      {/* Organization Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <div className="bg-blue-100 p-3 rounded-lg">
              <Users className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Users</p>
              <p className="text-2xl font-bold text-gray-900">
                {stats.totalUsers}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-green-100 p-3 rounded-lg">
              <BarChart3 className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Active Deals</p>
              <p className="text-2xl font-bold text-gray-900">
                {stats.totalDeals}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-purple-100 p-3 rounded-lg">
              <DollarSign className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Revenue</p>
              <p className="text-2xl font-bold text-gray-900">
                ${(stats.totalRevenue / 1000).toFixed(0)}K
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-yellow-100 p-3 rounded-lg">
              <Activity className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Active Users</p>
              <p className="text-2xl font-bold text-gray-900">
                {stats.activeUsers}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Quick Actions
          </h2>
          <div className="space-y-3">
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <UserPlus className="h-5 w-5 text-blue-600 mr-3" />
              <span>Invite New User</span>
            </button>
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <Settings className="h-5 w-5 text-gray-600 mr-3" />
              <span>Organization Settings</span>
            </button>
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <DollarSign className="h-5 w-5 text-green-600 mr-3" />
              <span>Billing & Subscription</span>
            </button>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Recent Activity
          </h2>
          <div className="space-y-4">
            {activity.length === 0 && (
              <p className="text-sm text-gray-500">No recent activity.</p>
            )}
            {activity.map((item, idx) => (
              <div key={idx} className="flex items-start space-x-3">
                <div
                  className={`h-2 w-2 rounded-full mt-2 ${
                    item.type === "DEAL_WON"
                      ? "bg-green-600"
                      : item.type === "USER_JOINED"
                      ? "bg-blue-600"
                      : "bg-yellow-600"
                  }`}
                ></div>
                <div>
                  <p className="text-sm text-gray-900">{item.message}</p>
                  <p className="text-xs text-gray-500">{item.timestamp}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default TenantAdminDashboard;

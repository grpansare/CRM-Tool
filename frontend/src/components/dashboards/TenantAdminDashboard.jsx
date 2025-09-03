import React, { useState, useEffect } from "react";
import api from "../../services/api.js";
import toast from "react-hot-toast";
import { useAuth } from "../../contexts/AuthContext";
import {
  Users,
  BarChart3,
  Building,
  Settings,
  DollarSign,
  TrendingUp,
  UserPlus,
  Activity,
  AlertCircle,
  RefreshCw,
} from "lucide-react";

const TenantAdminDashboard = () => {
  const { user, token } = useAuth();
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalDeals: 0,
    totalContacts: 0,
    totalRevenue: 0,
    activeUsers: 0,
    pendingInvitations: 0,
  });
  const [activity, setActivity] = useState([]);
  const [loading, setLoading] = useState({ stats: true, activity: true });
  const [error, setError] = useState({ stats: null, activity: null });

  const fetchStats = async () => {
    try {
      setLoading(prev => ({ ...prev, stats: true }));
      setError(prev => ({ ...prev, stats: null }));
      
      console.log("Fetching tenant admin dashboard stats...");
      
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
      } else {
        throw new Error(res.data?.message || "Failed to fetch dashboard stats");
      }
    } catch (e) {
      console.error("Error fetching dashboard stats:", e);
      const errorMessage = e.response?.data?.message || e.message || "Failed to load dashboard statistics";
      setError(prev => ({ ...prev, stats: errorMessage }));
      toast.error("Failed to load dashboard statistics");
    } finally {
      setLoading(prev => ({ ...prev, stats: false }));
    }
  };

  const fetchActivity = async () => {
    try {
      setLoading(prev => ({ ...prev, activity: true }));
      setError(prev => ({ ...prev, activity: null }));
      
      const res = await api.get("tenant-admin/dashboard/recent-activity", {
        params: { limit: 5 },
      });
      if (res.data?.success && Array.isArray(res.data?.data)) {
        setActivity(res.data.data);
      } else {
        throw new Error(res.data?.message || "Failed to fetch recent activity");
      }
    } catch (e) {
      console.error("Error fetching recent activity:", e);
      const errorMessage = e.response?.data?.message || e.message || "Failed to load recent activity";
      setError(prev => ({ ...prev, activity: errorMessage }));
      toast.error("Failed to load recent activity");
    } finally {
      setLoading(prev => ({ ...prev, activity: false }));
    }
  };

  const handleRetry = (type) => {
    if (type === 'stats') {
      fetchStats();
    } else if (type === 'activity') {
      fetchActivity();
    } else {
      fetchStats();
      fetchActivity();
    }
  };

  useEffect(() => {
    fetchStats();
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
      {error.stats ? (
        <div className="mb-8 p-6 bg-red-50 border border-red-200 rounded-lg">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <AlertCircle className="h-5 w-5 text-red-600 mr-2" />
              <div>
                <h3 className="text-sm font-medium text-red-800">Failed to load statistics</h3>
                <p className="text-sm text-red-600 mt-1">{error.stats}</p>
              </div>
            </div>
            <button
              onClick={() => handleRetry('stats')}
              disabled={loading.stats}
              className="flex items-center px-3 py-2 text-sm bg-red-100 text-red-700 rounded-md hover:bg-red-200 disabled:opacity-50"
            >
              <RefreshCw className={`h-4 w-4 mr-1 ${loading.stats ? 'animate-spin' : ''}`} />
              {loading.stats ? 'Loading...' : 'Retry'}
            </button>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="card">
            <div className="flex items-center">
              <div className="bg-blue-100 p-3 rounded-lg">
                <Users className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Users</p>
                <p className="text-2xl font-bold text-gray-900">
                  {loading.stats ? (
                    <div className="animate-pulse bg-gray-200 h-8 w-16 rounded"></div>
                  ) : (
                    stats.totalUsers
                  )}
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
                <p className="text-sm font-medium text-gray-600">Total Deals</p>
                <p className="text-2xl font-bold text-gray-900">
                  {loading.stats ? (
                    <div className="animate-pulse bg-gray-200 h-8 w-16 rounded"></div>
                  ) : (
                    stats.totalDeals
                  )}
                </p>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center">
              <div className="bg-yellow-100 p-3 rounded-lg">
                <DollarSign className="h-6 w-6 text-yellow-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Revenue</p>
                <p className="text-2xl font-bold text-gray-900">
                  {loading.stats ? (
                    <div className="animate-pulse bg-gray-200 h-8 w-20 rounded"></div>
                  ) : (
                    `$${stats.totalRevenue.toLocaleString()}`
                  )}
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
                  {loading.stats ? (
                    <div className="animate-pulse bg-gray-200 h-8 w-16 rounded"></div>
                  ) : (
                    stats.activeUsers
                  )}
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

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
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">
              Recent Activity
            </h2>
            {error.activity && (
              <button
                onClick={() => handleRetry('activity')}
                disabled={loading.activity}
                className="flex items-center px-2 py-1 text-xs bg-red-100 text-red-700 rounded hover:bg-red-200 disabled:opacity-50"
              >
                <RefreshCw className={`h-3 w-3 mr-1 ${loading.activity ? 'animate-spin' : ''}`} />
                {loading.activity ? 'Loading...' : 'Retry'}
              </button>
            )}
          </div>
          
          {error.activity ? (
            <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
              <div className="flex items-center">
                <AlertCircle className="h-4 w-4 text-red-600 mr-2" />
                <div>
                  <p className="text-sm font-medium text-red-800">Failed to load activity</p>
                  <p className="text-xs text-red-600 mt-1">{error.activity}</p>
                </div>
              </div>
            </div>
          ) : loading.activity ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, idx) => (
                <div key={idx} className="flex items-start space-x-3">
                  <div className="animate-pulse bg-gray-200 h-2 w-2 rounded-full mt-2"></div>
                  <div className="flex-1">
                    <div className="animate-pulse bg-gray-200 h-4 w-3/4 rounded mb-1"></div>
                    <div className="animate-pulse bg-gray-200 h-3 w-1/2 rounded"></div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="space-y-4">
              {activity.length === 0 ? (
                <p className="text-sm text-gray-500">No recent activity.</p>
              ) : (
                activity.map((item, idx) => (
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
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TenantAdminDashboard;

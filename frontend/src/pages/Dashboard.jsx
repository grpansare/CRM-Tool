import React from "react";
import { Routes, Route, Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext.jsx";
import {
  Users,
  BarChart3,
  Building,
  Settings,
  LogOut,
  Bell,
  Search,
  TrendingUp,
  DollarSign,
  UserPlus,
  Calendar,
  GitBranch,
  User,
  Plus,
} from "lucide-react";
import Contacts from "../components/Contacts.jsx";
import Deals from "../components/Deals.jsx";
import Accounts from "../components/Accounts.jsx";
import UsersManagement from "../components/UsersManagement.jsx";
import Pipeline from "../components/Pipeline.jsx";
import Settings1 from "../components/Settings.jsx";
import TenantAdminDashboard from "../components/dashboards/TenantAdminDashboard.jsx";
import SalesManagerDashboard from "../components/dashboards/SalesManagerDashboard.jsx";
import SalesRepDashboard from "../components/dashboards/SalesRepDashboard.jsx";

const Dashboard = () => {
  const { user, logout, loading } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
  };

  // Show loading spinner while authentication is being validated
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  // Only redirect after loading is complete and user is still null
  if (!loading && !user) {
    alert("wtf")
    navigate("/login");
    return null;
  }

  console.log(user?.email);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navigation */}
      <nav className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <div className="w-8 h-8 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg mr-3 flex items-center justify-center">
                <div className="w-3 h-3 bg-white rounded-sm"></div>
              </div>
              <h1 className="text-2xl font-bold text-blue-600">
                CRMFlow
              </h1>
            </div>

            <div className="flex items-center space-x-4">
              <button className="p-2 text-gray-400 hover:text-gray-600">
                <Search className="h-5 w-5" />
              </button>
              <button className="p-2 text-gray-400 hover:text-gray-600 relative">
                <Bell className="h-5 w-5" />
                <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-400"></span>
              </button>

              <div className="flex items-center space-x-3">
                <div className="text-right">
                  <p className="text-sm font-medium text-gray-900">
                    {user.firstName} {user.lastName}
                  </p>
                  <p className="text-xs text-gray-500">{user.role}</p>
                </div>
                <div className="h-8 w-8 bg-primary-100 rounded-full flex items-center justify-center">
                  <User className="h-4 w-4 text-primary-600" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <div className="flex">
        {/* Sidebar */}
        <div className="w-64 bg-white shadow-sm min-h-screen">
          <nav className="mt-8">
            <div className="px-4 space-y-2">
              <Link
                to="/dashboard"
                className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
              >
                <BarChart3 className="h-5 w-5 mr-3" />
                Dashboard
              </Link>

              <Link
                to="/dashboard/contacts"
                className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
              >
                <Users className="h-5 w-5 mr-3" />
                Contacts
              </Link>

              <Link
                to="/dashboard/deals"
                className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
              >
                <BarChart3 className="h-5 w-5 mr-3" />
                Deals
              </Link>

              <Link
                to="/dashboard/pipeline"
                className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
              >
                <GitBranch className="h-5 w-5 mr-3" />
                Pipeline
              </Link>

              <Link
                to="/dashboard/accounts"
                className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
              >
                <Building className="h-5 w-5 mr-3" />
                Accounts
              </Link>

              {(user.role === "TENANT_ADMIN" ||
                user.role === "SUPER_ADMIN") && (
                <Link
                  to="/dashboard/users"
                  className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
                >
                  <Users className="h-5 w-5 mr-3" />
                  Users
                </Link>
              )}

              <Link
                to="/dashboard/settings"
                className="flex items-center px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-600 rounded-lg"
              >
                <Settings className="h-5 w-5 mr-3" />
                Settings
              </Link>
            </div>
          </nav>

          <div className="absolute bottom-8 left-4 right-4">
            <button
              onClick={handleLogout}
              className="flex items-center w-full px-4 py-2 text-gray-700 hover:bg-red-50 hover:text-red-600 rounded-lg"
            >
              <LogOut className="h-5 w-5 mr-3" />
              Sign Out
            </button>
          </div>
        </div>

        {/* Main Content */}
        <div className="flex-1 p-8">
          <Routes>
            <Route path="/" element={<RoleBasedDashboard />} />
            <Route path="/contacts" element={<Contacts />} />
            <Route path="/deals" element={<Deals />} />
            <Route path="/pipeline" element={<Pipeline />} />
            <Route path="/accounts" element={<Accounts />} />
            <Route path="/users" element={<UsersManagement />} />
            <Route path="/settings" element={<Settings1 />} />
          </Routes>
        </div>
      </div>
    </div>
  );
};

const RoleBasedDashboard = () => {
  const { user } = useAuth();

  // Render different dashboards based on user role
  if (user.role === "TENANT_ADMIN" || user.role === "SUPER_ADMIN") {
    return <TenantAdminDashboard />;
  } else if (user.role === "SALES_MANAGER") {
    return <SalesManagerDashboard />;
  } else if (user.role === "SALES_REP") {
    return <SalesRepDashboard />;
  } else {
    return <DashboardOverview />; // Fallback for SUPPORT_AGENT, READ_ONLY, etc.
  }
};

const DashboardOverview = () => {
  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600">Welcome to your CRM workspace</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <div className="bg-primary-100 p-3 rounded-lg">
              <Users className="h-6 w-6 text-primary-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">
                Total Contacts
              </p>
              <p className="text-2xl font-bold text-gray-900">1,234</p>
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
              <p className="text-2xl font-bold text-gray-900">56</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-yellow-100 p-3 rounded-lg">
              <Building className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Accounts</p>
              <p className="text-2xl font-bold text-gray-900">89</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-purple-100 p-3 rounded-lg">
              <BarChart3 className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Revenue</p>
              <p className="text-2xl font-bold text-gray-900">$45.2K</p>
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
            <Link
              to="/dashboard/contacts"
              className="flex items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <Plus className="h-5 w-5 text-primary-600 mr-3" />
              <span>Add New Contact</span>
            </Link>
            <Link
              to="/dashboard/deals"
              className="flex items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <Plus className="h-5 w-5 text-primary-600 mr-3" />
              <span>Create New Deal</span>
            </Link>
            <Link
              to="/dashboard/accounts"
              className="flex items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <Plus className="h-5 w-5 text-primary-600 mr-3" />
              <span>Add New Account</span>
            </Link>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Recent Activity
          </h2>
          <div className="space-y-4">
            <div className="flex items-start space-x-3">
              <div className="h-2 w-2 bg-primary-600 rounded-full mt-2"></div>
              <div>
                <p className="text-sm text-gray-900">
                  New contact "John Smith" was added
                </p>
                <p className="text-xs text-gray-500">2 hours ago</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <div className="h-2 w-2 bg-green-600 rounded-full mt-2"></div>
              <div>
                <p className="text-sm text-gray-900">
                  Deal "Enterprise Software" moved to Closed Won
                </p>
                <p className="text-xs text-gray-500">4 hours ago</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <div className="h-2 w-2 bg-yellow-600 rounded-full mt-2"></div>
              <div>
                <p className="text-sm text-gray-900">
                  Account "TechCorp Inc" was updated
                </p>
                <p className="text-xs text-gray-500">1 day ago</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

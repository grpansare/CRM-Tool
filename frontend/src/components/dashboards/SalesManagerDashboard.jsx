import React, { useState, useEffect } from "react";
import {
  Users,
  BarChart3,
  TrendingUp,
  Target,
  Award,
  Activity,
  Calendar,
  DollarSign,
  Plus,
  X,
  UserPlus, FileText, Search, FileBarChart, Phone, Mail, CheckSquare, PieChart, Briefcase, Upload,
} from "lucide-react";
import api from "../../services/api";
import { useNavigate } from "react-router-dom";

const SalesManagerDashboard = () => {
  const navigate = useNavigate();
  const [teamStats, setTeamStats] = useState({
    teamSize: 0,
    totalDeals: 0,
    totalRevenue: 0,
    averageDealSize: 0,
    winRate: 0,
    pipelineValue: 0,
    teamPerformance: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showQuickActionModal, setShowQuickActionModal] = useState(false);
  const [quickActions, setQuickActions] = useState([
    {
      id: 1,
      title: "Create New Lead",
      description: "Add a new lead to the pipeline",
      icon: UserPlus,
      color: "blue",
      action: () => navigate("/dashboard/leads"),
    },
    {
      id: 2,
      title: "Create New Deal",
      description: "Start a new deal opportunity",
      icon: Briefcase,
      color: "green",
      action: () => navigate("/dashboard/deals"),
    },
    {
      id: 3,
      title: "Add Contact",
      description: "Add a new contact to the system",
      icon: Users,
      color: "purple",
      action: () => navigate("/dashboard/contacts"),
    },
    {
      id: 4,
      title: "Create Task",
      description: "Create a new task or reminder",
      icon: CheckSquare,
      color: "orange",
      action: () => navigate("/dashboard/tasks"),
    },
    {
      id: 5,
      title: "Upload Document",
      description: "Upload and manage documents",
      icon: Upload,
      color: "indigo",
      action: () => navigate("/dashboard/documents"),
    },
    {
      id: 6,
      title: "View Reports",
      description: "Access analytics and reports",
      icon: PieChart,
      color: "red",
      action: () => navigate("/dashboard/reports"),
    },
    {
      id: 7,
      title: "Advanced Search",
      description: "Search across all CRM data",
      icon: Search,
      color: "gray",
      action: () => navigate("/dashboard/advanced-search"),
    },
    {
      id: 8,
      title: "Import Contacts",
      description: "Import/export contact data",
      icon: FileBarChart,
      color: "teal",
      action: () => navigate("/dashboard/contact-import-export"),
    },
    {
      id: 9,
      title: "Team Performance",
      description: "View team performance metrics",
      icon: Award,
      color: "yellow",
      action: () => {
        const element = document.getElementById("team-performance-section");
        if (element) {
          element.scrollIntoView({ behavior: "smooth" });
        }
      },
    },
    {
      id: 10,
      title: "Schedule Meeting",
      description: "Schedule a meeting or appointment",
      icon: Calendar,
      color: "blue",
      action: () => window.open("https://calendar.google.com", "_blank"),
    },
    {
      id: 11,
      title: "Send Email",
      description: "Compose and send an email",
      icon: Mail,
      color: "green",
      action: () => window.open("mailto:", "_blank"),
    },
    {
      id: 12,
      title: "Make Call",
      description: "Initiate a phone call",
      icon: Phone,
      color: "purple",
      action: () => alert("Phone dialer integration - Connect your phone system"),
    },
  ]);

  useEffect(() => {
    fetchTeamStats();
  }, []);

  const fetchTeamStats = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch team statistics from sales manager API
      const response = await api.get('/sales-manager/team-stats');
      
      if (response.data.success) {
        setTeamStats(response.data.data);
      } else {
        setError('Failed to fetch team statistics');
      }
    } catch (err) {
      console.error('Error fetching team stats:', err);
      setError('Error loading team data');
      
      // Fallback to demo data for development
      setTeamStats({
        teamSize: 8,
        totalDeals: 45,
        totalRevenue: 85000,
        averageDealSize: 1889,
        winRate: 68,
        pipelineValue: 125000,
        teamPerformance: [
          { name: "John Smith", deals: 12, revenue: 25000, performance: 95 },
          { name: "Sarah Johnson", deals: 8, revenue: 18000, performance: 87 },
          { name: "Mike Davis", deals: 15, revenue: 32000, performance: 92 },
          { name: "Lisa Wilson", deals: 10, revenue: 20000, performance: 78 },
        ],
      });
    } finally {
      setLoading(false);
    }
  };

  const handleQuickAction = (action) => {
    if (action.action) {
      action.action();
    } else {
      console.log(`Quick action ${action.title} clicked`);
    }
  };

  const getActionColor = (color) => {
    switch (color) {
      case "blue":
        return "bg-blue-100 text-blue-600";
      case "green":
        return "bg-green-100 text-green-600";
      case "purple":
        return "bg-purple-100 text-purple-600";
      case "orange":
        return "bg-orange-100 text-orange-600";
      case "indigo":
        return "bg-indigo-100 text-indigo-600";
      case "red":
        return "bg-red-100 text-red-600";
      case "gray":
        return "bg-gray-100 text-gray-600";
      case "teal":
        return "bg-teal-100 text-teal-600";
      case "yellow":
        return "bg-yellow-100 text-yellow-600";
      default:
        return "bg-gray-100 text-gray-600";
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-red-600 mb-4">
            <svg className="h-16 w-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">Error Loading Dashboard</h3>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={fetchTeamStats}
            className="bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 sm:mb-8">
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">
          Sales Manager Dashboard
        </h1>
        <p className="text-gray-600 text-sm sm:text-base">
          Monitor team performance and sales analytics
        </p>
      </div>

      {/* Team Performance Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-6 sm:mb-8">
        <div className="card">
          <div className="flex items-center">
            <div className="bg-blue-100 p-3 rounded-lg">
              <Users className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Team Size</p>
              <p className="text-2xl font-bold text-gray-900">
                {teamStats.teamSize}
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
                {teamStats.totalDeals}
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
                ${(teamStats.totalRevenue / 1000).toFixed(0)}K
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-yellow-100 p-3 rounded-lg">
              <Target className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Win Rate</p>
              <p className="text-2xl font-bold text-gray-900">
                {teamStats.winRate}%
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Team Performance Table */}
      <div className="card mb-6 sm:mb-8" id="team-performance-section">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          Team Performance
        </h2>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Sales Rep
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Active Deals
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Revenue
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Performance
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {teamStats.teamPerformance.map((member, index) => (
                <tr key={index}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="h-8 w-8 bg-gray-200 rounded-full flex items-center justify-center">
                        <span className="text-sm font-medium text-gray-700">
                          {member.name
                            .split(" ")
                            .map((n) => n[0])
                            .join("")}
                        </span>
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">
                          {member.name}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {member.deals}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    ${(member.revenue / 1000).toFixed(0)}K
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="w-16 bg-gray-200 rounded-full h-2 mr-2">
                        <div
                          className="bg-green-600 h-2 rounded-full"
                          style={{ width: `${member.performance}%` }}
                        ></div>
                      </div>
                      <span className="text-sm text-gray-900">
                        {member.performance}%
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Quick Actions & Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:gap-8">
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Quick Actions
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {quickActions.slice(0, 8).map((action) => {
              const IconComponent = action.icon;
              return (
                <button
                  key={action.id}
                  className="flex items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50 hover:border-gray-300 transition-colors text-left"
                  onClick={() => handleQuickAction(action)}
                >
                  <div className={`p-2 rounded-lg mr-3 ${getActionColor(action.color)}`}>
                    <IconComponent className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {action.title}
                    </p>
                    <p className="text-xs text-gray-500 truncate">
                      {action.description}
                    </p>
                  </div>
                </button>
              );
            })}
          </div>
          
          {/* Show More Actions Button */}
          <div className="mt-4 pt-4 border-t border-gray-200">
            <button
              onClick={() => setShowQuickActionModal(true)}
              className="w-full flex items-center justify-center p-2 text-sm text-primary-600 hover:text-primary-700 hover:bg-primary-50 rounded-lg transition-colors"
            >
              <Plus className="h-4 w-4 mr-1" />
              View All Actions ({quickActions.length})
            </button>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Pipeline Overview
          </h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Pipeline Value</span>
              <span className="text-sm font-medium text-gray-900">
                ${(teamStats.pipelineValue / 1000).toFixed(0)}K
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Average Deal Size</span>
              <span className="text-sm font-medium text-gray-900">
                ${teamStats.averageDealSize.toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Conversion Rate</span>
              <span className="text-sm font-medium text-gray-900">
                {teamStats.winRate}%
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions Modal */}
      {showQuickActionModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl mx-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">All Quick Actions</h2>
              <button
                onClick={() => setShowQuickActionModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>
            
            <div className="p-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {quickActions.map((action) => {
                  const IconComponent = action.icon;
                  return (
                    <button
                      key={action.id}
                      className="flex flex-col items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 hover:border-gray-300 transition-colors"
                      onClick={() => {
                        handleQuickAction(action);
                        setShowQuickActionModal(false);
                      }}
                    >
                      <div className={`p-3 rounded-lg mb-3 ${getActionColor(action.color)}`}>
                        <IconComponent className="h-6 w-6" />
                      </div>
                      <h3 className="text-sm font-medium text-gray-900 mb-1 text-center">
                        {action.title}
                      </h3>
                      <p className="text-xs text-gray-500 text-center">
                        {action.description}
                      </p>
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SalesManagerDashboard;

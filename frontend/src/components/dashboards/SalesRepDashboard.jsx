import React, { useState, useEffect } from "react";
import {
  Target,
  BarChart3,
  Calendar,
  TrendingUp,
  CheckCircle,
  Clock,
  DollarSign,
  Activity,
} from "lucide-react";
import api from "../../services/api";
import { toast } from "react-hot-toast";

const SalesRepDashboard = () => {
  const [personalStats, setPersonalStats] = useState({
    activeDeals: 0,
    totalRevenue: 0,
    winRate: 0,
    averageDealSize: 0,
    tasksDue: 0,
    recentActivities: [],
    pipelineStages: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch dashboard data from backend APIs
  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch deals, analytics, and activities in parallel
      const [dealsResponse, analyticsResponse, activitiesResponse] = await Promise.allSettled([
        api.get('/deals'),
        api.get('/analytics/comprehensive'),
        api.get('/activities/my-activities')
      ]);

      let deals = [];
      let analytics = null;
      let activities = [];

      // Process deals response
      if (dealsResponse.status === 'fulfilled' && dealsResponse.value.data.success) {
        deals = dealsResponse.value.data.data || [];
      } else {
        console.warn('Failed to fetch deals:', dealsResponse.reason?.response?.data?.message);
      }

      // Process analytics response
      if (analyticsResponse.status === 'fulfilled' && analyticsResponse.value.data.success) {
        analytics = analyticsResponse.value.data.data;
      } else {
        console.warn('Failed to fetch analytics:', analyticsResponse.reason?.response?.data?.message);
      }

      // Process activities response
      if (activitiesResponse.status === 'fulfilled' && activitiesResponse.value.data.success) {
        activities = activitiesResponse.value.data.data || [];
      } else {
        console.warn('Failed to fetch activities:', activitiesResponse.reason?.response?.data?.message);
      }

      // Calculate stats from deals
      const activeDeals = deals.filter(deal => deal.stage?.name !== 'Closed Won' && deal.stage?.name !== 'Closed Lost').length;
      const closedWonDeals = deals.filter(deal => deal.stage?.name === 'Closed Won');
      const totalRevenue = closedWonDeals.reduce((sum, deal) => sum + (deal.value || 0), 0);
      const totalDeals = deals.length;
      const winRate = totalDeals > 0 ? Math.round((closedWonDeals.length / totalDeals) * 100) : 0;
      const averageDealSize = closedWonDeals.length > 0 ? Math.round(totalRevenue / closedWonDeals.length) : 0;

      // Group deals by stage for pipeline overview
      const stageGroups = deals.reduce((acc, deal) => {
        const stageName = deal.stage?.name || 'Unknown';
        if (!acc[stageName]) {
          acc[stageName] = { count: 0, value: 0 };
        }
        acc[stageName].count++;
        acc[stageName].value += deal.value || 0;
        return acc;
      }, {});

      const pipelineStages = Object.entries(stageGroups).map(([stage, data]) => ({
        stage,
        count: data.count,
        value: data.value
      }));

      // Format recent activities
      const recentActivities = activities.slice(0, 4).map(activity => ({
        type: activity.type || 'activity',
        message: activity.content || 'Activity recorded',
        time: formatTimeAgo(activity.timestamp)
      }));

      // Use analytics data if available, otherwise use calculated values
      const stats = {
        activeDeals,
        totalRevenue,
        winRate: analytics?.conversionRates?.winRate ? Math.round(analytics.conversionRates.winRate) : winRate,
        averageDealSize,
        tasksDue: 0, // TODO: Implement tasks/activities count
        recentActivities,
        pipelineStages
      };

      setPersonalStats(stats);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setError('Failed to load dashboard data. Please try again.');
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  // Helper function to format timestamp to relative time
  const formatTimeAgo = (timestamp) => {
    if (!timestamp) return 'Unknown time';
    
    const now = new Date();
    const activityTime = new Date(timestamp);
    const diffInMs = now - activityTime;
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInHours / 24);
    
    if (diffInDays > 0) {
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    } else if (diffInHours > 0) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    } else {
      return 'Less than an hour ago';
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button 
            onClick={fetchDashboardData}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 sm:mb-8">
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">My Dashboard</h1>
        <p className="text-gray-600 text-sm sm:text-base">
          Track your performance and manage your deals
        </p>
      </div>

      {/* Personal Performance Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-6 sm:mb-8">
        <div className="card">
          <div className="flex items-center">
            <div className="bg-blue-100 p-3 rounded-lg">
              <Target className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Active Deals</p>
              <p className="text-2xl font-bold text-gray-900">
                {personalStats.activeDeals}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-green-100 p-3 rounded-lg">
              <DollarSign className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Revenue</p>
              <p className="text-2xl font-bold text-gray-900">
                ${(personalStats.totalRevenue / 1000).toFixed(0)}K
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-purple-100 p-3 rounded-lg">
              <TrendingUp className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Win Rate</p>
              <p className="text-2xl font-bold text-gray-900">
                {personalStats.winRate}%
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="bg-yellow-100 p-3 rounded-lg">
              <Clock className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Tasks Due</p>
              <p className="text-2xl font-bold text-gray-900">
                {personalStats.tasksDue}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Pipeline Overview */}
      <div className="card mb-6 sm:mb-8">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          My Pipeline
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4">
          {personalStats.pipelineStages.map((stage, index) => (
            <div
              key={index}
              className="text-center p-4 border border-gray-200 rounded-lg"
            >
              <div className="text-2xl font-bold text-gray-900">
                {stage.count}
              </div>
              <div className="text-sm text-gray-600">{stage.stage}</div>
              <div className="text-xs text-gray-500">
                ${(stage.value / 1000).toFixed(0)}K
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Actions & Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:gap-8">
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Quick Actions
          </h2>
          <div className="space-y-3">
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <Target className="h-5 w-5 text-blue-600 mr-3" />
              <span>Create New Deal</span>
            </button>
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <Activity className="h-5 w-5 text-green-600 mr-3" />
              <span>Add New Contact</span>
            </button>
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <Calendar className="h-5 w-5 text-purple-600 mr-3" />
              <span>Schedule Follow-up</span>
            </button>
            <button className="flex items-center w-full p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <CheckCircle className="h-5 w-5 text-yellow-600 mr-3" />
              <span>Update Deal Stage</span>
            </button>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Recent Activity
          </h2>
          <div className="space-y-4">
            {personalStats.recentActivities.map((activity, index) => (
              <div key={index} className="flex items-start space-x-3">
                <div
                  className={`h-2 w-2 rounded-full mt-2 ${
                    activity.type === "deal"
                      ? "bg-blue-600"
                      : activity.type === "contact"
                      ? "bg-green-600"
                      : "bg-yellow-600"
                  }`}
                ></div>
                <div>
                  <p className="text-sm text-gray-900">{activity.message}</p>
                  <p className="text-xs text-gray-500">{activity.time}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Performance Metrics */}
      <div className="card mt-6 sm:mt-8">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          Performance Metrics
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 sm:gap-6">
          <div className="text-center">
            <div className="text-3xl font-bold text-blue-600">
              {personalStats.winRate}%
            </div>
            <div className="text-sm text-gray-600">Win Rate</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">
              ${personalStats.averageDealSize.toLocaleString()}
            </div>
            <div className="text-sm text-gray-600">Average Deal Size</div>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-purple-600">
              {personalStats.activeDeals}
            </div>
            <div className="text-sm text-gray-600">Active Deals</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SalesRepDashboard;

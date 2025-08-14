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

  useEffect(() => {
    // TODO: Fetch personal performance data
    setPersonalStats({
      activeDeals: 12,
      totalRevenue: 45000,
      winRate: 75,
      averageDealSize: 3750,
      tasksDue: 5,
      recentActivities: [
        {
          type: "deal",
          message: "Deal 'TechCorp Contract' moved to Negotiation",
          time: "2 hours ago",
        },
        {
          type: "contact",
          message: "New contact 'John Smith' added",
          time: "4 hours ago",
        },
        {
          type: "task",
          message: "Follow up with 'Acme Inc' due tomorrow",
          time: "1 day ago",
        },
        {
          type: "deal",
          message: "Deal 'Startup Software' closed - $15K",
          time: "2 days ago",
        },
      ],
      pipelineStages: [
        { stage: "Lead", count: 3, value: 15000 },
        { stage: "Qualified", count: 4, value: 25000 },
        { stage: "Proposal", count: 2, value: 18000 },
        { stage: "Negotiation", count: 2, value: 22000 },
        { stage: "Closed Won", count: 1, value: 15000 },
      ],
    });
  }, []);

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">My Dashboard</h1>
        <p className="text-gray-600">
          Track your performance and manage your deals
        </p>
      </div>

      {/* Personal Performance Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
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
      <div className="card mb-8">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          My Pipeline
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
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
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
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
      <div className="card mt-8">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">
          Performance Metrics
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
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

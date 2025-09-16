import React, { useState, useEffect } from 'react';
import {
  BarChart3,
  TrendingUp,
  DollarSign,
  Clock,
  Users,
  ArrowUp,
  ArrowDown
} from "lucide-react";
import { toast } from 'react-hot-toast';
import api from '../services/api';

const Analytics = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('revenue');

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      setLoading(true);
      const response = await api.get('/analytics/comprehensive');
      setAnalytics(response.data.data);
    } catch (error) {
      console.error('Error fetching analytics:', error);
      if (error.response?.status === 403) {
        toast.error('Access denied. Please ensure you have the required permissions.');
      } else {
        toast.error('Failed to load analytics data');
      }
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  const formatPercentage = (value) => {
    return `${parseFloat(value).toFixed(1)}%`;
  };

  const formatDays = (days) => {
    return `${Math.round(days)} days`;
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="bg-white p-6 rounded-lg shadow">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!analytics) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <BarChart3 className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">No analytics data</h3>
          <p className="mt-1 text-sm text-gray-500">Analytics data could not be loaded.</p>
        </div>
      </div>
    );
  }

  const { revenueForecast, salesVelocity, conversionRates } = analytics;

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Sales Analytics</h1>
        <p className="text-gray-600">Comprehensive insights into your sales performance</p>
      </div>

      {/* Key Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <DollarSign className="h-5 w-5 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Pipeline Value</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(revenueForecast.totalPipelineValue)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <TrendingUp className="h-8 w-8 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Weighted Forecast</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(revenueForecast.weightedForecast)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <Users className="h-8 w-8 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Avg Deal Cycle</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatDays(salesVelocity.averageDealCycleTime)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <BarChart3 className="h-8 w-8 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Win Rate</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatPercentage(conversionRates.winRate)}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: 'revenue', name: 'Revenue Forecast', icon: DollarSign },
            { id: 'velocity', name: 'Sales Velocity', icon: Clock },
            { id: 'conversion', name: 'Conversion Rates', icon: TrendingUp }
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`${
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center`}
            >
              <tab.icon className="h-5 w-5 mr-2" />
              {tab.name}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'revenue' && (
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Revenue Scenarios</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="text-center p-4 bg-green-50 rounded-lg">
                <p className="text-sm font-medium text-green-800">Best Case</p>
                <p className="text-2xl font-bold text-green-900">
                  {formatCurrency(revenueForecast.bestCaseScenario)}
                </p>
              </div>
              <div className="text-center p-4 bg-blue-50 rounded-lg">
                <p className="text-sm font-medium text-blue-800">Weighted Forecast</p>
                <p className="text-2xl font-bold text-blue-900">
                  {formatCurrency(revenueForecast.weightedForecast)}
                </p>
              </div>
              <div className="text-center p-4 bg-orange-50 rounded-lg">
                <p className="text-sm font-medium text-orange-800">Worst Case</p>
                <p className="text-2xl font-bold text-orange-900">
                  {formatCurrency(revenueForecast.worstCaseScenario)}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Revenue by Stage</h3>
            <div className="space-y-4">
              {revenueForecast.revenueByStage.map((stage) => (
                <div key={stage.stageId} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">{stage.stageName}</p>
                    <p className="text-sm text-gray-500">{stage.dealCount} deals</p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-gray-900">{formatCurrency(stage.totalValue)}</p>
                    <p className="text-sm text-gray-500">
                      Weighted: {formatCurrency(stage.weightedValue)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {activeTab === 'velocity' && (
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Sales Velocity Overview</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="text-center p-4 bg-blue-50 rounded-lg">
                <p className="text-sm font-medium text-blue-800">Average Deal Cycle</p>
                <p className="text-3xl font-bold text-blue-900">
                  {formatDays(salesVelocity.averageDealCycleTime)}
                </p>
              </div>
              <div className="text-center p-4 bg-purple-50 rounded-lg">
                <p className="text-sm font-medium text-purple-800">Average Stage Time</p>
                <p className="text-3xl font-bold text-purple-900">
                  {formatDays(salesVelocity.averageStageTime)}
                </p>
              </div>
            </div>
            <p className="text-sm text-gray-500 mt-4 text-center">
              Based on {salesVelocity.totalDealsAnalyzed} deals analyzed
            </p>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Time by Stage</h3>
            <div className="space-y-4">
              {salesVelocity.velocityByStage.map((stage) => (
                <div key={stage.stageId} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">{stage.stageName}</p>
                    <p className="text-sm text-gray-500">{stage.dealsAnalyzed} deals analyzed</p>
                  </div>
                  <div className="text-right">
                    <p className="text-xl font-bold text-gray-900">
                      {formatDays(stage.averageTimeInStage)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {activeTab === 'conversion' && (
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Overall Conversion Metrics</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="text-center p-4 bg-green-50 rounded-lg">
                <p className="text-sm font-medium text-green-800">Win Rate</p>
                <p className="text-3xl font-bold text-green-900">
                  {formatPercentage(conversionRates.winRate)}
                </p>
              </div>
              <div className="text-center p-4 bg-red-50 rounded-lg">
                <p className="text-sm font-medium text-red-800">Loss Rate</p>
                <p className="text-3xl font-bold text-red-900">
                  {formatPercentage(conversionRates.lossRate)}
                </p>
              </div>
              <div className="text-center p-4 bg-blue-50 rounded-lg">
                <p className="text-sm font-medium text-blue-800">Overall Conversion</p>
                <p className="text-3xl font-bold text-blue-900">
                  {formatPercentage(conversionRates.overallConversionRate)}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Stage-to-Stage Conversions</h3>
            <div className="space-y-4">
              {conversionRates.conversionByStage.map((conversion, index) => (
                <div key={index} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div className="flex items-center">
                    <div>
                      <p className="font-medium text-gray-900">
                        {conversion.fromStageName} → {conversion.toStageName}
                      </p>
                      <p className="text-sm text-gray-500">
                        {conversion.successfulTransitions} of {conversion.totalTransitions} transitions
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center">
                    <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                      conversion.conversionRate >= 50 
                        ? 'bg-green-100 text-green-800' 
                        : conversion.conversionRate >= 25
                        ? 'bg-yellow-100 text-yellow-800'
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {formatPercentage(conversion.conversionRate)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Refresh Button */}
      <div className="mt-8 text-center">
        <button
          onClick={fetchAnalytics}
          disabled={loading}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
        >
          {loading ? 'Refreshing...' : 'Refresh Analytics'}
        </button>
      </div>
    </div>
  );
};

export default Analytics;

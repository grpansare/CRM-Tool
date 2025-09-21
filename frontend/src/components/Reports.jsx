import React, { useState, useEffect } from 'react';
import { 
  BarChart3, 
  TrendingUp, 
  Users, 
  Target, 
  Phone, 
  Calendar, 
  Star, 
  Activity,
  PieChart,
  LineChart,
  Download,
  Filter,
  RefreshCw
} from 'lucide-react';
import api from '../services/api';
import toast from 'react-hot-toast';

const Reports = () => {
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedTimeRange, setSelectedTimeRange] = useState(30);
  const [activeTab, setActiveTab] = useState('overview');

  const tabs = [
    { id: 'overview', label: 'Overview', icon: BarChart3 },
    { id: 'dispositions', label: 'Dispositions', icon: Phone },
    { id: 'scores', label: 'Lead Scores', icon: Star },
    { id: 'sources', label: 'Lead Sources', icon: Target },
    { id: 'activity', label: 'Activity', icon: Activity }
  ];

  useEffect(() => {
    fetchReportData();
  }, [selectedTimeRange]);

  const fetchReportData = async () => {
    try {
      setLoading(true);
      const response = await api.get('/reports/dashboard');
      
      if (response.data.success) {
        setReportData(response.data.data);
      } else {
        toast.error('Failed to load report data');
      }
    } catch (error) {
      console.error('Error fetching report data:', error);
      toast.error('Failed to load reports');
    } finally {
      setLoading(false);
    }
  };

  const refreshData = () => {
    fetchReportData();
    toast.success('Reports refreshed');
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!reportData) {
    return (
      <div className="text-center py-12">
        <BarChart3 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 mb-2">No Report Data</h3>
        <p className="text-gray-600">Unable to load report data at this time.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Reports & Analytics</h1>
          <p className="text-gray-600">Comprehensive insights into your sales performance</p>
        </div>
        
        <div className="flex items-center space-x-3 mt-4 sm:mt-0">
          <select
            value={selectedTimeRange}
            onChange={(e) => setSelectedTimeRange(Number(e.target.value))}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value={7}>Last 7 days</option>
            <option value={30}>Last 30 days</option>
            <option value={90}>Last 90 days</option>
            <option value={365}>Last year</option>
          </select>
          
          <button
            onClick={refreshData}
            className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {tabs.map((tab) => {
            const IconComponent = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <IconComponent className="h-4 w-4 mr-2" />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="mt-6">
        {activeTab === 'overview' && <OverviewTab data={reportData.overview} />}
        {activeTab === 'dispositions' && <DispositionsTab data={reportData.dispositions} />}
        {activeTab === 'scores' && <ScoresTab data={reportData.scores} />}
        {activeTab === 'sources' && <SourcesTab data={reportData.sources} />}
        {activeTab === 'activity' && <ActivityTab data={reportData.activity} />}
      </div>
    </div>
  );
};

// Overview Tab Component
const OverviewTab = ({ data }) => {
  if (!data) return <div>No overview data available</div>;

  return (
    <div className="space-y-6">
      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Leads"
          value={data.totalLeads}
          icon={Users}
          color="blue"
        />
        <MetricCard
          title="Recent Leads (30d)"
          value={data.recentLeadsCount}
          icon={TrendingUp}
          color="green"
        />
        <MetricCard
          title="Conversion Rate"
          value={`${data.conversionRate}%`}
          icon={Target}
          color="purple"
        />
        <MetricCard
          title="Converted Leads"
          value={data.leadsByStatus?.CONVERTED || 0}
          icon={Star}
          color="yellow"
        />
      </div>

      {/* Lead Status Distribution */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Lead Status Distribution</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {data.leadsByStatus && Object.entries(data.leadsByStatus).map(([status, count]) => (
            <div key={status} className="text-center p-4 bg-gray-50 rounded-lg">
              <div className="text-2xl font-bold text-gray-900">{count}</div>
              <div className="text-sm text-gray-600 capitalize">{status.toLowerCase()}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// Dispositions Tab Component
const DispositionsTab = ({ data }) => {
  if (!data) return <div>No disposition data available</div>;

  const { dispositionCounts, categorySummary, followUpRequired } = data;

  return (
    <div className="space-y-6">
      {/* Category Summary */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <MetricCard
          title="Positive Outcomes"
          value={categorySummary?.positive || 0}
          icon={TrendingUp}
          color="green"
        />
        <MetricCard
          title="Follow-up Required"
          value={categorySummary?.followUp || 0}
          icon={Calendar}
          color="yellow"
        />
        <MetricCard
          title="Negative Outcomes"
          value={categorySummary?.negative || 0}
          icon={Users}
          color="red"
        />
        <MetricCard
          title="Needs Follow-up"
          value={followUpRequired || 0}
          icon={Phone}
          color="orange"
        />
      </div>

      {/* Detailed Disposition Breakdown */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Disposition Breakdown</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {dispositionCounts && Object.entries(dispositionCounts).map(([disposition, count]) => (
            <div key={disposition} className="p-4 border border-gray-200 rounded-lg">
              <div className="text-xl font-bold text-gray-900">{count}</div>
              <div className="text-sm text-gray-600">{disposition.replace(/_/g, ' ')}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// Scores Tab Component
const ScoresTab = ({ data }) => {
  if (!data) return <div>No score data available</div>;

  const { scoreDistribution, averageScore } = data;

  return (
    <div className="space-y-6">
      {/* Score Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <MetricCard
          title="Average Score"
          value={averageScore || 0}
          icon={Star}
          color="purple"
        />
        <MetricCard
          title="A-Grade Leads"
          value={scoreDistribution?.A_Grade_80_100 || 0}
          icon={TrendingUp}
          color="green"
        />
      </div>

      {/* Score Distribution */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Lead Score Distribution</h3>
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
          {scoreDistribution && Object.entries(scoreDistribution).map(([grade, count]) => {
            const gradeInfo = getGradeInfo(grade);
            return (
              <div key={grade} className={`p-4 rounded-lg ${gradeInfo.bgColor}`}>
                <div className={`text-2xl font-bold ${gradeInfo.textColor}`}>{count}</div>
                <div className="text-sm text-gray-600">{gradeInfo.label}</div>
                <div className="text-xs text-gray-500">{gradeInfo.range}</div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

// Sources Tab Component
const SourcesTab = ({ data }) => {
  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Lead Source Performance</h3>
      <p className="text-gray-600">Source performance data will be displayed here.</p>
    </div>
  );
};

// Activity Tab Component
const ActivityTab = ({ data }) => {
  if (!data) return <div>No activity data available</div>;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <MetricCard
          title="Leads with Dispositions"
          value={data.leadsWithDispositions || 0}
          icon={Phone}
          color="green"
        />
        <MetricCard
          title="Leads without Dispositions"
          value={data.leadsWithoutDispositions || 0}
          icon={Users}
          color="red"
        />
        <MetricCard
          title="Recent Activity (7d)"
          value={data.recentActivityCount || 0}
          icon={Activity}
          color="blue"
        />
      </div>
    </div>
  );
};

// Metric Card Component
const MetricCard = ({ title, value, icon: Icon, color }) => {
  const colorClasses = {
    blue: 'bg-blue-100 text-blue-600',
    green: 'bg-green-100 text-green-600',
    purple: 'bg-purple-100 text-purple-600',
    yellow: 'bg-yellow-100 text-yellow-600',
    red: 'bg-red-100 text-red-600',
    orange: 'bg-orange-100 text-orange-600'
  };

  return (
    <div className="card">
      <div className="flex items-center">
        <div className={`p-3 rounded-lg ${colorClasses[color]}`}>
          <Icon className="h-6 w-6" />
        </div>
        <div className="ml-4">
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-2xl font-bold text-gray-900">{value}</p>
        </div>
      </div>
    </div>
  );
};

// Helper function for grade information
const getGradeInfo = (grade) => {
  const gradeMap = {
    'A_Grade_80_100': { label: 'A Grade', range: '80-100', bgColor: 'bg-green-50', textColor: 'text-green-600' },
    'B_Grade_60_79': { label: 'B Grade', range: '60-79', bgColor: 'bg-blue-50', textColor: 'text-blue-600' },
    'C_Grade_40_59': { label: 'C Grade', range: '40-59', bgColor: 'bg-yellow-50', textColor: 'text-yellow-600' },
    'D_Grade_0_39': { label: 'D Grade', range: '0-39', bgColor: 'bg-red-50', textColor: 'text-red-600' },
    'No_Score': { label: 'No Score', range: 'Unscored', bgColor: 'bg-gray-50', textColor: 'text-gray-600' }
  };
  
  return gradeMap[grade] || { label: grade, range: '', bgColor: 'bg-gray-50', textColor: 'text-gray-600' };
};

export default Reports;

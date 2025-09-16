import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { 
  Phone, 
  Mail, 
  Calendar, 
  FileText, 
  CheckSquare, 
  Plus, 
  Clock,
  User,
  MessageCircle,
  Filter
} from 'lucide-react';
import api from '../services/api';

const ActivityTimeline = ({ contactId, accountId, dealId, leadId }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [filterType, setFilterType] = useState('ALL');
  const [activityForm, setActivityForm] = useState({
    type: 'NOTE',
    content: '',
    outcome: '',
    associations: {
      contacts: contactId ? [contactId] : [],
      accounts: accountId ? [accountId] : [],
      deals: dealId ? [dealId] : [],
      leads: leadId ? [leadId] : []
    }
  });

  useEffect(() => {
    fetchActivities();
  }, [contactId, accountId, dealId, leadId, filterType]);

  const fetchActivities = async () => {
    try {
      setLoading(true);
      let url = '/activities';
      
      // Use specific timeline endpoints for better performance
      if (contactId) {
        url = `/activities/contacts/${contactId}/timeline`;
      } else if (accountId) {
        url = `/activities/accounts/${accountId}/timeline`;
      } else if (dealId) {
        url = `/activities/deals/${dealId}/timeline`;
      } else if (leadId) {
        url = `/activities/leads/${leadId}/timeline`;
      } else {
        url = '/activities/my-activities';
      }
      
      // Add filter type as query parameter if specified
      const params = new URLSearchParams();
      if (filterType !== 'ALL') {
        params.append('type', filterType);
      }
      
      if (params.toString()) {
        url += `?${params.toString()}`;
      }
      
      const response = await api.get(url);
      if (response.data.success) {
        // Handle paginated response structure
        const responseData = response.data.data;
        if (responseData && responseData.content) {
          // Paginated response
          setActivities(responseData.content);
        } else if (Array.isArray(responseData)) {
          // Direct array response
          setActivities(responseData);
        } else {
          // Fallback to empty array
          setActivities([]);
        }
      }
    } catch (error) {
      toast.error('Failed to fetch activities');
      console.error('Error fetching activities:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateActivity = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/activities', activityForm);
      if (response.data.success) {
        toast.success('Activity created successfully');
        setShowCreateModal(false);
        setActivityForm({
          type: 'NOTE',
          content: '',
          outcome: '',
          associations: {
            contacts: contactId ? [contactId] : [],
            accounts: accountId ? [accountId] : [],
            deals: dealId ? [dealId] : [],
            leads: leadId ? [leadId] : []
          }
        });
        fetchActivities();
      }
    } catch (error) {
      toast.error('Failed to create activity');
      console.error('Error creating activity:', error);
    }
  };

  const getActivityIcon = (type) => {
    const iconProps = { className: "h-4 w-4" };
    switch (type) {
      case 'CALL':
        return <Phone {...iconProps} />;
      case 'EMAIL':
        return <Mail {...iconProps} />;
      case 'MEETING':
        return <Calendar {...iconProps} />;
      case 'NOTE':
        return <FileText {...iconProps} />;
      case 'TASK':
        return <CheckSquare {...iconProps} />;
      default:
        return <MessageCircle {...iconProps} />;
    }
  };

  const getActivityColor = (type) => {
    switch (type) {
      case 'CALL':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'EMAIL':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'MEETING':
        return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'NOTE':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'TASK':
        return 'bg-orange-100 text-orange-800 border-orange-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);

    if (diffInHours < 24) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (diffInHours < 168) { // 7 days
      return date.toLocaleDateString([], { weekday: 'short', hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
  };

  const activityTypes = [
    { value: 'ALL', label: 'All Activities' },
    { value: 'CALL', label: 'Calls' },
    { value: 'EMAIL', label: 'Emails' },
    { value: 'MEETING', label: 'Meetings' },
    { value: 'NOTE', label: 'Notes' },
    { value: 'TASK', label: 'Tasks' }
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-32">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold text-gray-900">Activity Timeline</h3>
        <div className="flex space-x-2">
          <div className="flex items-center space-x-2">
            <Filter className="h-4 w-4 text-gray-400" />
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {activityTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 flex items-center space-x-1 text-sm"
          >
            <Plus className="h-4 w-4" />
            <span>Log Activity</span>
          </button>
        </div>
      </div>

      {/* Timeline */}
      <div className="space-y-4">
        {activities.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <MessageCircle className="h-12 w-12 mx-auto mb-4 text-gray-300" />
            <p>No activities found</p>
            <p className="text-sm">Log your first activity to get started</p>
          </div>
        ) : (
          activities.map((activity, index) => (
            <div key={activity.id} className="relative">
              {/* Timeline line */}
              {index < activities.length - 1 && (
                <div className="absolute left-6 top-12 bottom-0 w-0.5 bg-gray-200"></div>
              )}
              
              {/* Activity card */}
              <div className="flex space-x-3">
                {/* Icon */}
                <div className={`flex-shrink-0 w-12 h-12 rounded-full border-2 flex items-center justify-center ${getActivityColor(activity.type)}`}>
                  {getActivityIcon(activity.type)}
                </div>
                
                {/* Content */}
                <div className="flex-1 bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
                  <div className="flex justify-between items-start mb-2">
                    <div className="flex items-center space-x-2">
                      <span className="font-medium text-gray-900">
                        {activity.type.charAt(0) + activity.type.slice(1).toLowerCase()}
                      </span>
                      {activity.outcome && (
                        <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded-full">
                          {activity.outcome}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-500">
                      <Clock className="h-3 w-3" />
                      <span>{formatTimestamp(activity.timestamp)}</span>
                    </div>
                  </div>
                  
                  {activity.content && (
                    <p className="text-gray-700 mb-3">{activity.content}</p>
                  )}
                  
                  <div className="flex items-center justify-between text-sm text-gray-500">
                    <div className="flex items-center space-x-1">
                      <User className="h-3 w-3" />
                      <span>{activity.userName || 'Unknown User'}</span>
                    </div>
                    
                    {/* Associated entities */}
                    <div className="flex space-x-2">
                      {activity.associations?.contacts?.length > 0 && (
                        <span className="px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded">
                          {activity.associations.contacts.length} contact(s)
                        </span>
                      )}
                      {activity.associations?.accounts?.length > 0 && (
                        <span className="px-2 py-1 bg-green-50 text-green-700 text-xs rounded">
                          {activity.associations.accounts.length} account(s)
                        </span>
                      )}
                      {activity.associations?.deals?.length > 0 && (
                        <span className="px-2 py-1 bg-purple-50 text-purple-700 text-xs rounded">
                          {activity.associations.deals.length} deal(s)
                        </span>
                      )}
                      {activity.associations?.leads?.length > 0 && (
                        <span className="px-2 py-1 bg-yellow-50 text-yellow-700 text-xs rounded">
                          {activity.associations.leads.length} lead(s)
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Create Activity Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-semibold mb-4">Log New Activity</h2>
            <form onSubmit={handleCreateActivity}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Activity Type
                  </label>
                  <select
                    value={activityForm.type}
                    onChange={(e) => setActivityForm({ ...activityForm, type: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="CALL">Call</option>
                    <option value="EMAIL">Email</option>
                    <option value="MEETING">Meeting</option>
                    <option value="NOTE">Note</option>
                    <option value="TASK">Task</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Content
                  </label>
                  <textarea
                    value={activityForm.content}
                    onChange={(e) => setActivityForm({ ...activityForm, content: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    rows="3"
                    placeholder="Describe the activity..."
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Outcome (Optional)
                  </label>
                  <input
                    type="text"
                    value={activityForm.outcome}
                    onChange={(e) => setActivityForm({ ...activityForm, outcome: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="e.g., Connected, Left voicemail, Scheduled demo"
                  />
                </div>
              </div>
              
              <div className="flex justify-end space-x-2 mt-6">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  Log Activity
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ActivityTimeline;

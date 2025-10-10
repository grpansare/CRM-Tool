import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Settings, Users, Target, RotateCcw, Shuffle, MapPin, Award } from 'lucide-react';
import { toast } from 'react-hot-toast';
import axios from 'axios';

const LeadAssignmentRules = () => {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingRule, setEditingRule] = useState(null);

  useEffect(() => {
    fetchAssignmentRules();
  }, []);

  const fetchAssignmentRules = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/v1/lead-assignment/rules');
      
      if (response.data.success) {
        setRules(response.data.data.content || []);
      }
    } catch (error) {
      console.error('Error fetching assignment rules:', error);
      toast.error('Failed to load assignment rules');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteRule = async (ruleId) => {
    if (!window.confirm('Are you sure you want to delete this assignment rule?')) {
      return;
    }

    try {
      await axios.delete(`/api/v1/lead-assignment/rules/${ruleId}`);
      toast.success('Assignment rule deleted successfully');
      fetchAssignmentRules();
    } catch (error) {
      console.error('Error deleting assignment rule:', error);
      toast.error('Failed to delete assignment rule');
    }
  };

  const getStrategyIcon = (strategy) => {
    const icons = {
      'ROUND_ROBIN': RotateCcw,
      'LOAD_BALANCED': Target,
      'TERRITORY_BASED': MapPin,
      'SKILL_BASED': Award,
      'RANDOM': Shuffle
    };
    return icons[strategy] || Settings;
  };

  const getStrategyColor = (strategy) => {
    const colors = {
      'ROUND_ROBIN': 'text-blue-600 bg-blue-100',
      'LOAD_BALANCED': 'text-green-600 bg-green-100',
      'TERRITORY_BASED': 'text-purple-600 bg-purple-100',
      'SKILL_BASED': 'text-yellow-600 bg-yellow-100',
      'RANDOM': 'text-gray-600 bg-gray-100'
    };
    return colors[strategy] || 'text-gray-600 bg-gray-100';
  };

  const formatStrategy = (strategy) => {
    return strategy.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Lead Assignment Rules</h1>
          <p className="text-gray-600 mt-1">Configure automated lead routing and assignment strategies</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="h-4 w-4" />
          <span>Create Rule</span>
        </button>
      </div>

      {/* Rules List */}
      <div className="bg-white rounded-lg shadow">
        {rules.length === 0 ? (
          <div className="text-center py-12">
            <Settings className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No Assignment Rules</h3>
            <p className="text-gray-500 mb-6">Create your first lead assignment rule to automate lead routing</p>
            <button
              onClick={() => setShowCreateModal(true)}
              className="inline-flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="h-4 w-4" />
              <span>Create Rule</span>
            </button>
          </div>
        ) : (
          <div className="overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Rule Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Strategy
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Priority
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Assigned Users
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {rules.map((rule) => {
                  const StrategyIcon = getStrategyIcon(rule.assignmentStrategy);
                  const userIds = rule.assignedUserIdsList || [];
                  
                  return (
                    <tr key={rule.ruleId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {rule.ruleName}
                          </div>
                          {rule.description && (
                            <div className="text-sm text-gray-500">
                              {rule.description}
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className={`inline-flex items-center space-x-2 px-3 py-1 rounded-full text-sm font-medium ${getStrategyColor(rule.assignmentStrategy)}`}>
                          <StrategyIcon className="h-4 w-4" />
                          <span>{formatStrategy(rule.assignmentStrategy)}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                          Priority {rule.priorityOrder}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center space-x-1">
                          <Users className="h-4 w-4 text-gray-400" />
                          <span className="text-sm text-gray-900">
                            {userIds.length} user{userIds.length !== 1 ? 's' : ''}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          rule.isActive 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {rule.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => setEditingRule(rule)}
                            className="text-blue-600 hover:text-blue-900 p-1 rounded"
                            title="Edit rule"
                          >
                            <Edit className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteRule(rule.ruleId)}
                            className="text-red-600 hover:text-red-900 p-1 rounded"
                            title="Delete rule"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Rule Conditions Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <h3 className="text-lg font-medium text-blue-900 mb-3">How Lead Assignment Works</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-blue-800">
          <div>
            <h4 className="font-medium mb-2">Assignment Strategies:</h4>
            <ul className="space-y-1">
              <li>• <strong>Round Robin:</strong> Distributes leads evenly among users</li>
              <li>• <strong>Load Balanced:</strong> Assigns to user with least active leads</li>
              <li>• <strong>Territory Based:</strong> Assigns based on geographic location</li>
              <li>• <strong>Skill Based:</strong> Matches leads to user expertise</li>
              <li>• <strong>Random:</strong> Randomly assigns to available users</li>
            </ul>
          </div>
          <div>
            <h4 className="font-medium mb-2">Rule Conditions:</h4>
            <ul className="space-y-1">
              <li>• Lead source (Website, Social Media, etc.)</li>
              <li>• Lead score range (0-100)</li>
              <li>• Lead status (New, Contacted, etc.)</li>
              <li>• Company size (employee count)</li>
              <li>• Custom criteria based on lead data</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Create/Edit Rule Modal */}
      {(showCreateModal || editingRule) && (
        <CreateEditRuleModal
          isOpen={showCreateModal || !!editingRule}
          onClose={() => {
            setShowCreateModal(false);
            setEditingRule(null);
          }}
          rule={editingRule}
          onSave={() => {
            fetchAssignmentRules();
            setShowCreateModal(false);
            setEditingRule(null);
          }}
        />
      )}
    </div>
  );
};

// Create/Edit Rule Modal Component
const CreateEditRuleModal = ({ isOpen, onClose, rule, onSave }) => {
  const [formData, setFormData] = useState({
    ruleName: '',
    description: '',
    priorityOrder: 1,
    assignmentStrategy: 'ROUND_ROBIN',
    conditions: {},
    assignedUserIds: [],
    assignedTeamIds: [],
    isActive: true
  });
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (rule) {
      setFormData({
        ruleName: rule.ruleName || '',
        description: rule.description || '',
        priorityOrder: rule.priorityOrder || 1,
        assignmentStrategy: rule.assignmentStrategy || 'ROUND_ROBIN',
        conditions: rule.conditionsMap || {},
        assignedUserIds: rule.assignedUserIdsList || [],
        assignedTeamIds: rule.assignedTeamIdsList || [],
        isActive: rule.isActive !== false
      });
    }
  }, [rule]);

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.ruleName.trim()) {
      toast.error('Rule name is required');
      return;
    }

    try {
      setIsSaving(true);
      
      const payload = {
        ...formData,
        priorityOrder: parseInt(formData.priorityOrder)
      };

      if (rule) {
        await axios.put(`/api/v1/lead-assignment/rules/${rule.ruleId}`, payload);
        toast.success('Assignment rule updated successfully');
      } else {
        await axios.post('/api/v1/lead-assignment/rules', payload);
        toast.success('Assignment rule created successfully');
      }

      onSave();
    } catch (error) {
      console.error('Error saving assignment rule:', error);
      toast.error(error.response?.data?.message || 'Failed to save assignment rule');
    } finally {
      setIsSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">
            {rule ? 'Edit Assignment Rule' : 'Create Assignment Rule'}
          </h2>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Rule Name */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Rule Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="ruleName"
              value={formData.ruleName}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter rule name"
              required
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Describe when this rule should apply"
            />
          </div>

          {/* Priority and Strategy */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Priority Order
              </label>
              <input
                type="number"
                name="priorityOrder"
                value={formData.priorityOrder}
                onChange={handleInputChange}
                min="1"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Assignment Strategy
              </label>
              <select
                name="assignmentStrategy"
                value={formData.assignmentStrategy}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="ROUND_ROBIN">Round Robin</option>
                <option value="LOAD_BALANCED">Load Balanced</option>
                <option value="TERRITORY_BASED">Territory Based</option>
                <option value="SKILL_BASED">Skill Based</option>
                <option value="RANDOM">Random</option>
              </select>
            </div>
          </div>

          {/* Assigned Users */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Assigned User IDs (comma-separated)
            </label>
            <input
              type="text"
              value={formData.assignedUserIds.join(', ')}
              onChange={(e) => {
                const userIds = e.target.value.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
                setFormData(prev => ({ ...prev, assignedUserIds: userIds }));
              }}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="1, 2, 3"
            />
            <p className="text-xs text-gray-500 mt-1">
              Enter user IDs separated by commas (e.g., 1, 2, 3)
            </p>
          </div>

          {/* Active Status */}
          <div className="flex items-center">
            <input
              type="checkbox"
              name="isActive"
              checked={formData.isActive}
              onChange={handleInputChange}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label className="ml-2 block text-sm text-gray-700">
              Rule is active
            </label>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="flex items-center space-x-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {isSaving ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>Saving...</span>
                </>
              ) : (
                <span>{rule ? 'Update Rule' : 'Create Rule'}</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LeadAssignmentRules;

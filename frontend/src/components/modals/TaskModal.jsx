import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { X, Calendar, User, Tag, AlertCircle } from 'lucide-react';
import api from '../../services/api';

const TaskModal = ({ isOpen, onClose, task = null, onTaskSaved }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    type: 'GENERAL',
    assignedToUserId: '',
    dueDate: '',
    relatedContactId: '',
    relatedAccountId: '',
    relatedDealId: '',
    relatedLeadId: '',
    relatedEntityType: ''
  });
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [deals, setDeals] = useState([]);
  const [leads, setLeads] = useState([]);

  const taskPriorities = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' }
  ];

  const taskTypes = [
    { value: 'GENERAL', label: 'General' },
    { value: 'CALL', label: 'Call' },
    { value: 'EMAIL', label: 'Email' },
    { value: 'MEETING', label: 'Meeting' },
    { value: 'FOLLOW_UP', label: 'Follow Up' },
    { value: 'DEMO', label: 'Demo' },
    { value: 'PROPOSAL', label: 'Proposal' },
    { value: 'CONTRACT', label: 'Contract' },
    { value: 'RESEARCH', label: 'Research' },
    { value: 'ADMINISTRATIVE', label: 'Administrative' }
  ];

  const entityTypes = [
    { value: '', label: 'None' },
    { value: 'CONTACT', label: 'Contact' },
    { value: 'ACCOUNT', label: 'Account' },
    { value: 'DEAL', label: 'Deal' },
    { value: 'LEAD', label: 'Lead' }
  ];

  useEffect(() => {
    if (isOpen) {
      fetchUsers();
      fetchContacts();
      fetchAccounts();
      fetchDeals();
      fetchLeads();
      
      if (task) {
        // Edit mode - populate form with existing task data
        setFormData({
          title: task.title || '',
          description: task.description || '',
          priority: task.priority || 'MEDIUM',
          type: task.type || 'GENERAL',
          assignedToUserId: task.assignedToUserId || '',
          dueDate: task.dueDate ? new Date(task.dueDate).toISOString().slice(0, 16) : '',
          relatedContactId: task.relatedContactId || '',
          relatedAccountId: task.relatedAccountId || '',
          relatedDealId: task.relatedDealId || '',
          relatedLeadId: task.relatedLeadId || '',
          relatedEntityType: task.relatedEntityType || ''
        });
      } else {
        // Create mode - reset form
        setFormData({
          title: '',
          description: '',
          priority: 'MEDIUM',
          type: 'GENERAL',
          assignedToUserId: '',
          dueDate: '',
          relatedContactId: '',
          relatedAccountId: '',
          relatedDealId: '',
          relatedLeadId: '',
          relatedEntityType: ''
        });
      }
    }
  }, [isOpen, task]);

  const fetchUsers = async () => {
    try {
      const response = await api.get('/users');
      if (response.data.success) {
        setUsers(response.data.data || []);
      }
    } catch (error) {
      console.error('Error fetching users:', error);
    }
  };

  const fetchContacts = async () => {
    try {
      const response = await api.get('/contacts', { params: { page: 0, size: 100 } });
      if (response.data.success) {
        // Handle both paginated and non-paginated responses
        const contacts = response.data.data.content || response.data.data || [];
        setContacts(contacts);
      }
    } catch (error) {
      console.error('Error fetching contacts:', error);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await api.get('/accounts', { params: { page: 0, size: 100 } });
      if (response.data.success) {
        // Handle both paginated and non-paginated responses
        const accounts = response.data.data.content || response.data.data || [];
        setAccounts(accounts);
      }
    } catch (error) {
      console.error('Error fetching accounts:', error);
    }
  };

  const fetchDeals = async () => {
    try {
      const response = await api.get('/deals', { params: { page: 0, size: 100 } });
      if (response.data.success) {
        // Handle both paginated and non-paginated responses
        const deals = response.data.data.content || response.data.data || [];
        setDeals(deals);
      }
    } catch (error) {
      console.error('Error fetching deals:', error);
    }
  };

  const fetchLeads = async () => {
    try {
      const response = await api.get('/leads', { params: { page: 0, size: 100 } });
      if (response.data.success) {
        setLeads(response.data.data.content || []);
      }
    } catch (error) {
      console.error('Error fetching leads:', error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Clear related entity IDs when entity type changes
    if (name === 'relatedEntityType') {
      setFormData(prev => ({
        ...prev,
        relatedContactId: '',
        relatedAccountId: '',
        relatedDealId: '',
        relatedLeadId: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim()) {
      toast.error('Task title is required');
      return;
    }

    if (!formData.assignedToUserId) {
      toast.error('Please assign the task to a user');
      return;
    }

    setLoading(true);
    
    try {
      const taskData = {
        ...formData,
        dueDate: formData.dueDate ? new Date(formData.dueDate).toISOString() : null
      };

      let response;
      if (task) {
        // Update existing task
        response = await api.put(`/tasks/${task.taskId}`, taskData);
      } else {
        // Create new task
        response = await api.post('/tasks', taskData);
      }

      if (response.data.success) {
        toast.success(task ? 'Task updated successfully' : 'Task created successfully');
        onTaskSaved();
        onClose();
      }
    } catch (error) {
      console.error('Error saving task:', error);
      toast.error(task ? 'Failed to update task' : 'Failed to create task');
    } finally {
      setLoading(false);
    }
  };

  const getRelatedEntityOptions = () => {
    switch (formData.relatedEntityType) {
      case 'CONTACT':
        return contacts.map(contact => ({
          value: contact.contactId,
          label: `${contact.firstName} ${contact.lastName}`
        }));
      case 'ACCOUNT':
        return accounts.map(account => ({
          value: account.accountId,
          label: account.accountName
        }));
      case 'DEAL':
        return deals.map(deal => ({
          value: deal.dealId,
          label: deal.dealTitle
        }));
      case 'LEAD':
        return leads.map(lead => ({
          value: lead.leadId,
          label: `${lead.firstName} ${lead.lastName}`
        }));
      default:
        return [];
    }
  };

  const getRelatedEntityFieldName = () => {
    switch (formData.relatedEntityType) {
      case 'CONTACT':
        return 'relatedContactId';
      case 'ACCOUNT':
        return 'relatedAccountId';
      case 'DEAL':
        return 'relatedDealId';
      case 'LEAD':
        return 'relatedLeadId';
      default:
        return '';
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">
            {task ? 'Edit Task' : 'Create New Task'}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Title *
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter task title"
              required
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              rows={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter task description"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Priority */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Priority *
              </label>
              <select
                name="priority"
                value={formData.priority}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              >
                {taskPriorities.map(priority => (
                  <option key={priority.value} value={priority.value}>
                    {priority.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Type */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Type *
              </label>
              <select
                name="type"
                value={formData.type}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              >
                {taskTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Assigned To */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Assign To *
              </label>
              <select
                name="assignedToUserId"
                value={formData.assignedToUserId}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              >
                <option value="">Select user...</option>
                {users.map(user => (
                  <option key={user.userId} value={user.userId}>
                    {user.firstName} {user.lastName} ({user.email})
                  </option>
                ))}
              </select>
            </div>

            {/* Due Date */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Due Date
              </label>
              <input
                type="datetime-local"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleInputChange}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Related Entity */}
          <div className="border-t border-gray-200 pt-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Related Entity (Optional)</h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Entity Type */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Entity Type
                </label>
                <select
                  name="relatedEntityType"
                  value={formData.relatedEntityType}
                  onChange={handleInputChange}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  {entityTypes.map(type => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* Related Entity Selection */}
              {formData.relatedEntityType && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Select {formData.relatedEntityType}
                  </label>
                  <select
                    name={getRelatedEntityFieldName()}
                    value={formData[getRelatedEntityFieldName()]}
                    onChange={handleInputChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">Select {formData.relatedEntityType.toLowerCase()}...</option>
                    {getRelatedEntityOptions().map(option => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>
          </div>

          {/* Form Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
            >
              {loading && <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>}
              <span>{task ? 'Update Task' : 'Create Task'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TaskModal;

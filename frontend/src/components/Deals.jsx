import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import api from '../services/api';
import {
  Plus,
  Search,
  Filter,
  Edit2,
  Trash2,
  DollarSign,
  Calendar,
  User,
  Building,
  TrendingUp,
  X,
  ChevronDown
} from 'lucide-react';

const Deals = () => {
  const [deals, setDeals] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [stages, setStages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStage, setSelectedStage] = useState('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showStageModal, setShowStageModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState(null);
  const [formData, setFormData] = useState({
    dealName: '',
    amount: '',
    expectedCloseDate: '',
    stageId: '',
    contactId: '',
    accountId: '',
    description: ''
  });
  const [stageFormData, setStageFormData] = useState({
    newStageId: ''
  });

  useEffect(() => {
    fetchDeals();
    fetchContacts();
    fetchAccounts();
    fetchStages();
  }, []);

  const fetchDeals = async () => {
    try {
      setLoading(true);
      const response = await api.get('/deals');
      if (response.data.success) {
        setDeals(response.data.data);
      }
    } catch (error) {
      toast.error('Failed to fetch deals');
      console.error('Error fetching deals:', error);
    } finally {
      setLoading(false);
    }
  };


  const fetchContacts = async () => {
    try {
      const response = await api.get('/contacts');
      if (response.data.success) {
        setContacts(response.data.data);
      }
    } catch (error) {
      console.error('Error fetching contacts:', error);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await api.get('/accounts');
      if (response.data.success) {
        setAccounts(response.data.data);
      }
    } catch (error) {
      console.error('Error fetching accounts:', error);
    }
  };

  const fetchStages = async () => {
    try {
      const response = await api.get('/deals/stages');
      if (response.data.success) {
        setStages(response.data.data);
      }
    } catch (error) {
      console.error('Error fetching stages:', error);
    }
  };

  const handleCreateDeal = async (e) => {
    e.preventDefault();
    try {
      const dealPayload = {
        ...formData,
        amount: parseFloat(formData.amount),
        stageId: parseInt(formData.stageId),
        contactId: parseInt(formData.contactId),
        accountId: parseInt(formData.accountId)
      };
      
      console.log('Creating deal with payload:', dealPayload);
      
      const response = await api.post('/deals', dealPayload);
      if (response.data.success) {
        toast.success('Deal created successfully');
        setShowCreateModal(false);
        setFormData({
          dealName: '',
          amount: '',
          expectedCloseDate: '',
          stageId: '',
          contactId: '',
          accountId: '',
          description: ''
        });
        fetchDeals();
      }
    } catch (error) {
      toast.error('Failed to create deal');
      console.error('Error creating deal:', error);
      console.error('Error response:', error.response?.data);
    }
  };

  const handleUpdateStage = async (e) => {
    e.preventDefault();
    try {
      const response = await api.put(`/deals/${selectedDeal.dealId}/stage`, {
        newStageId: parseInt(stageFormData.newStageId)
      });
      if (response.data.success) {
        toast.success('Deal stage updated successfully');
        setShowStageModal(false);
        setSelectedDeal(null);
        setStageFormData({ newStageId: '' });
        fetchDeals();
      }
    } catch (error) {
      toast.error('Failed to update deal stage');
      console.error('Error updating deal stage:', error);
    }
  };

  const handleDeleteDeal = async (dealId) => {
    if (window.confirm('Are you sure you want to delete this deal?')) {
      try {
        await api.delete(`/deals/${dealId}`);
        toast.success('Deal deleted successfully');
        fetchDeals();
      } catch (error) {
        toast.error('Failed to delete deal');
        console.error('Error deleting deal:', error);
      }
    }
  };

  const openStageModal = (deal) => {
    setSelectedDeal(deal);
    setStageFormData({ newStageId: deal.stageId.toString() });
    setShowStageModal(true);
  };

  const openEditModal = (deal) => {
    setSelectedDeal(deal);
    setFormData({
      dealName: deal.dealName,
      amount: deal.amount.toString(),
      expectedCloseDate: deal.expectedCloseDate || '',
      stageId: deal.stageId.toString(),
      contactId: deal.contactId.toString(),
      accountId: deal.accountId.toString(),
      description: deal.description || ''
    });
    setShowEditModal(true);
  };

  const handleUpdateDeal = async (e) => {
    e.preventDefault();
    try {
      const response = await api.put(`/deals/${selectedDeal.dealId}`, {
        ...formData,
        amount: parseFloat(formData.amount),
        stageId: parseInt(formData.stageId),
        contactId: parseInt(formData.contactId),
        accountId: parseInt(formData.accountId)
      });
      if (response.data.success) {
        toast.success('Deal updated successfully');
        setShowEditModal(false);
        setSelectedDeal(null);
        setFormData({
          dealName: '',
          amount: '',
          expectedCloseDate: '',
          stageId: '',
          contactId: '',
          accountId: '',
          description: ''
        });
        fetchDeals();
      }
    } catch (error) {
      toast.error('Failed to update deal');
      console.error('Error updating deal:', error);
    }
  };

  const filteredDeals = deals.filter(deal => {
    const matchesSearch = deal.dealName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         deal.contactName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         deal.accountName?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStage = selectedStage === 'all' || deal.stageId.toString() === selectedStage;
    return matchesSearch && matchesStage;
  });

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStageColor = (stageName) => {
    const lowerStageName = stageName?.toLowerCase();
    if (lowerStageName?.includes('won') || lowerStageName?.includes('closed won')) {
      return 'bg-green-100 text-green-800';
    } else if (lowerStageName?.includes('lost') || lowerStageName?.includes('closed lost')) {
      return 'bg-red-100 text-red-800';
    } else if (lowerStageName?.includes('proposal') || lowerStageName?.includes('negotiation')) {
      return 'bg-yellow-100 text-yellow-800';
    } else {
      return 'bg-blue-100 text-blue-800';
    }
  };



  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Deals</h1>
          <p className="text-gray-600 mt-1">Manage your sales pipeline and track deals</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="btn-primary flex items-center"
        >
          <Plus className="h-4 w-4 mr-2" />
          New Deal
        </button>
      </div>

      {/* Filters and Search */}
      <div className="card mb-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search deals, contacts, or accounts..."
                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
          <div className="sm:w-48">
            <select
              value={selectedStage}
              onChange={(e) => setSelectedStage(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="all">All Stages</option>
              {stages.map(stage => (
                <option key={stage.stageId} value={stage.stageId}>
                  {stage.stageName}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Deals Grid */}
      <div className="grid gap-6">
        {filteredDeals.length === 0 ? (
          <div className="card text-center py-12">
            <TrendingUp className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No deals found</h3>
            <p className="text-gray-600 mb-4">
              {searchTerm || selectedStage !== 'all'
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by creating your first deal'}
            </p>
            {!searchTerm && selectedStage === 'all' && (
              <button
                onClick={() => setShowCreateModal(true)}
                className="btn-primary"
              >
                Create Deal
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDeals.map((deal) => (
              <div key={deal.dealId} className="card hover:shadow-lg transition-shadow">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">
                      {deal.dealName}
                    </h3>
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      getStageColor(deal.stageName)
                    }`}>
                      {deal.stageName}
                    </span>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => openEditModal(deal)}
                      className="text-gray-400 hover:text-blue-600"
                      title="Edit Deal"
                    >
                      <Edit2 className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => openStageModal(deal)}
                      className="text-gray-400 hover:text-primary-600"
                      title="Change Stage"
                    >
                      <TrendingUp className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => handleDeleteDeal(deal.dealId)}
                      className="text-gray-400 hover:text-red-600"
                      title="Delete Deal"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                <div className="space-y-3">
                  <div className="flex items-center text-sm text-gray-600">
                    <DollarSign className="h-4 w-4 mr-2 text-green-600" />
                    <span className="font-semibold text-gray-900">
                      {formatCurrency(deal.amount)}
                    </span>
                  </div>

                  {deal.expectedCloseDate && (
                    <div className="flex items-center text-sm text-gray-600">
                      <Calendar className="h-4 w-4 mr-2" />
                      <span>Expected Close: {formatDate(deal.expectedCloseDate)}</span>
                    </div>
                  )}

                  {deal.contactName && (
                    <div className="flex items-center text-sm text-gray-600">
                      <User className="h-4 w-4 mr-2" />
                      <span>{deal.contactName}</span>
                    </div>
                  )}

                  {deal.accountName && (
                    <div className="flex items-center text-sm text-gray-600">
                      <Building className="h-4 w-4 mr-2" />
                      <span>{deal.accountName}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create Deal Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center p-6 border-b">
              <h2 className="text-xl font-semibold text-gray-900">Create New Deal</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            <form onSubmit={handleCreateDeal} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Deal Name *
                </label>
                <input
                  type="text"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.dealName}
                  onChange={(e) => setFormData({ ...formData, dealName: e.target.value })}
                  placeholder="Enter deal name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Amount *
                </label>
                <input
                  type="number"
                  required
                  min="0"
                  step="0.01"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                  placeholder="0.00"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expected Close Date
                </label>
                <input
                  type="date"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.expectedCloseDate}
                  onChange={(e) => setFormData({ ...formData, expectedCloseDate: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Stage *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.stageId}
                  onChange={(e) => setFormData({ ...formData, stageId: e.target.value })}
                >
                  <option value="">Select a stage</option>
                  {stages.map(stage => (
                    <option key={stage.stageId} value={stage.stageId}>
                      {stage.stageName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Contact *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.contactId}
                  onChange={(e) => setFormData({ ...formData, contactId: e.target.value })}
                >
                  <option value="">Select a contact</option>
                  {contacts.map(contact => (
                    <option key={contact.contactId} value={contact.contactId}>
                      {contact.firstName} {contact.lastName} - {contact.primaryEmail}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Account *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.accountId}
                  onChange={(e) => setFormData({ ...formData, accountId: e.target.value })}
                >
                  <option value="">Select an account</option>
                  {accounts.map(account => (
                    <option key={account.accountId} value={account.accountId}>
                      {account.accountName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Create Deal
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Update Stage Modal */}
      {showStageModal && selectedDeal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="flex justify-between items-center p-6 border-b">
              <h2 className="text-xl font-semibold text-gray-900">Update Deal Stage</h2>
              <button
                onClick={() => setShowStageModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            <form onSubmit={handleUpdateStage} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Deal: {selectedDeal.dealName}
                </label>
                <p className="text-sm text-gray-600 mb-4">
                  Current Stage: <span className="font-medium">{selectedDeal.stageName}</span>
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  New Stage *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={stageFormData.newStageId}
                  onChange={(e) => setStageFormData({ ...stageFormData, newStageId: e.target.value })}
                >
                  <option value="">Select a stage</option>
                  {stages.map(stage => (
                    <option key={stage.stageId} value={stage.stageId}>
                      {stage.stageName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowStageModal(false)}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Update Stage
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Deal Modal */}
      {showEditModal && selectedDeal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center p-6 border-b">
              <h2 className="text-xl font-semibold text-gray-900">Edit Deal</h2>
              <button
                onClick={() => setShowEditModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            <form onSubmit={handleUpdateDeal} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Deal Name *
                </label>
                <input
                  type="text"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.dealName}
                  onChange={(e) => setFormData({ ...formData, dealName: e.target.value })}
                  placeholder="Enter deal name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Amount *
                </label>
                <input
                  type="number"
                  required
                  min="0"
                  step="0.01"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                  placeholder="0.00"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expected Close Date
                </label>
                <input
                  type="date"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.expectedCloseDate}
                  onChange={(e) => setFormData({ ...formData, expectedCloseDate: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Stage *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.stageId}
                  onChange={(e) => setFormData({ ...formData, stageId: e.target.value })}
                >
                  <option value="">Select a stage</option>
                  {stages.map(stage => (
                    <option key={stage.stageId} value={stage.stageId}>
                      {stage.stageName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Contact *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.contactId}
                  onChange={(e) => setFormData({ ...formData, contactId: e.target.value })}
                >
                  <option value="">Select a contact</option>
                  {contacts.map(contact => (
                    <option key={contact.contactId} value={contact.contactId}>
                      {contact.firstName} {contact.lastName} - {contact.primaryEmail}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Account *
                </label>
                <select
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  value={formData.accountId}
                  onChange={(e) => setFormData({ ...formData, accountId: e.target.value })}
                >
                  <option value="">Select an account</option>
                  {accounts.map(account => (
                    <option key={account.accountId} value={account.accountId}>
                      {account.accountName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Update Deal
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Deals;

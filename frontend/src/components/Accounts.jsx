import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import api from '../services/api';
import ActivityTimeline from './ActivityTimeline';

const Accounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingAccount, setEditingAccount] = useState(null);
  const [accountContacts, setAccountContacts] = useState([]);
  const [loadingContacts, setLoadingContacts] = useState(false);
  const [formData, setFormData] = useState({
    accountName: '',
    website: '',
    industry: '',
    customFields: {}
  });

  useEffect(() => {
    fetchAccounts();
  }, []);

  const fetchAccounts = async () => {
    try {
      setLoading(true);
      const response = await api.get('/accounts', {
        params: { searchTerm, page: 0, size: 50 }
      });
      if (response.data.success) {
        setAccounts(response.data.data);
      }
    } catch (error) {
      toast.error('Failed to fetch accounts');
      console.error('Error fetching accounts:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAccountContacts = async (accountId) => {
    try {
      setLoadingContacts(true);
      const response = await api.get(`/accounts/${accountId}/contacts`);
      if (response.data.success) {
        setAccountContacts(response.data.data);
      }
    } catch (error) {
      console.error('Error fetching account contacts:', error);
      setAccountContacts([]);
    } finally {
      setLoadingContacts(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchAccounts();
  };

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/accounts', formData);
      if (response.data.success) {
        toast.success('Account created successfully');
        setShowCreateModal(false);
        resetForm();
        fetchAccounts();
      }
    } catch (error) {
      if (error.response?.data?.errorCode === 'ACCOUNT_DUPLICATE') {
        toast.error('Account with this name already exists');
      } else {
        toast.error('Failed to create account');
      }
      console.error('Error creating account:', error);
    }
  };

  const handleEditAccount = async (e) => {
    e.preventDefault();
    try {
      const response = await api.put(`/accounts/${editingAccount.accountId}`, formData);
      if (response.data.success) {
        toast.success('Account updated successfully');
        setShowEditModal(false);
        setEditingAccount(null);
        resetForm();
        fetchAccounts();
      }
    } catch (error) {
      toast.error('Failed to update account');
      console.error('Error updating account:', error);
    }
  };

  const resetForm = () => {
    setFormData({
      accountName: '',
      website: '',
      industry: '',
      customFields: {}
    });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleCustomFieldChange = (fieldName, value) => {
    setFormData(prev => ({
      ...prev,
      customFields: {
        ...prev.customFields,
        [fieldName]: value
      }
    }));
  };

  const addCustomField = () => {
    const fieldName = prompt('Enter custom field name:');
    if (fieldName && !formData.customFields[fieldName]) {
      handleCustomFieldChange(fieldName, '');
    }
  };

  const removeCustomField = (fieldName) => {
    setFormData(prev => {
      const newCustomFields = { ...prev.customFields };
      delete newCustomFields[fieldName];
      return {
        ...prev,
        customFields: newCustomFields
      };
    });
  };

  const viewAccountDetails = async (accountId) => {
    try {
      const response = await api.get(`/accounts/${accountId}`);
      if (response.data.success) {
        setSelectedAccount(response.data.data);
        // Fetch associated contacts
        fetchAccountContacts(accountId);
      }
    } catch (error) {
      toast.error('Failed to fetch account details');
      console.error('Error fetching account details:', error);
    }
  };

  const openEditModal = (account) => {
    setEditingAccount(account);
    setFormData({
      accountName: account.accountName || '',
      website: account.website || '',
      industry: account.industry || '',
      customFields: account.customFields || {}
    });
    setShowEditModal(true);
  };

  const getAccountContacts = async (accountId) => {
    try {
      const response = await api.get(`/accounts/${accountId}/contacts`);
      if (response.data.success) {
        return response.data.data;
      }
      return [];
    } catch (error) {
      console.error('Error fetching account contacts:', error);
      return [];
    }
  };

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4 mb-6 sm:mb-8">
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Accounts</h1>
        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors w-full sm:w-auto"
        >
          Add Account
        </button>
      </div>

      {/* Search Bar */}
      <div className="card mb-6">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4">
          <input
            type="text"
            placeholder="Search accounts by name or industry..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors w-full sm:w-auto"
          >
            Search
          </button>
        </form>
      </div>

      {/* Accounts List */}
      <div className="card">
        {loading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-2 text-gray-600">Loading accounts...</p>
          </div>
        ) : accounts.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600">No accounts found</p>
          </div>
        ) : (
          <>
            {/* Desktop Table View */}
            <div className="hidden lg:block overflow-x-auto">
              <table className="min-w-full">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Account Name</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Website</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Industry</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Created</th>
                    <th className="text-left py-3 px-4 font-semibold text-gray-700">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {accounts.map((account) => (
                    <tr key={account.accountId} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="py-3 px-4 font-medium">{account.accountName}</td>
                      <td className="py-3 px-4">
                        {account.website ? (
                          <a 
                            href={account.website.startsWith('http') ? account.website : `https://${account.website}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-blue-600 hover:text-blue-800"
                          >
                            {account.website}
                          </a>
                        ) : '-'}
                      </td>
                      <td className="py-3 px-4">{account.industry || '-'}</td>
                      <td className="py-3 px-4">
                        {new Date(account.createdAt).toLocaleDateString()}
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => viewAccountDetails(account.accountId)}
                            className="text-blue-600 hover:text-blue-800 text-sm"
                          >
                            View
                          </button>
                          <button
                            onClick={() => openEditModal(account)}
                            className="text-green-600 hover:text-green-800 text-sm"
                          >
                            Edit
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            
            {/* Mobile Card View */}
            <div className="lg:hidden space-y-4">
              {accounts.map((account) => (
                <div key={account.accountId} className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <h3 className="font-semibold text-gray-900">{account.accountName}</h3>
                      {account.website && (
                        <a 
                          href={account.website.startsWith('http') ? account.website : `https://${account.website}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-sm text-blue-600 hover:text-blue-800"
                        >
                          {account.website}
                        </a>
                      )}
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => viewAccountDetails(account.accountId)}
                        className="text-blue-600 hover:text-blue-800 text-sm px-2 py-1 border border-blue-200 rounded"
                      >
                        View
                      </button>
                      <button
                        onClick={() => openEditModal(account)}
                        className="text-green-600 hover:text-green-800 text-sm px-2 py-1 border border-green-200 rounded"
                      >
                        Edit
                      </button>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <span className="font-medium text-gray-500">Industry:</span>
                      <p className="text-gray-900">{account.industry || '-'}</p>
                    </div>
                    <div>
                      <span className="font-medium text-gray-500">Created:</span>
                      <p className="text-gray-900">{new Date(account.createdAt).toLocaleDateString()}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>

      {/* Create Account Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-4 sm:p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg sm:text-xl font-semibold">Create New Account</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>
            
            <form onSubmit={handleCreateAccount} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Account Name *
                  </label>
                  <input
                    type="text"
                    name="accountName"
                    value={newAccount.accountName}
                    onChange={handleInputChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Website
                  </label>
                  <input
                    type="url"
                    name="website"
                    value={newAccount.website}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Industry
                  </label>
                  <input
                    type="text"
                    name="industry"
                    value={newAccount.industry}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Phone
                  </label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={newAccount.phoneNumber}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Address
                </label>
                <textarea
                  name="address"
                  value={newAccount.address}
                  onChange={handleInputChange}
                  rows="3"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              
              <div className="flex flex-col sm:flex-row justify-end space-y-2 sm:space-y-0 sm:space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 w-full sm:w-auto"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 w-full sm:w-auto"
                >
                  Create Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Account Details Modal */}
      {selectedAccount && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-4 sm:p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">Account Details</h2>
              <button
                onClick={() => setSelectedAccount(null)}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Account Name</label>
                <p className="text-gray-900 font-medium">{selectedAccount.accountName}</p>
              </div>

              {selectedAccount.website && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">Website</label>
                  <a 
                    href={selectedAccount.website.startsWith('http') ? selectedAccount.website : `https://${selectedAccount.website}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-600 hover:text-blue-800"
                  >
                    {selectedAccount.website}
                  </a>
                </div>
              )}

              {selectedAccount.industry && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">Industry</label>
                  <p className="text-gray-900">{selectedAccount.industry}</p>
                </div>
              )}

              {selectedAccount.customFields && Object.keys(selectedAccount.customFields).length > 0 && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Custom Fields</label>
                  <div className="space-y-2">
                    {Object.entries(selectedAccount.customFields).map(([fieldName, value]) => (
                      <div key={fieldName} className="flex justify-between">
                        <span className="font-medium">{fieldName}:</span>
                        <span>{value}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700">Created At</label>
                <p className="text-gray-900">
                  {new Date(selectedAccount.createdAt).toLocaleDateString()}
                </p>
              </div>

              {/* Associated Contacts Section */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Associated Contacts</label>
                <div className="border border-gray-200 rounded-lg p-3 bg-gray-50">
                  {loadingContacts ? (
                    <p className="text-sm text-gray-600">Loading contacts...</p>
                  ) : accountContacts.length > 0 ? (
                    <div className="space-y-2">
                      {accountContacts.map((contact) => (
                        <div key={contact.contactId} className="flex justify-between items-center p-2 bg-white rounded border">
                          <div>
                            <p className="font-medium text-gray-900">
                              {contact.firstName} {contact.lastName}
                            </p>
                            <p className="text-sm text-gray-600">{contact.primaryEmail}</p>
                          </div>
                          <div className="text-xs text-gray-500">
                            {contact.jobTitle && <span>{contact.jobTitle}</span>}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-gray-600">No contacts associated with this account</p>
                  )}
                </div>
              </div>

              {/* Activity Timeline Section */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Activity Timeline</label>
                <ActivityTimeline 
                  entityType="Account" 
                  entityId={selectedAccount.accountId}
                  entityName={selectedAccount.accountName}
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Edit Account Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">Edit Account</h2>
              <button
                onClick={() => {
                  setShowEditModal(false);
                  setEditingAccount(null);
                  resetForm();
                }}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleEditAccount} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Account Name *
                </label>
                <input
                  type="text"
                  name="accountName"
                  value={formData.accountName}
                  onChange={handleInputChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Website
                </label>
                <input
                  type="url"
                  name="website"
                  value={formData.website}
                  onChange={handleInputChange}
                  placeholder="https://example.com"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Industry
                </label>
                <select
                  name="industry"
                  value={formData.industry}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Select Industry</option>
                  <option value="Technology">Technology</option>
                  <option value="Healthcare">Healthcare</option>
                  <option value="Finance">Finance</option>
                  <option value="Education">Education</option>
                  <option value="Manufacturing">Manufacturing</option>
                  <option value="Retail">Retail</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              {/* Custom Fields */}
              <div>
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-sm font-medium text-gray-700">
                    Custom Fields
                  </label>
                  <button
                    type="button"
                    onClick={addCustomField}
                    className="text-blue-600 hover:text-blue-800 text-sm"
                  >
                    + Add Field
                  </button>
                </div>
                {Object.entries(formData.customFields).map(([fieldName, value]) => (
                  <div key={fieldName} className="flex gap-2 mb-2">
                    <input
                      type="text"
                      value={fieldName}
                      onChange={(e) => {
                        const newCustomFields = { ...formData.customFields };
                        delete newCustomFields[fieldName];
                        newCustomFields[e.target.value] = value;
                        setFormData({ ...formData, customFields: newCustomFields });
                      }}
                      placeholder="Field name"
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <input
                      type="text"
                      value={value}
                      onChange={(e) => handleCustomFieldChange(fieldName, e.target.value)}
                      placeholder="Field value"
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                      type="button"
                      onClick={() => removeCustomField(fieldName)}
                      className="px-3 py-2 text-red-600 hover:text-red-800"
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>

              <div className="flex justify-end space-x-2 pt-4">
                <button
                  type="button"
                  onClick={() => {
                    setShowEditModal(false);
                    setEditingAccount(null);
                    resetForm();
                  }}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Update Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Accounts;

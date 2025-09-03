import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import api from '../services/api';
import ActivityTimeline from './ActivityTimeline';
import EditContactModal from './EditContactModal';

const Contacts = () => {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedContact, setSelectedContact] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingContact, setEditingContact] = useState(null);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    primaryEmail: '',
    phoneNumber: '',
    jobTitle: '',
    accountId: '',
    customFields: {}
  });

  useEffect(() => {
    fetchContacts();
  }, []);

  const fetchContacts = async () => {
    try {
      setLoading(true);
      const response = await api.get('/contacts', {
        params: { searchTerm, page: 0, size: 50 }
      });
      if (response.data.success) {
        setContacts(response.data.data);
      }
    } catch (error) {
      toast.error('Failed to fetch contacts');
      console.error('Error fetching contacts:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchContacts();
  };

  const handleCreateContact = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/contacts', formData);
      if (response.data.success) {
        toast.success('Contact created successfully');
        setShowCreateModal(false);
        resetForm();
        fetchContacts();
      }
    } catch (error) {
      if (error.response?.data?.errorCode === 'CONTACT_DUPLICATE') {
        toast.error('Contact with this email already exists');
      } else {
        toast.error('Failed to create contact');
      }
      console.error('Error creating contact:', error);
    }
  };

  const handleEditContact = async (e) => {
    e.preventDefault();
    try {
      const response = await api.put(`/contacts/${editingContact.contactId}`, formData);
      if (response.data.success) {
        toast.success('Contact updated successfully');
        setShowEditModal(false);
        setEditingContact(null);
        resetForm();
        fetchContacts();
      }
    } catch (error) {
      toast.error('Failed to update contact');
      console.error('Error updating contact:', error);
    }
  };

  const resetForm = () => {
    setFormData({
      firstName: '',
      lastName: '',
      primaryEmail: '',
      phoneNumber: '',
      jobTitle: '',
      accountName: '',
      customFields: {}
    });
  };

  const openEditModal = (contact) => {
    setEditingContact(contact);
    setFormData({
      firstName: contact.firstName || '',
      lastName: contact.lastName || '',
      primaryEmail: contact.primaryEmail || '',
      phoneNumber: contact.phoneNumber || '',
      jobTitle: contact.jobTitle || '',
      accountName: contact.account?.accountName || '',
      customFields: contact.customFields || {}
    });
    setShowEditModal(true);
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

  const viewContactDetails = async (contactId) => {
    try {
      const response = await api.get(`/contacts/${contactId}`);
      if (response.data.success) {
        setSelectedContact(response.data.data);
      }
    } catch (error) {
      toast.error('Failed to fetch contact details');
      console.error('Error fetching contact details:', error);
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Contacts</h1>
        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          Add Contact
        </button>
      </div>

      {/* Search Bar */}
      <div className="card mb-6">
        <form onSubmit={handleSearch} className="flex gap-4">
          <input
            type="text"
            placeholder="Search contacts by name or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {/* Contacts List */}
      <div className="card">
        {loading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-2 text-gray-600">Loading contacts...</p>
          </div>
        ) : contacts.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600">No contacts found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4 font-semibold text-gray-700">Name</th>
                  <th className="text-left py-3 px-4 font-semibold text-gray-700">Email</th>
                  <th className="text-left py-3 px-4 font-semibold text-gray-700">Phone</th>
                  <th className="text-left py-3 px-4 font-semibold text-gray-700">Job Title</th>
                  <th className="text-left py-3 px-4 font-semibold text-gray-700">Account</th>
                  <th className="text-left py-3 px-4 font-semibold text-gray-700">Actions</th>
                </tr>
              </thead>
              <tbody>
                {contacts.map((contact) => (
                  <tr key={contact.contactId} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="py-3 px-4">
                      {contact.firstName} {contact.lastName}
                    </td>
                    <td className="py-3 px-4">{contact.primaryEmail}</td>
                    <td className="py-3 px-4">{contact.phoneNumber || '-'}</td>
                    <td className="py-3 px-4">{contact.jobTitle || '-'}</td>
                    <td className="py-3 px-4">{contact.account?.accountName || '-'}</td>
                    <td className="py-3 px-4">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => viewContactDetails(contact.contactId)}
                          className="text-blue-600 hover:text-blue-800 text-sm"
                        >
                          View
                        </button>
                        <button
                          onClick={() => openEditModal(contact)}
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
        )}
      </div>

      {/* Create Contact Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">Create New Contact</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateContact} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    First Name
                  </label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Last Name *
                  </label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </label>
                <input
                  type="email"
                  name="primaryEmail"
                  value={formData.primaryEmail}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Phone Number
                  </label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Job Title
                  </label>
                  <input
                    type="text"
                    name="jobTitle"
                    value={formData.jobTitle}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Account Name
                </label>
                <input
                  type="text"
                  name="accountName"
                  value={formData.accountName}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
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
                      disabled
                      className="w-1/3 px-3 py-2 border border-gray-300 rounded-lg bg-gray-100"
                    />
                    <input
                      type="text"
                      value={value}
                      onChange={(e) => handleCustomFieldChange(fieldName, e.target.value)}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                      type="button"
                      onClick={() => removeCustomField(fieldName)}
                      className="text-red-600 hover:text-red-800 px-2"
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Create Contact
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Contact Details Modal */}
      {selectedContact && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">Contact Details</h2>
              <button
                onClick={() => setSelectedContact(null)}
                className="text-gray-500 hover:text-gray-700"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Name</label>
                  <p className="text-gray-900">{selectedContact.firstName} {selectedContact.lastName}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Email</label>
                  <p className="text-gray-900">{selectedContact.primaryEmail || '-'}</p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Phone</label>
                  <p className="text-gray-900">{selectedContact.phoneNumber || '-'}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Job Title</label>
                  <p className="text-gray-900">{selectedContact.jobTitle || '-'}</p>
                </div>
              </div>

              {selectedContact.account && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">Account</label>
                  <p className="text-gray-900">{selectedContact.account.accountName}</p>
                </div>
              )}

              {selectedContact.customFields && Object.keys(selectedContact.customFields).length > 0 && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Custom Fields</label>
                  <div className="space-y-2">
                    {Object.entries(selectedContact.customFields).map(([fieldName, value]) => (
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
                  {new Date(selectedContact.createdAt).toLocaleDateString()}
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Edit Contact Modal */}
      <EditContactModal
        showEditModal={showEditModal}
        setShowEditModal={setShowEditModal}
        editingContact={editingContact}
        setEditingContact={setEditingContact}
        formData={formData}
        setFormData={setFormData}
        handleEditContact={handleEditContact}
        handleInputChange={handleInputChange}
        addCustomField={addCustomField}
        removeCustomField={removeCustomField}
        resetForm={resetForm}
      />
    </div>
  );
};

export default Contacts;
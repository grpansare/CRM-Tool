import React, { useState } from 'react';
import { toast } from 'react-hot-toast';
import { X, User, Building, TrendingUp } from 'lucide-react';
import api from '../services/api';

const ConvertLeadModal = ({ show, onClose, onSuccess, lead }) => {
  const [formData, setFormData] = useState({
    createAccount: false,
    accountName: '',
    accountWebsite: '',
    accountIndustry: '',
    dealAmount: lead?.estimatedValue || ''
  });
  const [loading, setLoading] = useState(false);

  React.useEffect(() => {
    if (lead && show) {
      setFormData({
        createAccount: false,
        accountName: lead.company || '',
        accountWebsite: lead.website || '',
        accountIndustry: lead.industry || '',
        dealAmount: lead.estimatedValue || ''
      });
    }
  }, [lead, show]);

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const payload = {
        leadId: lead.leadId,
        createAccount: formData.createAccount,
        accountName: formData.createAccount ? formData.accountName : null,
        accountWebsite: formData.createAccount ? formData.accountWebsite : null,
        accountIndustry: formData.createAccount ? formData.accountIndustry : null,
        dealAmount: formData.dealAmount ? parseFloat(formData.dealAmount) : null
      };

      const response = await api.post('/leads/convert', payload);

      if (response.data.success) {
        toast.success('Lead converted successfully');
        onSuccess();
      }
    } catch (error) {
      if (error.response?.data?.errorCode === 'ALREADY_CONVERTED') {
        toast.error('Lead is already converted');
      } else if (error.response?.data?.errorCode === 'NOT_FOUND') {
        toast.error('Lead not found');
      } else if (error.response?.data?.message?.includes('must be QUALIFIED or higher status')) {
        toast.error('Please qualify the lead before converting it');
      } else {
        toast.error('Failed to convert lead');
      }
      console.error('Error converting lead:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!show || !lead) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg p-4 sm:p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">Convert Lead</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <div className="mb-6 p-4 bg-blue-50 rounded-lg">
          <h3 className="font-medium text-blue-900 mb-2">Lead Information</h3>
          <div className="text-sm text-blue-800 space-y-1">
            <p><strong>Name:</strong> {lead.firstName} {lead.lastName}</p>
            {lead.email && <p><strong>Email:</strong> {lead.email}</p>}
            {lead.company && <p><strong>Company:</strong> {lead.company}</p>}
            {lead.jobTitle && <p><strong>Job Title:</strong> {lead.jobTitle}</p>}
          </div>
        </div>

        <div className="mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <TrendingUp className="h-5 w-5 text-green-600" />
            <span className="font-medium text-green-600">Deal Information</span>
          </div>
          <div className="ml-7 space-y-4">
            <div>
              <label htmlFor="dealAmount" className="block text-sm font-medium text-gray-700 mb-1">
                Deal Amount ({lead?.currency || 'USD'})
              </label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span className="text-gray-500 sm:text-sm">$</span>
                </div>
                <input
                  type="number"
                  name="dealAmount"
                  id="dealAmount"
                  min="0.01"
                  step="0.01"
                  value={formData.dealAmount}
                  onChange={handleInputChange}
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-7 pr-12 sm:text-sm border-gray-300 rounded-md"
                  placeholder="0.00"
                  required
                />
                <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                  <span className="text-gray-500 sm:text-sm">
                    {lead?.currency || 'USD'}
                  </span>
                </div>
              </div>
              <p className="mt-1 text-sm text-gray-500">
                Enter the expected value of this deal
              </p>
            </div>
          </div>
        </div>

        <div className="mb-6">
          <div className="flex items-center space-x-2 mb-4">
            <User className="h-5 w-5 text-green-600" />
            <span className="font-medium text-green-600">Contact will be created</span>
          </div>
          <div className="text-sm text-gray-600 ml-7">
            A new contact will be created with the lead's information including name, email, phone, and job title.
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="border rounded-lg p-4">
            <div className="flex items-center space-x-2 mb-3">
              <input
                type="checkbox"
                id="createAccount"
                name="createAccount"
                checked={formData.createAccount}
                onChange={handleInputChange}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <Building className="h-5 w-5 text-blue-600" />
              <label htmlFor="createAccount" className="font-medium text-gray-900">
                Also create Account
              </label>
            </div>

            {formData.createAccount && (
              <div className="ml-6 space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Account Name *
                  </label>
                  <input
                    type="text"
                    name="accountName"
                    value={formData.accountName}
                    onChange={handleInputChange}
                    required={formData.createAccount}
                    placeholder="Enter account name"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Website
                  </label>
                  <input
                    type="url"
                    name="accountWebsite"
                    value={formData.accountWebsite}
                    onChange={handleInputChange}
                    placeholder="https://example.com"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Industry
                  </label>
                  <input
                    type="text"
                    name="accountIndustry"
                    value={formData.accountIndustry}
                    onChange={handleInputChange}
                    placeholder="e.g., Technology, Healthcare"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            )}
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex items-start space-x-2">
              <div className="text-yellow-600 mt-0.5">⚠️</div>
              <div className="text-sm text-yellow-800">
                <strong>Note:</strong> Converting this lead will:
                <ul className="mt-1 ml-4 list-disc space-y-1">
                  <li>Change the lead status to "Converted"</li>
                  <li>Create a new contact with the lead's information</li>
                  {formData.createAccount && <li>Create a new account with the specified details</li>}
                  <li>Link the lead to the created contact{formData.createAccount ? ' and account' : ''}</li>
                </ul>
                This action cannot be undone.
              </div>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row justify-end gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 w-full sm:w-auto"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || (formData.createAccount && !formData.accountName.trim())}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 w-full sm:w-auto"
            >
              {loading ? 'Converting...' : 'Convert Lead'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ConvertLeadModal;

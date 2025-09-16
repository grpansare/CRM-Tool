import React, { useState, useEffect } from 'react';
import {
  Plus,
  Pencil,
  Trash2,
  Eye,
  X,
  Mail,
  FileText
} from "lucide-react";
import { toast } from 'react-hot-toast';

const EmailTemplates = () => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState(null);
  const [formData, setFormData] = useState({
    templateName: '',
    subject: '',
    htmlContent: '',
    textContent: '',
    templateType: 'FOLLOW_UP',
    isActive: true
  });

  const templateTypes = [
    { value: 'FOLLOW_UP', label: 'Follow Up' },
    { value: 'PROPOSAL', label: 'Proposal' },
    { value: 'MEETING_REMINDER', label: 'Meeting Reminder' },
    { value: 'DEAL_UPDATE', label: 'Deal Update' },
    { value: 'MARKETING', label: 'Marketing' },
    { value: 'NOTIFICATION', label: 'Notification' }
  ];

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await fetch('/api/email/templates', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setTemplates(data);
      } else {
        toast.error('Failed to load email templates');
      }
    } catch (error) {
      console.error('Error fetching templates:', error);
      toast.error('Error loading templates');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const token = localStorage.getItem('token');
      const url = editingTemplate 
        ? `/api/email/templates/${editingTemplate.id}`
        : '/api/email/templates';
      
      const method = editingTemplate ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        toast.success(`Template ${editingTemplate ? 'updated' : 'created'} successfully`);
        setShowModal(false);
        setEditingTemplate(null);
        resetForm();
        fetchTemplates();
      } else {
        toast.error(`Failed to ${editingTemplate ? 'update' : 'create'} template`);
      }
    } catch (error) {
      console.error('Error saving template:', error);
      toast.error('Error saving template');
    }
  };

  const handleEdit = (template) => {
    setEditingTemplate(template);
    setFormData({
      templateName: template.templateName,
      subject: template.subject,
      htmlContent: template.htmlContent,
      textContent: template.textContent || '',
      templateType: template.templateType,
      isActive: template.isActive
    });
    setShowModal(true);
  };

  const handleDelete = async (templateId) => {
    if (!confirm('Are you sure you want to delete this template?')) return;

    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/email/templates/${templateId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        toast.success('Template deleted successfully');
        fetchTemplates();
      } else {
        toast.error('Failed to delete template');
      }
    } catch (error) {
      console.error('Error deleting template:', error);
      toast.error('Error deleting template');
    }
  };

  const resetForm = () => {
    setFormData({
      templateName: '',
      subject: '',
      htmlContent: '',
      textContent: '',
      templateType: 'FOLLOW_UP',
      isActive: true
    });
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
          <div className="space-y-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-white p-6 rounded-lg shadow">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Email Templates</h1>
          <p className="text-gray-600">Manage your email templates for automated workflows</p>
        </div>
        <button
          onClick={() => {
            resetForm();
            setEditingTemplate(null);
            setShowModal(true);
          }}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700"
        >
          <Plus className="h-5 w-5 mr-2" />
          New Template
        </button>
      </div>

      {templates.length === 0 ? (
        <div className="text-center py-12">
          <FileText className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">No email templates</h3>
          <p className="mt-1 text-sm text-gray-500">Get started by creating your first email template.</p>
          <div className="mt-6">
            <button
              onClick={() => {
                resetForm();
                setEditingTemplate(null);
                setShowModal(true);
              }}
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
            >
              <Plus className="h-5 w-5 mr-2" />
              New Template
            </button>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {templates.map((template) => (
            <div key={template.id} className="bg-white rounded-lg shadow p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center">
                    <Mail className="h-5 w-5 text-blue-600" />
                    <h3 className="text-lg font-medium text-gray-900">{template.templateName}</h3>
                  </div>
                  <p className="text-sm text-gray-600 mt-1">{template.subject}</p>
                  <div className="mt-2">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      template.templateType === 'FOLLOW_UP' ? 'bg-blue-100 text-blue-800' :
                      template.templateType === 'PROPOSAL' ? 'bg-green-100 text-green-800' :
                      template.templateType === 'MEETING_REMINDER' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {templateTypes.find(t => t.value === template.templateType)?.label || template.templateType}
                    </span>
                  </div>
                </div>
                <div className="flex items-center space-x-2 ml-4">
                  <button
                    onClick={() => handleEdit(template)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <Pencil className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => handleDelete(template.id)}
                    className="text-gray-400 hover:text-red-600"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
              <div className="mt-4">
                <p className="text-sm text-gray-500 line-clamp-3">
                  {template.htmlContent.replace(/<[^>]*>/g, '').substring(0, 100)}...
                </p>
              </div>
              <div className="mt-4 flex items-center justify-between">
                <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                  template.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                }`}>
                  {template.isActive ? 'Active' : 'Inactive'}
                </span>
                <span className="text-xs text-gray-500">
                  {new Date(template.updatedAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-medium text-gray-900">
                  {editingTemplate ? 'Edit Template' : 'Create New Template'}
                </h3>
                <button
                  onClick={() => {
                    setShowModal(false);
                    setEditingTemplate(null);
                    resetForm();
                  }}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <span className="sr-only">Close</span>
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Template Name</label>
                  <input
                    type="text"
                    name="templateName"
                    value={formData.templateName}
                    onChange={handleInputChange}
                    required
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">Subject</label>
                  <input
                    type="text"
                    name="subject"
                    value={formData.subject}
                    onChange={handleInputChange}
                    required
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Use {{variableName}} for dynamic content"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">Template Type</label>
                  <select
                    name="templateType"
                    value={formData.templateType}
                    onChange={handleInputChange}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  >
                    {templateTypes.map(type => (
                      <option key={type.value} value={type.value}>{type.label}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">HTML Content</label>
                  <textarea
                    name="htmlContent"
                    value={formData.htmlContent}
                    onChange={handleInputChange}
                    required
                    rows={10}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Use {{variableName}} for dynamic content like {{contactName}}, {{dealName}}, etc."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">Text Content (Optional)</label>
                  <textarea
                    name="textContent"
                    value={formData.textContent}
                    onChange={handleInputChange}
                    rows={5}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Plain text version of the email"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    name="isActive"
                    checked={formData.isActive}
                    onChange={handleInputChange}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label className="ml-2 block text-sm text-gray-900">Active</label>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => {
                      setShowModal(false);
                      setEditingTemplate(null);
                      resetForm();
                    }}
                    className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
                  >
                    {editingTemplate ? 'Update' : 'Create'} Template
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EmailTemplates;

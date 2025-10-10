import React, { useState, useEffect } from 'react';
import { X, Send, FileText, User, Mail, ChevronDown } from 'lucide-react';
import { toast } from 'react-hot-toast';
import axios from 'axios';

const EmailComposer = ({ isOpen, onClose, leadId, contactId, recipientEmail, recipientName }) => {
  const [formData, setFormData] = useState({
    recipientEmail: recipientEmail || '',
    subject: '',
    body: '',
    htmlBody: '',
    templateId: null
  });
  
  const [templates, setTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [showTemplateDropdown, setShowTemplateDropdown] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSending, setIsSending] = useState(false);

  // Fetch email templates on component mount
  useEffect(() => {
    if (isOpen) {
      fetchEmailTemplates();
    }
  }, [isOpen]);

  // Update recipient email when prop changes
  useEffect(() => {
    if (recipientEmail) {
      setFormData(prev => ({
        ...prev,
        recipientEmail: recipientEmail
      }));
    }
  }, [recipientEmail]);

  const fetchEmailTemplates = async () => {
    try {
      setIsLoading(true);
      const response = await axios.get('/api/v1/emails/templates');
      
      if (response.data.success) {
        setTemplates(response.data.data || []);
      }
    } catch (error) {
      console.error('Error fetching email templates:', error);
      toast.error('Failed to load email templates');
    } finally {
      setIsLoading(false);
    }
  };

  const handleTemplateSelect = (template) => {
    setSelectedTemplate(template);
    setFormData(prev => ({
      ...prev,
      subject: template.subjectLine,
      body: template.emailBody,
      htmlBody: template.htmlBody || '',
      templateId: template.templateId
    }));
    setShowTemplateDropdown(false);
    
    // Process template variables if we have lead/contact data
    if (leadId || contactId) {
      processTemplateVariables(template);
    }
    
    toast.success(`Template "${template.templateName}" loaded`);
  };

  const processTemplateVariables = async (template) => {
    // This would fetch lead/contact data and replace template variables
    // For now, we'll use placeholder data
    const variables = {
      firstName: recipientName?.split(' ')[0] || 'Valued Customer',
      lastName: recipientName?.split(' ')[1] || '',
      email: recipientEmail || '',
      senderName: 'Sales Representative',
      companyName: 'CRM Platform'
    };

    let processedSubject = template.subjectLine;
    let processedBody = template.emailBody;

    // Replace template variables
    Object.entries(variables).forEach(([key, value]) => {
      const placeholder = `{{${key}}}`;
      processedSubject = processedSubject.replace(new RegExp(placeholder, 'g'), value);
      processedBody = processedBody.replace(new RegExp(placeholder, 'g'), value);
    });

    setFormData(prev => ({
      ...prev,
      subject: processedSubject,
      body: processedBody
    }));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSendEmail = async () => {
    if (!formData.recipientEmail || !formData.subject || !formData.body) {
      toast.error('Please fill in all required fields');
      return;
    }

    try {
      setIsSending(true);

      let endpoint = '/api/v1/emails/send';
      let payload = {
        recipientEmail: formData.recipientEmail,
        subject: formData.subject,
        body: formData.body,
        htmlBody: formData.htmlBody,
        leadId: leadId,
        contactId: contactId,
        templateId: formData.templateId
      };

      // Use specific endpoint if sending to lead
      if (leadId && formData.templateId) {
        endpoint = `/api/v1/emails/send/lead/${leadId}`;
        payload = {
          templateId: formData.templateId,
          customVariables: {} // Add any custom variables here
        };
      } else if (leadId) {
        endpoint = `/api/v1/emails/send/lead/${leadId}/custom`;
        payload = {
          subject: formData.subject,
          body: formData.body,
          htmlBody: formData.htmlBody
        };
      }

      const response = await axios.post(endpoint, payload);

      if (response.data.success) {
        toast.success('Email sent successfully!');
        onClose();
        resetForm();
      } else {
        toast.error(response.data.message || 'Failed to send email');
      }
    } catch (error) {
      console.error('Error sending email:', error);
      toast.error(error.response?.data?.message || 'Failed to send email');
    } finally {
      setIsSending(false);
    }
  };

  const resetForm = () => {
    setFormData({
      recipientEmail: '',
      subject: '',
      body: '',
      htmlBody: '',
      templateId: null
    });
    setSelectedTemplate(null);
  };

  const getTemplateTypeColor = (type) => {
    const colors = {
      'LEAD_WELCOME': 'bg-green-100 text-green-800',
      'FOLLOW_UP': 'bg-blue-100 text-blue-800',
      'NURTURE': 'bg-purple-100 text-purple-800',
      'MEETING_INVITE': 'bg-yellow-100 text-yellow-800',
      'PROPOSAL': 'bg-red-100 text-red-800',
      'CUSTOM': 'bg-gray-100 text-gray-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <Mail className="h-6 w-6 text-blue-600" />
            <h2 className="text-xl font-semibold text-gray-900">Compose Email</h2>
            {recipientName && (
              <span className="text-sm text-gray-500">to {recipientName}</span>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <div className="flex h-[calc(90vh-140px)]">
          {/* Template Sidebar */}
          <div className="w-1/3 border-r border-gray-200 p-4 overflow-y-auto">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Email Templates</h3>
            
            {isLoading ? (
              <div className="text-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                <p className="text-sm text-gray-500 mt-2">Loading templates...</p>
              </div>
            ) : (
              <div className="space-y-2">
                {templates.length === 0 ? (
                  <p className="text-sm text-gray-500 text-center py-8">
                    No email templates available
                  </p>
                ) : (
                  templates.map((template) => (
                    <div
                      key={template.templateId}
                      onClick={() => handleTemplateSelect(template)}
                      className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                        selectedTemplate?.templateId === template.templateId
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <h4 className="font-medium text-gray-900 text-sm">
                          {template.templateName}
                        </h4>
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getTemplateTypeColor(template.templateType)}`}>
                          {template.templateType.replace('_', ' ')}
                        </span>
                      </div>
                      <p className="text-xs text-gray-600 mb-2">
                        {template.subjectLine}
                      </p>
                      <p className="text-xs text-gray-500 line-clamp-2">
                        {template.emailBody.substring(0, 100)}...
                      </p>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>

          {/* Email Composer */}
          <div className="flex-1 p-6 overflow-y-auto">
            <div className="space-y-4">
              {/* Recipient */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  To <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <input
                    type="email"
                    name="recipientEmail"
                    value={formData.recipientEmail}
                    onChange={handleInputChange}
                    className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="recipient@example.com"
                    required
                  />
                </div>
              </div>

              {/* Subject */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Subject <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  name="subject"
                  value={formData.subject}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Email subject"
                  required
                />
              </div>

              {/* Selected Template Info */}
              {selectedTemplate && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                  <div className="flex items-center space-x-2">
                    <FileText className="h-4 w-4 text-blue-600" />
                    <span className="text-sm font-medium text-blue-900">
                      Using template: {selectedTemplate.templateName}
                    </span>
                    <button
                      onClick={() => {
                        setSelectedTemplate(null);
                        setFormData(prev => ({
                          ...prev,
                          subject: '',
                          body: '',
                          htmlBody: '',
                          templateId: null
                        }));
                      }}
                      className="text-blue-600 hover:text-blue-800 text-sm"
                    >
                      Clear
                    </button>
                  </div>
                </div>
              )}

              {/* Email Body */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Message <span className="text-red-500">*</span>
                </label>
                <textarea
                  name="body"
                  value={formData.body}
                  onChange={handleInputChange}
                  rows={12}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                  placeholder="Type your email message here..."
                  required
                />
              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-between pt-4 border-t border-gray-200">
                <div className="flex items-center space-x-2 text-sm text-gray-500">
                  {leadId && (
                    <span className="bg-green-100 text-green-800 px-2 py-1 rounded-full text-xs">
                      Lead Email
                    </span>
                  )}
                  {contactId && (
                    <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs">
                      Contact Email
                    </span>
                  )}
                </div>

                <div className="flex items-center space-x-3">
                  <button
                    onClick={onClose}
                    className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSendEmail}
                    disabled={isSending || !formData.recipientEmail || !formData.subject || !formData.body}
                    className="flex items-center space-x-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {isSending ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        <span>Sending...</span>
                      </>
                    ) : (
                      <>
                        <Send className="h-4 w-4" />
                        <span>Send Email</span>
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmailComposer;

import React, { useState, useEffect } from "react";
import { X, Upload, FileText, AlertCircle } from "lucide-react";

const DocumentUploadModal = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    documentType: "",
    isPublic: false,
    contactId: "",
    accountId: "",
    dealId: "",
    leadId: "",
    taskId: "",
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  
  // Dropdown options
  const [documentTypes, setDocumentTypes] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [deals, setDeals] = useState([]);
  const [leads, setLeads] = useState([]);
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    if (isOpen) {
      fetchDropdownData();
    }
  }, [isOpen]);

  const fetchDropdownData = async () => {
    try {
      const token = localStorage.getItem("token");
      const headers = { Authorization: `Bearer ${token}` };

      // Fetch document types
const typesResponse = await fetch("/api/v1/documents/types", { headers });
if (typesResponse.ok) {
  const typesData = await typesResponse.json();
  // Document types might return a direct array or wrapped in response
  const types = Array.isArray(typesData) ? typesData : (typesData.data || []);
  setDocumentTypes(types);
}

      // Fetch contacts
     // Fetch contacts
const contactsResponse = await fetch("/api/v1/contacts", { headers });
if (contactsResponse.ok) {
  const contactsData = await contactsResponse.json();
  // Handle the API response structure: { success: true, data: { content: [...] } }
  const contacts = contactsData.success ? 
    (contactsData.data?.content || contactsData.data || []) : [];
  setContacts(contacts);
}

     
const accountsResponse = await fetch("/api/v1/accounts", { headers });
if (accountsResponse.ok) {
  const accountsData = await accountsResponse.json();
  const accounts = accountsData.success ? 
    (accountsData.data?.content || accountsData.data || []) : [];
  setAccounts(accounts);
}
      // Fetch deals
const dealsResponse = await fetch("/api/v1/deals", { headers });
if (dealsResponse.ok) {
  const dealsData = await dealsResponse.json();
  console.log("Deals API response:", dealsData); // Debug log
  const deals = dealsData.success ? 
    (dealsData.data?.content || dealsData.data || []) : [];
  console.log("Processed deals:", deals); // Debug log
  setDeals(deals);
} else {
  console.error("Failed to fetch deals:", dealsResponse.status, dealsResponse.statusText);
}
      // Fetch leads
    // Fetch leads
const leadsResponse = await fetch("/api/v1/leads", { headers });
if (leadsResponse.ok) {
  const leadsData = await leadsResponse.json();
  const leads = leadsData.success ? 
    (leadsData.data?.content || leadsData.data || []) : [];
  setLeads(leads);
}
      // Fetch tasks
     // Fetch tasks
const tasksResponse = await fetch("/api/v1/tasks", { headers });
if (tasksResponse.ok) {
  const tasksData = await tasksResponse.json();
  const tasks = tasksData.success ? 
    (tasksData.data?.content || tasksData.data || []) : [];
  setTasks(tasks);
}
    } catch (error) {
      console.error("Error fetching dropdown data:", error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));
  };

  const handleFileSelect = (file) => {
    if (file) {
      // Validate file size (max 50MB)
      if (file.size > 50 * 1024 * 1024) {
        setError("File size must be less than 50MB");
        return;
      }

      setSelectedFile(file);
      setError("");
      
      // Auto-fill title if not provided
      if (!formData.title) {
        setFormData(prev => ({
          ...prev,
          title: file.name
        }));
      }
    }
  };
  
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedFile) {
      setError("Please select a file to upload");
      return;
    }

    if (!formData.title.trim()) {
      setError("Please provide a title for the document");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const uploadFormData = new FormData();
      uploadFormData.append("file", selectedFile);
      uploadFormData.append("title", formData.title);
      
      if (formData.description) {
        uploadFormData.append("description", formData.description);
      }
      if (formData.documentType) {
        uploadFormData.append("documentType", formData.documentType);
      }
      uploadFormData.append("isPublic", formData.isPublic);
      
      // Add related entity IDs if selected
      if (formData.contactId) {
        uploadFormData.append("contactId", formData.contactId);
      }
      if (formData.accountId) {
        uploadFormData.append("accountId", formData.accountId);
      }
      if (formData.dealId) {
        uploadFormData.append("dealId", formData.dealId);
      }
      if (formData.leadId) {
        uploadFormData.append("leadId", formData.leadId);
      }
      if (formData.taskId) {
        uploadFormData.append("taskId", formData.taskId);
      }

      const response = await fetch("/api/v1/documents/upload", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: uploadFormData,
      });

      if (response.ok) {
        onSuccess();
        resetForm();
      }  else {
        let errorMessage = "Failed to upload document";
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch (jsonError) {
          // If response body is empty or not JSON, use status text
          errorMessage = `Failed to upload document (${response.status}: ${response.statusText})`;
        }
        setError(errorMessage);
      }
    } catch (error) {
      setError("Failed to upload document. Please try again.");
      console.error("Upload error:", error);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      title: "",
      description: "",
      documentType: "",
      isPublic: false,
      contactId: "",
      accountId: "",
      dealId: "",
      leadId: "",
      taskId: "",
    });
    setSelectedFile(null);
    setError("");
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  if (!isOpen) return null;
  const handleContactChange = (e) => {
    const contactId = e.target.value;
    console.log("Selected ID:", contactId);
    
    if (!contactId) {
      setFormData(prev => ({
        ...prev,
        contactId: '',
        accountId: '',
        dealId: '',
        leadId: ''
      }));
      return;
    }
  
    // Find the selected contact
    const selectedContact = contacts.find(c => c.contactId === parseInt(contactId));
    console.log("Selected contact:", selectedContact);
    
    if (selectedContact) {
      // Find related data
      const relatedDeal = deals.find(d => 
        d.contactId === parseInt(contactId) || 
        (selectedContact.accountId && d.accountId === selectedContact.accountId)
      );
      
      const relatedLead = leads.find(l => 
        l.email === selectedContact.primaryEmail ||
        l.email === selectedContact.secondaryEmail ||
        (l.firstName === selectedContact.firstName && l.lastName === selectedContact.lastName)
      );
  
      // Debug the actual values we're trying to set
      console.log("Trying to set dealId:", relatedDeal ? relatedDeal.dealId : 'none');
      console.log("Trying to set leadId:", relatedLead ? relatedLead.leadId : 'none');
      console.log("Trying to set accountId:", relatedDeal && relatedDeal.accountId ? String(relatedDeal.accountId) : 'none');
      console.log("Available deal IDs:", deals.map(d => d.dealId));
      console.log("Available lead IDs:", leads.map(l => l.leadId));
      console.log("Available account IDs:", accounts.map(a => a.accountId));
      console.log("Accounts array length:", accounts.length);
      console.log("Contact has accountId?", !!selectedContact.accountId);
      console.log("Selected contact accountId:", selectedContact.accountId);
      console.log("Selected contact account:", accounts.find(a => a.accountId === selectedContact.accountId));
      console.log("Related deal:", relatedDeal);
      console.log("Related lead:", relatedLead);
      console.log("Deals array length:", deals.length);
      console.log("Leads array length:", leads.length);
      console.log("Tasks array length:", tasks.length);
      console.log("Accounts array:", accounts);
      console.log("Deals array:", deals);
      console.log("Leads array:", leads);
      console.log("Tasks array:", tasks);
  
      // Update all fields in a single state update
      setFormData(prev => ({
        ...prev,
        contactId: contactId,
        accountId: relatedDeal && relatedDeal.accountId ? String(relatedDeal.accountId) : '',
        dealId: relatedDeal ? String(relatedDeal.dealId) : '',
        leadId: relatedLead ? String(relatedLead.leadId) : ''
      }));
  
      // Debug logging
      console.log("Auto-selected account:", relatedDeal && relatedDeal.accountId ? String(relatedDeal.accountId) : 'none');
      if (relatedDeal) console.log("Auto-selected deal:", relatedDeal.dealName);
      if (relatedLead) console.log("Auto-selected lead:", relatedLead.firstName, relatedLead.lastName);
    }
  };
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Upload Document</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md flex items-center gap-2">
              <AlertCircle className="h-4 w-4 text-red-500" />
              <span className="text-red-700 text-sm">{error}</span>
            </div>
          )}

          {/* File Upload Area */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              File *
            </label>
            <div
              className={`border-2 border-dashed rounded-lg p-6 text-center ${
                dragActive
                  ? "border-primary-500 bg-primary-50"
                  : selectedFile
                  ? "border-green-500 bg-green-50"
                  : "border-gray-300 hover:border-gray-400"
              }`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
            >
              {selectedFile ? (
                <div className="flex items-center justify-center gap-3">
                  <FileText className="h-8 w-8 text-green-500" />
                  <div className="text-left">
                    <p className="font-medium text-gray-900">{selectedFile.name}</p>
                    <p className="text-sm text-gray-500">{formatFileSize(selectedFile.size)}</p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setSelectedFile(null)}
                    className="text-red-500 hover:text-red-700"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
              ) : (
                <div>
                  <Upload className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-lg font-medium text-gray-900 mb-2">
                    Drop your file here, or{" "}
                    <label className="text-primary-600 hover:text-primary-700 cursor-pointer">
                      browse
                      <input
                        type="file"
                        className="hidden"
                        onChange={(e) => handleFileSelect(e.target.files[0])}
                        accept="*/*"
                      />
                    </label>
                  </p>
                  <p className="text-sm text-gray-500">
                    Maximum file size: 50MB
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Title */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Title *
            </label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              required
            />
          </div>

          {/* Description */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              rows="3"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>

          {/* Document Type */}
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Document Type
            </label>
            <select
              name="documentType"
              value={formData.documentType}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              <option value="">Select type...</option>
              {documentTypes.map((type) => (
                <option key={type} value={type}>
                  {type.replace("_", " ")}
                </option>
              ))}
            </select>
          </div>

          {/* Public/Private */}
          <div className="mb-6">
            <label className="flex items-center">
              <input
                type="checkbox"
                name="isPublic"
                checked={formData.isPublic}
                onChange={handleInputChange}
                className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
              />
              <span className="ml-2 text-sm text-gray-700">
                Make this document public (visible to all users)
              </span>
            </label>
          </div>

          {/* Related Entities */}
          <div className="mb-6">
            <h3 className="text-sm font-medium text-gray-700 mb-3">
              Link to Related Records (Optional)
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Contact */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">Contact</label>
                <select
  name="contactId"
  value={formData.contactId}
  onChange={handleContactChange} // Use new handler
  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
>
  <option value="">Select contact...</option>
  {contacts.map((contact) => (
    <option key={contact.contactId} value={contact.contactId}>
      {contact.firstName} {contact.lastName}
      {contact.account && ` (${contact.account.accountName})`}
    </option>
  ))}
</select>
              </div>

              {/* Account */}
             {/* Account - Show if auto-populated */}
<div>
  <label className="block text-sm text-gray-600 mb-1">
    Account {formData.accountId && <span className="text-green-600 text-xs">(Auto-selected)</span>}
  </label>
  <select
    name="accountId"
    value={formData.accountId}
    onChange={handleInputChange}
    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
  >
    <option value="">Select account...</option>
    {accounts.map((account) => (
      <option key={account.accountId} value={account.accountId}>
        {account.accountName}
      </option>
    ))}
  </select>
</div>

              {/* Deal */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">Deal</label>
                <select
                  name="dealId"
                  value={formData.dealId}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                >
                  <option value="">Select deal...</option>
                  {deals.map((deal) => (
  <option key={deal.dealId} value={deal.dealId}>
    {deal.dealName}
  </option>
))}
                </select>
              </div>

              {/* Lead */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">Lead</label>
                <select
                  name="leadId"
                  value={formData.leadId}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                >
                  <option value="">Select lead...</option>
                  {leads.map((lead) => (
  <option key={lead.leadId} value={lead.leadId}>
    {lead.firstName} {lead.lastName}
  </option>
))}
                </select>
              </div>

              {/* Task */}
              <div className="md:col-span-2">
                <label className="block text-sm text-gray-600 mb-1">Task</label>
                <select
                  name="taskId"
                  value={formData.taskId}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                >
                  <option value="">Select task...</option>
                  {tasks.map((task) => (
                    <option key={task.taskId} value={task.taskId}>
                      {task.title}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-700 border border-gray-300 rounded-md hover:bg-gray-50"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              disabled={loading || !selectedFile}
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  Uploading...
                </>
              ) : (
                <>
                  <Upload className="h-4 w-4" />
                  Upload Document
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DocumentUploadModal;

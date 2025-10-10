import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { Search, Plus, Edit, Edit2, Trash2, Eye, Filter, UserCheck, X, Phone, Mail, FileText, Star, RefreshCw, Building, TrendingUp, Calendar, User, PhoneCall, Clock } from 'lucide-react';
import api from '../services/api';
import LeadModal from './LeadModal';
import ConvertLeadModal from './ConvertLeadModal';
import SetDispositionModal from './modals/SetDispositionModal';
import ActivityTimeline from './ActivityTimeline';

const Leads = () => {
  const [leads, setLeads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [selectedSource, setSelectedSource] = useState('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showConvertModal, setShowConvertModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showDispositionModal, setShowDispositionModal] = useState(false);
  const [selectedLead, setSelectedLead] = useState(null);
  const [editingLead, setEditingLead] = useState(null);

  const leadStatuses = [
    { value: 'NEW', label: 'New', color: 'bg-blue-100 text-blue-800' },
    { value: 'CONTACTED', label: 'Contacted', color: 'bg-yellow-100 text-yellow-800' },
    { value: 'QUALIFIED', label: 'Qualified', color: 'bg-green-100 text-green-800' },
    { value: 'UNQUALIFIED', label: 'Unqualified', color: 'bg-red-100 text-red-800' },
    { value: 'NURTURING', label: 'Nurturing', color: 'bg-purple-100 text-purple-800' },
    { value: 'CONVERTED', label: 'Converted', color: 'bg-emerald-100 text-emerald-800' },
    { value: 'LOST', label: 'Lost', color: 'bg-gray-100 text-gray-800' }
  ];

  const leadSources = [
    'WEBSITE', 'SOCIAL_MEDIA', 'EMAIL_CAMPAIGN', 'COLD_CALL', 'REFERRAL', 
    'TRADE_SHOW', 'WEBINAR', 'CONTENT_DOWNLOAD', 'ADVERTISEMENT', 'PARTNER', 'OTHER'
  ];

  useEffect(() => {
    fetchLeads();
  }, []);

  const fetchLeads = async () => {
    try {
      setLoading(true);
      const params = { page: 0, size: 50 };
      if (searchTerm) params.searchTerm = searchTerm;
      
      const response = await api.get('/leads', { params });
      if (response.data.success) {
        setLeads(response.data.data.content || []);
      }
    } catch (error) {
      toast.error('Failed to fetch leads');
      console.error('Error fetching leads:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchLeads();
  };

  const handleDeleteLead = async (leadId) => {
    if (!window.confirm('Are you sure you want to delete this lead?')) return;

    try {
      await api.delete(`/leads/${leadId}`);
      toast.success('Lead deleted successfully');
      fetchLeads();
    } catch (error) {
      toast.error('Failed to delete lead');
      console.error('Error deleting lead:', error);
    }
  };

  const openEditModal = (lead) => {
    setEditingLead(lead);
    setShowEditModal(true);
  };

  const openDetailModal = (lead) => {
    setSelectedLead(lead);
    setShowDetailModal(true);
  };

  const logQuickActivity = async (type, content) => {
    try {
      const activityData = {
        type: type,
        content: content,
        associations: {
          leads: [selectedLead.leadId]
        }
      };
      
      const response = await api.post('/activities', activityData);
      if (response.data.success) {
        toast.success(`${type.toLowerCase()} logged successfully`);
        
        // Auto-update lead status to CONTACTED for email/call activities
        if ((type === 'EMAIL' || type === 'CALL') && selectedLead.leadStatus === 'NEW') {
          try {
            const updateResponse = await api.put(`/leads/${selectedLead.leadId}`, {
              leadStatus: 'CONTACTED'
            });
            if (updateResponse.data.success) {
              // Update the selected lead status in state
              setSelectedLead(prev => ({
                ...prev,
                leadStatus: 'CONTACTED'
              }));
              toast.success('Lead status updated to CONTACTED');
            }
          } catch (statusError) {
            console.error('Error updating lead status:', statusError);
            // Don't show error toast as the activity was still logged successfully
          }
        }
        
        // The ActivityTimeline component will refresh automatically
      }
    } catch (error) {
      toast.error(`Failed to log ${type.toLowerCase()}`);
      console.error('Error logging activity:', error);
    }
  };

  const openConvertModal = (lead) => {
    setSelectedLead(lead);
    setShowConvertModal(true);
  };

  const openDispositionModal = (lead) => {
    setSelectedLead(lead);
    setShowDispositionModal(true);
  };

  const handleDispositionSet = (updatedLead) => {
    // Update the lead in the list
    setLeads(prevLeads => 
      prevLeads.map(lead => 
        lead.leadId === updatedLead.leadId ? updatedLead : lead
      )
    );
    
    // Update selected lead if it's the same one
    if (selectedLead && selectedLead.leadId === updatedLead.leadId) {
      setSelectedLead(updatedLead);
    }
  };

  const getStatusColor = (status) => {
    const statusObj = leadStatuses.find(s => s.value === status);
    return statusObj ? statusObj.color : 'bg-gray-100 text-gray-800';
  };

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    if (score >= 40) return 'text-orange-600';
    return 'text-red-600';
  };

  const filteredLeads = leads.filter(lead => {
    const matchesStatus = selectedStatus === 'all' || lead.leadStatus === selectedStatus;
    const matchesSource = selectedSource === 'all' || lead.leadSource === selectedSource;
    return matchesStatus && matchesSource;
  });

  const leadsNeedingFollowUp = leads.filter(lead => lead.nextFollowUpDate && new Date(lead.nextFollowUpDate).toLocaleDateString() === new Date().toLocaleDateString());

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4 mb-6 sm:mb-8">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Leads</h1>
          <p className="text-gray-600 mt-1">Manage and convert your sales leads</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center w-full sm:w-auto"
        >
          <Plus className="h-4 w-4 mr-2" />
          Add Lead
        </button>
      </div>

      {/* Follow-up Notification Banner */}
      {leadsNeedingFollowUp.length > 0 && (
        <div className="bg-orange-50 border border-orange-200 rounded-lg p-4 mb-6">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center">
              <Clock className="h-5 w-5 text-orange-600 mr-2" />
              <h3 className="text-lg font-medium text-orange-800">
                {leadsNeedingFollowUp.length} Lead{leadsNeedingFollowUp.length > 1 ? 's' : ''} Need Follow-up Today
              </h3>
            </div>
            <button
              onClick={() => {
                setSelectedStatus('all');
                setSelectedSource('all');
                setSearchTerm('');
                fetchLeads();
              }}
              className="text-orange-600 hover:text-orange-700 text-sm font-medium"
            >
              Refresh
            </button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {leadsNeedingFollowUp.slice(0, 6).map((lead) => (
              <div 
                key={lead.leadId} 
                className="bg-white border border-orange-200 rounded-lg p-3 hover:shadow-md transition-shadow cursor-pointer"
                onClick={() => openDetailModal(lead)}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-gray-900">
                      {lead.firstName} {lead.lastName}
                    </p>
                    <p className="text-sm text-gray-600">{lead.company}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-orange-600">
                      {new Date(lead.nextFollowUpDate).toLocaleDateString()}
                    </p>
                    <p className="text-xs text-orange-600">
                      {new Date(lead.nextFollowUpDate).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
          {leadsNeedingFollowUp.length > 6 && (
            <p className="text-sm text-orange-600 mt-3 text-center">
              +{leadsNeedingFollowUp.length - 6} more leads need follow-up
            </p>
          )}
        </div>
      )}

      {/* Filters and Search */}
      <div className="card mb-6">
        <div className="flex flex-col gap-4">
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search leads by name, email, or company..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <button
              type="submit"
              className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors w-full sm:w-auto"
            >
              Search
            </button>
          </form>
          
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="sm:w-48">
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="all">All Statuses</option>
                {leadStatuses.map(status => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="sm:w-48">
              <select
                value={selectedSource}
                onChange={(e) => setSelectedSource(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="all">All Sources</option>
                {leadSources.map(source => (
                  <option key={source} value={source}>
                    {source.replace('_', ' ')}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Leads Grid */}
      <div className="grid gap-6">
        {filteredLeads.length === 0 ? (
          <div className="card text-center py-12">
            <User className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No leads found</h3>
            <p className="text-gray-600 mb-4">
              {searchTerm || selectedStatus !== 'all' || selectedSource !== 'all'
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by creating your first lead'}
            </p>
            {!searchTerm && selectedStatus === 'all' && selectedSource === 'all' && (
              <button
                onClick={() => setShowCreateModal(true)}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Create Lead
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
            {filteredLeads.map((lead) => (
              <div key={lead.leadId} className="card hover:shadow-lg transition-shadow">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">
                      {lead.firstName ? `${lead.firstName} ${lead.lastName}` : lead.lastName}
                    </h3>
                    <div className="flex items-center gap-2 mb-2">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(lead.leadStatus)}`}>
                        {lead.leadStatus}
                      </span>
                      {lead.leadScore && (
                        <div className="flex items-center">
                          <Star className={`h-4 w-4 ${getScoreColor(lead.leadScore)} mr-1`} />
                          <span className={`text-sm font-medium ${getScoreColor(lead.leadScore)}`}>
                            {lead.leadScore}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => openDetailModal(lead)}
                      className="text-gray-400 hover:text-purple-600 p-1"
                      title="View Details & Activities"
                    >
                      <Eye className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => openEditModal(lead)}
                      className="text-gray-400 hover:text-blue-600 p-1"
                      title="Edit Lead"
                    >
                      <Edit2 className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => openDispositionModal(lead)}
                      className="text-gray-400 hover:text-orange-600 p-1"
                      title="Set Call Disposition"
                    >
                      <PhoneCall className="h-4 w-4" />
                    </button>
                    {lead.leadStatus === 'QUALIFIED' ? (
                      <button
                        onClick={() => openConvertModal(lead)}
                        className="text-gray-400 hover:text-green-600 p-1"
                        title="Convert Lead"
                      >
                        <RefreshCw className="h-4 w-4" />
                      </button>
                    ) : lead.leadStatus === 'CONVERTED' ? null : (
                      <span 
                        className="p-1 text-gray-300 cursor-not-allowed"
                        title="Lead must be qualified before converting"
                      >
                        <RefreshCw className="h-4 w-4" />
                      </span>
                    )}
                    <button
                      onClick={() => handleDeleteLead(lead.leadId)}
                      className="text-gray-400 hover:text-red-600 p-1"
                      title="Delete Lead"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                <div className="space-y-2">
                  {lead.email && (
                    <div className="flex items-center text-sm text-gray-600">
                      <Mail className="h-4 w-4 mr-2" />
                      <span>{lead.email}</span>
                    </div>
                  )}
                  
                  {lead.phoneNumber && (
                    <div className="flex items-center text-sm text-gray-600">
                      <Phone className="h-4 w-4 mr-2" />
                      <span>{lead.phoneNumber}</span>
                    </div>
                  )}
                  
                  {lead.company && (
                    <div className="flex items-center text-sm text-gray-600">
                      <Building className="h-4 w-4 mr-2" />
                      <span>{lead.company}</span>
                    </div>
                  )}
                  
                  {lead.leadSource && (
                    <div className="flex items-center text-sm text-gray-600">
                      <TrendingUp className="h-4 w-4 mr-2" />
                      <span>{lead.leadSource.replace('_', ' ')}</span>
                    </div>
                  )}
                  
                  <div className="flex items-center text-sm text-gray-600">
                    <Calendar className="h-4 w-4 mr-2" />
                    <span>Created: {new Date(lead.createdAt).toLocaleDateString()}</span>
                  </div>
                  {/* Lead Owner Information */}
{lead.ownerUserId && (
  <div className="flex items-center text-sm text-blue-600 bg-blue-50 px-2 py-1 rounded">
    <User className="h-4 w-4 mr-2" />
    <span>Owner: {lead.ownerName || `User ${lead.ownerUserId}`}</span>
  </div>
)}
                  
                  {lead.nextFollowUpDate && (
                    <div className="flex items-center text-sm text-orange-600 bg-orange-50 px-2 py-1 rounded">
                      <Clock className="h-4 w-4 mr-2" />
                      <span>Follow-up: {new Date(lead.nextFollowUpDate).toLocaleDateString()} at {new Date(lead.nextFollowUpDate).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Lead Detail Modal with Activity Timeline */}
      {showDetailModal && selectedLead && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg w-full max-w-4xl max-h-[90vh] overflow-hidden">
            <div className="flex justify-between items-center p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">
                Lead Details: {selectedLead.firstName ? `${selectedLead.firstName} ${selectedLead.lastName}` : selectedLead.lastName}
              </h2>
              <button
                onClick={() => {
                  setShowDetailModal(false);
                  setSelectedLead(null);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>
            
            <div className="flex h-[calc(90vh-80px)]">
              {/* Lead Information Panel */}
              <div className="w-1/3 p-6 border-r border-gray-200 overflow-y-auto">
                <div className="space-y-4">
                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-3">Lead Information</h3>
                    <div className="space-y-3">
                      <div>
                        <label className="text-sm font-medium text-gray-500">Status</label>
                        <div className="mt-1">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(selectedLead.leadStatus)}`}>
                            {selectedLead.leadStatus}
                          </span>
                        </div>
                      </div>
                      
                      <div>
                        <label className="text-sm font-medium text-gray-500">Email</label>
                        <p className="mt-1 text-sm text-gray-900">{selectedLead.email}</p>
                      </div>
                      
                      {selectedLead.phoneNumber && (
                        <div>
                          <label className="text-sm font-medium text-gray-500">Phone</label>
                          <p className="mt-1 text-sm text-gray-900">{selectedLead.phoneNumber}</p>
                        </div>
                      )}
                      
                      {selectedLead.company && (
                        <div>
                          <label className="text-sm font-medium text-gray-500">Company</label>
                          <p className="mt-1 text-sm text-gray-900">{selectedLead.company}</p>
                        </div>
                      )}
                      {selectedLead.ownerUserId && (
  <div>
    <label className="text-sm font-medium text-gray-500">Assigned To</label>
    <p className="mt-1 text-sm text-gray-900">
      {selectedLead.ownerName || `User ${selectedLead.ownerUserId}`}
    </p>
  </div>
)}
                      
                      {selectedLead.jobTitle && (
                        <div>
                          <label className="text-sm font-medium text-gray-500">Job Title</label>
                          <p className="mt-1 text-sm text-gray-900">{selectedLead.jobTitle}</p>
                        </div>
                      )}
                      
                      <div>
                        <label className="text-sm font-medium text-gray-500">Source</label>
                        <p className="mt-1 text-sm text-gray-900">{selectedLead.leadSource}</p>
                      </div>
                      
                      {selectedLead.leadScore && (
                        <div>
                          <label className="text-sm font-medium text-gray-500">Lead Score</label>
                          <p className="mt-1 text-sm text-gray-900">{selectedLead.leadScore}/100</p>
                        </div>
                      )}
                      
                      {selectedLead.notes && (
                        <div>
                          <label className="text-sm font-medium text-gray-500">Notes</label>
                          <p className="mt-1 text-sm text-gray-900">{selectedLead.notes}</p>
                        </div>
                      )}
                      
                      <div>
                        <label className="text-sm font-medium text-gray-500">Created</label>
                        <p className="mt-1 text-sm text-gray-900">
                          {new Date(selectedLead.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                      
                      {selectedLead.nextFollowUpDate && (
                        <div>
                          <label className="text-sm font-medium text-gray-500">Next Follow-up</label>
                          <p className="mt-1 text-sm text-orange-600">
                            {new Date(selectedLead.nextFollowUpDate).toLocaleDateString()} at {new Date(selectedLead.nextFollowUpDate).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  {/* Quick Actions */}
                  <div className="pt-4 border-t border-gray-200">
                    <h4 className="text-sm font-medium text-gray-900 mb-3">Quick Actions</h4>
                    <div className="space-y-2">
                      <button
                        onClick={() => {
                          setShowDetailModal(false);
                          openEditModal(selectedLead);
                        }}
                        className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md flex items-center"
                      >
                        <Edit className="h-4 w-4 mr-2" />
                        Edit Lead
                      </button>
                      {selectedLead.leadStatus === 'QUALIFIED' ? (
                        <button
                          onClick={() => {
                            setShowDetailModal(false);
                            openConvertModal(selectedLead);
                          }}
                          className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-md flex items-center"
                        >
                          <UserCheck className="h-4 w-4 mr-2" />
                          Convert Lead
                        </button>
                      ) : selectedLead.leadStatus !== 'CONVERTED' ? (
                        <div 
                          className="w-full text-left px-3 py-2 text-sm text-gray-400 rounded-md flex items-center cursor-not-allowed"
                          title="Lead must be qualified before converting"
                        >
                          <UserCheck className="h-4 w-4 mr-2" />
                          Convert Lead
                        </div>
                      ) : null}
                    </div>
                  </div>
                  
                  {/* Quick Activity Logging */}
                  <div className="pt-4 border-t border-gray-200">
                    <h4 className="text-sm font-medium text-gray-900 mb-3">Quick Activities</h4>
                    <div className="space-y-2">
                      <button
                        onClick={() => logQuickActivity('CALL', `Called ${selectedLead.firstName || ''} ${selectedLead.lastName}`.trim())}
                        className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-blue-50 rounded-md flex items-center"
                      >
                        <PhoneCall className="h-4 w-4 mr-2 text-blue-600" />
                        Log Call
                      </button>
                      <button
                        onClick={() => logQuickActivity('EMAIL', `Sent email to ${selectedLead.firstName || ''} ${selectedLead.lastName}`.trim())}
                        className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-green-50 rounded-md flex items-center"
                      >
                        <Mail className="h-4 w-4 mr-2 text-green-600" />
                        Log Email
                      </button>
                      <button
                        onClick={() => logQuickActivity('NOTE', `Added note for ${selectedLead.firstName || ''} ${selectedLead.lastName}`.trim())}
                        className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md flex items-center"
                      >
                        <FileText className="h-4 w-4 mr-2 text-gray-600" />
                        Add Note
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* Activity Timeline Panel */}
              <div className="flex-1 p-6 overflow-y-auto">
                <ActivityTimeline leadId={selectedLead.leadId} />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modals */}
      <LeadModal
        show={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={() => {
          setShowCreateModal(false);
          fetchLeads();
        }}
        title="Create New Lead"
      />

      <LeadModal
        show={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setEditingLead(null);
        }}
        onSuccess={() => {
          setShowEditModal(false);
          setEditingLead(null);
          fetchLeads();
        }}
        lead={editingLead}
        title="Edit Lead"
      />

      <ConvertLeadModal
        show={showConvertModal}
        onClose={() => {
          setShowConvertModal(false);
          setSelectedLead(null);
        }}
        onSuccess={() => {
          setShowConvertModal(false);
          setSelectedLead(null);
          fetchLeads();
        }}
        lead={selectedLead}
      />

      <SetDispositionModal
        isOpen={showDispositionModal}
        onClose={() => {
          setShowDispositionModal(false);
          setSelectedLead(null);
        }}
        lead={selectedLead}
        onDispositionSet={handleDispositionSet}
      />
    </div>
  );
};

export default Leads;

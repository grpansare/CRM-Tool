import React, { useState, useEffect } from 'react';
import { X, Phone, Calendar, MessageSquare, AlertCircle, CheckCircle, XCircle, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

const SetDispositionModal = ({ isOpen, onClose, lead, onDispositionSet }) => {
  const [selectedDisposition, setSelectedDisposition] = useState('');
  const [notes, setNotes] = useState('');
  const [nextFollowUpDate, setNextFollowUpDate] = useState('');
  const [nextFollowUpTime, setNextFollowUpTime] = useState('');
  const [loading, setLoading] = useState(false);

  // Disposition categories with icons and colors
  const dispositionCategories = {
    positive: {
      label: 'Positive Outcomes',
      icon: CheckCircle,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
      borderColor: 'border-green-200',
      dispositions: [
        { value: 'INTERESTED', label: 'Interested', description: 'Lead showed interest' },
        { value: 'MEETING_SCHEDULED', label: 'Meeting Scheduled', description: 'Meeting or demo scheduled' },
        { value: 'DEMO_REQUESTED', label: 'Demo Requested', description: 'Lead requested a demo' },
        { value: 'PROPOSAL_SENT', label: 'Proposal Sent', description: 'Proposal or quote sent' }
      ]
    },
    followUp: {
      label: 'Follow-up Required',
      icon: Clock,
      color: 'text-yellow-600',
      bgColor: 'bg-yellow-50',
      borderColor: 'border-yellow-200',
      dispositions: [
        { value: 'CALL_BACK_LATER', label: 'Call Back Later', description: 'Lead requested callback' },
        { value: 'NO_ANSWER', label: 'No Answer', description: 'No one answered the call' },
        { value: 'VOICEMAIL_LEFT', label: 'Voicemail Left', description: 'Left voicemail message' },
        { value: 'EMAIL_SENT', label: 'Email Sent', description: 'Follow-up email sent' },
        { value: 'BUSY', label: 'Busy', description: 'Lead was busy, try later' }
      ]
    },
    negative: {
      label: 'Negative Outcomes',
      icon: XCircle,
      color: 'text-red-600',
      bgColor: 'bg-red-50',
      borderColor: 'border-red-200',
      dispositions: [
        { value: 'NOT_INTERESTED', label: 'Not Interested', description: 'Lead not interested' },
        { value: 'NOT_QUALIFIED', label: 'Not Qualified', description: 'Lead doesn\'t meet criteria' },
        { value: 'WRONG_NUMBER', label: 'Wrong Number', description: 'Incorrect contact information' },
        { value: 'DO_NOT_CALL', label: 'Do Not Call', description: 'Lead requested no further contact' },
        { value: 'COMPETITOR', label: 'Using Competitor', description: 'Already using competitor' },
        { value: 'NO_BUDGET', label: 'No Budget', description: 'No budget available' },
        { value: 'NO_AUTHORITY', label: 'No Authority', description: 'Not decision maker' }
      ]
    },
    administrative: {
      label: 'Administrative',
      icon: AlertCircle,
      color: 'text-gray-600',
      bgColor: 'bg-gray-50',
      borderColor: 'border-gray-200',
      dispositions: [
        { value: 'CONVERTED', label: 'Converted to Deal', description: 'Lead converted to opportunity' },
        { value: 'LOST', label: 'Lost Lead', description: 'Lead is lost' }
      ]
    }
  };

  // Check if disposition requires follow-up
  const requiresFollowUp = (disposition) => {
    const followUpDispositions = ['CALL_BACK_LATER', 'NO_ANSWER', 'VOICEMAIL_LEFT', 'EMAIL_SENT', 'BUSY'];
    return followUpDispositions.includes(disposition);
  };

  useEffect(() => {
    if (isOpen) {
      // Reset form when modal opens
      setSelectedDisposition('');
      setNotes('');
      setNextFollowUpDate('');
      setNextFollowUpTime('');
    }
  }, [isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedDisposition) {
      toast.error('Please select a disposition');
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem('token');
      let nextFollowUpDateTime = null;

      // Combine date and time if both are provided
      if (nextFollowUpDate && nextFollowUpTime) {
        nextFollowUpDateTime = `${nextFollowUpDate}T${nextFollowUpTime}:00`;
      } else if (nextFollowUpDate) {
        nextFollowUpDateTime = `${nextFollowUpDate}T09:00:00`;
      }

      const requestBody = {
        disposition: selectedDisposition,
        notes: notes.trim() || null,
        nextFollowUpDate: nextFollowUpDateTime
      };

      const response = await fetch(`/api/v1/leads/${lead.leadId}/disposition`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(requestBody)
      });

      const data = await response.json();

      if (data.success) {
        toast.success('Disposition set successfully');
        onDispositionSet(data.data);
        onClose();
      } else {
        toast.error(data.message || 'Failed to set disposition');
      }
    } catch (error) {
      console.error('Error setting disposition:', error);
      toast.error('Failed to set disposition');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <Phone className="h-6 w-6 text-blue-600" />
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Set Call Disposition</h2>
              <p className="text-sm text-gray-600">
                {lead?.firstName} {lead?.lastName} - {lead?.company}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6">
          {/* Disposition Selection */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-4">
              Select Call Outcome *
            </label>
            
            <div className="space-y-4">
              {Object.entries(dispositionCategories).map(([categoryKey, category]) => {
                const IconComponent = category.icon;
                return (
                  <div key={categoryKey} className={`border rounded-lg p-4 ${category.borderColor} ${category.bgColor}`}>
                    <div className={`flex items-center space-x-2 mb-3 ${category.color}`}>
                      <IconComponent className="h-5 w-5" />
                      <h3 className="font-medium">{category.label}</h3>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                      {category.dispositions.map((disposition) => (
                        <label
                          key={disposition.value}
                          className={`flex items-start space-x-3 p-3 rounded-lg border cursor-pointer transition-all ${
                            selectedDisposition === disposition.value
                              ? 'border-blue-500 bg-blue-50'
                              : 'border-gray-200 hover:border-gray-300 bg-white'
                          }`}
                        >
                          <input
                            type="radio"
                            name="disposition"
                            value={disposition.value}
                            checked={selectedDisposition === disposition.value}
                            onChange={(e) => setSelectedDisposition(e.target.value)}
                            className="mt-1 text-blue-600 focus:ring-blue-500"
                          />
                          <div className="flex-1 min-w-0">
                            <div className="text-sm font-medium text-gray-900">
                              {disposition.label}
                            </div>
                            <div className="text-xs text-gray-500">
                              {disposition.description}
                            </div>
                          </div>
                        </label>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Notes */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <MessageSquare className="h-4 w-4 inline mr-1" />
              Call Notes
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Add any notes about the call, conversation details, or next steps..."
            />
          </div>

          {/* Follow-up Date (conditional) */}
          {requiresFollowUp(selectedDisposition) && (
            <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div className="flex items-center space-x-2 mb-3">
                <Calendar className="h-5 w-5 text-yellow-600" />
                <h3 className="font-medium text-yellow-800">Schedule Follow-up</h3>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Follow-up Date
                  </label>
                  <input
                    type="date"
                    value={nextFollowUpDate}
                    onChange={(e) => setNextFollowUpDate(e.target.value)}
                    min={new Date().toISOString().split('T')[0]}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Follow-up Time
                  </label>
                  <input
                    type="time"
                    value={nextFollowUpTime}
                    onChange={(e) => setNextFollowUpTime(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !selectedDisposition}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Setting...' : 'Set Disposition'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SetDispositionModal;

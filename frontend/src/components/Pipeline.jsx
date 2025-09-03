import React, { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-hot-toast';
import { Plus, Edit2, Trash2, DollarSign, GripVertical } from 'lucide-react';
import api from '../services/api';

const Pipeline = () => {
  const [pipelines, setPipelines] = useState([]);
  const [selectedPipeline, setSelectedPipeline] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showStageModal, setShowStageModal] = useState(false);
  const [editingStage, setEditingStage] = useState(null);
  const [pipelineForm, setPipelineForm] = useState({ pipelineName: '' });
  const [stageForm, setStageForm] = useState({
    stageName: '',
    stageOrder: 1,
    stageType: 'OPEN',
    winProbability: 0
  });
  const [draggedElement, setDraggedElement] = useState(null);
  const [draggedOverElement, setDraggedOverElement] = useState(null);

  useEffect(() => {
    fetchPipelines();
  }, []);

  const fetchPipelines = async () => {
    try {
      setLoading(true);
      const response = await api.get('/pipelines');
      if (response.data.success) {
        setPipelines(response.data.data);
        if (response.data.data.length > 0 && !selectedPipeline) {
          setSelectedPipeline(response.data.data[0]);
        }
      }
    } catch (error) {
      toast.error('Failed to fetch pipelines');
      console.error('Error fetching pipelines:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePipeline = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/pipelines', pipelineForm);
      if (response.data.success) {
        toast.success('Pipeline created successfully');
        setShowCreateModal(false);
        setPipelineForm({ pipelineName: '' });
        fetchPipelines();
      }
    } catch (error) {
      toast.error('Failed to create pipeline');
      console.error('Error creating pipeline:', error);
    }
  };

  const handleCreateStage = async (e) => {
    e.preventDefault();
    try {
      const url = editingStage
        ? `/pipelines/stages/${editingStage.stageId}`
        : `/pipelines/${selectedPipeline.pipelineId}/stages`;

      const method = editingStage ? 'put' : 'post';
      const response = await api[method](url, stageForm);

      if (response.data.success) {
        toast.success(`Stage ${editingStage ? 'updated' : 'created'} successfully`);
        setShowStageModal(false);
        setEditingStage(null);
        setStageForm({
          stageName: '',
          stageOrder: 1,
          stageType: 'OPEN',
          winProbability: 0
        });
        fetchPipelines();
      }
    } catch (error) {
      toast.error(`Failed to ${editingStage ? 'update' : 'create'} stage`);
      console.error('Error with stage:', error);
    }
  };

  const handleDeleteStage = async (stageId) => {
    if (!window.confirm('Are you sure you want to delete this stage?')) return;

    try {
      const response = await api.delete(`/pipelines/stages/${stageId}`);
      if (response.data.success) {
        toast.success('Stage deleted successfully');
        fetchPipelines();
      }
    } catch (error) {
      toast.error('Failed to delete stage');
      console.error('Error deleting stage:', error);
    }
  };

  // Native HTML5 Drag and Drop handlers
  const handleDragStart = (e, item, type) => {
    setDraggedElement({ item, type });
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/html', e.target.outerHTML);
    e.target.style.opacity = '0.5';
  };

  const handleDragEnd = (e) => {
    e.target.style.opacity = '1';
    setDraggedElement(null);
    setDraggedOverElement(null);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDragEnter = (e, targetStage) => {
    e.preventDefault();
    setDraggedOverElement(targetStage);
  };

  const handleDragLeave = (e) => {
    // Only clear if we're leaving the drop zone entirely
    if (!e.currentTarget.contains(e.relatedTarget)) {
      setDraggedOverElement(null);
    }
  };

  const handleDrop = async (e, targetStage) => {
    e.preventDefault();
    setDraggedOverElement(null);

    if (!draggedElement) return;

    const { item, type } = draggedElement;

    if (type === 'stage') {
      // Handle stage reordering
      const sourceIndex = selectedPipeline.stages.findIndex(s => s.stageId === item.stageId);
      const targetIndex = selectedPipeline.stages.findIndex(s => s.stageId === targetStage.stageId);

      if (sourceIndex === targetIndex) return;

      try {
        await api.put(`/pipelines/stages/${item.stageId}`, {
          pipelineId: selectedPipeline.pipelineId,
          stageOrder: targetIndex + 1,
          stageName: item.stageName,
          stageType: item.stageType,
          winProbability: item.winProbability
        });
        toast.success('Stage reordered successfully');
        fetchPipelines();
      } catch (error) {
        toast.error('Failed to reorder stage');
        console.error('API error:', error.response?.data || error.message);
      }
    } else if (type === 'deal') {
      // Handle deal movement
      if (item.stageId === targetStage.stageId) return;

      try {
        await api.put(`/deals/${item.dealId}/stage`, {
          newStageId: targetStage.stageId
        });
        toast.success('Deal moved successfully');
        fetchPipelines();
      } catch (error) {
        toast.error('Failed to move deal');
        console.error('API error:', error.response?.data || error.message);
      }
    }
  };
  const openEditStage = (stage) => {
    setEditingStage(stage);
    setStageForm({
      stageName: stage.stageName,
      stageOrder: stage.stageOrder,
      stageType: stage.stageType,
      winProbability: stage.winProbability
    });
    setShowStageModal(true);
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-4">
          <h1 className="text-2xl font-bold text-gray-900">Sales Pipeline</h1>
          {pipelines.length > 1 && (
            <select
              value={selectedPipeline?.pipelineId || ''}
              onChange={(e) => {
                const pipeline = pipelines.find(p => p.pipelineId === parseInt(e.target.value));
                setSelectedPipeline(pipeline);
              }}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {pipelines.map(pipeline => (
                <option key={pipeline.pipelineId} value={pipeline.pipelineId}>
                  {pipeline.pipelineName}
                </option>
              ))}
            </select>
          )}
        </div>
        <div className="flex space-x-2">
          <button
            onClick={() => {
              setEditingStage(null);
              setStageForm({
                stageName: '',
                stageOrder: (selectedPipeline?.stages.length || 0) + 1,
                stageType: 'OPEN',
                winProbability: 0
              });
              setShowStageModal(true);
            }}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center space-x-2"
          >
            <Plus className="h-4 w-4" />
            <span>Add Stage</span>
          </button>
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center space-x-2"
          >
            <Plus className="h-4 w-4" />
            <span>New Pipeline</span>
          </button>
        </div>
      </div>

      {/* Pipeline Stats */}
      {selectedPipeline && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{selectedPipeline.totalDeals}</div>
              <div className="text-sm text-gray-600">Total Deals</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {formatCurrency(selectedPipeline.totalValue)}
              </div>
              <div className="text-sm text-gray-600">Pipeline Value</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">
                {selectedPipeline.stages?.filter(s => s.stageType === 'OPEN').length || 0}
              </div>
              <div className="text-sm text-gray-600">Active Stages</div>
            </div>
          </div>
        </div>
      )}

      {/* Pipeline Board */}
      {selectedPipeline && selectedPipeline.stages && (
        <div className="bg-white rounded-lg shadow overflow-x-auto">
          <div className="flex space-x-4 p-6 min-w-max">
            {selectedPipeline.stages.map((stage, stageIndex) => (
              <div
                key={`stage-${stage.stageId}`}
                className="flex-shrink-0 w-80"
                draggable
                onDragStart={(e) => handleDragStart(e, stage, 'stage')}
                onDragEnd={handleDragEnd}
                onDragOver={handleDragOver}
                onDragEnter={(e) => handleDragEnter(e, stage)}
                onDragLeave={handleDragLeave}
                onDrop={(e) => handleDrop(e, stage)}
              >
                <div className={`bg-gray-50 rounded-lg border-2 transition-colors ${
                  draggedOverElement?.stageId === stage.stageId ? 'border-blue-400 bg-blue-50' : 'border-transparent'
                }`}>
                  {/* Stage Header */}
                  <div className="p-4 border-b border-gray-200 cursor-move hover:bg-gray-100 select-none">
                    <div className="flex justify-between items-center mb-2">
                      <div className="flex items-center space-x-2">
                        <GripVertical className="h-4 w-4 text-gray-400" />
                        <h3 className="font-semibold text-gray-900">{stage.stageName}</h3>
                      </div>
                      <div className="flex space-x-1">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            openEditStage(stage);
                          }}
                          className="p-1 text-gray-400 hover:text-blue-600"
                        >
                          <Edit2 className="h-4 w-4" />
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteStage(stage.stageId);
                          }}
                          className="p-1 text-gray-400 hover:text-red-600"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                    <div className="flex justify-between text-sm text-gray-600">
                      <span>{stage.dealCount} deals</span>
                      <span>{formatCurrency(stage.totalValue)}</span>
                    </div>
                    <div className="text-xs text-gray-500 mt-1">
                      Win Rate: {stage.winProbability}%
                    </div>
                  </div>

                  {/* Deals Area */}
                  <div className="p-4 min-h-[200px] space-y-3">
                    {stage.deals?.map((deal) => (
                      <div
                        key={`deal-${deal.dealId}`}
                        className="bg-white rounded-lg shadow-sm border p-3 cursor-move select-none hover:shadow-md transition-shadow"
                        draggable
                        onDragStart={(e) => {
                          e.stopPropagation();
                          handleDragStart(e, { ...deal, stageId: stage.stageId }, 'deal');
                        }}
                        onDragEnd={handleDragEnd}
                      >
                        <div className="flex items-center justify-between mb-2">
                          <div className="font-medium text-gray-900">
                            {deal.dealName}
                          </div>
                          <GripVertical className="h-4 w-4 text-gray-400" />
                        </div>
                        <div className="flex items-center justify-between text-sm text-gray-600">
                          <div className="flex items-center space-x-1">
                            <DollarSign className="h-3 w-3" />
                            <span>{formatCurrency(deal.amount)}</span>
                          </div>
                          <span>Owner: {deal.ownerUserId}</span>
                        </div>
                      </div>
                    ))}
                    {stage.deals?.length === 0 && (
                      <div className="text-center text-gray-500 py-8">
                        No deals in this stage
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Create Pipeline Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-semibold mb-4">Create New Pipeline</h2>
            <form onSubmit={handleCreatePipeline}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Pipeline Name
                </label>
                <input
                  type="text"
                  value={pipelineForm.pipelineName}
                  onChange={(e) => setPipelineForm({ pipelineName: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div className="flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  Create Pipeline
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create/Edit Stage Modal */}
      {showStageModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-semibold mb-4">
              {editingStage ? 'Edit Stage' : 'Create New Stage'}
            </h2>
            <form onSubmit={handleCreateStage}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Stage Name
                  </label>
                  <input
                    type="text"
                    value={stageForm.stageName}
                    onChange={(e) => setStageForm({ ...stageForm, stageName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Stage Order
                  </label>
                  <input
                    type="number"
                    value={stageForm.stageOrder}
                    onChange={(e) => setStageForm({ ...stageForm, stageOrder: parseInt(e.target.value) })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min="1"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Stage Type
                  </label>
                  <select
                    value={stageForm.stageType}
                    onChange={(e) => setStageForm({ ...stageForm, stageType: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="OPEN">Open</option>
                    <option value="WON">Won</option>
                    <option value="LOST">Lost</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Win Probability (%)
                  </label>
                  <input
                    type="number"
                    value={stageForm.winProbability}
                    onChange={(e) => setStageForm({ ...stageForm, winProbability: parseFloat(e.target.value) })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min="0"
                    max="100"
                    step="0.1"
                  />
                </div>
              </div>
              <div className="flex justify-end space-x-2 mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowStageModal(false);
                    setEditingStage(null);
                  }}
                  className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  {editingStage ? 'Update Stage' : 'Create Stage'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Pipeline;
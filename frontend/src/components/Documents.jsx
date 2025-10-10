import React, { useState, useEffect } from "react";
import {
  Upload,
  Download,
  Search,
  Filter,
  Eye,
  Trash2,
  FileText,
  Image,
  File,
  Calendar,
  User,
  Tag,
  MoreVertical,
  Plus,
  Grid,
  List,
  SortAsc,
  SortDesc,
} from "lucide-react";
import DocumentUploadModal from "./modals/DocumentUploadModal";
import DocumentPreviewModal from "./modals/DocumentPreviewModal";

const Documents = () => {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedType, setSelectedType] = useState("");
  const [selectedEntity, setSelectedEntity] = useState("");
  const [isPublicFilter, setIsPublicFilter] = useState("");
  const [sortBy, setSortBy] = useState("uploadedAt");
  const [sortDir, setSortDir] = useState("desc");
  const [viewMode, setViewMode] = useState("grid"); // grid or list
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [documentTypes, setDocumentTypes] = useState([]);

  useEffect(() => {
    fetchDocuments();
    fetchDocumentTypes();
  }, [currentPage, sortBy, sortDir, searchTerm, selectedType, isPublicFilter]);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      let url = `/api/v1/documents?page=${currentPage}&size=20&sortBy=${sortBy}&sortDir=${sortDir}`;
      
      if (searchTerm) {
        url = `/api/v1/documents/search?query=${encodeURIComponent(searchTerm)}&page=${currentPage}&size=20&sortBy=${sortBy}&sortDir=${sortDir}`;
      } else if (selectedType || isPublicFilter !== "") {
        const params = new URLSearchParams();
        if (selectedType) params.append("documentType", selectedType);
        if (isPublicFilter !== "") params.append("isPublic", isPublicFilter);
        params.append("page", currentPage);
        params.append("size", "20");
        params.append("sortBy", sortBy);
        params.append("sortDir", sortDir);
        url = `/api/v1/documents/advanced-search?${params.toString()}`;
      }

      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setDocuments(data.content);
        setTotalPages(data.totalPages);
      }
    } catch (error) {
      console.error("Error fetching documents:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchDocumentTypes = async () => {
    try {
      const response = await fetch("/api/v1/documents/types", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const types = await response.json();
        setDocumentTypes(types);
      }
    } catch (error) {
      console.error("Error fetching document types:", error);
    }
  };

  const handleDownload = async (documentId, fileName) => {
    try {
      const response = await fetch(`/api/v1/documents/${documentId}/download`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      }
    } catch (error) {
      console.error("Error downloading document:", error);
    }
  };

  const handleDelete = async (documentId) => {
    if (window.confirm("Are you sure you want to delete this document?")) {
      try {
        const response = await fetch(`/api/v1/documents/${documentId}`, {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        if (response.ok) {
          fetchDocuments();
        }
      } catch (error) {
        console.error("Error deleting document:", error);
      }
    }
  };

  const handlePreview = (document) => {
    setSelectedDocument(document);
    setShowPreviewModal(true);
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  const getFileIcon = (document) => {
    if (document.isImage) {
      return <Image className="h-8 w-8 text-green-500" />;
    } else if (document.isPdf) {
      return <FileText className="h-8 w-8 text-red-500" />;
    } else {
      return <File className="h-8 w-8 text-blue-500" />;
    }
  };

  const resetFilters = () => {
    setSearchTerm("");
    setSelectedType("");
    setIsPublicFilter("");
    setCurrentPage(0);
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Documents</h1>
          <p className="text-gray-600">Manage and organize your documents</p>
        </div>
        <button
          onClick={() => setShowUploadModal(true)}
          className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 flex items-center gap-2"
        >
          <Plus className="h-4 w-4" />
          Upload Document
        </button>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
        <div className="flex flex-wrap gap-4 items-center">
          {/* Search */}
          <div className="flex-1 min-w-64">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search documents..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Document Type Filter */}
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">All Types</option>
            {documentTypes.map((type) => (
              <option key={type} value={type}>
                {type.replace("_", " ")}
              </option>
            ))}
          </select>

          {/* Public/Private Filter */}
          <select
            value={isPublicFilter}
            onChange={(e) => setIsPublicFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">All Documents</option>
            <option value="true">Public</option>
            <option value="false">Private</option>
          </select>

          {/* Sort */}
          <select
            value={`${sortBy}-${sortDir}`}
            onChange={(e) => {
              const [field, direction] = e.target.value.split("-");
              setSortBy(field);
              setSortDir(direction);
            }}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="uploadedAt-desc">Newest First</option>
            <option value="uploadedAt-asc">Oldest First</option>
            <option value="title-asc">Name A-Z</option>
            <option value="title-desc">Name Z-A</option>
            <option value="fileSize-desc">Largest First</option>
            <option value="fileSize-asc">Smallest First</option>
          </select>

          {/* View Mode Toggle */}
          <div className="flex border border-gray-300 rounded-lg">
            <button
              onClick={() => setViewMode("grid")}
              className={`p-2 ${
                viewMode === "grid"
                  ? "bg-primary-100 text-primary-600"
                  : "text-gray-400 hover:text-gray-600"
              }`}
            >
              <Grid className="h-4 w-4" />
            </button>
            <button
              onClick={() => setViewMode("list")}
              className={`p-2 ${
                viewMode === "list"
                  ? "bg-primary-100 text-primary-600"
                  : "text-gray-400 hover:text-gray-600"
              }`}
            >
              <List className="h-4 w-4" />
            </button>
          </div>

          {/* Clear Filters */}
          {(searchTerm || selectedType || isPublicFilter !== "") && (
            <button
              onClick={resetFilters}
              className="text-primary-600 hover:text-primary-700 text-sm font-medium"
            >
              Clear Filters
            </button>
          )}
        </div>
      </div>

      {/* Documents Grid/List */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      ) : documents.length === 0 ? (
        <div className="text-center py-12">
          <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No documents found</h3>
          <p className="text-gray-500 mb-4">
            {searchTerm || selectedType || isPublicFilter !== ""
              ? "Try adjusting your search criteria"
              : "Upload your first document to get started"}
          </p>
          <button
            onClick={() => setShowUploadModal(true)}
            className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700"
          >
            Upload Document
          </button>
        </div>
      ) : viewMode === "grid" ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {documents.map((document) => (
            <div
              key={document.documentId}
              className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-3">
                {getFileIcon(document)}
                <div className="relative">
                  <button className="text-gray-400 hover:text-gray-600">
                    <MoreVertical className="h-4 w-4" />
                  </button>
                </div>
              </div>
              
              <h3 className="font-medium text-gray-900 mb-1 truncate" title={document.title}>
                {document.title}
              </h3>
              
              <p className="text-sm text-gray-500 mb-2 line-clamp-2">
                {document.description || "No description"}
              </p>
              
              <div className="flex items-center justify-between text-xs text-gray-400 mb-3">
                <span>{formatFileSize(document.fileSize)}</span>
                <span>{new Date(document.uploadedAt).toLocaleDateString()}</span>
              </div>
              
              <div className="flex items-center gap-2">
                {(document.isImage || document.isPdf || document.fileType?.toLowerCase().includes('pdf') || document.fileType?.toLowerCase().includes('image')) && (
                  <button
                    onClick={() => handlePreview(document)}
                    className="flex-1 bg-gray-100 text-gray-700 px-3 py-1 rounded text-xs hover:bg-gray-200 flex items-center justify-center gap-1"
                  >
                    <Eye className="h-3 w-3" />
                    Preview
                  </button>
                )}
                <button
                  onClick={() => handleDownload(document.documentId, document.fileName)}
                  className="flex-1 bg-primary-100 text-primary-700 px-3 py-1 rounded text-xs hover:bg-primary-200 flex items-center justify-center gap-1"
                >
                  <Download className="h-3 w-3" />
                  Download
                </button>
                <button
                  onClick={() => handleDelete(document.documentId)}
                  className="bg-red-100 text-red-700 px-3 py-1 rounded text-xs hover:bg-red-200 flex items-center justify-center"
                >
                  <Trash2 className="h-3 w-3" />
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Document
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Size
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Uploaded
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {documents.map((document) => (
                  <tr key={document.documentId} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {getFileIcon(document)}
                        <div className="ml-3">
                          <div className="text-sm font-medium text-gray-900">
                            {document.title}
                          </div>
                          <div className="text-sm text-gray-500">
                            {document.fileName}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {document.documentType?.replace("_", " ") || "Other"}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatFileSize(document.fileSize)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(document.uploadedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex items-center gap-2">
                        {(document.isImage || document.isPdf || document.fileType?.toLowerCase().includes('pdf') || document.fileType?.toLowerCase().includes('image')) && (
                          <button
                            onClick={() => handlePreview(document)}
                            className="text-primary-600 hover:text-primary-900"
                          >
                            <Eye className="h-4 w-4" />
                          </button>
                        )}
                        <button
                          onClick={() => handleDownload(document.documentId, document.fileName)}
                          className="text-primary-600 hover:text-primary-900"
                        >
                          <Download className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(document.documentId)}
                          className="text-red-600 hover:text-red-900"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center mt-6 gap-2">
          <button
            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
            disabled={currentPage === 0}
            className="px-3 py-1 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            Previous
          </button>
          
          <span className="text-sm text-gray-600">
            Page {currentPage + 1} of {totalPages}
          </span>
          
          <button
            onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
            disabled={currentPage >= totalPages - 1}
            className="px-3 py-1 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            Next
          </button>
        </div>
      )}

      {/* Modals */}
      {showUploadModal && (
        <DocumentUploadModal
          isOpen={showUploadModal}
          onClose={() => setShowUploadModal(false)}
          onSuccess={() => {
            setShowUploadModal(false);
            fetchDocuments();
          }}
        />
      )}

      {showPreviewModal && selectedDocument && (
        <DocumentPreviewModal
          isOpen={showPreviewModal}
          onClose={() => setShowPreviewModal(false)}
          document={selectedDocument}
        />
      )}
    </div>
  );
};

export default Documents;

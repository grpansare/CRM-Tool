import React, { useState, useEffect } from "react";
import { X, Download, ExternalLink, FileText, Image as ImageIcon } from "lucide-react";

const DocumentPreviewModal = ({ isOpen, onClose, document }) => {
  const [previewUrl, setPreviewUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isOpen && document) {
      loadPreview();
    }
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [isOpen, document]);

  const loadPreview = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await fetch(`/api/v1/documents/${document.documentId}/preview`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        setPreviewUrl(url);
      } else {
        setError("Failed to load preview");
      }
    } catch (error) {
      console.error("Error loading preview:", error);
      setError("Failed to load preview");
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    try {
      const response = await fetch(`/api/v1/documents/${document.documentId}/download`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = document.fileName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      }
    } catch (error) {
      console.error("Error downloading document:", error);
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  if (!isOpen || !document) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl mx-4 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-200">
          <div className="flex items-center gap-3">
            {document.isImage ? (
              <ImageIcon className="h-6 w-6 text-green-500" />
            ) : document.isPdf ? (
              <FileText className="h-6 w-6 text-red-500" />
            ) : (
              <FileText className="h-6 w-6 text-blue-500" />
            )}
            <div>
              <h2 className="text-lg font-semibold text-gray-900">{document.title}</h2>
              <p className="text-sm text-gray-500">
                {document.fileName} • {formatFileSize(document.fileSize)}
              </p>
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            <button
              onClick={handleDownload}
              className="flex items-center gap-2 px-3 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
            >
              <Download className="h-4 w-4" />
              Download
            </button>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 p-2"
            >
              <X className="h-6 w-6" />
            </button>
          </div>
        </div>

        {/* Preview Content */}
        <div className="flex-1 p-4 overflow-auto">
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center h-64 text-gray-500">
              <FileText className="h-16 w-16 mb-4" />
              <p className="text-lg font-medium mb-2">Preview not available</p>
              <p className="text-sm">{error}</p>
              <button
                onClick={handleDownload}
                className="mt-4 flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
              >
                <Download className="h-4 w-4" />
                Download to view
              </button>
            </div>
          ) : document.isImage ? (
            <div className="flex justify-center">
              <img
                src={previewUrl}
                alt={document.title}
                className="max-w-full max-h-full object-contain rounded-lg shadow-sm"
                style={{ maxHeight: "calc(90vh - 200px)" }}
              />
            </div>
          ) : document.isPdf ? (
            <div className="w-full h-full">
              <iframe
                src={previewUrl}
                className="w-full border-0 rounded-lg"
                style={{ height: "calc(90vh - 200px)" }}
                title={document.title}
              />
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-64 text-gray-500">
              <FileText className="h-16 w-16 mb-4" />
              <p className="text-lg font-medium mb-2">Preview not available</p>
              <p className="text-sm mb-4">This file type cannot be previewed in the browser</p>
              <button
                onClick={handleDownload}
                className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
              >
                <Download className="h-4 w-4" />
                Download to view
              </button>
            </div>
          )}
        </div>

        {/* Document Info */}
        {document.description && (
          <div className="border-t border-gray-200 p-4">
            <h3 className="text-sm font-medium text-gray-700 mb-2">Description</h3>
            <p className="text-sm text-gray-600">{document.description}</p>
          </div>
        )}

        {/* Document Metadata */}
        <div className="border-t border-gray-200 p-4 bg-gray-50">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span className="font-medium text-gray-700">Type:</span>
              <p className="text-gray-600">
                {document.documentType?.replace("_", " ") || "Other"}
              </p>
            </div>
            <div>
              <span className="font-medium text-gray-700">Uploaded:</span>
              <p className="text-gray-600">
                {new Date(document.uploadedAt).toLocaleDateString()}
              </p>
            </div>
            <div>
              <span className="font-medium text-gray-700">Visibility:</span>
              <p className="text-gray-600">
                {document.isPublic ? "Public" : "Private"}
              </p>
            </div>
            <div>
              <span className="font-medium text-gray-700">Downloads:</span>
              <p className="text-gray-600">{document.downloadCount || 0}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DocumentPreviewModal;

import React, { useState } from "react";
import {
  Upload,
  Download,
  FileText,
  AlertCircle,
  CheckCircle,
  X,
  Info,
  Users,
  FileDown,
  FileUp,
  Loader,
} from "lucide-react";

const ContactImportExport = () => {
  const [activeTab, setActiveTab] = useState("import");
  const [importFile, setImportFile] = useState(null);
  const [importLoading, setImportLoading] = useState(false);
  const [importResult, setImportResult] = useState(null);
  const [exportLoading, setExportLoading] = useState(false);
  const [dragActive, setDragActive] = useState(false);

  // Import functionality
  const handleFileSelect = (file) => {
    if (file && file.type === "text/csv") {
      setImportFile(file);
      setImportResult(null);
    } else {
      alert("Please select a CSV file");
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

  const handleImport = async () => {
    if (!importFile) {
      alert("Please select a file to import");
      return;
    }

    setImportLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", importFile);

      const response = await fetch("/api/v1/contacts/import", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: formData,
      });

      const result = await response.json();
      setImportResult(result);

      if (result.success) {
        // Reset file after successful import
        setImportFile(null);
      }
    } catch (error) {
      console.error("Import error:", error);
      setImportResult({
        success: false,
        message: "Error importing contacts. Please try again.",
        errors: ["Network error occurred"],
      });
    } finally {
      setImportLoading(false);
    }
  };

  // Export functionality
  const handleExportAll = async () => {
    setExportLoading(true);
    try {
      const response = await fetch("/api/v1/contacts/export", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `contacts_export_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      } else {
        alert("Error exporting contacts");
      }
    } catch (error) {
      console.error("Export error:", error);
      alert("Error exporting contacts");
    } finally {
      setExportLoading(false);
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      const response = await fetch("/api/v1/contacts/import/template", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "contacts_import_template.csv";
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      } else {
        alert("Error downloading template");
      }
    } catch (error) {
      console.error("Template download error:", error);
      alert("Error downloading template");
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Contact Import/Export</h1>
        <p className="text-gray-600">Import contacts from CSV files or export your existing contacts</p>
      </div>

      {/* Tabs */}
      <div className="mb-6">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab("import")}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === "import"
                  ? "border-primary-500 text-primary-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              <FileUp className="h-4 w-4 inline mr-2" />
              Import Contacts
            </button>
            <button
              onClick={() => setActiveTab("export")}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === "export"
                  ? "border-primary-500 text-primary-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              <FileDown className="h-4 w-4 inline mr-2" />
              Export Contacts
            </button>
          </nav>
        </div>
      </div>

      {/* Import Tab */}
      {activeTab === "import" && (
        <div className="space-y-6">
          {/* Instructions */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start">
              <Info className="h-5 w-5 text-blue-500 mt-0.5 mr-3" />
              <div>
                <h3 className="font-medium text-blue-900 mb-2">Import Instructions</h3>
                <ul className="text-sm text-blue-800 space-y-1">
                  <li>• Upload a CSV file with contact information</li>
                  <li>• Required fields: firstName, lastName, email</li>
                  <li>• Optional fields: phone, company, jobTitle, address, city, state, country, postalCode, website, notes</li>
                  <li>• Duplicate emails will be skipped</li>
                  <li>• Maximum file size: 10MB</li>
                </ul>
                <button
                  onClick={handleDownloadTemplate}
                  className="mt-3 text-blue-600 hover:text-blue-700 text-sm font-medium underline"
                >
                  Download CSV Template
                </button>
              </div>
            </div>
          </div>

          {/* File Upload Area */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <div
              className={`border-2 border-dashed rounded-lg p-8 text-center ${
                dragActive
                  ? "border-primary-500 bg-primary-50"
                  : importFile
                  ? "border-green-500 bg-green-50"
                  : "border-gray-300 hover:border-gray-400"
              }`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
            >
              {importFile ? (
                <div className="flex items-center justify-center gap-3">
                  <FileText className="h-8 w-8 text-green-500" />
                  <div className="text-left">
                    <p className="font-medium text-gray-900">{importFile.name}</p>
                    <p className="text-sm text-gray-500">{formatFileSize(importFile.size)}</p>
                  </div>
                  <button
                    onClick={() => setImportFile(null)}
                    className="text-red-500 hover:text-red-700"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
              ) : (
                <div>
                  <Upload className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-lg font-medium text-gray-900 mb-2">
                    Drop your CSV file here, or{" "}
                    <label className="text-primary-600 hover:text-primary-700 cursor-pointer">
                      browse
                      <input
                        type="file"
                        className="hidden"
                        accept=".csv"
                        onChange={(e) => handleFileSelect(e.target.files[0])}
                      />
                    </label>
                  </p>
                  <p className="text-sm text-gray-500">CSV files only, up to 10MB</p>
                </div>
              )}
            </div>

            {importFile && (
              <div className="mt-4 flex justify-end">
                <button
                  onClick={handleImport}
                  disabled={importLoading}
                  className="bg-primary-600 text-white px-6 py-2 rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  {importLoading ? (
                    <>
                      <Loader className="h-4 w-4 animate-spin" />
                      Importing...
                    </>
                  ) : (
                    <>
                      <Upload className="h-4 w-4" />
                      Import Contacts
                    </>
                  )}
                </button>
              </div>
            )}
          </div>

          {/* Import Results */}
          {importResult && (
            <div className={`rounded-lg p-4 ${
              importResult.success 
                ? "bg-green-50 border border-green-200" 
                : "bg-red-50 border border-red-200"
            }`}>
              <div className="flex items-start">
                {importResult.success ? (
                  <CheckCircle className="h-5 w-5 text-green-500 mt-0.5 mr-3" />
                ) : (
                  <AlertCircle className="h-5 w-5 text-red-500 mt-0.5 mr-3" />
                )}
                <div className="flex-1">
                  <h3 className={`font-medium mb-2 ${
                    importResult.success ? "text-green-900" : "text-red-900"
                  }`}>
                    {importResult.message}
                  </h3>
                  
                  {importResult.success && (
                    <div className="text-sm text-green-800">
                      <p>Successfully imported: {importResult.successCount} contacts</p>
                      {importResult.duplicateCount > 0 && (
                        <p>Duplicates skipped: {importResult.duplicateCount}</p>
                      )}
                    </div>
                  )}

                  {importResult.errors && importResult.errors.length > 0 && (
                    <div className="mt-2">
                      <p className="text-sm font-medium text-red-800 mb-1">Errors:</p>
                      <ul className="text-sm text-red-700 space-y-1">
                        {importResult.errors.map((error, index) => (
                          <li key={index}>• {error}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {importResult.duplicates && importResult.duplicates.length > 0 && (
                    <div className="mt-2">
                      <p className="text-sm font-medium text-yellow-800 mb-1">
                        Duplicate emails skipped ({importResult.duplicates.length}):
                      </p>
                      <div className="text-sm text-yellow-700 max-h-32 overflow-y-auto">
                        {importResult.duplicates.slice(0, 10).map((email, index) => (
                          <div key={index}>• {email}</div>
                        ))}
                        {importResult.duplicates.length > 10 && (
                          <div>... and {importResult.duplicates.length - 10} more</div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Export Tab */}
      {activeTab === "export" && (
        <div className="space-y-6">
          {/* Export Options */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Export All Contacts */}
            <div className="bg-white rounded-lg border border-gray-200 p-6">
              <div className="flex items-start">
                <Users className="h-8 w-8 text-blue-500 mr-4" />
                <div className="flex-1">
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Export All Contacts</h3>
                  <p className="text-gray-600 mb-4">
                    Download all your contacts as a CSV file. This will include all contact information
                    and can be imported into other systems.
                  </p>
                  <button
                    onClick={handleExportAll}
                    disabled={exportLoading}
                    className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                  >
                    {exportLoading ? (
                      <>
                        <Loader className="h-4 w-4 animate-spin" />
                        Exporting...
                      </>
                    ) : (
                      <>
                        <Download className="h-4 w-4" />
                        Export All
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>

            {/* Export Template */}
            <div className="bg-white rounded-lg border border-gray-200 p-6">
              <div className="flex items-start">
                <FileText className="h-8 w-8 text-green-500 mr-4" />
                <div className="flex-1">
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Download Template</h3>
                  <p className="text-gray-600 mb-4">
                    Download a CSV template with sample data to understand the required format
                    for importing contacts.
                  </p>
                  <button
                    onClick={handleDownloadTemplate}
                    className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 flex items-center gap-2"
                  >
                    <FileDown className="h-4 w-4" />
                    Download Template
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Export Information */}
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
            <h3 className="font-medium text-gray-900 mb-2">Export Information</h3>
            <ul className="text-sm text-gray-600 space-y-1">
              <li>• Exported files are in CSV format compatible with Excel and other spreadsheet applications</li>
              <li>• All contact fields are included in the export</li>
              <li>• Files are generated with timestamps in the filename</li>
              <li>• Exports respect your current user permissions and tenant isolation</li>
            </ul>
          </div>
        </div>
      )}
    </div>
  );
};

export default ContactImportExport;

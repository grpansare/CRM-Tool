import React, { useState, useEffect } from "react";
import {
  Search,
  Filter,
  X,
  Calendar,
  User,
  Building,
  Target,
  BarChart3,
  CheckSquare,
  FileText,
  Mail,
  Phone,
  MapPin,
  Tag,
  DollarSign,
  Clock,
  Zap,
  ChevronDown,
  ChevronRight,
} from "lucide-react";

const AdvancedSearch = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedFilters, setSelectedFilters] = useState({
    entityTypes: [],
    dateRange: { from: "", to: "" },
    status: [],
    priority: [],
    assignedTo: [],
    tags: [],
    customFields: {},
  });
  const [searchResults, setSearchResults] = useState({
    contacts: [],
    leads: [],
    deals: [],
    accounts: [],
    tasks: [],
    documents: [],
  });
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("all");
  const [showFilters, setShowFilters] = useState(false);
  const [totalResults, setTotalResults] = useState(0);

  // Filter options
  const entityTypes = [
    { value: "contacts", label: "Contacts", icon: User, color: "blue" },
    { value: "leads", label: "Leads", icon: Target, color: "green" },
    { value: "deals", label: "Deals", icon: BarChart3, color: "purple" },
    { value: "accounts", label: "Accounts", icon: Building, color: "orange" },
    { value: "tasks", label: "Tasks", icon: CheckSquare, color: "red" },
    { value: "documents", label: "Documents", icon: FileText, color: "gray" },
  ];

  const statusOptions = {
    leads: ["NEW", "CONTACTED", "QUALIFIED", "UNQUALIFIED", "NURTURING", "CONVERTED"],
    deals: ["PROSPECTING", "QUALIFICATION", "PROPOSAL", "NEGOTIATION", "CLOSED_WON", "CLOSED_LOST"],
    tasks: ["PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ON_HOLD"],
  };

  const priorityOptions = ["LOW", "MEDIUM", "HIGH", "URGENT"];

  useEffect(() => {
    if (searchQuery.length > 2) {
      performSearch();
    } else {
      setSearchResults({
        contacts: [],
        leads: [],
        deals: [],
        accounts: [],
        tasks: [],
        documents: [],
      });
      setTotalResults(0);
    }
  }, [searchQuery, selectedFilters]);

  const performSearch = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem("token");
      const headers = { Authorization: `Bearer ${token}` };

      // Build search parameters
      const searchParams = new URLSearchParams();
      searchParams.append("query", searchQuery);
      
      if (selectedFilters.dateRange.from) {
        searchParams.append("dateFrom", selectedFilters.dateRange.from);
      }
      if (selectedFilters.dateRange.to) {
        searchParams.append("dateTo", selectedFilters.dateRange.to);
      }

      const results = {
        contacts: [],
        leads: [],
        deals: [],
        accounts: [],
        tasks: [],
        documents: [],
      };

      // Search across different entities based on selected filters
      const entitiesToSearch = selectedFilters.entityTypes.length > 0 
        ? selectedFilters.entityTypes 
        : ["contacts", "leads", "deals", "accounts", "tasks", "documents"];

      const searchPromises = entitiesToSearch.map(async (entityType) => {
        try {
          let endpoint = "";
          let searchParam = searchParams.toString();

          switch (entityType) {
            case "contacts":
              endpoint = `/api/v1/contacts/search?${searchParam}`;
              break;
            case "leads":
              endpoint = `/api/v1/leads/search?${searchParam}`;
              break;
            case "deals":
              endpoint = `/api/v1/deals/search?${searchParam}`;
              break;
            case "accounts":
              endpoint = `/api/v1/accounts/search?${searchParam}`;
              break;
            case "tasks":
              endpoint = `/api/v1/tasks/search?${searchParam}`;
              break;
            case "documents":
              endpoint = `/api/v1/documents/search?${searchParam}`;
              break;
            default:
              return;
          }

          const response = await fetch(endpoint, { headers });
          if (response.ok) {
            const data = await response.json();
            results[entityType] = data.content || data || [];
          }
        } catch (error) {
          console.error(`Error searching ${entityType}:`, error);
        }
      });

      await Promise.all(searchPromises);
      setSearchResults(results);
      
      // Calculate total results
      const total = Object.values(results).reduce((sum, arr) => sum + arr.length, 0);
      setTotalResults(total);

    } catch (error) {
      console.error("Search error:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (filterType, value) => {
    setSelectedFilters(prev => {
      const updated = { ...prev };
      
      if (filterType === "entityTypes") {
        if (updated.entityTypes.includes(value)) {
          updated.entityTypes = updated.entityTypes.filter(item => item !== value);
        } else {
          updated.entityTypes = [...updated.entityTypes, value];
        }
      } else if (filterType === "dateRange") {
        updated.dateRange = { ...updated.dateRange, ...value };
      } else if (Array.isArray(updated[filterType])) {
        if (updated[filterType].includes(value)) {
          updated[filterType] = updated[filterType].filter(item => item !== value);
        } else {
          updated[filterType] = [...updated[filterType], value];
        }
      }
      
      return updated;
    });
  };

  const clearFilters = () => {
    setSelectedFilters({
      entityTypes: [],
      dateRange: { from: "", to: "" },
      status: [],
      priority: [],
      assignedTo: [],
      tags: [],
      customFields: {},
    });
  };

  const getEntityIcon = (entityType) => {
    const entity = entityTypes.find(e => e.value === entityType);
    return entity ? entity.icon : Search;
  };

  const getEntityColor = (entityType) => {
    const entity = entityTypes.find(e => e.value === entityType);
    return entity ? entity.color : "gray";
  };

  const renderSearchResult = (item, entityType) => {
    const Icon = getEntityIcon(entityType);
    const color = getEntityColor(entityType);
    
    return (
      <div
        key={`${entityType}-${item.id}`}
        className="p-4 border border-gray-200 rounded-lg hover:shadow-md transition-shadow cursor-pointer"
      >
        <div className="flex items-start justify-between">
          <div className="flex items-start gap-3">
            <Icon className={`h-5 w-5 text-${color}-500 mt-1`} />
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <h3 className="font-medium text-gray-900">
                  {item.title || item.name || `${item.firstName} ${item.lastName}` || item.fileName}
                </h3>
                <span className={`px-2 py-1 text-xs rounded-full bg-${color}-100 text-${color}-800`}>
                  {entityType.charAt(0).toUpperCase() + entityType.slice(1, -1)}
                </span>
              </div>
              
              {/* Entity-specific details */}
              {entityType === "contacts" && (
                <div className="text-sm text-gray-600">
                  <p>{item.email}</p>
                  <p>{item.company}</p>
                </div>
              )}
              
              {entityType === "leads" && (
                <div className="text-sm text-gray-600">
                  <p>{item.email}</p>
                  <p>Score: {item.leadScore}/100</p>
                  <span className={`inline-block px-2 py-1 text-xs rounded-full ${
                    item.leadStatus === "NEW" ? "bg-blue-100 text-blue-800" :
                    item.leadStatus === "QUALIFIED" ? "bg-green-100 text-green-800" :
                    "bg-gray-100 text-gray-800"
                  }`}>
                    {item.leadStatus}
                  </span>
                </div>
              )}
              
              {entityType === "deals" && (
                <div className="text-sm text-gray-600">
                  <p>Value: ${item.dealValue?.toLocaleString()}</p>
                  <p>Stage: {item.stage}</p>
                  <p>Close Date: {new Date(item.expectedCloseDate).toLocaleDateString()}</p>
                </div>
              )}
              
              {entityType === "accounts" && (
                <div className="text-sm text-gray-600">
                  <p>{item.industry}</p>
                  <p>{item.website}</p>
                </div>
              )}
              
              {entityType === "tasks" && (
                <div className="text-sm text-gray-600">
                  <p>{item.description}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      item.priority === "HIGH" ? "bg-red-100 text-red-800" :
                      item.priority === "MEDIUM" ? "bg-yellow-100 text-yellow-800" :
                      "bg-gray-100 text-gray-800"
                    }`}>
                      {item.priority}
                    </span>
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      item.status === "COMPLETED" ? "bg-green-100 text-green-800" :
                      item.status === "IN_PROGRESS" ? "bg-blue-100 text-blue-800" :
                      "bg-gray-100 text-gray-800"
                    }`}>
                      {item.status}
                    </span>
                  </div>
                </div>
              )}
              
              {entityType === "documents" && (
                <div className="text-sm text-gray-600">
                  <p>{item.description}</p>
                  <p>Type: {item.documentType}</p>
                  <p>Size: {item.formattedFileSize}</p>
                </div>
              )}
            </div>
          </div>
          
          <div className="text-xs text-gray-500">
            {new Date(item.createdAt || item.uploadedAt).toLocaleDateString()}
          </div>
        </div>
      </div>
    );
  };

  const getTabCount = (entityType) => {
    return searchResults[entityType]?.length || 0;
  };

  const getAllResults = () => {
    return Object.values(searchResults).flat();
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Advanced Search</h1>
        <p className="text-gray-600">Search across all your CRM data with powerful filters</p>
      </div>

      {/* Search Bar */}
      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
          <input
            type="text"
            placeholder="Search contacts, leads, deals, accounts, tasks, documents..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent text-lg"
          />
          {loading && (
            <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-primary-600"></div>
            </div>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-4">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            <Filter className="h-4 w-4" />
            Filters
            {showFilters ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          </button>
          
          {Object.values(selectedFilters).some(filter => 
            Array.isArray(filter) ? filter.length > 0 : 
            typeof filter === 'object' ? Object.keys(filter).some(key => filter[key]) : 
            filter
          ) && (
            <button
              onClick={clearFilters}
              className="text-primary-600 hover:text-primary-700 text-sm font-medium"
            >
              Clear All Filters
            </button>
          )}
        </div>

        {showFilters && (
          <div className="bg-gray-50 p-4 rounded-lg space-y-4">
            {/* Entity Types */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Search In
              </label>
              <div className="flex flex-wrap gap-2">
                {entityTypes.map((entityType) => (
                  <label key={entityType.value} className="flex items-center">
                    <input
                      type="checkbox"
                      checked={selectedFilters.entityTypes.includes(entityType.value)}
                      onChange={() => handleFilterChange("entityTypes", entityType.value)}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">{entityType.label}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Date Range */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  From Date
                </label>
                <input
                  type="date"
                  value={selectedFilters.dateRange.from}
                  onChange={(e) => handleFilterChange("dateRange", { from: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  To Date
                </label>
                <input
                  type="date"
                  value={selectedFilters.dateRange.to}
                  onChange={(e) => handleFilterChange("dateRange", { to: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Results Summary */}
      {searchQuery.length > 2 && (
        <div className="mb-6">
          <p className="text-gray-600">
            Found <span className="font-semibold">{totalResults}</span> results for "{searchQuery}"
          </p>
        </div>
      )}

      {/* Results Tabs */}
      {totalResults > 0 && (
        <div className="mb-6">
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              <button
                onClick={() => setActiveTab("all")}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === "all"
                    ? "border-primary-500 text-primary-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                }`}
              >
                All Results ({totalResults})
              </button>
              
              {entityTypes.map((entityType) => {
                const count = getTabCount(entityType.value);
                if (count === 0) return null;
                
                return (
                  <button
                    key={entityType.value}
                    onClick={() => setActiveTab(entityType.value)}
                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                      activeTab === entityType.value
                        ? "border-primary-500 text-primary-600"
                        : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                    }`}
                  >
                    {entityType.label} ({count})
                  </button>
                );
              })}
            </nav>
          </div>
        </div>
      )}

      {/* Results */}
      <div className="space-y-4">
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
          </div>
        ) : totalResults === 0 && searchQuery.length > 2 ? (
          <div className="text-center py-12">
            <Search className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No results found</h3>
            <p className="text-gray-500">
              Try adjusting your search terms or filters
            </p>
          </div>
        ) : (
          <>
            {activeTab === "all" ? (
              getAllResults().map((item, index) => {
                const entityType = Object.keys(searchResults).find(key => 
                  searchResults[key].some(result => result.id === item.id)
                );
                return renderSearchResult(item, entityType);
              })
            ) : (
              searchResults[activeTab]?.map((item) => renderSearchResult(item, activeTab))
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default AdvancedSearch;

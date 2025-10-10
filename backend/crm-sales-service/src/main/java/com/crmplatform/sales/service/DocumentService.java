package com.crmplatform.sales.service;

import com.crmplatform.sales.dto.DocumentResponse;
import java.util.HashMap;
import java.util.Map;
import com.crmplatform.sales.dto.DocumentUploadRequest;
import com.crmplatform.sales.entity.Document;
import com.crmplatform.sales.entity.DocumentType;
import com.crmplatform.sales.repository.DocumentRepository;
import com.crmplatform.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Value("${app.document.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${app.document.max-file-size:10485760}") // 10MB default
    private long maxFileSize;
    
    private final List<String> allowedMimeTypes = List.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain",
        "text/csv",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp"
    );
    
    public Page<DocumentResponse> getAllDocuments(int page, int size, String searchTerm, DocumentType documentType, 
                                                 Long uploadedByUserId, Boolean isPublic, LocalDateTime startDate, LocalDateTime endDate) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Document> documents = documentRepository.findDocumentsWithFilters(
            tenantId, searchTerm, documentType, uploadedByUserId, isPublic, startDate, endDate, pageable
        );
        
        return documents.map(this::convertToDocumentResponse);
    }
    
    public Page<DocumentResponse> getMyDocuments(int page, int size, String searchTerm) {
        Long tenantId = UserContext.getCurrentTenantId();
        Long userId = UserContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Document> documents;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            documents = documentRepository.searchDocuments(tenantId, searchTerm.trim(), pageable);
        } else {
            documents = documentRepository.findByTenantIdAndUploadedByUserId(tenantId, userId, pageable);
        }
        
        return documents.map(this::convertToDocumentResponse);
    }
    
    public DocumentResponse getDocumentById(Long documentId) {
        Long tenantId = UserContext.getCurrentTenantId();
        Document document = documentRepository.findByDocumentIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
        
        return convertToDocumentResponse(document);
    }
    
    public DocumentResponse uploadDocument(MultipartFile file, DocumentUploadRequest request) {
        validateFile(file);
        
        Long tenantId = UserContext.getCurrentTenantId();
        Long currentUserId = UserContext.getCurrentUserId();
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, tenantId.toString());
            Files.createDirectories(uploadPath);
            
            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create document entity
            Document document = new Document();
            document.setTenantId(tenantId);
            document.setFileName(uniqueFilename);
            document.setOriginalFileName(originalFilename);
            document.setFilePath(filePath.toString());
            document.setFileSize(file.getSize());
            document.setMimeType(file.getContentType());
            document.setFileExtension(fileExtension);
            document.setDocumentType(request.getDocumentType());
            document.setTitle(request.getTitle() != null ? request.getTitle() : originalFilename);
            document.setDescription(request.getDescription());
            document.setUploadedByUserId(currentUserId);
            document.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
            
            // Set related entity information
            document.setRelatedContactId(request.getRelatedContactId());
            document.setRelatedAccountId(request.getRelatedAccountId());
            document.setRelatedDealId(request.getRelatedDealId());
            document.setRelatedLeadId(request.getRelatedLeadId());
            document.setRelatedTaskId(request.getRelatedTaskId());
            document.setRelatedEntityType(request.getRelatedEntityType());
            
            Document savedDocument = documentRepository.save(document);
            log.info("Uploaded document: {} for tenant: {}", originalFilename, tenantId);
            
            return convertToDocumentResponse(savedDocument);
            
        } catch (IOException e) {
            log.error("Error uploading document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }
    
    public void deleteDocument(Long documentId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Document document = documentRepository.findByDocumentIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
        
        try {
            // Delete file from disk
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Delete from database
            documentRepository.delete(document);
            log.info("Deleted document: {} for tenant: {}", document.getOriginalFileName(), tenantId);
            
        } catch (IOException e) {
            log.error("Error deleting document file: {}", e.getMessage(), e);
            // Still delete from database even if file deletion fails
            documentRepository.delete(document);
        }
    }
    
    public Resource downloadDocument(Long documentId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Document document = documentRepository.findByDocumentIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Increment download count
                documentRepository.incrementDownloadCount(documentId, tenantId);
                return resource;
            } else {
                throw new RuntimeException("Document file not found or not readable");
            }
            
        } catch (MalformedURLException e) {
            log.error("Error creating resource for document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download document: " + e.getMessage());
        }
    }
    
    public List<DocumentResponse> getDocumentsForEntity(String entityType, Long entityId) {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Document> documents;
        
        switch (entityType.toUpperCase()) {
            case "CONTACT":
                documents = documentRepository.findByTenantIdAndRelatedContactId(tenantId, entityId);
                break;
            case "ACCOUNT":
                documents = documentRepository.findByTenantIdAndRelatedAccountId(tenantId, entityId);
                break;
            case "DEAL":
                documents = documentRepository.findByTenantIdAndRelatedDealId(tenantId, entityId);
                break;
            case "LEAD":
                documents = documentRepository.findByTenantIdAndRelatedLeadId(tenantId, entityId);
                break;
            case "TASK":
                documents = documentRepository.findByTenantIdAndRelatedTaskId(tenantId, entityId);
                break;
            default:
                throw new IllegalArgumentException("Invalid entity type: " + entityType);
        }
        
        return documents.stream().map(this::convertToDocumentResponse).collect(Collectors.toList());
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new RuntimeException("File type not allowed. Allowed types: " + String.join(", ", allowedMimeTypes));
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new RuntimeException("Invalid filename");
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private DocumentResponse convertToDocumentResponse(Document document) {
        DocumentResponse response = new DocumentResponse(document);
        
        // TODO: Fetch and set user names from auth service
        // response.setUploadedByUserName(getUserName(document.getUploadedByUserId()));
        
        // TODO: Fetch and set related entity names from respective services
        // Similar to TaskService implementation
        
        return response;
    }
 // Add these methods to DocumentService.java

    public Page<DocumentResponse> getPublicDocuments(Pageable pageable) {
        Long tenantId = UserContext.getCurrentTenantId();
        Page<Document> documents = documentRepository.findByTenantIdAndIsPublic(tenantId, true, pageable);
        return documents.map(this::convertToDocumentResponse);
    }

    public Page<DocumentResponse> getPrivateDocuments(Pageable pageable) {
        Long tenantId = UserContext.getCurrentTenantId();
        Page<Document> documents = documentRepository.findByTenantIdAndIsPublic(tenantId, false, pageable);
        return documents.map(this::convertToDocumentResponse);
    }

    public Page<DocumentResponse> searchDocuments(String query, Pageable pageable) {
        Long tenantId = UserContext.getCurrentTenantId();
        Page<Document> documents = documentRepository.searchDocuments(tenantId, query, pageable);
        return documents.map(this::convertToDocumentResponse);
    }

    public Page<DocumentResponse> getDocumentsByType(DocumentType documentType, Pageable pageable) {
        Long tenantId = UserContext.getCurrentTenantId();
        Page<Document> documents = documentRepository.findByTenantIdAndDocumentType(tenantId, documentType, pageable);
        return documents.map(this::convertToDocumentResponse);
    }

    public List<DocumentResponse> getDocumentsByContact(Long contactId) {
        return getDocumentsForEntity("CONTACT", contactId);
    }

    public List<DocumentResponse> getDocumentsByAccount(Long accountId) {
        return getDocumentsForEntity("ACCOUNT", accountId);
    }

    public List<DocumentResponse> getDocumentsByDeal(Long dealId) {
        return getDocumentsForEntity("DEAL", dealId);
    }

    public List<DocumentResponse> getDocumentsByLead(Long leadId) {
        return getDocumentsForEntity("LEAD", leadId);
    }

    public List<DocumentResponse> getDocumentsByTask(Long taskId) {
        return getDocumentsForEntity("TASK", taskId);
    }

    public List<DocumentResponse> getRecentDocuments(int limit) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Document> documents = documentRepository.findByTenantId(tenantId, pageable);
        return documents.getContent().stream().map(this::convertToDocumentResponse).collect(Collectors.toList());
    }

    public List<DocumentResponse> getMostDownloadedDocuments(int limit) {
        Long tenantId = UserContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "downloadCount"));
        Page<Document> documents = documentRepository.findByTenantId(tenantId, pageable);
        return documents.getContent().stream().map(this::convertToDocumentResponse).collect(Collectors.toList());
    }

    public Page<DocumentResponse> advancedSearch(String title, String description, DocumentType documentType, 
                                               String fileType, Boolean isPublic, Long contactId, Long accountId, 
                                               Long dealId, Long leadId, Long taskId, LocalDateTime uploadedAfter, 
                                               LocalDateTime uploadedBefore, Pageable pageable) {
        Long tenantId = UserContext.getCurrentTenantId();
        Page<Document> documents = documentRepository.findDocumentsWithAdvancedFilters(
            tenantId, title, description, documentType, fileType, isPublic, 
            contactId, accountId, dealId, leadId, taskId, uploadedAfter, uploadedBefore, pageable
        );
        return documents.map(this::convertToDocumentResponse);
    }

    public Map<String, Object> getDocumentAnalytics() {
        Long tenantId = UserContext.getCurrentTenantId();
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalDocuments", documentRepository.countByTenantId(tenantId));
        analytics.put("publicDocuments", documentRepository.countByTenantIdAndIsPublic(tenantId, true));
        analytics.put("privateDocuments", documentRepository.countByTenantIdAndIsPublic(tenantId, false));
        analytics.put("totalDownloads", documentRepository.sumDownloadCountByTenantId(tenantId));
        
        return analytics;
    }

    public DocumentResponse updateDocument(Long documentId, DocumentUploadRequest request) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Document document = documentRepository.findByDocumentIdAndTenantId(documentId, tenantId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
        
        // Update fields
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getDocumentType() != null) {
            document.setDocumentType(request.getDocumentType());
        }
        if (request.getIsPublic() != null) {
            document.setIsPublic(request.getIsPublic());
        }
        
        // Update related entity fields
        document.setRelatedContactId(request.getRelatedContactId());
        document.setRelatedAccountId(request.getRelatedAccountId());
        document.setRelatedDealId(request.getRelatedDealId());
        document.setRelatedLeadId(request.getRelatedLeadId());
        document.setRelatedTaskId(request.getRelatedTaskId());
        document.setRelatedEntityType(request.getRelatedEntityType());
        
        Document savedDocument = documentRepository.save(document);
        return convertToDocumentResponse(savedDocument);
    }
}

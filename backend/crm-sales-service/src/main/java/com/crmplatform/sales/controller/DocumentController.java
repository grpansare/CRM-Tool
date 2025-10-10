package com.crmplatform.sales.controller;

import com.crmplatform.sales.dto.DocumentResponse;

import com.crmplatform.sales.dto.DocumentUploadRequest;
import com.crmplatform.sales.entity.DocumentType;
import com.crmplatform.sales.service.DocumentService;
import com.crmplatform.common.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * Upload a new document
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "contactId", required = false) Long contactId,
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "dealId", required = false) Long dealId,
            @RequestParam(value = "leadId", required = false) Long leadId,
            @RequestParam(value = "taskId", required = false) Long taskId) {

        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setTitle(title != null ? title : file.getOriginalFilename());
        request.setDescription(description);
        if (documentType != null) {
            request.setDocumentType(DocumentType.valueOf(documentType.toUpperCase()));
        }
        request.setIsPublic(isPublic);
        request.setRelatedContactId(contactId);   // ✅ Correct
        request.setRelatedAccountId(accountId);   // ✅ Correct
        request.setRelatedDealId(dealId);         // ✅ Correct
        request.setRelatedLeadId(leadId);         // ✅ Correct
        request.setRelatedTaskId(taskId);         // ✅ Correct
        DocumentResponse response = documentService.uploadDocument(file, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all documents with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentResponse> documents = documentService.getAllDocuments(page, size, null, null, null, null, null, null);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        DocumentResponse document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    /**
     * Download document
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource resource = documentService.downloadDocument(id);
        DocumentResponse document = documentService.getDocumentById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Preview document (for images and PDFs)
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewDocument(@PathVariable Long id) {
        Resource resource = documentService.downloadDocument(id);
        DocumentResponse document = documentService.getDocumentById(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (document.isImage()) {
            if (document.getFileName().toLowerCase().endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (document.getFileName().toLowerCase().endsWith(".jpg") || 
                       document.getFileName().toLowerCase().endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (document.getFileName().toLowerCase().endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            }
        } else if (document.isPdf()) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Delete document
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search documents
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentResponse> documents = documentService.searchDocuments(query, pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by type
     */
    @GetMapping("/type/{documentType}")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByType(
            @PathVariable DocumentType documentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentResponse> documents = documentService.getDocumentsByType(documentType, pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get public documents
     */
    @GetMapping("/public")
    public ResponseEntity<Page<DocumentResponse>> getPublicDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentResponse> documents = documentService.getPublicDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get private documents
     */
    @GetMapping("/private")
    public ResponseEntity<Page<DocumentResponse>> getPrivateDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentResponse> documents = documentService.getPrivateDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by contact
     */
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByContact(@PathVariable Long contactId) {
        List<DocumentResponse> documents = documentService.getDocumentsByContact(contactId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by account
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByAccount(@PathVariable Long accountId) {
        List<DocumentResponse> documents = documentService.getDocumentsByAccount(accountId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by deal
     */
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByDeal(@PathVariable Long dealId) {
        List<DocumentResponse> documents = documentService.getDocumentsByDeal(dealId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by lead
     */
    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByLead(@PathVariable Long leadId) {
        List<DocumentResponse> documents = documentService.getDocumentsByLead(leadId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByTask(@PathVariable Long taskId) {
        List<DocumentResponse> documents = documentService.getDocumentsByTask(taskId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get recent documents
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DocumentResponse>> getRecentDocuments(
            @RequestParam(defaultValue = "10") int limit) {
        List<DocumentResponse> documents = documentService.getRecentDocuments(limit);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get most downloaded documents
     */
    @GetMapping("/popular")
    public ResponseEntity<List<DocumentResponse>> getMostDownloadedDocuments(
            @RequestParam(defaultValue = "10") int limit) {
        List<DocumentResponse> documents = documentService.getMostDownloadedDocuments(limit);
        return ResponseEntity.ok(documents);
    }

    /**
     * Advanced search with multiple filters
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Page<DocumentResponse>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) Long contactId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long dealId,
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String uploadedAfter,
            @RequestParam(required = false) String uploadedBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime uploadedAfterDate = null;
        LocalDateTime uploadedBeforeDate = null;
        
        if (uploadedAfter != null) {
            uploadedAfterDate = LocalDateTime.parse(uploadedAfter);
        }
        if (uploadedBefore != null) {
            uploadedBeforeDate = LocalDateTime.parse(uploadedBefore);
        }

        Page<DocumentResponse> documents = documentService.advancedSearch(
                title, description, documentType, fileType, isPublic,
                contactId, accountId, dealId, leadId, taskId,
                uploadedAfterDate, uploadedBeforeDate, pageable);
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Get document analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getDocumentAnalytics() {
        Map<String, Object> analytics = documentService.getDocumentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get document types
     */
    @GetMapping("/types")
    public ResponseEntity<DocumentType[]> getDocumentTypes() {
        return ResponseEntity.ok(DocumentType.values());
    }

    /**
     * Update document metadata
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentUploadRequest request) {
        DocumentResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.ok(response);
    }
}

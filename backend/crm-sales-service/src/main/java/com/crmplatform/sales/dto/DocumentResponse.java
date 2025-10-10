package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.Document;
import com.crmplatform.sales.entity.DocumentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentResponse {
    
    private Long documentId;
    private Long tenantId;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String formattedFileSize;
    private String mimeType;
    private String fileExtension;
    private DocumentType documentType;
    private String title;
    private String description;
    private Long uploadedByUserId;
    private String uploadedByUserName;
    private Boolean isPublic;
    private Integer downloadCount;
    
    // Related entity information
    private Long relatedContactId;
    private String relatedContactName;
    private Long relatedAccountId;
    private String relatedAccountName;
    private Long relatedDealId;
    private String relatedDealTitle;
    private Long relatedLeadId;
    private String relatedLeadName;
    private Long relatedTaskId;
    private String relatedTaskTitle;
    private String relatedEntityType;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private boolean image;
    private boolean pdf;
    private boolean document;
    private String downloadUrl;
    private String previewUrl;
    
    public DocumentResponse(Document document) {
        this.documentId = document.getDocumentId();
        this.tenantId = document.getTenantId();
        this.fileName = document.getFileName();
        this.originalFileName = document.getOriginalFileName();
        this.filePath = document.getFilePath();
        this.fileSize = document.getFileSize();
        this.formattedFileSize = document.getFormattedFileSize();
        this.mimeType = document.getMimeType();
        this.fileExtension = document.getFileExtension();
        this.documentType = document.getDocumentType();
        this.title = document.getTitle();
        this.description = document.getDescription();
        this.uploadedByUserId = document.getUploadedByUserId();
        this.isPublic = document.getIsPublic();
        this.downloadCount = document.getDownloadCount();
        this.relatedContactId = document.getRelatedContactId();
        this.relatedAccountId = document.getRelatedAccountId();
        this.relatedDealId = document.getRelatedDealId();
        this.relatedLeadId = document.getRelatedLeadId();
        this.relatedTaskId = document.getRelatedTaskId();
        this.relatedEntityType = document.getRelatedEntityType();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        
        // Set computed fields
        this.image = document.isImage();
        this.pdf = document.isPdf();
        this.document = document.isDocument();
        this.downloadUrl = "/api/v1/documents/" + document.getDocumentId() + "/download";
        this.previewUrl = "/api/v1/documents/" + document.getDocumentId() + "/preview";
    }
}

package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@EqualsAndHashCode(callSuper = false)
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "file_extension", length = 10)
    private String fileExtension;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "uploaded_by_user_id", nullable = false)
    private Long uploadedByUserId;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;
    
    // Related entity fields
    @Column(name = "related_contact_id")
    private Long relatedContactId;
    
    @Column(name = "related_account_id")
    private Long relatedAccountId;
    
    @Column(name = "related_deal_id")
    private Long relatedDealId;
    
    @Column(name = "related_lead_id")
    private Long relatedLeadId;
    
    @Column(name = "related_task_id")
    private Long relatedTaskId;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Computed methods
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public boolean isImage() {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/");
    }
    
    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }
    
    public boolean isDocument() {
        if (mimeType == null) return false;
        return mimeType.contains("document") || 
               mimeType.contains("word") || 
               mimeType.contains("excel") || 
               mimeType.contains("powerpoint") ||
               mimeType.contains("text");
    }
}

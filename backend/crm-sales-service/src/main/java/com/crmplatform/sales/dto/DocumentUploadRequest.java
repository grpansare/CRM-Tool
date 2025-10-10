package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.DocumentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentUploadRequest {
    
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Document type is required")
    private DocumentType documentType;
    
    private Boolean isPublic = false;
    
    // Related entity fields
    private Long relatedContactId;
    private Long relatedAccountId;
    private Long relatedDealId;
    private Long relatedLeadId;
    private Long relatedTaskId;
    private String relatedEntityType;
}

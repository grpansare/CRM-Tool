package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.LeadDisposition;
import java.time.LocalDateTime;

public class DispositionHistoryResponse {
    
    private LeadDisposition disposition;
    private String notes;
    private LocalDateTime contactDate;
    private LocalDateTime nextFollowUpDate;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private String updatedByName;
    
    // Constructors
    public DispositionHistoryResponse() {}
    
    public DispositionHistoryResponse(LeadDisposition disposition, String notes, 
                                    LocalDateTime contactDate, LocalDateTime nextFollowUpDate,
                                    LocalDateTime updatedAt, Long updatedBy, String updatedByName) {
        this.disposition = disposition;
        this.notes = notes;
        this.contactDate = contactDate;
        this.nextFollowUpDate = nextFollowUpDate;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.updatedByName = updatedByName;
    }
    
    // Getters and Setters
    public LeadDisposition getDisposition() {
        return disposition;
    }
    
    public void setDisposition(LeadDisposition disposition) {
        this.disposition = disposition;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getContactDate() {
        return contactDate;
    }
    
    public void setContactDate(LocalDateTime contactDate) {
        this.contactDate = contactDate;
    }
    
    public LocalDateTime getNextFollowUpDate() {
        return nextFollowUpDate;
    }
    
    public void setNextFollowUpDate(LocalDateTime nextFollowUpDate) {
        this.nextFollowUpDate = nextFollowUpDate;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getUpdatedByName() {
        return updatedByName;
    }
    
    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
}

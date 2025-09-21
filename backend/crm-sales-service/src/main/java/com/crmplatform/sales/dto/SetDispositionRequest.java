package com.crmplatform.sales.dto;

import com.crmplatform.sales.entity.LeadDisposition;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SetDispositionRequest {
    
    @NotNull(message = "Disposition is required")
    private LeadDisposition disposition;
    
    private String notes;
    
    private LocalDateTime nextFollowUpDate;
    
    // Constructors
    public SetDispositionRequest() {}
    
    public SetDispositionRequest(LeadDisposition disposition, String notes, LocalDateTime nextFollowUpDate) {
        this.disposition = disposition;
        this.notes = notes;
        this.nextFollowUpDate = nextFollowUpDate;
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
    
    public LocalDateTime getNextFollowUpDate() {
        return nextFollowUpDate;
    }
    
    public void setNextFollowUpDate(LocalDateTime nextFollowUpDate) {
        this.nextFollowUpDate = nextFollowUpDate;
    }
}

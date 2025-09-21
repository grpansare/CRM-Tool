package com.crmplatform.sales.entity;

public enum LeadDisposition {
    // Positive Dispositions
    INTERESTED("Interested"),
    MEETING_SCHEDULED("Meeting Scheduled"),
    DEMO_REQUESTED("Demo Requested"),
    PROPOSAL_SENT("Proposal Sent"),
    
    // Neutral/Follow-up Dispositions
    CALL_BACK_LATER("Call Back Later"),
    NO_ANSWER("No Answer"),
    VOICEMAIL_LEFT("Voicemail Left"),
    EMAIL_SENT("Email Sent"),
    BUSY("Busy"),
    
    // Negative Dispositions
    NOT_INTERESTED("Not Interested"),
    NOT_QUALIFIED("Not Qualified"),
    WRONG_NUMBER("Wrong Number"),
    DO_NOT_CALL("Do Not Call"),
    COMPETITOR("Using Competitor"),
    NO_BUDGET("No Budget"),
    NO_AUTHORITY("No Authority"),
    
    // Administrative
    CONVERTED("Converted to Deal"),
    LOST("Lost Lead");
    
    private final String displayName;
    
    LeadDisposition(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isPositive() {
        return this == INTERESTED || this == MEETING_SCHEDULED || 
               this == DEMO_REQUESTED || this == PROPOSAL_SENT;
    }
    
    public boolean isNegative() {
        return this == NOT_INTERESTED || this == NOT_QUALIFIED || 
               this == WRONG_NUMBER || this == DO_NOT_CALL || 
               this == COMPETITOR || this == NO_BUDGET || this == NO_AUTHORITY;
    }
    
    public boolean requiresFollowUp() {
        return this == CALL_BACK_LATER || this == NO_ANSWER || 
               this == VOICEMAIL_LEFT || this == EMAIL_SENT || this == BUSY;
    }
}

package com.crmplatform.sales.entity;

public enum TaskType {
    GENERAL("General"),
    CALL("Call"),
    EMAIL("Email"),
    MEETING("Meeting"),
    FOLLOW_UP("Follow Up"),
    DEMO("Demo"),
    PROPOSAL("Proposal"),
    CONTRACT("Contract"),
    RESEARCH("Research"),
    ADMINISTRATIVE("Administrative");
    
    private final String displayName;
    
    TaskType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

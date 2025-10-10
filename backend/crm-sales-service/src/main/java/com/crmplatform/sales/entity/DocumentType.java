package com.crmplatform.sales.entity;

public enum DocumentType {
    CONTRACT("Contract"),
    PROPOSAL("Proposal"),
    INVOICE("Invoice"),
    RECEIPT("Receipt"),
    PRESENTATION("Presentation"),
    BROCHURE("Brochure"),
    SPECIFICATION("Specification"),
    AGREEMENT("Agreement"),
    REPORT("Report"),
    IMAGE("Image"),
    SPREADSHEET("Spreadsheet"),
    EMAIL_ATTACHMENT("Email Attachment"),
    LEGAL_DOCUMENT("Legal Document"),
    MARKETING_MATERIAL("Marketing Material"),
    TECHNICAL_DOCUMENT("Technical Document"),
    OTHER("Other");
    
    private final String displayName;
    
    DocumentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

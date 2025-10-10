package com.crmplatform.contacts.service;

import com.crmplatform.contacts.entity.Contact;
import com.crmplatform.contacts.repository.ContactRepository;
import com.crmplatform.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactImportExportService {

    @Autowired
    private ContactRepository contactRepository;

    // CSV header matching actual Contact entity fields
    private static final String CSV_HEADER = "firstName,lastName,primaryEmail,phoneNumber,jobTitle";
    private static final String[] REQUIRED_FIELDS = {"firstName", "lastName", "primaryEmail"};
    
    /**
     * Import contacts from CSV file
     */
    public ImportResult importContactsFromCsv(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = csvContent.split("\n");
            
            if (lines.length < 2) {
                result.addError("CSV file must contain at least a header row and one data row");
                return result;
            }
            
            // Parse header
            String[] headers = parseCSVLine(lines[0]);
            Map<String, Integer> headerMap = createHeaderMap(headers);
            
            // Validate required fields
            for (String requiredField : REQUIRED_FIELDS) {
                if (!headerMap.containsKey(requiredField.toLowerCase())) {
                    result.addError("Missing required field: " + requiredField);
                }
            }
            
            if (!result.getErrors().isEmpty()) {
                return result;
            }
            
            // Process data rows
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) continue;
                
                try {
                    String[] values = parseCSVLine(lines[i]);
                    Contact contact = createContactFromCsvRow(values, headerMap);
                    
                    if (contact != null) {
                        // Check for duplicates using correct repository method
                        if (contactRepository.existsByTenantIdAndPrimaryEmail(contact.getTenantId(), contact.getPrimaryEmail())) {
                            result.addDuplicate(contact.getPrimaryEmail());
                        } else {
                            contactRepository.save(contact);
                            result.incrementSuccessCount();
                        }
                    }
                } catch (Exception e) {
                    result.addError("Error processing row " + (i + 1) + ": " + e.getMessage());
                    log.error("Error processing CSV row {}: {}", i + 1, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.addError("Error reading CSV file: " + e.getMessage());
            log.error("Error importing contacts from CSV", e);
        }
        
        return result;
    }
    /**
     * Export contacts to CSV
     */
    public String exportContactsToCsv() {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Contact> contacts = contactRepository.findByTenantId(tenantId, 
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER).append("\n");
        
        for (Contact contact : contacts) {
            csv.append(contactToCsvRow(contact)).append("\n");
        }
        
        return csv.toString();
    }

    /**
     * Export filtered contacts to CSV
     */
    public String exportFilteredContactsToCsv(String searchTerm, String company, String jobTitle) {
        Long tenantId = UserContext.getCurrentTenantId();
        List<Contact> contacts;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            contacts = contactRepository.searchContacts(tenantId, searchTerm);
        } else {
            contacts = contactRepository.findByTenantId(tenantId, 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
        
        // Apply additional filters that are available in Contact entity
        // Note: 'company' field doesn't exist in Contact entity, so we skip it
        
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            contacts = contacts.stream()
                .filter(c -> c.getJobTitle() != null && 
                            c.getJobTitle().toLowerCase().contains(jobTitle.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER).append("\n");
        
        for (Contact contact : contacts) {
            csv.append(contactToCsvRow(contact)).append("\n");
        }
        
        return csv.toString();
    }
    /**
     * Get import template CSV
     */
    public String getImportTemplate() {
        StringBuilder template = new StringBuilder();
        template.append(CSV_HEADER).append("\n");
        template.append("John,Doe,john.doe@example.com,+1-555-0123,Sales Manager\n");
        template.append("Jane,Smith,jane.smith@example.com,+1-555-0124,Software Developer\n");
        return template.toString();
    }
    private Contact createContactFromCsvRow(String[] values, Map<String, Integer> headerMap) {
        try {
            Contact contact = new Contact();
            
            // Set tenant and user context
            contact.setTenantId(UserContext.getCurrentTenantId());
            contact.setOwnerUserId(UserContext.getCurrentUserId());
            
            // Map CSV values to contact fields (matching actual Contact entity)
            contact.setFirstName(getValueFromMap("firstname", headerMap, values));
            contact.setLastName(getValueFromMap("lastname", headerMap, values));
            contact.setPrimaryEmail(getValueFromMap("primaryemail", headerMap, values));
            contact.setPhoneNumber(getValueFromMap("phonenumber", headerMap, values));
            contact.setJobTitle(getValueFromMap("jobtitle", headerMap, values));
            
            // Validate required fields
            if (contact.getFirstName() == null || contact.getFirstName().trim().isEmpty() ||
                contact.getLastName() == null || contact.getLastName().trim().isEmpty() ||
                contact.getPrimaryEmail() == null || contact.getPrimaryEmail().trim().isEmpty()) {
                return null;
            }
            
            return contact;
            
        } catch (Exception e) {
            log.error("Error creating contact from CSV row", e);
            return null;
        }
    }

    private String contactToCsvRow(Contact contact) {
        StringBuilder row = new StringBuilder();
        
        row.append(escapeCsvValue(contact.getFirstName())).append(",");
        row.append(escapeCsvValue(contact.getLastName())).append(",");
        row.append(escapeCsvValue(contact.getPrimaryEmail())).append(",");
        row.append(escapeCsvValue(contact.getPhoneNumber())).append(",");
        row.append(escapeCsvValue(contact.getJobTitle()));
        
        return row.toString();
    }

    // Include all other helper methods: createHeaderMap, getValueFromMap, 
    // escapeCsvValue, parseCSVLine, and the ImportResult class
    
    /**
     * Import result class
     */
    public static class ImportResult {
        private int successCount = 0;
        private List<String> errors = new ArrayList<>();
        private List<String> duplicates = new ArrayList<>();
        
        public void incrementSuccessCount() {
            successCount++;
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addDuplicate(String email) {
            duplicates.add(email);
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getDuplicates() {
            return duplicates;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public int getTotalProcessed() {
            return successCount + duplicates.size();
        }
        
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append("Import Summary:\n");
            summary.append("- Successfully imported: ").append(successCount).append(" contacts\n");
            summary.append("- Duplicates skipped: ").append(duplicates.size()).append(" contacts\n");
            summary.append("- Errors encountered: ").append(errors.size()).append(" errors\n");
            summary.append("- Total processed: ").append(getTotalProcessed()).append(" contacts\n");
            
            if (!duplicates.isEmpty()) {
                summary.append("\nDuplicate emails skipped:\n");
                duplicates.forEach(email -> summary.append("- ").append(email).append("\n"));
            }
            
            if (!errors.isEmpty()) {
                summary.append("\nErrors encountered:\n");
                errors.forEach(error -> summary.append("- ").append(error).append("\n"));
            }
            
            return summary.toString();
        }
    }
    private Map<String, Integer> createHeaderMap(String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].trim().toLowerCase(), i);
        }
        return headerMap;
    }

    private String getValueFromMap(String fieldName, Map<String, Integer> headerMap, String[] values) {
        Integer index = headerMap.get(fieldName.toLowerCase());
        if (index != null && index < values.length) {
            String value = values[index].trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private String escapeCsvValue(String value) {
        if (value == null) return "";
        
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add last field
        result.add(currentField.toString());
        
        return result.toArray(new String[0]);
    }
}
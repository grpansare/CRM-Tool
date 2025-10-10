package com.crmplatform.contacts.controller;

import com.crmplatform.contacts.service.ContactImportExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contacts")
@CrossOrigin(origins = "*")
@Slf4j
public class ContactImportExportController {

    @Autowired
    private ContactImportExportService importExportService;

    /**
     * Import contacts from CSV file
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importContacts(
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("File is empty"));
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("File must be a CSV file"));
            }
            
            // Process import
            ContactImportExportService.ImportResult result = importExportService.importContactsFromCsv(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", !result.hasErrors());
            response.put("successCount", result.getSuccessCount());
            response.put("duplicateCount", result.getDuplicates().size());
            response.put("totalProcessed", result.getTotalProcessed());
            response.put("errors", result.getErrors());
            response.put("duplicates", result.getDuplicates());
            
            if (result.hasErrors()) {
                response.put("message", "Import completed with errors");
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            } else {
                response.put("message", String.format("Successfully imported %d contacts", result.getSuccessCount()));
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("Error importing contacts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error importing contacts: " + e.getMessage()));
        }
    }

    /**
     * Export all contacts to CSV
     */
    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportContacts() {
        try {
            String csvContent = importExportService.exportContactsToCsv();
            
            ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());
            
            String filename = "contacts_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error exporting contacts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export filtered contacts to CSV
     */
    @GetMapping("/export/filtered")
    public ResponseEntity<ByteArrayResource> exportFilteredContacts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String jobTitle) {
        
        try {
            String csvContent = importExportService.exportFilteredContactsToCsv(search, company, jobTitle);
            
            ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());
            
            String filename = "contacts_filtered_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error exporting filtered contacts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download CSV import template
     */
    @GetMapping("/import/template")
    public ResponseEntity<ByteArrayResource> downloadImportTemplate() {
        try {
            String templateContent = importExportService.getImportTemplate();
            
            ByteArrayResource resource = new ByteArrayResource(templateContent.getBytes());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contacts_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error generating import template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get import/export statistics
     */
    @GetMapping("/import-export/stats")
    public ResponseEntity<Map<String, Object>> getImportExportStats() {
        try {
            // This could be enhanced to track actual import/export history
            Map<String, Object> stats = new HashMap<>();
            stats.put("supportedFormats", new String[]{"CSV"});
            stats.put("maxFileSize", "10MB");
            stats.put("requiredFields", new String[]{"firstName", "lastName", "email"});
            stats.put("optionalFields", new String[]{"phone", "company", "jobTitle", "address", "city", "state", "country", "postalCode", "website", "notes"});
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting import/export stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error getting statistics"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }
}

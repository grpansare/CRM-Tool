package com.crmplatform.contacts.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.contacts.dto.ContactResponse;
import com.crmplatform.contacts.dto.CreateContactRequest;
import com.crmplatform.contacts.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {
    
    private final ContactService contactService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> createContact(
            @Valid @RequestBody CreateContactRequest request) {
        
        log.info("Creating contact: {}", request.getPrimaryEmail());
        
        ApiResponse<ContactResponse> response = contactService.createContact(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
    
    @GetMapping("/{contactId}")
    public ResponseEntity<ApiResponse<ContactResponse>> getContact(@PathVariable Long contactId) {
        log.info("Getting contact: {}", contactId);
        
        ApiResponse<ContactResponse> response = contactService.getContact(contactId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactResponse>>> searchContacts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching contacts with term: {}", searchTerm);
        
        ApiResponse<List<ContactResponse>> response = contactService.searchContacts(searchTerm, page, size);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{contactId}")
    public ResponseEntity<ApiResponse<ContactResponse>> updateContact(
            @PathVariable Long contactId,
            @Valid @RequestBody CreateContactRequest request) {
        
        log.info("Updating contact: {}", contactId);
        
        ApiResponse<ContactResponse> response = contactService.updateContact(contactId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
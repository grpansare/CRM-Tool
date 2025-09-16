package com.crmplatform.contacts.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.contacts.dto.AccountResponse;
import com.crmplatform.contacts.dto.AccountHierarchyResponse;
import com.crmplatform.contacts.dto.ContactResponse;
import com.crmplatform.contacts.dto.CreateAccountRequest;
import com.crmplatform.contacts.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    
    private final AccountService accountService;
    
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long accountId) {
        log.info("Getting account: {}", accountId);
        
        ApiResponse<AccountResponse> response = accountService.getAccount(accountId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        
        log.info("Creating account: {}", request.getAccountName());
        
        ApiResponse<AccountResponse> response = accountService.createAccount(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> searchAccounts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching accounts with term: {}", searchTerm);
        
        ApiResponse<List<AccountResponse>> response = accountService.searchAccounts(searchTerm, page, size);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateAccountRequest request) {
        
        log.info("Updating account: {} with data: {}", accountId, request.getAccountName());
        
        ApiResponse<AccountResponse> response = accountService.updateAccount(accountId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{accountId}/contacts")
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getAccountContacts(@PathVariable Long accountId) {
        log.info("Getting contacts for account: {}", accountId);
        
        ApiResponse<List<ContactResponse>> response = accountService.getAccountContacts(accountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{accountId}/hierarchy")
    public ResponseEntity<ApiResponse<AccountHierarchyResponse>> getAccountHierarchy(@PathVariable Long accountId) {
        log.info("Getting account hierarchy for: {}", accountId);
        
        ApiResponse<AccountHierarchyResponse> response = accountService.getAccountHierarchy(accountId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/hierarchy/parents")
    public ResponseEntity<ApiResponse<List<AccountHierarchyResponse>>> getParentAccounts() {
        log.info("Getting all parent accounts");
        
        ApiResponse<List<AccountHierarchyResponse>> response = accountService.getParentAccounts();
        return ResponseEntity.ok(response);
    }
} 
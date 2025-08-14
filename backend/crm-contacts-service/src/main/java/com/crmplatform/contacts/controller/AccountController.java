package com.crmplatform.contacts.controller;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.contacts.dto.AccountResponse;
import com.crmplatform.contacts.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> searchAccounts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching accounts with term: {}", searchTerm);
        
        ApiResponse<List<AccountResponse>> response = accountService.searchAccounts(searchTerm, page, size);
        return ResponseEntity.ok(response);
    }
} 
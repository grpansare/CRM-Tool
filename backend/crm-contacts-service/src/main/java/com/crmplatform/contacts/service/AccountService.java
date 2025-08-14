package com.crmplatform.contacts.service;

import com.crmplatform.common.dto.ApiResponse;
import com.crmplatform.common.security.UserContext;
import com.crmplatform.contacts.dto.AccountResponse;
import com.crmplatform.contacts.entity.Account;
import com.crmplatform.contacts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    public ApiResponse<AccountResponse> getAccount(Long accountId) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        Optional<Account> accountOpt = accountRepository.findByTenantIdAndAccountId(tenantId, accountId);
        if (accountOpt.isEmpty()) {
            return ApiResponse.error("Account not found", "ACCOUNT_NOT_FOUND");
        }
        
        Account account = accountOpt.get();
        AccountResponse response = buildAccountResponse(account);
        return ApiResponse.success(response);
    }
    
    public ApiResponse<List<AccountResponse>> searchAccounts(String searchTerm, int page, int size) {
        Long tenantId = UserContext.getCurrentTenantId();
        
        List<Account> accounts;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            accounts = accountRepository.searchAccounts(tenantId, searchTerm.trim());
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Account> accountPage = accountRepository.findByTenantId(tenantId, pageable);
            accounts = accountPage.getContent();
        }
        
        List<AccountResponse> responses = accounts.stream()
                .map(this::buildAccountResponse)
                .toList();
        
        return ApiResponse.success(responses);
    }
    
    private AccountResponse buildAccountResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .accountName(account.getAccountName())
                .website(account.getWebsite())
                .industry(account.getIndustry())
                .ownerUserId(account.getOwnerUserId())
                .createdAt(account.getCreatedAt())
                .contacts(null) // TODO: Add contact relationships when implemented
                .build();
    }
} 
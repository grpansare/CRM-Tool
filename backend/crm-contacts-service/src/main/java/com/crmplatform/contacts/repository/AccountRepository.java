package com.crmplatform.contacts.repository;

import com.crmplatform.contacts.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.accountName = :accountName")
    Optional<Account> findByTenantIdAndAccountName(@Param("tenantId") Long tenantId, 
                                                  @Param("accountName") String accountName);
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.accountId = :accountId")
    Optional<Account> findByTenantIdAndAccountId(@Param("tenantId") Long tenantId, 
                                                @Param("accountId") Long accountId);
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND " +
           "(a.website LIKE %:domain% OR a.accountName = :accountName)")
    Optional<Account> findByTenantIdAndWebsiteOrAccountName(@Param("tenantId") Long tenantId, 
                                                           @Param("domain") String domain, 
                                                           @Param("accountName") String accountName);
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.ownerUserId = :ownerUserId")
    List<Account> findByTenantIdAndOwnerUserId(@Param("tenantId") Long tenantId, 
                                              @Param("ownerUserId") Long ownerUserId);
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId")
    Page<Account> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND " +
           "LOWER(a.accountName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Account> searchAccounts(@Param("tenantId") Long tenantId, 
                                @Param("searchTerm") String searchTerm);
    
    // Account hierarchy queries
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.parentAccountId = :parentAccountId")
    List<Account> findChildAccounts(@Param("tenantId") Long tenantId, 
                                   @Param("parentAccountId") Long parentAccountId);
    
    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.parentAccountId IS NULL")
    List<Account> findParentAccounts(@Param("tenantId") Long tenantId);
    
    // Revenue tracking queries
    @Query("SELECT SUM(a.annualRevenue) FROM Account a WHERE a.tenantId = :tenantId AND a.parentAccountId = :parentAccountId")
    java.math.BigDecimal getTotalRevenueForParentAccount(@Param("tenantId") Long tenantId, 
                                                        @Param("parentAccountId") Long parentAccountId);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.tenantId = :tenantId AND a.parentAccountId = :parentAccountId")
    Long getChildAccountCount(@Param("tenantId") Long tenantId, 
                             @Param("parentAccountId") Long parentAccountId);
    
    boolean existsByTenantIdAndAccountName(Long tenantId, String accountName);
}
package com.crmplatform.contacts.repository;

import com.crmplatform.contacts.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    @Query("SELECT c FROM Contact c WHERE c.tenantId = :tenantId AND c.primaryEmail = :email")
    Optional<Contact> findByTenantIdAndPrimaryEmail(@Param("tenantId") Long tenantId, 
                                                   @Param("email") String email);
    
    @Query("SELECT c FROM Contact c WHERE c.tenantId = :tenantId AND c.contactId = :contactId")
    Optional<Contact> findByTenantIdAndContactId(@Param("tenantId") Long tenantId, 
                                                @Param("contactId") Long contactId);
    
    @Query("SELECT c FROM Contact c WHERE c.tenantId = :tenantId AND c.ownerUserId = :ownerUserId")
    List<Contact> findByTenantIdAndOwnerUserId(@Param("tenantId") Long tenantId, 
                                              @Param("ownerUserId") Long ownerUserId);
    
    @Query("SELECT c FROM Contact c WHERE c.tenantId = :tenantId")
    Page<Contact> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.tenantId = :tenantId AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.primaryEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Contact> searchContacts(@Param("tenantId") Long tenantId, 
                                @Param("searchTerm") String searchTerm);
    
    boolean existsByTenantIdAndPrimaryEmail(Long tenantId, String primaryEmail);
} 
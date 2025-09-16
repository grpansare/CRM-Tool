package com.crmplatform.contacts.repository;

import com.crmplatform.contacts.entity.AccountContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountContactRepository extends JpaRepository<AccountContact, AccountContact.AccountContactId> {
    
    @Query("SELECT ac FROM AccountContact ac WHERE ac.id.contactId = :contactId AND ac.id.tenantId = :tenantId")
    Optional<AccountContact> findContactAssociation(@Param("contactId") Long contactId, @Param("tenantId") Long tenantId);
    
    @Query("SELECT ac FROM AccountContact ac WHERE ac.id.accountId = :accountId AND ac.id.tenantId = :tenantId")
    List<AccountContact> findAccountAssociations(@Param("accountId") Long accountId, @Param("tenantId") Long tenantId);
    
    @Modifying
    @Query("DELETE FROM AccountContact ac WHERE ac.id.contactId = :contactId AND ac.id.tenantId = :tenantId")
    void removeContactAssociation(@Param("contactId") Long contactId, @Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(ac) FROM AccountContact ac WHERE ac.id.accountId = :accountId AND ac.id.tenantId = :tenantId")
    Long countContactsForAccount(@Param("accountId") Long accountId, @Param("tenantId") Long tenantId);
}

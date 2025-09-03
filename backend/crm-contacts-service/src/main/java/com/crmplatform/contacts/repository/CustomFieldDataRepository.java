package com.crmplatform.contacts.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crmplatform.contacts.entity.CustomFieldData;

import java.util.List;

@Repository
public interface CustomFieldDataRepository extends JpaRepository<CustomFieldData, Long> {
    
    List<CustomFieldData> findByTenantIdAndEntityIdAndEntityType(
            Long tenantId, Long entityId, CustomFieldData.EntityType entityType);
    
    @Query("SELECT c FROM CustomFieldData c WHERE c.tenantId = :tenantId AND c.entityId = :entityId AND c.entityType = :entityType AND c.fieldName = :fieldName")
    CustomFieldData findByTenantIdAndEntityIdAndEntityTypeAndFieldName(
            @Param("tenantId") Long tenantId, 
            @Param("entityId") Long entityId, 
            @Param("entityType") CustomFieldData.EntityType entityType,
            @Param("fieldName") String fieldName);
    
    void deleteByTenantIdAndEntityIdAndEntityType(
            Long tenantId, Long entityId, CustomFieldData.EntityType entityType);
}
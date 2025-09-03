package com.crmplatform.contacts.service;

import com.crmplatform.contacts.entity.CustomFieldData;
import com.crmplatform.contacts.repository.CustomFieldDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomFieldService {
    
    private final CustomFieldDataRepository customFieldDataRepository;
    
    @Transactional
    public void saveCustomFields(Long tenantId, Long entityId, CustomFieldData.EntityType entityType, Map<String, String> customFields) {
        if (customFields == null || customFields.isEmpty()) {
            return;
        }
        
        log.info("Saving custom fields for entity: {} of type: {}", entityId, entityType);
        
        // Delete existing custom fields for this entity
        customFieldDataRepository.deleteByTenantIdAndEntityIdAndEntityType(tenantId, entityId, entityType);
        
        // Save new custom fields
        List<CustomFieldData> customFieldDataList = customFields.entrySet().stream()
                .map(entry -> CustomFieldData.builder()
                        .tenantId(tenantId)
                        .entityId(entityId)
                        .entityType(entityType)
                        .fieldName(entry.getKey())
                        .fieldValue(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        customFieldDataRepository.saveAll(customFieldDataList);
    }
    
    public Map<String, String> getCustomFields(Long tenantId, Long entityId, CustomFieldData.EntityType entityType) {
        List<CustomFieldData> customFieldDataList = customFieldDataRepository
                .findByTenantIdAndEntityIdAndEntityType(tenantId, entityId, entityType);
        
        return customFieldDataList.stream()
                .collect(Collectors.toMap(
                        CustomFieldData::getFieldName,
                        CustomFieldData::getFieldValue
                ));
    }
    
    @Transactional
    public void deleteCustomFields(Long tenantId, Long entityId, CustomFieldData.EntityType entityType) {
        customFieldDataRepository.deleteByTenantIdAndEntityIdAndEntityType(tenantId, entityId, entityType);
    }
}

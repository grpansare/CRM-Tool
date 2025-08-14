package com.crmplatform.email.repository;

import com.crmplatform.email.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    
    Optional<EmailTemplate> findByTenantIdAndTemplateNameAndIsActiveTrue(String tenantId, String templateName);
    
    List<EmailTemplate> findByTenantIdAndIsActiveTrue(String tenantId);
    
    List<EmailTemplate> findByTenantIdAndTemplateTypeAndIsActiveTrue(String tenantId, EmailTemplate.TemplateType templateType);
    
    boolean existsByTenantIdAndTemplateName(String tenantId, String templateName);
}

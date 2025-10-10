//package com.crmplatform.sales.repository;
//
//import com.crmplatform.sales.entity.EmailTemplate;
//import com.crmplatform.sales.entity.EmailTemplateType;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
//    
//    // Find templates by tenant and type
//    List<EmailTemplate> findByTenantIdAndTemplateTypeAndIsActiveOrderByTemplateName(
//        Long tenantId, EmailTemplateType templateType, Boolean isActive);
//    
//    // Find all active templates by tenant
//    List<EmailTemplate> findByTenantIdAndIsActiveOrderByTemplateTypeAscTemplateNameAsc(
//        Long tenantId, Boolean isActive);
//    
//    // Find default template for a type
//    Optional<EmailTemplate> findByTenantIdAndTemplateTypeAndIsDefaultAndIsActive(
//        Long tenantId, EmailTemplateType templateType, Boolean isDefault, Boolean isActive);
//    
//    // Find templates by tenant with pagination
//    Page<EmailTemplate> findByTenantIdOrderByTemplateTypeAscTemplateNameAsc(Long tenantId, Pageable pageable);
//    
//    // Find templates by name (search)
//    @Query("SELECT t FROM EmailTemplate t WHERE t.tenantId = :tenantId " +
//           "AND t.isActive = true " +
//           "AND LOWER(t.templateName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
//    List<EmailTemplate> searchByTemplateName(@Param("tenantId") Long tenantId, 
//                                           @Param("searchTerm") String searchTerm);
//    
//    // Check if template name exists
//    boolean existsByTenantIdAndTemplateName(Long tenantId, String templateName);
//    
//    // Count templates by type
//    long countByTenantIdAndTemplateTypeAndIsActive(Long tenantId, EmailTemplateType templateType, Boolean isActive);
//    
//    // Count all active templates
//    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);
//    
//    // Find templates by created user
//    List<EmailTemplate> findByTenantIdAndCreatedByOrderByCreatedAtDesc(Long tenantId, Long createdBy);
//    
//    // Find templates by type and active status
//    List<EmailTemplate> findByTenantIdAndTemplateTypeAndIsActive(
//        Long tenantId, EmailTemplateType templateType, Boolean isActive);
//    
//    // Get most recently used templates
//    @Query("SELECT t FROM EmailTemplate t WHERE t.tenantId = :tenantId " +
//           "AND t.isActive = true " +
//           "ORDER BY t.updatedAt DESC")
//    List<EmailTemplate> findRecentlyUpdatedTemplates(@Param("tenantId") Long tenantId, Pageable pageable);
//}

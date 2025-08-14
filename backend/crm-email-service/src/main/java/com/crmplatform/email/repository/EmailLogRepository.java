package com.crmplatform.email.repository;

import com.crmplatform.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    List<EmailLog> findByTenantIdOrderBySentAtDesc(String tenantId);
    
    List<EmailLog> findByTenantIdAndStatusOrderBySentAtDesc(String tenantId, EmailLog.EmailStatus status);
    
    List<EmailLog> findByTenantIdAndEmailTypeOrderBySentAtDesc(String tenantId, EmailLog.EmailType emailType);
    
    @Query("SELECT el FROM EmailLog el WHERE el.tenantId = :tenantId AND el.sentAt BETWEEN :startDate AND :endDate ORDER BY el.sentAt DESC")
    List<EmailLog> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.tenantId = :tenantId AND el.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") EmailLog.EmailStatus status);
}

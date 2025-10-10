package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.LeadRoutingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRoutingQueueRepository extends JpaRepository<LeadRoutingQueue, Long> {
    
    // Find queue entry by lead
    Optional<LeadRoutingQueue> findByTenantIdAndLeadId(Long tenantId, Long leadId);
    
    // Find pending queue entries
    List<LeadRoutingQueue> findByTenantIdAndQueueStatusOrderByPriorityScoreDescCreatedAtAsc(
        Long tenantId, String queueStatus);
    
    // Find failed queue entries
    List<LeadRoutingQueue> findByTenantIdAndQueueStatusAndRoutingAttemptsLessThanOrderByCreatedAtAsc(
        Long tenantId, String queueStatus, Integer maxAttempts);
    
    // Find queue entries by status
    List<LeadRoutingQueue> findByTenantIdAndQueueStatusOrderByCreatedAtDesc(
        Long tenantId, String queueStatus);
    
    // Find queue entries needing retry
    @Query("SELECT q FROM LeadRoutingQueue q WHERE q.tenantId = :tenantId " +
           "AND q.queueStatus = 'FAILED' AND q.routingAttempts < q.maxAttempts " +
           "AND (q.lastAttemptAt IS NULL OR q.lastAttemptAt < :retryAfter) " +
           "ORDER BY q.priorityScore DESC, q.createdAt ASC")
    List<LeadRoutingQueue> findEntriesNeedingRetry(@Param("tenantId") Long tenantId,
                                                 @Param("retryAfter") LocalDateTime retryAfter);
    
    // Count by status
    long countByTenantIdAndQueueStatus(Long tenantId, String queueStatus);
    
    // Find high priority pending entries
    @Query("SELECT q FROM LeadRoutingQueue q WHERE q.tenantId = :tenantId " +
           "AND q.queueStatus = 'PENDING' AND q.priorityScore >= :minPriority " +
           "ORDER BY q.priorityScore DESC, q.createdAt ASC")
    List<LeadRoutingQueue> findHighPriorityPendingEntries(@Param("tenantId") Long tenantId,
                                                         @Param("minPriority") Integer minPriority);
}

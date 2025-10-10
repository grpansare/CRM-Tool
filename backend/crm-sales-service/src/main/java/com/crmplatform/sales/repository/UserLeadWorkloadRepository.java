package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.UserLeadWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLeadWorkloadRepository extends JpaRepository<UserLeadWorkload, Long> {
    
    // Find workload by tenant and user
    Optional<UserLeadWorkload> findByTenantIdAndUserId(Long tenantId, Long userId);
    
    // Find all workloads for a tenant
    List<UserLeadWorkload> findByTenantIdOrderByActiveLeadsCountAsc(Long tenantId);
    
    // Find available users
    List<UserLeadWorkload> findByTenantIdAndIsAvailableOrderByActiveLeadsCountAsc(
        Long tenantId, Boolean isAvailable);
    
    // Find users with capacity
    @Query("SELECT w FROM UserLeadWorkload w WHERE w.tenantId = :tenantId " +
           "AND w.isAvailable = true AND w.activeLeadsCount < w.maxLeadCapacity " +
           "ORDER BY w.activeLeadsCount ASC")
    List<UserLeadWorkload> findAvailableUsersWithCapacity(@Param("tenantId") Long tenantId);
    
    // Find users by workload range
    @Query("SELECT w FROM UserLeadWorkload w WHERE w.tenantId = :tenantId " +
           "AND w.activeLeadsCount BETWEEN :minCount AND :maxCount " +
           "ORDER BY w.activeLeadsCount ASC")
    List<UserLeadWorkload> findByWorkloadRange(@Param("tenantId") Long tenantId,
                                             @Param("minCount") Integer minCount,
                                             @Param("maxCount") Integer maxCount);
    
    // Get user with minimum workload
    @Query("SELECT w FROM UserLeadWorkload w WHERE w.tenantId = :tenantId " +
           "AND w.isAvailable = true AND w.activeLeadsCount < w.maxLeadCapacity " +
           "ORDER BY w.activeLeadsCount ASC LIMIT 1")
    Optional<UserLeadWorkload> findUserWithMinimumWorkload(@Param("tenantId") Long tenantId);
}

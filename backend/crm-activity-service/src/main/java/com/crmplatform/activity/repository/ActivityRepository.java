package com.crmplatform.activity.repository;

import com.crmplatform.activity.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {
    
    // Find activities by tenant ID
    List<Activity> findByTenantIdOrderByTimestampDesc(Long tenantId);
    
    // Find activities associated with a specific contact
    @Query("{ 'tenant_id': ?0, 'associations.contacts': { $in: [?1] } }")
    Page<Activity> findByTenantIdAndContactId(Long tenantId, Long contactId, Pageable pageable);
    
    // Find activities associated with a specific account
    @Query("{ 'tenant_id': ?0, 'associations.accounts': { $in: [?1] } }")
    Page<Activity> findByTenantIdAndAccountId(Long tenantId, Long accountId, Pageable pageable);
    
    // Find activities associated with a specific deal
    @Query("{ 'tenant_id': ?0, 'associations.deals': { $in: [?1] } }")
    Page<Activity> findByTenantIdAndDealId(Long tenantId, Long dealId, Pageable pageable);
    
    // Find activities associated with a specific lead
    @Query("{ 'tenant_id': ?0, 'associations.leads': { $in: [?1] } }")
    Page<Activity> findByTenantIdAndLeadId(Long tenantId, Long leadId, Pageable pageable);
    
    // Find activities by user and tenant
    Page<Activity> findByTenantIdAndUserIdOrderByTimestampDesc(Long tenantId, Long userId, Pageable pageable);
    
    // Find activities by type and tenant
    Page<Activity> findByTenantIdAndTypeOrderByTimestampDesc(Long tenantId, Activity.ActivityType type, Pageable pageable);
    
    // Company-wide activity tracking methods
    @Query("{ 'tenant_id': ?0, 'associations.accounts': { $in: ?1 } }")
    List<Activity> findByTenantIdAndAccountIds(Long tenantId, List<Long> accountIds);
    
    @Query("{ 'tenant_id': ?0, 'associations.accounts': { $in: ?1 } }")
    Page<Activity> findByTenantIdAndAccountIds(Long tenantId, List<Long> accountIds, Pageable pageable);
    
    // Count activities by account for aggregation
    @Query(value = "{ 'tenant_id': ?0, 'associations.accounts': { $in: [?1] } }", count = true)
    Long countByTenantIdAndAccountId(Long tenantId, Long accountId);
    
    // Find recent activities for account hierarchy
    @Query("{ 'tenant_id': ?0, 'associations.accounts': { $in: ?1 }, 'timestamp': { $gte: ?2 } }")
    List<Activity> findRecentActivitiesByAccountIds(Long tenantId, List<Long> accountIds, java.time.LocalDateTime since);
}

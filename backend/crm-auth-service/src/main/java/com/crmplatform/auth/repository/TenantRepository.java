package com.crmplatform.auth.repository;

import com.crmplatform.auth.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
   
    
    @Query("SELECT t FROM Tenant t WHERE t.isActive = true")
    List<Tenant> findAllActive();
    
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionPlan = :plan AND t.isActive = true")
    List<Tenant> findBySubscriptionPlan(@Param("plan") Tenant.SubscriptionPlan plan);
    
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.subscriptionPlan = :plan AND t.isActive = true")
    long countBySubscriptionPlan(@Param("plan") Tenant.SubscriptionPlan plan);
    
    @Query("SELECT t FROM Tenant t WHERE t.trialEndsAt <= :date AND t.subscriptionPlan = 'FREE'")
    List<Tenant> findExpiredTrials(@Param("date") java.time.LocalDateTime date);
} 
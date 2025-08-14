package com.crmplatform.auth.repository;

import com.crmplatform.auth.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    
    @Query("SELECT up FROM UserPermission up WHERE up.tenantId = :tenantId AND up.userId = :userId")
    List<UserPermission> findByTenantIdAndUserId(@Param("tenantId") Long tenantId, 
                                                @Param("userId") Long userId);
    
    @Query("SELECT up FROM UserPermission up WHERE up.tenantId = :tenantId AND up.userId = :userId " +
           "AND up.resource = :resource AND up.action = :action")
    List<UserPermission> findByTenantIdAndUserIdAndResourceAndAction(@Param("tenantId") Long tenantId, 
                                                                    @Param("userId") Long userId,
                                                                    @Param("resource") String resource,
                                                                    @Param("action") String action);
    
    @Query("SELECT up FROM UserPermission up WHERE up.tenantId = :tenantId AND up.userId = :userId " +
           "AND up.resource = :resource")
    List<UserPermission> findByTenantIdAndUserIdAndResource(@Param("tenantId") Long tenantId, 
                                                           @Param("userId") Long userId,
                                                           @Param("resource") String resource);
} 
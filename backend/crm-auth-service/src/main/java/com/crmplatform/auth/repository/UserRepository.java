package com.crmplatform.auth.repository;

import com.crmplatform.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndActive(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findByUsernameAndActive(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.isActive = true")
    List<User> findByTenantIdAndActive(@Param("tenantId") Long tenantId);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    List<User> findByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.managerId = :managerId AND u.isActive = true")
    List<User> findByTenantIdAndManagerIdAndActive(@Param("tenantId") Long tenantId, 
                                                  @Param("managerId") Long managerId);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.role = :role AND u.isActive = true")
    List<User> findByTenantIdAndRoleAndActive(@Param("tenantId") Long tenantId, 
                                             @Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.userId IN " +
           "(SELECT u2.userId FROM User u2 WHERE u2.managerId = :managerId OR u2.userId = :managerId) AND u.isActive = true")
    List<User> findTeamMembersByManagerId(@Param("tenantId") Long tenantId, 
                                         @Param("managerId") Long managerId);
    
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    
    boolean existsByUsernameAndTenantId(String username, Long tenantId);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
} 
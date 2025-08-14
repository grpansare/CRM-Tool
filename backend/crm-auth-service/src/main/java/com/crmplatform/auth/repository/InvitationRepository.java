package com.crmplatform.auth.repository;

import com.crmplatform.auth.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @Query("SELECT i FROM Invitation i WHERE i.tenantId = :tenantId AND i.status = 'PENDING'")
    List<Invitation> findPendingByTenant(@Param("tenantId") Long tenantId);

    Optional<Invitation> findByTenantIdAndInvitationId(Long tenantId, Long invitationId);

    Optional<Invitation> findByToken(String token);
}


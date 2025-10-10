package com.crmplatform.sales.repository;

import com.crmplatform.sales.entity.Document;
import com.crmplatform.sales.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    // Basic queries
    Page<Document> findByTenantId(Long tenantId, Pageable pageable);
    
    Optional<Document> findByDocumentIdAndTenantId(Long documentId, Long tenantId);
    
    // User-specific queries
    Page<Document> findByTenantIdAndUploadedByUserId(Long tenantId, Long uploadedByUserId, Pageable pageable);
    
    // Document type queries
    Page<Document> findByTenantIdAndDocumentType(Long tenantId, DocumentType documentType, Pageable pageable);
    
    // Public/Private queries
    Page<Document> findByTenantIdAndIsPublic(Long tenantId, Boolean isPublic, Pageable pageable);
    
    // Related entity queries
    List<Document> findByTenantIdAndRelatedContactId(Long tenantId, Long relatedContactId);
    
    List<Document> findByTenantIdAndRelatedAccountId(Long tenantId, Long relatedAccountId);
    
    List<Document> findByTenantIdAndRelatedDealId(Long tenantId, Long relatedDealId);
    
    List<Document> findByTenantIdAndRelatedLeadId(Long tenantId, Long relatedLeadId);
    
    List<Document> findByTenantIdAndRelatedTaskId(Long tenantId, Long relatedTaskId);
    
    // Search queries
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId AND " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.originalFileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Document> searchDocuments(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    // File type queries
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId AND d.mimeType LIKE :mimeTypePattern")
    Page<Document> findByTenantIdAndMimeTypePattern(@Param("tenantId") Long tenantId, @Param("mimeTypePattern") String mimeTypePattern, Pageable pageable);
    
    // Size-based queries
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId AND d.fileSize BETWEEN :minSize AND :maxSize")
    Page<Document> findByTenantIdAndFileSizeBetween(@Param("tenantId") Long tenantId, @Param("minSize") Long minSize, @Param("maxSize") Long maxSize, Pageable pageable);
    
    // Date-based queries
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId AND d.createdAt BETWEEN :startDate AND :endDate")
    Page<Document> findByTenantIdAndCreatedAtBetween(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    // Recent documents
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId ORDER BY d.createdAt DESC")
    Page<Document> findRecentDocuments(@Param("tenantId") Long tenantId, Pageable pageable);
    
    // Most downloaded documents
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId ORDER BY d.downloadCount DESC")
    Page<Document> findMostDownloadedDocuments(@Param("tenantId") Long tenantId, Pageable pageable);
    
    // Update download count
    @Modifying
    @Query("UPDATE Document d SET d.downloadCount = d.downloadCount + 1 WHERE d.documentId = :documentId AND d.tenantId = :tenantId")
    void incrementDownloadCount(@Param("documentId") Long documentId, @Param("tenantId") Long tenantId);
    
    // Analytics queries
    @Query("SELECT COUNT(d) FROM Document d WHERE d.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.tenantId = :tenantId AND d.documentType = :documentType")
    long countByTenantIdAndDocumentType(@Param("tenantId") Long tenantId, @Param("documentType") DocumentType documentType);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.tenantId = :tenantId AND d.uploadedByUserId = :userId")
    long countByTenantIdAndUploadedByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
    
    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.tenantId = :tenantId")
    Long getTotalStorageUsed(@Param("tenantId") Long tenantId);
    
    @Query("SELECT d.documentType, COUNT(d) FROM Document d WHERE d.tenantId = :tenantId GROUP BY d.documentType")
    List<Object[]> getDocumentTypeStatistics(@Param("tenantId") Long tenantId);
    
    // Advanced search with multiple filters
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId " +
           "AND (:searchTerm IS NULL OR " +
           "LOWER(d.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.originalFileName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:documentType IS NULL OR d.documentType = :documentType) " +
           "AND (:uploadedByUserId IS NULL OR d.uploadedByUserId = :uploadedByUserId) " +
           "AND (:isPublic IS NULL OR d.isPublic = :isPublic) " +
           "AND (:startDate IS NULL OR d.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR d.createdAt <= :endDate)")
    Page<Document> findDocumentsWithFilters(
            @Param("tenantId") Long tenantId,
            @Param("searchTerm") String searchTerm,
            @Param("documentType") DocumentType documentType,
            @Param("uploadedByUserId") Long uploadedByUserId,
            @Param("isPublic") Boolean isPublic,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.tenantId = :tenantId AND d.isPublic = :isPublic")
    long countByTenantIdAndIsPublic(@Param("tenantId") Long tenantId, @Param("isPublic") Boolean isPublic);

    @Query("SELECT SUM(d.downloadCount) FROM Document d WHERE d.tenantId = :tenantId")
    Long sumDownloadCountByTenantId(@Param("tenantId") Long tenantId);

    // Advanced search with all filters including related entities
    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId " +
           "AND (:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:description IS NULL OR LOWER(d.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:documentType IS NULL OR d.documentType = :documentType) " +
           "AND (:fileType IS NULL OR LOWER(d.fileExtension) = LOWER(:fileType)) " +
           "AND (:isPublic IS NULL OR d.isPublic = :isPublic) " +
           "AND (:contactId IS NULL OR d.relatedContactId = :contactId) " +
           "AND (:accountId IS NULL OR d.relatedAccountId = :accountId) " +
           "AND (:dealId IS NULL OR d.relatedDealId = :dealId) " +
           "AND (:leadId IS NULL OR d.relatedLeadId = :leadId) " +
           "AND (:taskId IS NULL OR d.relatedTaskId = :taskId) " +
           "AND (:uploadedAfter IS NULL OR d.createdAt >= :uploadedAfter) " +
           "AND (:uploadedBefore IS NULL OR d.createdAt <= :uploadedBefore)")
    Page<Document> findDocumentsWithAdvancedFilters(
            @Param("tenantId") Long tenantId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("documentType") DocumentType documentType,
            @Param("fileType") String fileType,
            @Param("isPublic") Boolean isPublic,
            @Param("contactId") Long contactId,
            @Param("accountId") Long accountId,
            @Param("dealId") Long dealId,
            @Param("leadId") Long leadId,
            @Param("taskId") Long taskId,
            @Param("uploadedAfter") LocalDateTime uploadedAfter,
            @Param("uploadedBefore") LocalDateTime uploadedBefore,
            Pageable pageable
    );
}

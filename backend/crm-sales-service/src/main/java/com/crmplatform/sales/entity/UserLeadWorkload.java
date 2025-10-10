package com.crmplatform.sales.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_lead_workload")
public class UserLeadWorkload {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workload_id")
    private Long workloadId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "active_leads_count")
    private Integer activeLeadsCount = 0;
    
    @Column(name = "total_leads_assigned")
    private Integer totalLeadsAssigned = 0;
    
    @Column(name = "leads_converted_this_month")
    private Integer leadsConvertedThisMonth = 0;
    
    @Column(name = "average_response_time_hours", precision = 10, scale = 2)
    private BigDecimal averageResponseTimeHours;
    
    @Column(name = "max_lead_capacity")
    private Integer maxLeadCapacity = 50;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "availability_hours", columnDefinition = "JSON")
    private String availabilityHours;
    
    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate = BigDecimal.ZERO;
    
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public UserLeadWorkload() {}
    
    public UserLeadWorkload(Long tenantId, Long userId) {
        this.tenantId = tenantId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getWorkloadId() {
        return workloadId;
    }
    
    public void setWorkloadId(Long workloadId) {
        this.workloadId = workloadId;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getActiveLeadsCount() {
        return activeLeadsCount;
    }
    
    public void setActiveLeadsCount(Integer activeLeadsCount) {
        this.activeLeadsCount = activeLeadsCount;
    }
    
    public Integer getTotalLeadsAssigned() {
        return totalLeadsAssigned;
    }
    
    public void setTotalLeadsAssigned(Integer totalLeadsAssigned) {
        this.totalLeadsAssigned = totalLeadsAssigned;
    }
    
    public Integer getLeadsConvertedThisMonth() {
        return leadsConvertedThisMonth;
    }
    
    public void setLeadsConvertedThisMonth(Integer leadsConvertedThisMonth) {
        this.leadsConvertedThisMonth = leadsConvertedThisMonth;
    }
    
    public BigDecimal getAverageResponseTimeHours() {
        return averageResponseTimeHours;
    }
    
    public void setAverageResponseTimeHours(BigDecimal averageResponseTimeHours) {
        this.averageResponseTimeHours = averageResponseTimeHours;
    }
    
    public Integer getMaxLeadCapacity() {
        return maxLeadCapacity;
    }
    
    public void setMaxLeadCapacity(Integer maxLeadCapacity) {
        this.maxLeadCapacity = maxLeadCapacity;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public String getAvailabilityHours() {
        return availabilityHours;
    }
    
    public void setAvailabilityHours(String availabilityHours) {
        this.availabilityHours = availabilityHours;
    }
    
    public BigDecimal getConversionRate() {
        return conversionRate;
    }
    
    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }
    
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

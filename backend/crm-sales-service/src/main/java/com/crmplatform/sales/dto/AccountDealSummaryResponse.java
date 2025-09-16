package com.crmplatform.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDealSummaryResponse {
    private Long accountId;
    private String accountName;
    private Long totalDeals;
    private BigDecimal totalDealValue;
    private Long wonDeals;
    private BigDecimal wonDealValue;
    private BigDecimal winRate;
    private List<DealResponse> topOpenDeals;
    private BigDecimal forecastedRevenue;
    
    // Constructor without topOpenDeals for basic summary
    public AccountDealSummaryResponse(Long accountId, String accountName, Long totalDeals, 
                                    BigDecimal totalDealValue, Long wonDeals, BigDecimal wonDealValue) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.totalDeals = totalDeals;
        this.totalDealValue = totalDealValue;
        this.wonDeals = wonDeals;
        this.wonDealValue = wonDealValue;
        this.winRate = calculateWinRate(wonDeals, totalDeals);
        this.forecastedRevenue = calculateForecastedRevenue(totalDealValue, wonDealValue);
    }
    
    private BigDecimal calculateWinRate(Long wonDeals, Long totalDeals) {
        if (totalDeals == null || totalDeals == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(wonDeals).divide(BigDecimal.valueOf(totalDeals), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateForecastedRevenue(BigDecimal totalValue, BigDecimal wonValue) {
        if (totalValue == null) {
            return BigDecimal.ZERO;
        }
        if (wonValue == null) {
            wonValue = BigDecimal.ZERO;
        }
        // Simple forecasting: remaining pipeline value * average win rate (assuming 25% default)
        BigDecimal remainingValue = totalValue.subtract(wonValue);
        return remainingValue.multiply(BigDecimal.valueOf(0.25));
    }
}

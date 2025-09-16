package com.crmplatform.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private RevenueForecast revenueForecast;
    private SalesVelocity salesVelocity;
    private ConversionRates conversionRates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueForecast {
        private BigDecimal totalPipelineValue;
        private BigDecimal weightedForecast;
        private BigDecimal bestCaseScenario;
        private BigDecimal worstCaseScenario;
        private List<StageRevenue> revenueByStage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageRevenue {
        private Long stageId;
        private String stageName;
        private BigDecimal totalValue;
        private BigDecimal weightedValue;
        private Integer dealCount;
        private BigDecimal winProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesVelocity {
        private Double averageDealCycleTime; // in days
        private Double averageStageTime; // in days
        private List<StageVelocity> velocityByStage;
        private Integer totalDealsAnalyzed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageVelocity {
        private Long stageId;
        private String stageName;
        private Double averageTimeInStage; // in days
        private Integer dealsAnalyzed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionRates {
        private BigDecimal overallConversionRate;
        private List<StageConversion> conversionByStage;
        private BigDecimal winRate;
        private BigDecimal lossRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageConversion {
        private Long fromStageId;
        private String fromStageName;
        private Long toStageId;
        private String toStageName;
        private BigDecimal conversionRate;
        private Integer totalTransitions;
        private Integer successfulTransitions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesMetrics {
        private RevenueForecast revenueForecast;
        private SalesVelocity salesVelocity;
        private ConversionRates conversionRates;
        private Map<String, Object> additionalMetrics;
    }
}

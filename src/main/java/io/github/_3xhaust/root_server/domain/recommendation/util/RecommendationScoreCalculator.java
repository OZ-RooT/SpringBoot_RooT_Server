package io.github._3xhaust.root_server.domain.recommendation.util;

import java.time.Instant;
import java.util.List;

public class RecommendationScoreCalculator {

    private static final double WEIGHT_DISTANCE = 0.30;
    private static final double WEIGHT_TIME = 0.20;
    private static final double WEIGHT_POPULARITY = 0.20;
    private static final double WEIGHT_TAG_MATCH = 0.20;
    private static final double WEIGHT_RECENCY = 0.10;
    private static final double WEIGHT_PRODUCT_COUNT = 0.10;

    public static double calculateGarageSaleScore(
            Double distanceKm,
            boolean isCurrentlyActive,
            Double hoursUntilEnd,
            Long favoriteCount,
            Integer productCount,
            double tagMatchScore,
            long daysSinceCreated
    ) {
        double distanceScore = calculateDistanceScore(distanceKm);
        double timeScore = calculateTimeScore(isCurrentlyActive, hoursUntilEnd);
        double popularityScore = calculatePopularityScore(favoriteCount);
        double productCountScore = Math.min(productCount != null ? productCount / 10.0 : 0.0, 1.0);
        double recencyScore = calculateRecencyScore(daysSinceCreated);

        double baseScore = (distanceScore * WEIGHT_DISTANCE +
                timeScore * WEIGHT_TIME +
                popularityScore * WEIGHT_POPULARITY +
                tagMatchScore * WEIGHT_TAG_MATCH +
                recencyScore * WEIGHT_RECENCY);

        double finalScore = baseScore + (productCountScore * WEIGHT_PRODUCT_COUNT);

        return Math.min(Math.max(finalScore, 0.0), 1.0);
    }

    public static double calculateProductScore(
            Double distanceKm,
            Long favoriteCount,
            double tagMatchScore,
            long daysSinceCreated,
            Double price,
            Integer averagePrice
    ) {
        double distanceScore = distanceKm != null ? calculateDistanceScore(distanceKm) : 0.5;
        double popularityScore = calculatePopularityScore(favoriteCount);
        double recencyScore = calculateRecencyScore(daysSinceCreated);
        double priceScore = calculatePriceScore(price, averagePrice);

        double finalScore = (distanceScore * WEIGHT_DISTANCE +
                popularityScore * WEIGHT_POPULARITY +
                tagMatchScore * WEIGHT_TAG_MATCH +
                recencyScore * WEIGHT_RECENCY +
                priceScore * 0.15);

        return Math.min(Math.max(finalScore, 0.0), 1.0);
    }

    public static double calculateTagScore(
            Long usageCount,
            Long recentUsageCount,
            Integer relatedTagCount
    ) {
        double usageScore = Math.min(Math.log10(usageCount != null && usageCount > 0 ? usageCount + 1 : 1) / 3.0, 1.0);
        double recentUsageScore = Math.min(recentUsageCount != null ? recentUsageCount / 10.0 : 0.0, 1.0);
        double relatednessScore = Math.min(relatedTagCount != null ? relatedTagCount / 5.0 : 0.0, 1.0);

        double finalScore = (usageScore * 0.4 +
                recentUsageScore * 0.4 +
                relatednessScore * 0.2);

        return Math.min(Math.max(finalScore, 0.0), 1.0);
    }

    private static double calculateDistanceScore(Double distanceKm) {
        if (distanceKm == null) return 0.5;
        if (distanceKm <= 3) return 1.0;
        if (distanceKm <= 10) return 0.9;
        if (distanceKm <= 25) return 0.7;
        if (distanceKm <= 50) return 0.5;
        if (distanceKm <= 100) return 0.3;
        return 0.1;
    }

    private static double calculateTimeScore(boolean isCurrentlyActive, Double hoursUntilEnd) {
        if (!isCurrentlyActive) {
            return 0.3;
        }

        if (hoursUntilEnd == null) {
            return 0.5;
        }

        if (hoursUntilEnd <= 6) return 1.0;
        if (hoursUntilEnd <= 24) return 0.9;
        if (hoursUntilEnd <= 48) return 0.8;
        if (hoursUntilEnd <= 72) return 0.7;
        return 0.6;
    }

    private static double calculatePopularityScore(Long favoriteCount) {
        if (favoriteCount == null || favoriteCount == 0) {
            return 0.1;
        }
        return Math.min(Math.log10(favoriteCount + 1) / 1.3, 1.0);
    }

    private static double calculateRecencyScore(long daysSinceCreated) {
        if (daysSinceCreated <= 1) return 1.0;
        if (daysSinceCreated <= 3) return 0.9;
        if (daysSinceCreated <= 7) return 0.8;
        if (daysSinceCreated <= 14) return 0.7;
        if (daysSinceCreated <= 30) return 0.5;
        if (daysSinceCreated <= 60) return 0.3;
        return 0.1;
    }

    private static double calculatePriceScore(Double price, Integer averagePrice) {
        if (price == null || averagePrice == null || averagePrice == 0) {
            return 0.5;
        }

        double ratio = (double) price / averagePrice;
        if (ratio >= 0.75 && ratio <= 1.25) {
            return 1.0;
        } else if (ratio >= 0.5 && ratio <= 1.5) {
            return 0.8;
        } else if (ratio >= 0.3 && ratio <= 2.0) {
            return 0.6;
        } else {
            return 0.3;
        }
    }

    public static double calculateTagMatchScore(List<String> userTags, List<String> itemTags) {
        if (userTags == null || userTags.isEmpty() || itemTags == null || itemTags.isEmpty()) {
            return 0.0;
        }

        long matchCount = userTags.stream()
                .filter(itemTags::contains)
                .count();

        return Math.min((double) matchCount / userTags.size(), 1.0);
    }

    public static long calculateDaysSinceCreated(Instant createdAt) {
        if (createdAt == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(createdAt, Instant.now()).toDays();
    }

    public static Double calculateHoursUntilEnd(Instant endTime) {
        if (endTime == null) {
            return null;
        }
        Instant now = Instant.now();
        if (now.isAfter(endTime)) {
            return 0.0;
        }
        return (double) java.time.Duration.between(now, endTime).toHours();
    }
}

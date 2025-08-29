/**
 * Interface for collecting metrics from rate limiters.
 * Follows the Observer pattern for metrics collection.
 */
public interface MetricsCollector {
    /**
     * Records a rate limit decision
     * @param endpoint The API endpoint
     * @param allowed Whether the request was allowed
     * @param timestamp When the decision was made
     */
    void recordDecision(String endpoint, boolean allowed, long timestamp);
    
    /**
     * Records a rate limit hit
     * @param endpoint The API endpoint
     * @param timestamp When the rate limit was hit
     */
    void recordRateLimitHit(String endpoint, long timestamp);
    
    /**
     * Gets metrics for a specific endpoint
     * @param endpoint The API endpoint
     * @return Metrics data
     */
    MetricsData getMetrics(String endpoint);
}

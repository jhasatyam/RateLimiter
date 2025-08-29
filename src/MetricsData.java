import java.util.concurrent.atomic.AtomicLong;

/**
 * Immutable data class for rate limiting metrics.
 */
public class MetricsData {
    private final String endpoint;
    private final long totalRequests;
    private final long allowedRequests;
    private final long rateLimitedRequests;
    private final long lastRequestTime;
    
    public MetricsData(String endpoint, long totalRequests, long allowedRequests, 
                      long rateLimitedRequests, long lastRequestTime) {
        this.endpoint = endpoint;
        this.totalRequests = totalRequests;
        this.allowedRequests = allowedRequests;
        this.rateLimitedRequests = rateLimitedRequests;
        this.lastRequestTime = lastRequestTime;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public long getTotalRequests() {
        return totalRequests;
    }
    
    public long getAllowedRequests() {
        return allowedRequests;
    }
    
    public long getRateLimitedRequests() {
        return rateLimitedRequests;
    }
    
    public long getLastRequestTime() {
        return lastRequestTime;
    }
    
    public double getSuccessRate() {
        return totalRequests > 0 ? (double) allowedRequests / totalRequests : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("MetricsData{endpoint='%s', total=%d, allowed=%d, limited=%d, successRate=%.2f%%}", 
                           endpoint, totalRequests, allowedRequests, rateLimitedRequests, getSuccessRate() * 100);
    }
}

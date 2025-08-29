import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of MetricsCollector using thread-safe data structures.
 */
public class DefaultMetricsCollector implements MetricsCollector {
    private final ConcurrentHashMap<String, EndpointMetrics> metricsMap = new ConcurrentHashMap<>();
    
    @Override
    public void recordDecision(String endpoint, boolean allowed, long timestamp) {
        EndpointMetrics metrics = metricsMap.computeIfAbsent(endpoint, k -> new EndpointMetrics());
        metrics.recordDecision(allowed, timestamp);
    }
    
    @Override
    public void recordRateLimitHit(String endpoint, long timestamp) {
        EndpointMetrics metrics = metricsMap.computeIfAbsent(endpoint, k -> new EndpointMetrics());
        metrics.recordRateLimitHit(timestamp);
    }
    
    @Override
    public MetricsData getMetrics(String endpoint) {
        EndpointMetrics metrics = metricsMap.get(endpoint);
        if (metrics == null) {
            return new MetricsData(endpoint, 0, 0, 0, 0);
        }
        return metrics.getMetricsData(endpoint);
    }
    
    /**
     * Thread-safe metrics storage for a single endpoint
     */
    private static class EndpointMetrics {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong allowedRequests = new AtomicLong(0);
        private final AtomicLong rateLimitedRequests = new AtomicLong(0);
        private final AtomicLong lastRequestTime = new AtomicLong(0);
        
        public void recordDecision(boolean allowed, long timestamp) {
            totalRequests.incrementAndGet();
            if (allowed) {
                allowedRequests.incrementAndGet();
            } else {
                rateLimitedRequests.incrementAndGet();
            }
            lastRequestTime.set(timestamp);
        }
        
        public void recordRateLimitHit(long timestamp) {
            rateLimitedRequests.incrementAndGet();
            lastRequestTime.set(timestamp);
        }
        
        public MetricsData getMetricsData(String endpoint) {
            return new MetricsData(
                endpoint,
                totalRequests.get(),
                allowedRequests.get(),
                rateLimitedRequests.get(),
                lastRequestTime.get()
            );
        }
    }
}

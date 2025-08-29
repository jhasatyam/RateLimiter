import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service for managing rate limiters for different API endpoints.
 * Follows Single Responsibility Principle by focusing only on rate limiter management.
 * Thread-safe implementation using concurrent data structures.
 */
public class RateLimiterService {
    private final ConcurrentMap<String, RateLimit> rateLimiters;
    private final ConcurrentMap<String, RateLimiterConfig> configurations;
    private final MetricsCollector metricsCollector;
    private final ReadWriteLock configurationLock;
    
    /**
     * Creates a new RateLimiterService with the specified metrics collector.
     * 
     * @param metricsCollector The metrics collector to use
     */
    public RateLimiterService(MetricsCollector metricsCollector) {
        this.rateLimiters = new ConcurrentHashMap<>();
        this.configurations = new ConcurrentHashMap<>();
        this.metricsCollector = metricsCollector;
        this.configurationLock = new ReentrantReadWriteLock();
    }
    
    /**
     * Creates a new RateLimiterService with a default metrics collector.
     */
    public RateLimiterService() {
        this(new DefaultMetricsCollector());
    }
    
    /**
     * Registers a rate limiter configuration for an API endpoint.
     * 
     * @param endpoint The API endpoint
     * @param config The rate limiter configuration
     */
    public void registerConfiguration(String endpoint, RateLimiterConfig config) {
        configurationLock.writeLock().lock();
        try {
            configurations.put(endpoint, config);
            // Create and store the rate limiter
            RateLimit rateLimiter = RateLimiterFactory.create(config);
            rateLimiters.put(endpoint, rateLimiter);
        } finally {
            configurationLock.writeLock().unlock();
        }
    }
    
    /**
     * Applies rate limiting for a request to the specified endpoint.
     * 
     * @param endpoint The API endpoint
     * @param requestId Unique identifier for the request
     * @return true if the request is allowed, false if rate limited
     * @throws IllegalArgumentException if the endpoint is not configured
     */
    public boolean isAllowed(String endpoint, String requestId) {
        RateLimit rateLimiter = getRateLimiter(endpoint);
        if (rateLimiter == null) {
            throw new IllegalArgumentException("No rate limiter configured for endpoint: " + endpoint);
        }
        
        boolean allowed = rateLimiter.isAllowed(requestId);
        long timestamp = System.currentTimeMillis();
        
        // Record metrics
        metricsCollector.recordDecision(endpoint, allowed, timestamp);
        if (!allowed) {
            metricsCollector.recordRateLimitHit(endpoint, timestamp);
        }
        
        return allowed;
    }
    
    /**
     * Gets the current status of a rate limiter for an endpoint.
     * 
     * @param endpoint The API endpoint
     * @return The rate limiter status, or null if not configured
     */
    public RateLimitStatus getStatus(String endpoint) {
        RateLimit rateLimiter = getRateLimiter(endpoint);
        return rateLimiter != null ? rateLimiter.getStatus() : null;
    }
    
    /**
     * Gets metrics for a specific endpoint.
     * 
     * @param endpoint The API endpoint
     * @return The metrics data
     */
    public MetricsData getMetrics(String endpoint) {
        return metricsCollector.getMetrics(endpoint);
    }
    
    /**
     * Removes the configuration for an endpoint.
     * 
     * @param endpoint The API endpoint to remove
     */
    public void removeConfiguration(String endpoint) {
        configurationLock.writeLock().lock();
        try {
            configurations.remove(endpoint);
            rateLimiters.remove(endpoint);
        } finally {
            configurationLock.writeLock().unlock();
        }
    }
    
    /**
     * Checks if an endpoint has a rate limiter configured.
     * 
     * @param endpoint The API endpoint
     * @return true if configured, false otherwise
     */
    public boolean isConfigured(String endpoint) {
        configurationLock.readLock().lock();
        try {
            return configurations.containsKey(endpoint);
        } finally {
            configurationLock.readLock().unlock();
        }
    }
    
    /**
     * Gets the rate limiter for an endpoint.
     * 
     * @param endpoint The API endpoint
     * @return The rate limiter, or null if not configured
     */
    private RateLimit getRateLimiter(String endpoint) {
        configurationLock.readLock().lock();
        try {
            return rateLimiters.get(endpoint);
        } finally {
            configurationLock.readLock().unlock();
        }
    }
    
    // Legacy method for backward compatibility
    public boolean applyRateLimit(String apiEndpoint) throws Exception {
        try {
            return isAllowed(apiEndpoint, "legacy-" + System.currentTimeMillis());
        } catch (IllegalArgumentException e) {
            throw new Exception("Not allowed");
        }
    }
    
    // Legacy method for backward compatibility
    public void registerApiConfig(String apiEndpoint, RateLimiterConfig config) {
        registerConfiguration(apiEndpoint, config);
    }
}

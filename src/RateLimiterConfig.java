import java.time.Duration;

/**
 * Configuration for rate limiters.
 * Immutable configuration object following the Builder pattern.
 */
public class RateLimiterConfig {
    private final RateLimiterType rateLimiterType;
    private final Duration windowSize;
    private final int maxRequests;
    private final Duration refillInterval;
    private final int refillAmount;
    private final boolean enableMetrics;
    
    private RateLimiterConfig(Builder builder) {
        this.rateLimiterType = builder.rateLimiterType;
        this.windowSize = builder.windowSize;
        this.maxRequests = builder.maxRequests;
        this.refillInterval = builder.refillInterval;
        this.refillAmount = builder.refillAmount;
        this.enableMetrics = builder.enableMetrics;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public RateLimiterType getRateLimiterType() {
        return this.rateLimiterType;
    }
    
    public Duration getWindowSize() {
        return this.windowSize;
    }
    
    public int getMaxRequests() {
        return this.maxRequests;
    }
    
    public Duration getRefillInterval() {
        return this.refillInterval;
    }
    
    public int getRefillAmount() {
        return this.refillAmount;
    }
    
    public boolean isMetricsEnabled() {
        return this.enableMetrics;
    }
    
    // Legacy getter for backward compatibility
    public Duration getDuration() {
        return this.windowSize;
    }
    
    // Legacy getter for backward compatibility
    public int getLimit() {
        return this.maxRequests;
    }
    
    /**
     * Builder class for RateLimiterConfig
     */
    public static class Builder {
        private RateLimiterType rateLimiterType = RateLimiterType.TOKEN_BUCKET;
        private Duration windowSize = Duration.ofSeconds(1);
        private int maxRequests = 10;
        private Duration refillInterval = Duration.ofSeconds(1);
        private int refillAmount = 1;
        private boolean enableMetrics = false;
        
        public Builder type(RateLimiterType type) {
            this.rateLimiterType = type;
            return this;
        }
        
        public Builder windowSize(Duration windowSize) {
            this.windowSize = windowSize;
            return this;
        }
        
        public Builder maxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
            return this;
        }
        
        public Builder refillInterval(Duration refillInterval) {
            this.refillInterval = refillInterval;
            return this;
        }
        
        public Builder refillAmount(int refillAmount) {
            this.refillAmount = refillAmount;
            return this;
        }
        
        public Builder enableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
            return this;
        }
        
        public RateLimiterConfig build() {
            return new RateLimiterConfig(this);
        }
    }
}

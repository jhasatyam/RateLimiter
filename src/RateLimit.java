/**
 * Interface for rate limiting algorithms.
 * Follows the Strategy pattern for different rate limiting implementations.
 */
public interface RateLimit {
    /**
     * Attempts to process an API request based on the current rate limiting state.
     * 
     * @param requestId Unique identifier for the request (for logging/tracking)
     * @return true if the request is allowed, false if rate limited
     */
    boolean isAllowed(String requestId);
    
    /**
     * Gets the current status of the rate limiter
     * @return RateLimitStatus containing current state information
     */
    RateLimitStatus getStatus();
}

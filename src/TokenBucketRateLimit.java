import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token Bucket rate limiting algorithm implementation.
 * Thread-safe implementation using atomic operations for better performance.
 */
public class TokenBucketRateLimit implements RateLimit {
    private final AtomicInteger currentTokens;
    private final AtomicLong lastRefillTimestamp;
    private final int maxTokens;
    private final long refillIntervalMillis;
    private final int refillAmount;
    
    public TokenBucketRateLimit(RateLimiterConfig config) {
        this.maxTokens = config.getMaxRequests();
        this.refillIntervalMillis = config.getRefillInterval().toMillis();
        this.refillAmount = config.getRefillAmount();
        this.currentTokens = new AtomicInteger(maxTokens);
        this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());
    }
    
    @Override
    public boolean isAllowed(String requestId) {
        refillTokens();
        
        // Try to consume a token
        while (true) {
            int current = currentTokens.get();
            if (current <= 0) {
                return false;
            }
            
            if (currentTokens.compareAndSet(current, current - 1)) {
                return true;
            }
            // Retry if CAS failed
        }
    }
    
    @Override
    public RateLimitStatus getStatus() {
        refillTokens();
        long nextReset = lastRefillTimestamp.get() + refillIntervalMillis;
        return new RateLimitStatus(
            currentTokens.get() > 0,
            currentTokens.get(),
            nextReset,
            "TokenBucket"
        );
    }
    
    /**
     * Refills tokens based on elapsed time since last refill.
     * Uses atomic operations to avoid double-locking issues.
     */
    private void refillTokens() {
        long currentTime = System.currentTimeMillis();
        long lastRefill = lastRefillTimestamp.get();
        long elapsed = currentTime - lastRefill;
        
        if (elapsed >= refillIntervalMillis) {
            // Try to update the timestamp atomically
            if (lastRefillTimestamp.compareAndSet(lastRefill, currentTime)) {
                int tokensToAdd = (int) (elapsed / refillIntervalMillis) * refillAmount;
                int current = currentTokens.get();
                int newTokens = Math.min(maxTokens, current + tokensToAdd);
                currentTokens.set(newTokens);
            }
        }
    }
}

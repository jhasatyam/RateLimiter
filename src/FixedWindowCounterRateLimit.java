import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fixed Window Counter rate limiting algorithm implementation.
 * Thread-safe implementation using atomic operations.
 */
public class FixedWindowCounterRateLimit implements RateLimit {
    private final AtomicInteger currentCount;
    private final AtomicLong windowStartTime;
    private final int maxRequests;
    private final long windowSizeMillis;
    
    public FixedWindowCounterRateLimit(RateLimiterConfig config) {
        this.maxRequests = config.getMaxRequests();
        this.windowSizeMillis = config.getWindowSize().toMillis();
        this.currentCount = new AtomicInteger(0);
        this.windowStartTime = new AtomicLong(System.currentTimeMillis());
    }
    
    @Override
    public boolean isAllowed(String requestId) {
        resetWindowIfNeeded();
        
        // Try to increment the counter
        while (true) {
            int current = currentCount.get();
            if (current >= maxRequests) {
                return false;
            }
            
            if (currentCount.compareAndSet(current, current + 1)) {
                return true;
            }
            // Retry if CAS failed
        }
    }
    
    @Override
    public RateLimitStatus getStatus() {
        resetWindowIfNeeded();
        long nextReset = windowStartTime.get() + windowSizeMillis;
        return new RateLimitStatus(
            currentCount.get() < maxRequests,
            maxRequests - currentCount.get(),
            nextReset,
            "FixedWindowCounter"
        );
    }
    
    /**
     * Resets the window if the current window has expired.
     * Uses atomic operations to avoid race conditions.
     */
    private void resetWindowIfNeeded() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        
        if (currentTime - windowStart >= windowSizeMillis) {
            // Try to reset the window atomically
            if (windowStartTime.compareAndSet(windowStart, currentTime)) {
                currentCount.set(0);
            }
        }
    }
}

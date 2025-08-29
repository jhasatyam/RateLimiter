import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sliding Window Log rate limiting algorithm implementation.
 * Thread-safe implementation using concurrent data structures.
 */
public class SlidingWindowLogRateLimit implements RateLimit {
    private final ConcurrentLinkedQueue<Long> requestTimestamps;
    private final int maxRequests;
    private final long windowSizeMillis;
    private final ReentrantReadWriteLock cleanupLock;
    
    public SlidingWindowLogRateLimit(RateLimiterConfig config) {
        this.maxRequests = config.getMaxRequests();
        this.windowSizeMillis = config.getWindowSize().toMillis();
        this.requestTimestamps = new ConcurrentLinkedQueue<>();
        this.cleanupLock = new ReentrantReadWriteLock();
    }
    
    @Override
    public boolean isAllowed(String requestId) {
        long currentTime = System.currentTimeMillis();
        
        // Clean up expired timestamps
        cleanupExpiredTimestamps(currentTime);
        
        // Check if we can add a new request
        if (requestTimestamps.size() < maxRequests) {
            requestTimestamps.offer(currentTime);
            return true;
        }
        
        return false;
    }
    
    @Override
    public RateLimitStatus getStatus() {
        long currentTime = System.currentTimeMillis();
        cleanupExpiredTimestamps(currentTime);
        
        long nextReset = currentTime + windowSizeMillis;
        return new RateLimitStatus(
            requestTimestamps.size() < maxRequests,
            maxRequests - requestTimestamps.size(),
            nextReset,
            "SlidingWindowLog"
        );
    }
    
    /**
     * Removes expired timestamps from the queue.
     * Uses read-write lock to prevent cleanup during status checks.
     */
    private void cleanupExpiredTimestamps(long currentTime) {
        cleanupLock.writeLock().lock();
        try {
            Long timestamp;
            while ((timestamp = requestTimestamps.peek()) != null) {
                if (currentTime - timestamp > windowSizeMillis) {
                    requestTimestamps.poll(); // Remove expired timestamp
                } else {
                    break; // Found a valid timestamp, stop cleanup
                }
            }
        } finally {
            cleanupLock.writeLock().unlock();
        }
    }
}

/**
 * Represents the current status of a rate limiter.
 * Provides immutable information about the rate limiter's state.
 */
public class RateLimitStatus {
    private final boolean isAllowed;
    private final int remainingRequests;
    private final long nextResetTime;
    private final String algorithmType;
    
    public RateLimitStatus(boolean isAllowed, int remainingRequests, long nextResetTime, String algorithmType) {
        this.isAllowed = isAllowed;
        this.remainingRequests = remainingRequests;
        this.nextResetTime = nextResetTime;
        this.algorithmType = algorithmType;
    }
    
    public boolean isAllowed() {
        return isAllowed;
    }
    
    public int getRemainingRequests() {
        return remainingRequests;
    }
    
    public long getNextResetTime() {
        return nextResetTime;
    }
    
    public String getAlgorithmType() {
        return algorithmType;
    }
    
    @Override
    public String toString() {
        return String.format("RateLimitStatus{algorithm=%s, allowed=%s, remaining=%d, nextReset=%d}", 
                           algorithmType, isAllowed, remainingRequests, nextResetTime);
    }
}

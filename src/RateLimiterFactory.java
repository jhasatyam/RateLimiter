import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Factory for creating rate limiters using the Strategy pattern.
 * Follows the Open/Closed principle by allowing new algorithms without modification.
 */
public class RateLimiterFactory {
    private static final Map<RateLimiterType, Function<RateLimiterConfig, RateLimit>> STRATEGIES = new EnumMap<>(RateLimiterType.class);
    
    static {
        STRATEGIES.put(RateLimiterType.TOKEN_BUCKET, TokenBucketRateLimit::new);
        STRATEGIES.put(RateLimiterType.FIXED_WINDOW_COUNTER, FixedWindowCounterRateLimit::new);
        STRATEGIES.put(RateLimiterType.SLIDING_WINDOW_LOG, SlidingWindowLogRateLimit::new);
    }
    
    /**
     * Creates a rate limiter based on the configuration.
     * 
     * @param config The rate limiter configuration
     * @return A new rate limiter instance
     * @throws IllegalArgumentException if the rate limiter type is not supported
     */
    public static RateLimit create(RateLimiterConfig config) {
        Function<RateLimiterConfig, RateLimit> strategy = STRATEGIES.get(config.getRateLimiterType());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported rate limiter type: " + config.getRateLimiterType());
        }
        return strategy.apply(config);
    }
    
    /**
     * Registers a new rate limiter strategy.
     * 
     * @param type The rate limiter type
     * @param factory The factory function to create the rate limiter
     */
    public static void registerStrategy(RateLimiterType type, Function<RateLimiterConfig, RateLimit> factory) {
        STRATEGIES.put(type, factory);
    }
    
    // Legacy method for backward compatibility
    public static RateLimit get(RateLimiterConfig config) {
        return create(config);
    }
}

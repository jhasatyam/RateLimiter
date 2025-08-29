import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Manages rate limiter configurations with validation and change notifications.
 * Follows the Observer pattern for configuration changes.
 */
public class ConfigurationManager {
    private final ConcurrentMap<String, RateLimiterConfig> configurations;
    private final ConcurrentMap<String, Consumer<RateLimiterConfig>> changeListeners;
    
    public ConfigurationManager() {
        this.configurations = new ConcurrentHashMap<>();
        this.changeListeners = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers a configuration for an endpoint.
     * 
     * @param endpoint The API endpoint
     * @param config The rate limiter configuration
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public void registerConfiguration(String endpoint, RateLimiterConfig config) {
        validateConfiguration(config);
        configurations.put(endpoint, config);
        
        // Notify listeners of configuration change
        Consumer<RateLimiterConfig> listener = changeListeners.get(endpoint);
        if (listener != null) {
            listener.accept(config);
        }
    }
    
    /**
     * Gets a configuration for an endpoint.
     * 
     * @param endpoint The API endpoint
     * @return The configuration, or null if not found
     */
    public RateLimiterConfig getConfiguration(String endpoint) {
        return configurations.get(endpoint);
    }
    
    /**
     * Removes a configuration for an endpoint.
     * 
     * @param endpoint The API endpoint
     */
    public void removeConfiguration(String endpoint) {
        configurations.remove(endpoint);
        changeListeners.remove(endpoint);
    }
    
    /**
     * Registers a listener for configuration changes.
     * 
     * @param endpoint The API endpoint
     * @param listener The listener to call when configuration changes
     */
    public void addChangeListener(String endpoint, Consumer<RateLimiterConfig> listener) {
        changeListeners.put(endpoint, listener);
    }
    
    /**
     * Validates a rate limiter configuration.
     * 
     * @param config The configuration to validate
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private void validateConfiguration(RateLimiterConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        if (config.getMaxRequests() <= 0) {
            throw new IllegalArgumentException("Max requests must be positive");
        }
        
        if (config.getWindowSize().toMillis() <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        
        if (config.getRefillInterval().toMillis() <= 0) {
            throw new IllegalArgumentException("Refill interval must be positive");
        }
        
        if (config.getRefillAmount() <= 0) {
            throw new IllegalArgumentException("Refill amount must be positive");
        }
    }
    
    /**
     * Checks if an endpoint has a configuration.
     * 
     * @param endpoint The API endpoint
     * @return true if configured, false otherwise
     */
    public boolean hasConfiguration(String endpoint) {
        return configurations.containsKey(endpoint);
    }
    
    /**
     * Gets all configured endpoints.
     * 
     * @return Set of configured endpoints
     */
    public java.util.Set<String> getConfiguredEndpoints() {
        return java.util.Collections.unmodifiableSet(configurations.keySet());
    }
}

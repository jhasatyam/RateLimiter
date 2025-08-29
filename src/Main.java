import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main class demonstrating the refactored rate limiter system.
 * Shows concurrent usage, metrics collection, and proper error handling.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Refactored Rate Limiter System Demo ===\n");
        
        // Create rate limiter service with metrics
        RateLimiterService service = new RateLimiterService();
        
        // Configure different rate limiting strategies for different endpoints
        configureEndpoints(service);
        
        // Demonstrate concurrent usage
        demonstrateConcurrentUsage(service);
        
        // Show metrics
        showMetrics(service);
        
        // Demonstrate different algorithms
        demonstrateAlgorithms(service);
    }
    
    private static void configureEndpoints(RateLimiterService service) {
        System.out.println("Configuring endpoints...");
        
        // Token Bucket for user API
        RateLimiterConfig tokenBucketConfig = RateLimiterConfig.builder()
            .type(RateLimiterType.TOKEN_BUCKET)
            .maxRequests(5)
            .windowSize(Duration.ofSeconds(1))
            .refillInterval(Duration.ofSeconds(1))
            .refillAmount(1)
            .enableMetrics(true)
            .build();
        service.registerConfiguration("/user", tokenBucketConfig);
        
        // Fixed Window for admin API
        RateLimiterConfig fixedWindowConfig = RateLimiterConfig.builder()
            .type(RateLimiterType.FIXED_WINDOW_COUNTER)
            .maxRequests(3)
            .windowSize(Duration.ofSeconds(2))
            .enableMetrics(true)
            .build();
        service.registerConfiguration("/admin", fixedWindowConfig);
        
        // Sliding Window for public API
        RateLimiterConfig slidingWindowConfig = RateLimiterConfig.builder()
            .type(RateLimiterType.SLIDING_WINDOW_LOG)
            .maxRequests(10)
            .windowSize(Duration.ofSeconds(1))
            .enableMetrics(true)
            .build();
        service.registerConfiguration("/public", slidingWindowConfig);
        
        System.out.println("✓ Endpoints configured successfully\n");
    }
    
    private static void demonstrateConcurrentUsage(RateLimiterService service) {
        System.out.println("Demonstrating concurrent usage...");
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        String endpoint = "/user";
        
        for (int i = 0; i < 20; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    String requestIdStr = UUID.randomUUID().toString().substring(0, 8);
                    boolean allowed = service.isAllowed(endpoint, requestIdStr);
                    System.out.printf("Request %d (%s): %s%n", 
                        requestId, requestIdStr, allowed ? "✓ Allowed" : "✗ Rate Limited");
                    
                    if (!allowed) {
                        Thread.sleep(100); // Small delay for rate limited requests
                    }
                } catch (Exception e) {
                    System.err.printf("Request %d failed: %s%n", requestId, e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✓ Concurrent usage demonstration completed\n");
    }
    
    private static void showMetrics(RateLimiterService service) {
        System.out.println("Current metrics:");
        
        String[] endpoints = {"/user", "/admin", "/public"};
        for (String endpoint : endpoints) {
            try {
                MetricsData metrics = service.getMetrics(endpoint);
                System.out.printf("  %s: %s%n", endpoint, metrics);
                
                RateLimitStatus status = service.getStatus(endpoint);
                if (status != null) {
                    System.out.printf("    Status: %s%n", status);
                }
            } catch (Exception e) {
                System.err.printf("  Error getting metrics for %s: %s%n", endpoint, e.getMessage());
            }
        }
        System.out.println();
    }
    
    private static void demonstrateAlgorithms(RateLimiterService service) {
        System.out.println("Demonstrating different algorithms...");
        
        String[] endpoints = {"/user", "/admin", "/public"};
        String[] algorithmNames = {"Token Bucket", "Fixed Window Counter", "Sliding Window Log"};
        
        for (int i = 0; i < endpoints.length; i++) {
            String endpoint = endpoints[i];
            System.out.printf("\n--- %s Algorithm (%s) ---%n", algorithmNames[i], endpoint);
            
            // Make several requests to see the algorithm in action
            for (int j = 0; j < 8; j++) {
                try {
                    String requestId = "demo-" + j;
                    boolean allowed = service.isAllowed(endpoint, requestId);
                    System.out.printf("  Request %d: %s%n", j, allowed ? "✓ Allowed" : "✗ Rate Limited");
                    
                    Thread.sleep(200); // Small delay between requests
                } catch (Exception e) {
                    System.err.printf("  Request %d failed: %s%n", j, e.getMessage());
                }
            }
        }
        
        System.out.println("\n✓ Algorithm demonstration completed");
    }
}
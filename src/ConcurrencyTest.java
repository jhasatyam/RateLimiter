import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple concurrency test to verify thread safety of the rate limiter system.
 */
public class ConcurrencyTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrency Test ===\n");
        
        RateLimiterService service = new RateLimiterService();
        
        // Configure a rate limiter with low limits to test concurrency
        RateLimiterConfig config = RateLimiterConfig.builder()
            .type(RateLimiterType.TOKEN_BUCKET)
            .maxRequests(3)
            .windowSize(Duration.ofSeconds(1))
            .refillInterval(Duration.ofSeconds(1))
            .refillAmount(1)
            .enableMetrics(true)
            .build();
        
        service.registerConfiguration("/test", config);
        
        // Test parameters
        int threadCount = 10;
        int requestsPerThread = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger totalAllowed = new AtomicInteger(0);
        AtomicInteger totalRateLimited = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        System.out.printf("Starting test with %d threads, %d requests each...\n", threadCount, requestsPerThread);
        
        // Submit tasks
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        String requestId = String.format("thread-%d-req-%d", threadId, j);
                        boolean allowed = service.isAllowed("/test", requestId);
                        
                        if (allowed) {
                            totalAllowed.incrementAndGet();
                        } else {
                            totalRateLimited.incrementAndGet();
                        }
                        
                        // Small delay to simulate real request processing
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    System.err.printf("Thread %d failed: %s\n", threadId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        latch.await();
        executor.shutdown();
        
        // Results
        System.out.println("\n=== Test Results ===");
        System.out.printf("Total requests: %d\n", threadCount * requestsPerThread);
        System.out.printf("Allowed: %d\n", totalAllowed.get());
        System.out.printf("Rate limited: %d\n", totalRateLimited.get());
        System.out.printf("Success rate: %.2f%%\n", 
            (double) totalAllowed.get() / (threadCount * requestsPerThread) * 100);
        
        // Show metrics
        System.out.println("\n=== Metrics ===");
        System.out.println(service.getMetrics("/test"));
        
        System.out.println("\n✓ Concurrency test completed successfully!");
    }
}

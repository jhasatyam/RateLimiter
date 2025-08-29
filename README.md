# Rate Limiter System - Refactored

A robust, scalable rate limiting system built with Java that follows SOLID principles and handles concurrency properly.

## 🚀 Features

- **Multiple Rate Limiting Algorithms**: Token Bucket, Fixed Window Counter, and Sliding Window Log
- **Thread-Safe**: Built with concurrent data structures and atomic operations
- **Metrics Collection**: Built-in metrics for monitoring and observability
- **Configuration Management**: Flexible configuration with validation
- **SOLID Principles**: Clean architecture following software engineering best practices
- **Extensible**: Easy to add new rate limiting algorithms
- **High Performance**: Optimized for high-concurrency scenarios

## 🏗️ Architecture

### Core Components

1. **RateLimit Interface**: Strategy pattern for different algorithms
2. **RateLimiterService**: Main service managing rate limiters
3. **ConfigurationManager**: Handles configuration updates and validation
4. **MetricsCollector**: Collects and provides metrics data
5. **RateLimiterFactory**: Creates rate limiters using the Strategy pattern

### Design Patterns Used

- **Strategy Pattern**: For different rate limiting algorithms
- **Factory Pattern**: For creating rate limiters
- **Observer Pattern**: For configuration change notifications
- **Builder Pattern**: For configuration objects

## 📊 Rate Limiting Algorithms

### 1. Token Bucket
- **Use Case**: Smooth rate limiting with burst allowance
- **Pros**: Allows bursts, smooths traffic
- **Cons**: May allow more requests than expected in burst scenarios

### 2. Fixed Window Counter
- **Use Case**: Simple, predictable rate limiting
- **Pros**: Simple to understand, predictable behavior
- **Cons**: Can allow double the limit at window boundaries

### 3. Sliding Window Log
- **Use Case**: Precise rate limiting with sliding time windows
- **Pros**: Most accurate, no boundary issues
- **Cons**: Higher memory usage, more complex

## 🔧 Usage

### Basic Setup

```java
// Create service
RateLimiterService service = new RateLimiterService();

// Configure endpoint with Token Bucket
RateLimiterConfig config = RateLimiterConfig.builder()
    .type(RateLimiterType.TOKEN_BUCKET)
    .maxRequests(10)
    .windowSize(Duration.ofSeconds(1))
    .refillInterval(Duration.ofSeconds(1))
    .refillAmount(1)
    .enableMetrics(true)
    .build();

service.registerConfiguration("/api/users", config);
```

### Rate Limiting Requests

```java
// Check if request is allowed
boolean allowed = service.isAllowed("/api/users", "request-123");

if (allowed) {
    // Process request
} else {
    // Handle rate limiting
}
```

### Getting Metrics

```java
// Get metrics for an endpoint
MetricsData metrics = service.getMetrics("/api/users");
System.out.println("Success rate: " + metrics.getSuccessRate());

// Get current status
RateLimitStatus status = service.getStatus("/api/users");
System.out.println("Remaining requests: " + status.getRemainingRequests());
```

## 🧪 Testing

Run the main class to see a demonstration:

```bash
cd src
javac *.java
java Main
```

The demo shows:
- Concurrent usage with multiple threads
- Different rate limiting algorithms
- Metrics collection
- Error handling

## 🔒 Concurrency Features

- **Atomic Operations**: Uses `AtomicInteger` and `AtomicLong` for thread-safe counters
- **Concurrent Collections**: `ConcurrentHashMap` for configuration storage
- **Read-Write Locks**: Efficient locking for configuration updates
- **Lock-Free Algorithms**: Where possible, uses lock-free approaches

## 📈 Performance Characteristics

- **Token Bucket**: O(1) time complexity, minimal contention
- **Fixed Window**: O(1) time complexity, atomic operations
- **Sliding Window**: O(n) cleanup time, but amortized O(1) for requests

## 🚀 Scalability Features

- **No Global State**: Each service instance is independent
- **Configurable Metrics**: Enable/disable metrics per endpoint
- **Memory Efficient**: Automatic cleanup of expired data
- **Horizontal Scaling**: Can run multiple instances

## 🔧 Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `type` | Rate limiting algorithm | `TOKEN_BUCKET` |
| `maxRequests` | Maximum requests allowed | `10` |
| `windowSize` | Time window size | `1 second` |
| `refillInterval` | Token refill interval | `1 second` |
| `refillAmount` | Tokens refilled per interval | `1` |
| `enableMetrics` | Enable metrics collection | `false` |

## 🛠️ Extending the System

### Adding New Algorithms

1. Implement the `RateLimit` interface
2. Register in `RateLimiterFactory`
3. Add to `RateLimiterType` enum

```java
public class CustomRateLimit implements RateLimit {
    // Implementation
}

// Register in factory
RateLimiterFactory.registerStrategy(RateLimiterType.CUSTOM, CustomRateLimit::new);
```

### Custom Metrics

Implement `MetricsCollector` interface for custom metrics storage:

```java
public class CustomMetricsCollector implements MetricsCollector {
    // Implementation
}

RateLimiterService service = new RateLimiterService(new CustomMetricsCollector());
```

## 📚 SOLID Principles Implementation

### Single Responsibility Principle
- Each class has a single, well-defined responsibility
- `RateLimiterService` manages rate limiters
- `ConfigurationManager` handles configurations
- `MetricsCollector` collects metrics

### Open/Closed Principle
- System is open for extension (new algorithms) but closed for modification
- Factory uses strategy pattern for algorithm selection

### Liskov Substitution Principle
- All rate limiting implementations can be used interchangeably
- Interface contracts are properly maintained

### Interface Segregation Principle
- `RateLimit` interface is focused and minimal
- `MetricsCollector` provides only necessary methods

### Dependency Inversion Principle
- High-level modules depend on abstractions
- Dependencies are injected rather than hardcoded

## 🚨 Error Handling

- **Configuration Validation**: Automatic validation of configuration parameters
- **Graceful Degradation**: System continues to work even with invalid configurations
- **Exception Hierarchy**: Proper exception types for different error scenarios

## 🔍 Monitoring and Observability

- **Request Metrics**: Total, allowed, and rate-limited request counts
- **Success Rates**: Percentage of successful requests
- **Status Information**: Current state of rate limiters
- **Timing Data**: Last request timestamps and next reset times

## 📝 License

This project is open source and available under the MIT License.

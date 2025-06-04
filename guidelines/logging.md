# Logging Guidelines

## Purpose

Provide administrators and operators with a reliable way to monitor system health and diagnose issues without direct access to source code.

**Goals:**
- Establish uniform rules for what we log
- Define clear standards for logging levels
- Ensure consistent application across all applications

## What Should We Log?

### 1. Error Detection and Analysis
- Technical errors (database failures, network issues)
- Business logic failures (rejected orders, failed validations)
- Custom interface errors not caught by frameworks

### 2. Business Process Traceability
- Important business milestones ("Order created successfully")
- Process completion states ("Document archived")
- Workflow progress indicators

### 3. Operations-Relevant Events
- System start/stop
- Batch process completion with statistics
- Performance bottlenecks or system overload

### 4. Security-Relevant Events
- User role/permission changes
- Suspicious access attempts
- Security exceptions and access denials

### 5. Technical Process Details
- Fine-grained debugging information
- API request/response details
- Internal processing steps

## Logging Levels

We use **only** four levels: `DEBUG`, `INFO`, `WARN`, `ERROR` (no TRACE).

### Decision Process

```
Is the main audience developers? → DEBUG
Is there an undesired state? → No: INFO, Yes: Continue
Can the process continue despite the undesired state? → Yes: WARN, No: ERROR
```

### DEBUG
**Target:** Developers and support staff
**Purpose:** Detailed technical information for troubleshooting

**Examples:**
- Method entry/exit with parameters
- Internal calculation steps
- Kafka message processing details
- API request/response details

**Anti-patterns:**
- High-level process steps ("Order processing started")
- Successful user interactions
- Framework-handled operations

### INFO
**Target:** Operations and administrators
**Purpose:** Important milestones in successful processes

**Examples:**
- "Order created successfully"
- "Document processed and stored"
- "User logged in successfully"
- "Batch process completed: 345 records processed"

**Anti-patterns:**
- Every HTTP request
- Detailed method calls
- Validation errors in normal flow

### WARN
**Target:** Operations team
**Purpose:** Undesired states where process can continue

**Examples:**
- External service responding slowly but still working
- Repeated failed login attempts
- System approaching capacity limits
- Fallback mechanisms activated

**Anti-patterns:**
- User input validation failures
- Normal business rule rejections
- Successfully handled exceptions

### ERROR
**Target:** Operations team (potential alerts)
**Purpose:** Critical failures requiring immediate attention

**2AM Rule:** Would this wake someone at 2 AM? If yes, it's probably ERROR level.

**Examples:**
- Database connection failures
- External service timeouts
- Failed payment processing
- Kafka message processing failures

**Anti-patterns:**
- Invalid user input
- Business logic rejections
- Optional feature timeouts

## Structured Logging

### MDC (Mapped Diagnostic Context)
Use MDC to add context information to all log entries:

```java
MDC.put("orderId", request.getOrderId());
MDC.put("customerId", request.getCustomerId());
log.info("Starting order processing");
```

### Structured Key-Value Logging
For message-specific structured data, use SLF4J's fluent API instead of string interpolation:

```java
// GOOD: Structured logging with addKeyValue
log.atDebug()
    .addKeyValue("requestId", requestId)
    .addKeyValue("processingTime", duration)
    .log("Request processing completed");

log.atInfo()
    .addKeyValue("action", action.name())
    .addKeyValue("userId", userId)
    .log("User action executed");

// AVOID: String interpolation
log.info("User {} executed action {} in {}ms", userId, action.name(), duration);
```

**Benefits:**
- Better filtering capabilities
- Consistency across log entries
- Context propagation across distributed systems
- Machine-readable structured data

### JSON Output Format
JSON formatting is handled automatically by the logging infrastructure - no configuration needed by application developers.

## Exception Logging

### Core Rules
1. **Log where handled:** Always log exceptions where they are caught and handled
2. **Use ERROR level:** All exceptions should be logged at ERROR level
3. **Include full stacktrace:** Essential for root cause analysis
4. **Avoid double logging:** Don't log the same exception multiple times

### Retry Scenarios
- Temporary failures during retries: Use WARN
- Final failure after all retries: Use ERROR

### Exception Wrapping
When wrapping exceptions (technical → business), log only at the final handling point to avoid duplication.

## Sensitive Data Handling

### GDPR Compliance
- **Never log personal data** (names, addresses, emails)
- **Minimize risk:** Logs often have different access controls than business databases
- **Retention issues:** Logs may be kept longer than legally allowed

### Business Identifiers
- **Allowed:** Order numbers, transaction IDs, request IDs
- **Use MDC only:** Enable masking/filtering in target applications
- **Don't embed in messages:** Keep identifiers as separate MDC values

```java
// GOOD: Using MDC for global context
MDC.put("orderNumber", "ORD-123456");
log.info("Order validation failed");

// GOOD: Using addKeyValue for message-specific data
log.atWarn()
    .addKeyValue("validationError", error.getCode())
    .addKeyValue("fieldName", error.getField())
    .log("Validation failed");

// BAD: Embedding in message
log.info("Order validation failed for ORD-123456");
```

## Example Implementation

```java
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderResult createOrder(OrderRequest request) {
        // Set MDC context for global request context
        try (MDCClosable ignored = MDC.putCloseable("orderId", request.getOrderId())) {
            log.info("Starting order processing");

            try {
                log.atDebug()
                        .addKeyValue("requestSize", request.getItems().size())
                        .log("Validating order request");

                ValidationResult validation = validate(request);

                if (!validation.isValid()) {
                    log.atWarn()
                            .addKeyValue("validationErrors", validation.getErrorCount())
                            .log("Validation failed");
                    return OrderResult.failed(validation);
                }

                log.info("Order validation successful");

                // Continue processing...
                log.atInfo()
                        .addKeyValue("totalAmount", order.getTotalAmount())
                        .addKeyValue("itemCount", order.getItems().size())
                        .log("Order processing completed");
                return OrderResult.success();

            } catch (DatabaseException e) {
                log.atError()
                        .addKeyValue("errorCode", e.getErrorCode())
                        .log("Database error during order processing: {}", e.getMessage(), e);
                return OrderResult.failed("Database error");
            }
        }
    }
}
```

## Key Takeaways

1. **Signal-to-Noise Ratio:** Balance detail with readability
2. **Consistency:** Use levels uniformly across all applications
3. **Context:** Always use MDC for business identifiers
4. **Privacy:** Never log personal data, minimize sensitive information
5. **Structure:** Enable JSON output for better analysis
6. **Exceptions:** Log where handled, include full stacktraces
7. **Target Audience:** Consider who will read each log level
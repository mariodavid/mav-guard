# Project Guidelines

This document outlines conventions and recommendations for development, testing, and project structure in this repository.

## Technologies & Setup

- **Java version**: 21
- **Spring Boot**: current stable version (see `pom.xml`)
- **Build tool**: Maven (multi-module structure)
- **Testing framework**: JUnit 5
- **Logging**: SLF4J + Logback

## General Development Guidelines

- Use **Java Records** wherever possible.
- Avoid **classical `for` loops**. Prefer functional constructs (`stream()`, `forEach`, `map`, etc.).
- Use **`List.of()`** and other modern collection factory methods from Java 21.
- Favor **immutable classes**, especially for DTOs and value objects.
- Avoid placing `@Service`, `@Component`, or `@Configuration` annotations in core modules – keep Spring dependencies isolated to Spring-specific modules.

## Test Guidelines

### Testing Framework

- Use **JUnit 5** (`org.junit.jupiter`)
- Use AssertJ for assertions (`org.assertj.core`)
- Always structure tests using **Given – When – Then** comments to improve readability.
- Example:

```
@Test
void shouldCalculateFinalPrice_withValidInput() {
// Given
Product product = new Product("Book", 10.0);
DiscountService discountService = new DiscountService();

    // When
    double finalPrice = discountService.applyDiscount(product, 0.1);

    // Then
    assertEquals(9.0, finalPrice);
}
```

### Test Types & Priorities

1. **Prefer unit tests** using real objects – avoid mocking if not strictly necessary.
2. If multiple classes interact, prefer a **Spring Integration Test** using `@SpringBootTest` or similar.
3. Use `@MockBean` **sparingly**, mainly for external dependencies like HTTP clients.
4. Use `@SpringBootTest(classes = ...)` to limit context startup scope when needed.

### Additional Testing Rules

- Place tests in `src/test/java` within the corresponding module.
- Name test classes after the class under test with a `Test` suffix: `OrderServiceTest`.
- **Test coverage matters**, but don't chase 100%. High-quality, meaningful tests are more important than quantity.
- Do **not** test trivial getters/setters or boilerplate code.
- Always cover **edge cases** and **failure scenarios**.

---
For questions or contributions, please stick to these guidelines to ensure consistency across the codebase.
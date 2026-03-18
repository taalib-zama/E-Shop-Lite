# Config Reference (Additions)

## Service Registry (Eureka) — Client Config (applies to services & gateway)
```yaml
spring:
  application:
    name: USER-SERVICE  # logical ID visible in Eureka

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

## Feign & Resilience4j (applies to calling services from Sprint 2)
```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 1000
            readTimeout: 2000
            loggerLevel: basic

resilience4j:
  retry:
    instances:
      inventoryClient:
        maxAttempts: 3
        waitDuration: 200ms
  circuitbreaker:
    instances:
      inventoryClient:
        slidingWindowSize: 20
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

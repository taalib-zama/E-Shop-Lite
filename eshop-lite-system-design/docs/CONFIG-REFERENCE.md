# Config Reference

**Date:** 2026-03-03

---

## 1. Shared (via Config Server)

```yaml
logging:
  level:
    root: INFO
  json: true  # custom convention
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

## 2. API Gateway

```yaml
server:
  port: 8080
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter # dev-only
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/users/**, /auth/**
        - id: catalog-service
          uri: http://localhost:8082
          predicates:
            - Path=/products/**
security:
  jwt:
    secret: ${JWT_SECRET} # env var
    expMinutes: 15
```

## 3. User Service

```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/users_db
    username: eshop
    password: eshop
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
security:
  jwt:
    secret: ${JWT_SECRET}
    expMinutes: 15
```

## 4. Catalog Service

```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/catalog_db
    username: eshop
    password: eshop
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
security:
  jwt:
    secret: ${JWT_SECRET}
    expMinutes: 15
```

## 5. Service Registry (Eureka)

```yaml
server:
  port: 8761
spring:
  application:
    name: service-registry
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

## 6. Config Server

```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: file://${CONFIG_REPO_PATH} # local folder path
```

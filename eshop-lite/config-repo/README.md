# E-Shop Lite: Configuration Repository

This repository serves as the **Centralized Configuration Store** for all microservices in the eShopLite ecosystem. It is consumed by the `platform/config-server` and served to individual services at startup.

## 🏗 Structure

Each configuration file corresponds to a service's `spring.application.name`:

- `api-gateway.yml`: Routing, security, and global filter configurations.
- `user-service.yml`: Database connections, JWT parameters, and identity-specific settings.
- `catalog-service.yml`: Product management and inventory settings.
- `service-registry.yml`: Eureka server and peer awareness settings.

## ⚙️ Environment Specifics (Profiles)

We use **Spring Profiles** to manage environment-specific properties within the same file using the `---` separator:

- **Default (Dev)**: Used for local development (e.g., `ddl-auto: validate`, `DEBUG` logging).
- **`prod` Profile**: Triggered by setting `SPRING_PROFILES_ACTIVE=prod`. Optimized for performance and security (e.g., `WARN` logging, strict Flyway baselining).

## 🔐 DevOps & Security Best Practices

1. **Parameterization**: Never hardcode sensitive values. Use the syntax `${ENV_VAR_NAME:DEFAULT_VALUE}`.
   - Example: `url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/users_db}`
   - This allows DevOps to override settings via **Docker Compose environment variables** or **Kubernetes Secrets** without changing the code.

2. **Observability**: All service configs include standard Micrometer/Otel settings:
   - `management.endpoints.web.exposure.include: health,info,metrics,prometheus`
   - Logging patterns include `traceId` and `spanId` for distributed tracing.

3. **External Management**: Because these files are external to the service JARs, you can update configuration (like log levels or feature flags) and refresh the services without a full redeploy (using `@RefreshScope` and `/actuator/refresh`).

## 🚀 How to use locally

1. Ensure `config-server` (Port 8888) is running and pointing to this directory.
2. Start your service (e.g., `user-service`).
3. The service will fetch its config from `http://localhost:8888/user-service/default`.

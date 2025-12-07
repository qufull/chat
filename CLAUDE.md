# CLAUDE.md - Chat Application Microservices

## Overview

This is a **Spring Boot microservices-based chat application** using Spring Cloud, Keycloak for authentication, Kafka for event streaming, and Redis for session management. The architecture follows cloud-native patterns with service discovery, API gateway, and event-driven communication.

## Project Structure

```
chat/
├── api-gateway/              # API Gateway service (Spring Cloud Gateway)
├── discovery-service/        # Eureka service registry
├── user-service/            # User profile management service
├── providers/               # Keycloak custom providers (Kafka event publisher)
├── docker-compose.yml       # Infrastructure orchestration
├── realm-export.json        # Keycloak realm configuration
└── pom.xml                  # Parent Maven POM
```

## Technology Stack

### Core Technologies
- **Java**: 17
- **Spring Boot**: 3.4.4 - 3.5.6
- **Spring Cloud**: 2024.0.1 - 2025.0.0
- **Maven**: Multi-module project

### Key Frameworks & Libraries
- **Spring Cloud Gateway**: API routing with WebFlux (reactive)
- **Spring Cloud Netflix Eureka**: Service discovery and registration
- **Spring Security OAuth2**: Resource server and OAuth2 client
- **Spring Data JPA**: Database access with Hibernate
- **Spring Kafka**: Event streaming
- **Spring Session Data Redis**: Distributed session management
- **Liquibase**: Database schema versioning
- **Lombok**: Boilerplate code reduction

### Infrastructure
- **Keycloak**: Identity and access management (IAM)
- **PostgreSQL**: Relational database (Keycloak + UserService)
- **Redis**: Session store
- **Apache Kafka**: Event streaming (KRaft mode, v4.1.0)
- **Docker Compose**: Container orchestration

## Microservices Architecture

### 1. Discovery Service (Eureka Server)
**Port**: 8761
**Location**: `./discovery-service/`

**Purpose**: Service registry for microservices discovery

**Configuration**:
- Does NOT register itself with Eureka (`register-with-eureka: false`)
- Does NOT fetch registry (`fetch-registry: false`)
- Provides service discovery for all other microservices

**Key Files**:
- `src/main/java/com/example/discoveryservice/DiscoveryServiceApplication.java`
- `src/main/resources/application.yml`

### 2. API Gateway
**Port**: 8080
**Location**: `./api-gateway/`

**Purpose**: Entry point for all client requests, handles routing, authentication, and session management

**Key Features**:
- Reactive WebFlux-based gateway
- OAuth2 login with Keycloak
- JWT token validation
- Session management with Redis
- Automatic token refresh
- OIDC logout with Keycloak integration
- Routes requests to downstream services via service discovery

**Routes**:
- `/profile/**` → user-service

**Configuration**:
- Keycloak client ID: `chatclient`
- Redis namespace: `spring:session`
- Eureka client enabled
- CSRF disabled (API mode)

**Key Files**:
- `src/main/java/com/example/apigateway/config/SecurityConfig.java` - Security configuration
- `src/main/java/com/example/apigateway/filter/TokenRelayFilter.java` - Token relay to downstream services
- `src/main/java/com/example/apigateway/handler/JsonErrorHandler.java` - Error handling

**Security Patterns**:
- OAuth2 Resource Server (JWT validation)
- OAuth2 Login (authorization code flow)
- Session management via Redis
- Clear site data on logout (cookies, cache, storage)
- Post-logout redirect to base URL

### 3. User Service
**Port**: Dynamic (assigned by Eureka)
**Location**: `./user-service/`

**Purpose**: Manages user profiles and handles user-related operations

**Key Features**:
- User profile CRUD operations
- Kafka event consumer for user registration
- Automatic profile creation when users register in Keycloak
- Liquibase database migrations
- JPA/Hibernate for data persistence

**Database Schema**:
- Table: `user_profiles`
- Columns: id (Keycloak user ID), nickname, avatar_url, status, about, last_seen

**Kafka Integration**:
- **Topic**: `keycloak-events`
- **Consumer Group**: `user-service`
- **Event Type**: `REGISTER`
- **Behavior**: Creates user profile automatically when registration event received

**Key Files**:
- `src/main/java/com/example/user_service/controller/ProfileController.java` - REST endpoints
- `src/main/java/com/example/user_service/service/UserProfileService.java` - Business logic
- `src/main/java/com/example/user_service/listener/RegistrationListener.java` - Kafka event listener
- `src/main/java/com/example/user_service/model/UserProfile.java` - JPA entity
- `src/main/java/com/example/user_service/repository/UserRepository.java` - JPA repository
- `src/main/java/com/example/user_service/config/KafkaConfig.java` - Kafka consumer configuration
- `src/main/resources/db/changelog/db.changelog-master.yml` - Liquibase master changelog

**Database Configuration**:
- Database: `chatdb`
- User: `postgres`
- Password: `1234`
- Port: 5432 (local PostgreSQL)

## Infrastructure Components

### Keycloak
**Port**: 9090 (exposed) → 8080 (internal)
**Admin Credentials**: admin/admin

**Configuration**:
- Realm: `chat` (imported from `realm-export.json`)
- Database: PostgreSQL (Keycloak dedicated database)
- Custom Provider: Kafka event publisher (`keycloak-kafka-1.3.0-jar-with-dependencies.jar`)

**Kafka Integration**:
- Bootstrap servers: `kafka:9094`
- Topic: `keycloak-events`
- Client ID: `keycloak-producer`

**Events Published**:
- User registration events → consumed by user-service

### PostgreSQL
**Port**: 5435 (exposed) → 5432 (internal)

**Databases**:
- `keycloak` - Keycloak metadata
- `chatdb` - User service data (configured separately, not in docker-compose)

**Credentials**: root/root

### Kafka
**Ports**:
- 9092 (external access)
- 9094 (internal service communication)
- 9093 (controller)

**Mode**: KRaft (no Zookeeper required)
**Image**: apache/kafka:4.1.0

**Configuration**:
- Process roles: broker + controller
- Replication factor: 1 (single node)
- Topic: `keycloak-events`

### Redis
**Port**: 6379 (local, not containerized)

**Purpose**: Session storage for API Gateway

## Code Patterns and Conventions

### Package Structure
Each service follows standard Spring Boot structure:
```
src/
├── main/
│   ├── java/com/example/{service}/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access
│   │   ├── model/           # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   │   ├── request/     # Request DTOs
│   │   │   └── exception/   # Exception DTOs
│   │   ├── exception/       # Custom exceptions
│   │   ├── aspect/          # AOP aspects
│   │   ├── listener/        # Event listeners (Kafka)
│   │   └── filter/          # Filters (Gateway)
│   └── resources/
│       ├── application.yml  # Configuration
│       └── db/changelog/    # Liquibase migrations
└── test/
    └── java/                # Unit tests
```

### Naming Conventions
- **Controllers**: `*Controller.java` (e.g., `ProfileController`)
- **Services**: `*Service.java` (e.g., `UserProfileService`)
- **Repositories**: `*Repository.java` (e.g., `UserRepository`)
- **DTOs**: `*Request.java`, `*Response.java`
- **Exceptions**: `*Exception.java` with `GlobalControllerAdvice` for handling
- **Config**: `*Config.java` (e.g., `SecurityConfig`, `KafkaConfig`)
- **Listeners**: `*Listener.java` (e.g., `RegistrationListener`)

### Common Annotations
- `@RestController` - REST endpoints
- `@RequiredArgsConstructor` - Lombok constructor injection
- `@Service` - Service layer
- `@Configuration` - Configuration classes
- `@Component` - Generic Spring components
- `@KafkaListener` - Kafka event consumers
- `@EnableEurekaClient` - Eureka service registration (implicit in newer versions)
- `@EnableWebFluxSecurity` - WebFlux security (API Gateway)
- `@EnableRedisWebSession` - Redis session management

### Lombok Usage
Extensively used across all services:
- `@RequiredArgsConstructor` - Constructor injection
- `@Getter`, `@Setter` - Accessors
- `@Builder` - Builder pattern
- `@NoArgsConstructor`, `@AllArgsConstructor` - Constructors
- `@Value` - Immutable DTOs (rare usage)

### Exception Handling
- Global exception handler: `GlobalControllerAdvice`
- Custom exceptions extend `BaseException`
- Error responses use `ErrorResponse` DTO

### Logging
- AOP-based logging with `LoggingAspect`
- Console output for development

## Development Workflow

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Redis (local installation or Docker)
- PostgreSQL (for user-service database)

### Initial Setup

1. **Start Infrastructure**:
```bash
docker-compose up -d
```
This starts: Keycloak, PostgreSQL (Keycloak DB), Kafka

2. **Verify Keycloak**:
- Access: http://localhost:9090
- Login with admin/admin
- Verify realm 'chat' is imported

3. **Start Redis** (if not using Docker):
```bash
redis-server
```

4. **Create User Service Database** (if not exists):
```bash
psql -U postgres
CREATE DATABASE chatdb;
```

5. **Build All Services**:
```bash
mvn clean install
```

6. **Start Services in Order**:
```bash
# 1. Discovery Service (must be first)
cd discovery-service
mvn spring-boot:run

# 2. API Gateway (wait for Eureka to be ready)
cd api-gateway
mvn spring-boot:run

# 3. User Service
cd user-service
mvn spring-boot:run
```

### Verifying Setup

1. **Eureka Dashboard**: http://localhost:8761
   - Should show api-gateway and user-service registered

2. **API Gateway**: http://localhost:8080
   - Should redirect to Keycloak login

3. **Keycloak Admin**: http://localhost:9090
   - Verify realm configuration

4. **Kafka Topics**:
```bash
docker exec -it Kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Making Changes

#### Adding New Endpoints
1. Create controller in appropriate service
2. Add service layer if needed
3. Use DTOs for request/response
4. Configure routing in API Gateway if needed
5. Secure endpoints appropriately (OAuth2)

#### Database Changes
1. Create Liquibase changeset in `src/main/resources/db/changelog/changeset/`
2. Include in `db.changelog-master.yml`
3. Liquibase runs automatically on application startup

#### Adding New Services
1. Create new Maven module in parent POM
2. Add Spring Boot dependencies
3. Configure Eureka client
4. Add routes in API Gateway if external access needed
5. Configure security (OAuth2 resource server if protected)

### Testing Strategy

#### Unit Tests
- Located in `src/test/java/`
- Use `@SpringBootTest` for integration tests
- Mock external dependencies with Mockito

#### Manual Testing
- Use REST clients (IntelliJ HTTP Client, Postman, curl)
- Test authentication flow through API Gateway
- Verify Kafka events in consumer logs

### Common Operations

#### Rebuilding a Service
```bash
cd <service-directory>
mvn clean install
mvn spring-boot:run
```

#### Resetting Keycloak
```bash
docker-compose down
docker volume rm chat_postgres_data
docker-compose up -d
```

#### Checking Kafka Events
```bash
docker exec -it Kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic keycloak-events \
  --from-beginning
```

#### Checking Redis Sessions
```bash
redis-cli
KEYS spring:session:*
```

## Security Considerations

### Authentication Flow
1. Client accesses API Gateway
2. Gateway redirects to Keycloak for login
3. User authenticates with Keycloak
4. Keycloak issues access_token and refresh_token
5. Gateway stores tokens in Redis session
6. Gateway validates JWT for each request
7. Gateway relays token to downstream services

### Token Management
- Access tokens are JWT (validated by gateway)
- Refresh tokens stored in Redis session
- Automatic token refresh via OAuth2 client
- Tokens cleared on logout

### Service-to-Service Communication
- Services trust tokens validated by gateway
- Internal services are OAuth2 resource servers
- JWT validation against Keycloak issuer

## Environment Configuration

### Local Development
- Keycloak: http://localhost:9090
- API Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Kafka: localhost:9092
- Redis: localhost:6379
- PostgreSQL: localhost:5435 (Keycloak), localhost:5432 (UserService)

### Configuration Files
- `application.yml` in each service's resources
- No profiles currently configured (defaults to local)

### Secrets Management
- Currently hardcoded in application.yml (NOT production-ready)
- **TODO**: Use Spring Cloud Config or external secrets management

## Current Limitations & TODOs

1. **Redis not containerized** - Must be installed locally
2. **User Service DB not in docker-compose** - Manual setup required
3. **No API documentation** - Consider adding Swagger/OpenAPI
4. **Hardcoded secrets** - Move to environment variables or secrets management
5. **No profiles** - Add dev/staging/prod configurations
6. **Limited error handling** - Enhance error responses
7. **No observability** - Add distributed tracing (Zipkin/Sleuth) and metrics (Actuator/Prometheus)
8. **User Service controller incomplete** - Only has placeholder endpoint
9. **No integration tests** - Add testcontainers-based tests
10. **CSRF disabled globally** - Review security requirements

## Key Design Decisions

### Why Kafka?
- Decouples Keycloak from user-service
- Enables event-driven architecture
- Allows multiple consumers for user events
- Provides event audit trail

### Why Redis for Sessions?
- Enables horizontal scaling of API Gateway
- Centralized session management
- Session persistence across gateway restarts

### Why WebFlux in Gateway?
- Non-blocking reactive architecture
- Better performance for I/O-bound operations (routing)
- Required by Spring Cloud Gateway

### Why Eureka?
- Simple service discovery
- Client-side load balancing
- Spring Cloud native integration
- Self-healing service registry

## AI Assistant Guidelines

When working with this codebase:

1. **Always start services in order**: Discovery → Gateway → Services
2. **Check Eureka dashboard** before debugging service communication
3. **Use Lombok annotations** for new classes (consistent with existing code)
4. **Follow package structure conventions** listed above
5. **Add Liquibase changesets** for any database changes
6. **Configure new endpoints in API Gateway routes** if external access needed
7. **Use OAuth2 resource server** for new protected services
8. **Test Kafka events** using console consumer
9. **Verify Redis sessions** for authentication issues
10. **Check docker-compose logs** for infrastructure issues
11. **Use `@RequiredArgsConstructor`** for dependency injection (constructor injection via Lombok)
12. **Create DTOs for API contracts** - don't expose entities directly
13. **Use Spring's conventional REST mappings** - `@GetMapping`, `@PostMapping`, etc.
14. **Follow reactive patterns in API Gateway** - return `Mono`/`Flux` types
15. **Validate inputs with Bean Validation** - `@Valid`, `@NotNull`, etc.

## Troubleshooting

### Service Not Registering with Eureka
- Verify Discovery Service is running on 8761
- Check `eureka.client.service-url.defaultZone` in application.yml
- Wait 30-60 seconds for registration to appear

### Authentication Failing
- Check Keycloak is running and accessible
- Verify realm 'chat' exists and client 'chatclient' is configured
- Check Redis is running (sessions won't persist without it)
- Verify issuer-uri matches Keycloak realm URL

### Kafka Events Not Consumed
- Verify Kafka is running: `docker ps | grep Kafka`
- Check topic exists: `kafka-topics.sh --list`
- Verify Keycloak provider is loaded (check logs)
- Ensure user-service consumer group is active

### Database Migration Failing
- Check PostgreSQL is accessible
- Verify credentials in application.yml
- Check Liquibase changelog syntax
- Review application startup logs for errors

## Git Workflow

### Current Branch
- Feature branch: `claude/claude-md-mivl84qqd3l6pisi-01NdYc2jy9TztpEwdWq4KRQa`
- All development happens on feature branches prefixed with `claude/`

### Recent Commits
- Keycloak authentication integration
- Kafka event streaming setup
- Redis session management
- User registration event handling

### Commit Message Conventions
- Descriptive messages focusing on "why" not "what"
- Reference service name when applicable
- Example: "Api - Gateway аутентификация через keycloak хранение токенов в Redis"

## Additional Resources

- [Spring Cloud Gateway Docs](https://spring.io/projects/spring-cloud-gateway)
- [Spring Security OAuth2 Docs](https://spring.io/projects/spring-security-oauth)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Cloud Netflix Docs](https://spring.io/projects/spring-cloud-netflix)

---

**Last Updated**: December 7, 2025
**Maintained For**: AI Assistant Context and Developer Onboarding

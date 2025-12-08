# CLAUDE.md - AI Assistant Guide for Chat Application

This document provides comprehensive guidance for AI assistants working with this codebase.

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Technology Stack](#architecture--technology-stack)
3. [Project Structure](#project-structure)
4. [Services & Modules](#services--modules)
5. [Development Setup](#development-setup)
6. [Configuration Management](#configuration-management)
7. [Data Flows & Patterns](#data-flows--patterns)
8. [Coding Conventions](#coding-conventions)
9. [Testing Guidelines](#testing-guidelines)
10. [Common Development Tasks](#common-development-tasks)
11. [Important Notes for AI Assistants](#important-notes-for-ai-assistants)

---

## Project Overview

**Project Name:** Chat Application
**Group ID:** org.example
**Artifact ID:** Chat
**Version:** 1.0-SNAPSHOT
**Build Tool:** Maven (multi-module project)
**Java Version:** 17

This is a microservices-based chat application backend implementing modern Spring Cloud patterns. The application provides user authentication, registration, and profile management capabilities with OAuth2/OIDC security via Keycloak.

### Key Features
- OAuth2/OIDC authentication with Keycloak
- Microservices architecture with service discovery
- Event-driven communication via Apache Kafka
- API Gateway with token relay
- Redis-based session management
- PostgreSQL database with Liquibase migrations
- Device signature verification for token refresh

---

## Architecture & Technology Stack

### Architecture Pattern
**Microservices Architecture** with the following components:
- **API Gateway** - Single entry point, handles routing and token relay
- **Service Discovery** - Eureka-based registration and discovery
- **Authentication Service** - User auth, registration, token management
- **User Service** - User profile management
- **Keycloak** - Identity and access management (OIDC provider)
- **Kafka** - Event streaming for async communication
- **PostgreSQL** - Persistent data storage
- **Redis** - Session and token storage

### Technology Stack

#### Core Framework
- **Spring Boot:** 3.4.4 - 3.5.6
- **Spring Cloud:** 2024.0.1 - 2025.0.0
- **Java:** 17

#### Key Dependencies
- **Spring Cloud Gateway** (WebFlux) - API routing and filtering
- **Spring Cloud Netflix Eureka** - Service discovery
- **Spring Security OAuth2** - Authentication and authorization
- **Spring Data JPA** - Database access
- **Spring Kafka** - Event streaming
- **Spring Session Data Redis** - Session management
- **Liquibase** - Database migrations
- **Keycloak Admin Client** (v26.0.7) - User management
- **Lombok** - Code generation
- **Jackson** - JSON processing

#### Infrastructure
- **Database:** PostgreSQL
- **Message Broker:** Apache Kafka 4.1.0 (KRaft mode)
- **Cache/Session Store:** Redis
- **Identity Provider:** Keycloak (latest)
- **Containerization:** Docker Compose

---

## Project Structure

```
/home/user/chat/
├── discovery-service/          # Eureka service registry
│   ├── src/main/java/
│   │   └── com/qufull/chat/discovery/
│   │       └── DiscoveryServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── api-gateway/                # API Gateway service
│   ├── src/main/java/
│   │   └── com/qufull/chat/gateway/
│   │       ├── ApiGatewayApplication.java
│   │       ├── config/
│   │       │   └── SecurityConfig.java
│   │       ├── filter/
│   │       │   └── TokenRelayFilter.java
│   │       └── handler/
│   │           └── JsonErrorHandler.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── authentication-service/     # Auth & registration service
│   ├── src/main/java/
│   │   └── com/qufull/chat/authentication/
│   │       ├── AuthenticationServiceApplication.java
│   │       ├── controller/
│   │       │   └── AuthController.java
│   │       ├── service/
│   │       │   ├── RegisterService.java
│   │       │   ├── LoginService.java
│   │       │   ├── RefreshService.java
│   │       │   ├── LogoutService.java
│   │       │   ├── TokenService.java
│   │       │   └── SessionService.java
│   │       ├── kafka/
│   │       │   └── UserEventProducer.java
│   │       ├── dto/
│   │       ├── exception/
│   │       ├── config/
│   │       │   ├── KeycloakConfig.java
│   │       │   └── KafkaConfig.java
│   │       └── handler/
│   │           ├── DeviceSignatureHandler.java
│   │           └── DeviceSignatureVerifierHandler.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── user-service/               # User profile service
│   ├── src/main/java/
│   │   └── com/qufull/chat/user/
│   │       ├── UserServiceApplication.java
│   │       ├── controller/
│   │       │   └── ProfileController.java
│   │       ├── service/
│   │       │   └── UserProfileService.java
│   │       ├── repository/
│   │       │   └── UserRepository.java
│   │       ├── model/
│   │       │   └── UserProfile.java
│   │       ├── listener/
│   │       │   └── RegistrationListener.java
│   │       ├── aspect/
│   │       │   └── LoggingAspect.java
│   │       └── config/
│   │           └── KafkaConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/changelog/
│   │       ├── db.changelog-master.yml
│   │       └── changeset/
│   │           └── create-profiles-table.sql
│   └── pom.xml
│
├── providers/                  # Keycloak custom providers
│
├── docker-compose.yml          # Infrastructure orchestration
├── realm-export.json           # Keycloak realm configuration
├── pom.xml                     # Parent POM
└── .gitignore
```

---

## Services & Modules

### 1. Discovery Service
**Port:** 8761
**Purpose:** Service registry and discovery using Eureka Server
**Key Technology:** Spring Cloud Netflix Eureka

**Responsibilities:**
- Service registration endpoint
- Service discovery for inter-service communication
- Health monitoring of registered services

**Key Files:**
- `DiscoveryServiceApplication.java` - Main application with `@EnableEurekaServer`
- `application.yml` - Eureka server configuration

**Configuration Notes:**
- Runs standalone (does not register with itself)
- All other services register with this service at `http://localhost:8761/eureka/`

### 2. API Gateway
**Port:** 8080
**Purpose:** Central entry point for all client requests
**Key Technology:** Spring Cloud Gateway (WebFlux - reactive)

**Responsibilities:**
- Route external requests to internal services
- OAuth2 client authentication with Keycloak
- JWT validation for incoming requests
- Token relay to downstream services
- Session management via Redis
- Global error handling

**Key Components:**
- `SecurityConfig.java` - OAuth2 client and resource server configuration
- `TokenRelayFilter.java` - Propagates access tokens to backend services
- `JsonErrorHandler.java` - Centralized JSON error responses

**Routes:**
- `/profile/**` → user-service (via load balancer)
- Dynamic service discovery enabled

**Security:**
- OAuth2 Client ID: `chatclient`
- OAuth2 Realm: `chat`
- JWT Issuer: `http://localhost:9090/realms/chat`
- Session stored in Redis with namespace `spring:session`

### 3. Authentication Service
**Port:** 8080
**Purpose:** User authentication, registration, and token management
**Key Technology:** Spring Boot Web, Keycloak Admin API, Kafka, Redis

**Responsibilities:**
- User registration via Keycloak Admin API
- User login with credential validation
- Token refresh with device signature verification
- User logout and session cleanup
- Publish user creation events to Kafka

**Key Components:**
- `AuthController.java` - REST endpoints (`/register`, `/login`, `/refresh`, `/logout`, `/logout/all`)
- `RegisterService.java` - Creates users in Keycloak, publishes events
- `LoginService.java` - Authenticates users against Keycloak
- `RefreshService.java` - Token refresh with device verification
- `LogoutService.java` - Session cleanup
- `TokenService.java` - Token validation and management
- `UserEventProducer.java` - Kafka event publisher
- `SessionService.java` - Redis session management
- `DeviceSignatureHandler.java` / `DeviceSignatureVerifierHandler.java` - Device verification

**Kafka Integration:**
- Topic: `user.created`
- Event published on successful registration

**Keycloak Integration:**
- Admin Realm: `master` (for admin operations)
- User Realm: `chat`
- Admin Client: `admin-cli` with credentials admin/admin
- User Client: `chatclient`

**Security:**
- Cookie name: `sid`
- Cookie settings: HttpOnly, Secure, SameSite=Lax
- Cookie max age: 1,209,600 seconds (14 days)
- Access token TTL: 300 seconds (5 minutes)
- Refresh token TTL: 1,209,600 seconds (14 days)

### 4. User Service
**Port:** Dynamic (assigned by Eureka)
**Purpose:** User profile management
**Key Technology:** Spring Boot Web, Spring Data JPA, Liquibase, Kafka Consumer

**Responsibilities:**
- Store and manage user profiles
- Listen to Kafka events for user registration
- Auto-create profiles on user creation events
- Provide profile endpoints
- OAuth2 resource server (JWT validation)

**Key Components:**
- `ProfileController.java` - Profile endpoints (`/profile/me`)
- `UserProfileService.java` - Profile business logic
- `RegistrationListener.java` - Kafka consumer for `user.created` events
- `UserProfile.java` - JPA entity
- `UserRepository.java` - Spring Data JPA repository
- `LoggingAspect.java` - AOP logging

**Database Schema:**
Table: `user_profiles`
```sql
id           VARCHAR(128) PRIMARY KEY UNIQUE
nickname     VARCHAR(30)
avatar_url   VARCHAR(512)
status       VARCHAR(20) DEFAULT 'ONLINE' CHECK (status IN ('ONLINE', 'OFFLINE'))
last_seen    TIMESTAMP WITH TIME ZONE
about        TEXT
```

**Liquibase:**
- Master changelog: `/db/changelog/db.changelog-master.yml`
- Migration: `/db/changelog/changeset/create-profiles-table.sql`

**Kafka Integration:**
- Topic: `user.created`
- Consumer group: (defined in config)

**Database:**
- Database: `chatdb`
- JDBC URL: `jdbc:postgresql://localhost:5432/chatdb`

---

## Development Setup

### Prerequisites
- Java 17 JDK
- Maven 3.x
- Docker & Docker Compose
- IDE with Lombok support (IntelliJ IDEA, Eclipse, VS Code)

### Initial Setup

1. **Start Infrastructure Services:**
```bash
cd /home/user/chat
docker-compose up -d
```

This starts:
- PostgreSQL (port 5435)
- Kafka (port 9092)
- Keycloak (port 9090)

2. **Start Redis (if not in docker-compose):**
```bash
redis-server
```

3. **Build All Services:**
```bash
mvn clean install
```

4. **Start Services in Order:**

a. Discovery Service (must start first):
```bash
cd discovery-service
mvn spring-boot:run
```

b. API Gateway:
```bash
cd api-gateway
mvn spring-boot:run
```

c. User Service:
```bash
cd user-service
mvn spring-boot:run
```

d. Authentication Service:
```bash
cd authentication-service
mvn spring-boot:run
```

### Service URLs
- **Eureka Dashboard:** http://localhost:8761
- **API Gateway:** http://localhost:8080
- **Keycloak Admin Console:** http://localhost:9090 (admin/admin)
- **Kafka:** localhost:9092
- **PostgreSQL:** localhost:5435 (root/root)
- **Redis:** localhost:6379

### Docker Services

**PostgreSQL Container:**
- Container: `Postgres`
- Port: 5435 → 5432
- Database: `keycloak`
- Credentials: root/root
- Volume: `postgres_data`

**Kafka Container:**
- Container: `Kafka`
- Ports: 9092 (external), 9094 (internal)
- Mode: KRaft (no Zookeeper)
- Topics: `keycloak-events`, `user.created`

**Keycloak Container:**
- Container: `Keycloak`
- Port: 9090 → 8080
- Admin: admin/admin
- Realm import: `realm-export.json`
- Provider directory: `./providers`
- Command: `start-dev --import-realm`

### Network
All Docker services run on `keycloak-network` bridge network.

---

## Configuration Management

### Environment-Specific Configuration

All services use `application.yml` files in `src/main/resources/`. Currently, all configurations are for **development environment** with hardcoded credentials.

### Key Configuration Properties

#### API Gateway (`api-gateway/src/main/resources/application.yml`)
```yaml
spring:
  application:
    name: api-gateway
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/chat
      client:
        provider:
          keycloak:
            issuer-uri: http://localhost:9090/realms/chat
            user-name-attribute: preferred_username
        registration:
          keycloak:
            client-id: chatclient
            client-secret: vx2C1sgWJnd17ywYMUTVUx2kvsztB2TJ
            scope: openid, profile
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
  session:
    redis:
      namespace: spring:session
  data:
    redis:
      host: localhost
      port: 6379
server:
  port: 8080
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### Authentication Service (`authentication-service/src/main/resources/application.yml`)
```yaml
spring:
  application:
    name: authentication-service
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000
auth:
  cookie:
    name: sid
    domain: localhost
    secure: true
    http-only: true
    same-site: Lax
    max-age-seconds: 1209600  # 14 days
  keycloak:
    base-url: http://localhost:9090
    realm: chat
    client-id: chatclient
    client-secret: vx2C1sgWJnd17ywYMUTVUx2kvsztB2TJ
    admin:
      realm: master
      client-id: admin-cli
      username: admin
      password: admin
  tokens:
    access-ttl-seconds: 300  # 5 minutes
    refresh-ttl-seconds: 1209600  # 14 days
kafka:
  topics:
    user-created: user.created
server:
  port: 8080
```

#### User Service (`user-service/src/main/resources/application.yml`)
```yaml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/chatdb
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yml
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: user-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
server:
  port: 0  # Dynamic port
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    instance-id: ${spring.application.name}:${random.value}
```

### Important Configuration Notes

**Security Warnings:**
- All credentials are hardcoded for development
- Client secrets are in plaintext
- No encryption for sensitive data
- Redis has no authentication
- Kafka uses plaintext protocol

**Production Considerations:**
- Use environment variables or external configuration (Spring Cloud Config)
- Implement secrets management (Vault, AWS Secrets Manager)
- Enable SSL/TLS for all communication
- Use secure cookie flags properly
- Enable Redis authentication
- Configure Kafka SSL/SASL

---

## Data Flows & Patterns

### 1. User Registration Flow

```
Client
  ↓ POST /register {username, password, email}
API Gateway (port 8080)
  ↓ Route to authentication-service
Authentication Service
  ↓ RegisterService.register()
  ├→ Keycloak Admin API: Create user in 'chat' realm
  └→ Kafka: Publish 'user.created' event
        ↓ Topic: user.created
User Service
  ↓ RegistrationListener.handleUserRegistration()
  └→ PostgreSQL: Insert into user_profiles
```

**Key Points:**
- User is created in Keycloak first
- Event-driven profile creation ensures eventual consistency
- Profile is created asynchronously
- User ID from Keycloak is used as primary key in user_profiles

### 2. User Login Flow

```
Client
  ↓ POST /login {username, password}
API Gateway
  ↓ Route to authentication-service
Authentication Service
  ↓ LoginService.login()
  ├→ Keycloak: Token request (OAuth2 password grant)
  ├→ Redis: Store tokens in session
  └→ Response: {accessToken, refreshToken} + Set-Cookie: sid
Client
  ↓ Subsequent requests with Cookie: sid
API Gateway
  ↓ Retrieve session from Redis
  ├→ Validate JWT
  └→ TokenRelayFilter: Add Authorization header
Backend Service
  └→ Receive request with valid JWT
```

**Key Points:**
- Tokens stored in Redis-backed session
- Cookie-based session management
- JWT validation at API Gateway
- Token relay to downstream services

### 3. Token Refresh Flow

```
Client
  ↓ POST /refresh
  ↓ Headers: X-Device-Id, X-Timestamp, X-Signature
  ↓ Cookie: sid (refresh token)
API Gateway
  ↓ Route to authentication-service
Authentication Service
  ↓ RefreshService.refresh()
  ├→ Verify device signature
  ├→ Validate refresh token from Redis session
  ├→ Keycloak: Exchange refresh token for new access token
  ├→ Redis: Update session with new tokens
  └→ Response: {accessToken}
```

**Key Points:**
- Device signature verification prevents token theft
- Refresh token rotation for security
- Session updated in Redis

### 4. Logout Flow

```
Client
  ↓ POST /logout or /logout/all
API Gateway
  ↓ Route to authentication-service
Authentication Service
  ↓ LogoutService.logout()
  ├→ Redis: Delete session(s)
  ├→ Clear security context
  └→ Response: Success + Clear-Cookie header
```

**Key Points:**
- `/logout` - single session logout
- `/logout/all` - all sessions for user
- Tokens invalidated by removing from Redis
- Keycloak tokens remain valid until expiry (consider implementing Keycloak logout)

### 5. Profile Access Flow

```
Client
  ↓ GET /profile/me
  ↓ Authorization: Bearer <access-token>
API Gateway
  ↓ Validate JWT
  ↓ TokenRelayFilter adds token
  ↓ Route to user-service
User Service
  ↓ ProfileController.getProfile()
  ↓ Extract user ID from JWT
  ↓ UserRepository.findById()
  ↓ PostgreSQL query
  └→ Response: UserProfile JSON
```

**Key Points:**
- JWT contains user ID (sub claim)
- User service validates JWT as OAuth2 resource server
- No session lookup needed for profile access

---

## Coding Conventions

### Package Structure

All services follow this package organization:
```
com.qufull.chat.<service-name>/
├── <ServiceName>Application.java    # Main class
├── controller/                       # REST controllers
├── service/                          # Business logic
├── repository/                       # Data access (JPA)
├── model/                            # Domain entities
├── dto/                              # Data Transfer Objects
├── config/                           # Configuration classes
├── exception/                        # Custom exceptions
├── handler/                          # Exception handlers, filters
├── aspect/                           # AOP aspects
├── listener/                         # Event listeners (Kafka)
└── kafka/                            # Kafka producers
```

### Naming Conventions

**Classes:**
- Controllers: `*Controller` (e.g., `AuthController`, `ProfileController`)
- Services: `*Service` (e.g., `LoginService`, `UserProfileService`)
- Repositories: `*Repository` (e.g., `UserRepository`)
- Configuration: `*Config` (e.g., `SecurityConfig`, `KafkaConfig`)
- DTOs: `*Request`, `*Response`, `*DTO` (e.g., `LoginRequest`, `UserResponse`)
- Entities: Domain name (e.g., `UserProfile`)
- Exceptions: `*Exception` (e.g., `InvalidCredentialsException`)
- Event publishers: `*Producer` (e.g., `UserEventProducer`)
- Event listeners: `*Listener` (e.g., `RegistrationListener`)
- Handlers: `*Handler` (e.g., `JsonErrorHandler`, `DeviceSignatureHandler`)
- Aspects: `*Aspect` (e.g., `LoggingAspect`)

**Methods:**
- Use verb prefixes: `get*`, `create*`, `update*`, `delete*`, `find*`, `validate*`, `handle*`
- Boolean methods: `is*`, `has*`, `can*`

**Variables:**
- Use camelCase
- Use descriptive names
- Avoid single-letter variables except in loops

### Lombok Usage

Lombok is heavily used to reduce boilerplate:
```java
@Data                       // Getters, setters, toString, equals, hashCode
@RequiredArgsConstructor    // Constructor with required fields (final/non-null)
@Builder                    // Builder pattern
@NoArgsConstructor          // Default constructor (for JPA)
@AllArgsConstructor         // All-args constructor
@Slf4j                      // Logger field
```

**Important:** Ensure Lombok annotation processing is enabled in your IDE.

### Dependency Injection

**Always use constructor-based injection:**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SomeOtherService otherService;

    // No @Autowired needed with @RequiredArgsConstructor
}
```

**Avoid field injection:**
```java
// ❌ Don't do this
@Autowired
private UserRepository userRepository;
```

### Exception Handling

**Custom Exception Hierarchy:**
```java
// Base exception
public class AuthenticationException extends RuntimeException

// Specific exceptions
public class InvalidCredentialsException extends AuthenticationException
public class UserAlreadyExistsException extends AuthenticationException
```

**Global Exception Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        // Return structured error response
    }
}
```

### API Response Format

**Success Response:**
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 300
}
```

**Error Response:**
```json
{
  "timestamp": "2025-12-08T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid credentials",
  "path": "/login"
}
```

### Configuration Properties

**Use `@ConfigurationProperties` for grouped properties:**
```java
@ConfigurationProperties(prefix = "auth.keycloak")
@Data
public class KeycloakProperties {
    private String baseUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private AdminProperties admin;

    @Data
    public static class AdminProperties {
        private String realm;
        private String clientId;
        private String username;
        private String password;
    }
}
```

### Logging

**Use SLF4J with Lombok:**
```java
@Slf4j
@Service
public class UserService {
    public void someMethod() {
        log.info("Processing user: {}", userId);
        log.error("Error occurred", exception);
    }
}
```

**Logging Levels:**
- `ERROR` - Errors and exceptions
- `WARN` - Warning conditions
- `INFO` - Important business events (user login, registration)
- `DEBUG` - Detailed flow information
- `TRACE` - Very detailed debugging

### Database Access

**Use Spring Data JPA:**
```java
public interface UserRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByNickname(String nickname);

    @Query("SELECT u FROM UserProfile u WHERE u.status = :status")
    List<UserProfile> findByStatus(@Param("status") String status);

    @Query(value = "SELECT * FROM user_profiles WHERE last_seen > :timestamp", nativeQuery = true)
    List<UserProfile> findRecentlyActive(@Param("timestamp") Timestamp timestamp);
}
```

**Entity Conventions:**
```java
@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    private String id;

    @Column(length = 30)
    private String nickname;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    // Use proper column names with snake_case in @Column annotations
}
```

---

## Testing Guidelines

### Current State
**Testing is minimal.** Only basic context load tests exist:
- `DiscoveryServiceApplicationTests.java`
- `ApiGatewayApplicationTests.java`
- `UserServiceApplicationTests.java`
- `AuthenticationServiceApplicationTests.java`

### Testing Framework
- **JUnit 5** - Test framework
- **Spring Boot Test** - Spring testing support
- **Mockito** - Mocking framework
- **Reactor Test** - Reactive testing (for API Gateway)
- **Spring Kafka Test** - Kafka testing

### Testing Strategy (To Be Implemented)

#### Unit Tests
Test individual components in isolation:
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void shouldGetUserProfile() {
        // Given
        UserProfile expected = UserProfile.builder()
            .id("123")
            .nickname("testuser")
            .build();
        when(userRepository.findById("123")).thenReturn(Optional.of(expected));

        // When
        UserProfile actual = userProfileService.getProfile("123");

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findById("123");
    }
}
```

#### Integration Tests
Test service integration with dependencies:
```java
@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    void shouldGetProfileForAuthenticatedUser() throws Exception {
        // Test with mocked OAuth2 authentication
    }
}
```

#### Kafka Tests
Test Kafka event publishing and consumption:
```java
@SpringBootTest
@EmbeddedKafka(topics = {"user.created"})
class KafkaIntegrationTest {
    @Autowired
    private UserEventProducer producer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void shouldPublishUserCreatedEvent() {
        // Test event publishing
    }
}
```

#### Test Database
Use **Testcontainers** for PostgreSQL integration tests:
```java
@SpringBootTest
@Testcontainers
class UserRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldSaveUserProfile() {
        // Test repository operations
    }
}
```

### Test Naming Conventions
- Test classes: `*Test` (unit), `*IntegrationTest` (integration)
- Test methods: `should*` or `given*When*Then*`
  - Examples: `shouldReturnUserWhenValidId()`, `givenInvalidCredentials_whenLogin_thenThrowException()`

---

## Common Development Tasks

### Adding a New REST Endpoint

1. **Define DTO classes** (request/response):
```java
@Data
public class CreateUserRequest {
    private String username;
    private String email;
}

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
}
```

2. **Add service method:**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        // Implementation
    }
}
```

3. **Add controller endpoint:**
```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }
}
```

4. **Add route in API Gateway** (if needed):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-management
          uri: lb://user-service
          predicates:
            - Path=/users/**
```

### Adding a New Database Table

1. **Create Liquibase changeset** in `user-service/src/main/resources/db/changelog/changeset/`:
```sql
-- File: create-messages-table.sql
CREATE TABLE messages (
    id VARCHAR(128) PRIMARY KEY,
    content TEXT NOT NULL,
    sender_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

2. **Update master changelog** in `db.changelog-master.yml`:
```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changeset/create-profiles-table.sql
  - include:
      file: db/changelog/changeset/create-messages-table.sql
```

3. **Create JPA entity:**
```java
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    private String id;

    @Column(nullable = false)
    private String content;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
```

4. **Create repository:**
```java
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findBySenderId(String senderId);
}
```

5. **Restart user-service** - Liquibase will apply migrations automatically.

### Adding Kafka Event

1. **Define event DTO:**
```java
@Data
@Builder
public class MessageSentEvent {
    private String messageId;
    private String senderId;
    private String content;
    private String timestamp;
}
```

2. **Create producer:**
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.message-sent}")
    private String topic;

    public void publishMessageSent(MessageSentEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getMessageId(), json);
            log.info("Published message.sent event: {}", event.getMessageId());
        } catch (Exception e) {
            log.error("Failed to publish message.sent event", e);
        }
    }
}
```

3. **Create listener in consuming service:**
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageEventListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.message-sent}", groupId = "notification-service")
    public void handleMessageSent(String message) {
        try {
            MessageSentEvent event = objectMapper.readValue(message, MessageSentEvent.class);
            log.info("Received message.sent event: {}", event.getMessageId());
            notificationService.notifyRecipient(event);
        } catch (Exception e) {
            log.error("Failed to process message.sent event", e);
        }
    }
}
```

4. **Add topic to configuration:**
```yaml
kafka:
  topics:
    message-sent: message.sent
```

### Adding a New Microservice

1. **Create Maven module:**
```bash
cd /home/user/chat
mkdir notification-service
cd notification-service
```

2. **Create `pom.xml`** (copy from existing service and modify):
```xml
<parent>
    <groupId>org.example</groupId>
    <artifactId>Chat</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<artifactId>notification-service</artifactId>
<name>notification-service</name>
```

3. **Add module to parent POM:**
```xml
<modules>
    <module>discovery-service</module>
    <module>api-gateway</module>
    <module>user-service</module>
    <module>authentication-service</module>
    <module>notification-service</module>
</modules>
```

4. **Create main application class:**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

5. **Create `application.yml`:**
```yaml
spring:
  application:
    name: notification-service
server:
  port: 0
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

6. **Build and run:**
```bash
cd /home/user/chat
mvn clean install
cd notification-service
mvn spring-boot:run
```

### Modifying Keycloak Configuration

1. **Update realm-export.json** for new clients/roles/settings
2. **Restart Keycloak container:**
```bash
docker-compose down keycloak
docker-compose up -d keycloak
```

3. **Or configure via Keycloak Admin Console:**
- Navigate to http://localhost:9090
- Login with admin/admin
- Select 'chat' realm
- Make changes via UI

### Building and Running

**Build all services:**
```bash
cd /home/user/chat
mvn clean package
```

**Run specific service:**
```bash
cd <service-directory>
mvn spring-boot:run
```

**Run with profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Build Docker images (if Dockerfiles exist):**
```bash
docker build -t chat/api-gateway:latest ./api-gateway
```

### Database Operations

**Access PostgreSQL:**
```bash
docker exec -it Postgres psql -U root -d chatdb
```

**Rollback Liquibase migration:**
```bash
cd user-service
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

**Generate Liquibase changeset:**
```bash
mvn liquibase:diff
```

### Kafka Operations

**List topics:**
```bash
docker exec -it Kafka kafka-topics --bootstrap-server localhost:9092 --list
```

**Create topic:**
```bash
docker exec -it Kafka kafka-topics --bootstrap-server localhost:9092 --create --topic message.sent --partitions 1 --replication-factor 1
```

**Consume messages:**
```bash
docker exec -it Kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user.created --from-beginning
```

**Produce test message:**
```bash
docker exec -it Kafka kafka-console-producer --bootstrap-server localhost:9092 --topic user.created
```

### Git Workflow

**Feature branch naming:**
- Format: `claude/claude-md-<id>-<session-id>`
- Current branch: `claude/claude-md-mix2l8c5ljezxcuq-01UdB5y7fTapcexMrHr67rNY`

**Commit message format:**
```
<type>: <short description>

<detailed description if needed>

Examples:
- feat: Add message sending functionality
- fix: Resolve token refresh race condition
- refactor: Extract user validation logic
- docs: Update API documentation
- test: Add integration tests for auth flow
```

**Push to remote:**
```bash
git push -u origin claude/claude-md-mix2l8c5ljezxcuq-01UdB5y7fTapcexMrHr67rNY
```

---

## Important Notes for AI Assistants

### Code Modification Guidelines

1. **Always read files before modifying:**
   - Use Read tool to view current code
   - Understand existing patterns and conventions
   - Match the coding style of the file

2. **Follow existing patterns:**
   - Use Lombok annotations consistently
   - Follow package structure conventions
   - Match naming conventions
   - Use constructor-based DI with `@RequiredArgsConstructor`

3. **Avoid over-engineering:**
   - Don't add unnecessary abstractions
   - Don't add features not requested
   - Keep solutions simple and focused
   - Don't add comments unless logic is complex

4. **Security considerations:**
   - Validate user input at controller layer
   - Use parameterized queries (JPA handles this)
   - Don't log sensitive data (passwords, tokens)
   - Validate JWT tokens properly
   - Check for common vulnerabilities (XSS, SQL injection, etc.)

5. **Error handling:**
   - Use custom exceptions for business errors
   - Implement global exception handlers
   - Return structured error responses
   - Log errors with appropriate level

### Common Pitfalls

1. **Port conflicts:**
   - API Gateway and Authentication Service both use port 8080
   - In production, they run on different hosts/containers
   - For local dev, change one port temporarily

2. **Service startup order:**
   - Always start Discovery Service first
   - Wait for services to register before testing
   - Check Eureka dashboard to verify registration

3. **Keycloak configuration:**
   - Ensure realm is imported correctly
   - Verify client secrets match configuration
   - Check redirect URIs for OAuth2 flows

4. **Database migrations:**
   - Test Liquibase changesets before deploying
   - Never modify executed changesets
   - Use rollback for fixing mistakes

5. **Kafka events:**
   - Ensure topics exist before publishing
   - Use proper serialization/deserialization
   - Handle event processing failures gracefully
   - Implement idempotency in event handlers

6. **Session management:**
   - Ensure Redis is running
   - Check session TTL configuration
   - Verify cookie settings for your environment

### Testing Before Committing

1. **Build all services:**
```bash
mvn clean install
```

2. **Check for compilation errors**

3. **Run existing tests:**
```bash
mvn test
```

4. **Manual testing:**
   - Start infrastructure (docker-compose up)
   - Start services in order
   - Test affected endpoints
   - Check logs for errors

5. **Verify Eureka registration:**
   - Open http://localhost:8761
   - Confirm all services registered

### Documentation

1. **Update this CLAUDE.md when:**
   - Adding new services
   - Changing architecture
   - Adding new dependencies
   - Modifying configuration structure
   - Adding new patterns or conventions

2. **Don't create unnecessary docs:**
   - Avoid creating README files unless requested
   - Code should be self-documenting
   - Use clear naming and structure

### Performance Considerations

1. **Database:**
   - Use appropriate indexes for queries
   - Avoid N+1 query problems
   - Use pagination for large result sets

2. **Caching:**
   - Consider caching frequently accessed data
   - Use Redis for distributed caching
   - Set appropriate TTLs

3. **Async processing:**
   - Use Kafka for long-running operations
   - Don't block request threads
   - Consider reactive patterns for high throughput

### Deployment Notes

**Current setup is for development only:**
- Hardcoded credentials
- No SSL/TLS
- Single instances
- No monitoring
- No logging aggregation

**Production requirements:**
- External configuration management
- Secrets management (Vault, AWS Secrets Manager)
- SSL/TLS everywhere
- Load balancing
- Health checks
- Distributed tracing (Sleuth + Zipkin)
- Centralized logging (ELK Stack)
- Metrics (Micrometer + Prometheus)
- Container orchestration (Kubernetes)
- CI/CD pipeline

---

## Version History

**Last Updated:** 2025-12-08
**Version:** 1.0
**Codebase Snapshot:** Branch `claude/claude-md-mix2l8c5ljezxcuq-01UdB5y7fTapcexMrHr67rNY`

---

## Quick Reference

### Service Ports
- Discovery Service: 8761
- API Gateway: 8080
- Authentication Service: 8080 (different host in production)
- User Service: Dynamic
- Keycloak: 9090
- PostgreSQL: 5435
- Kafka: 9092
- Redis: 6379

### Important URLs
- Eureka: http://localhost:8761
- Keycloak Admin: http://localhost:9090 (admin/admin)
- API Gateway: http://localhost:8080

### Key Technologies
- Java 17, Spring Boot 3.4-3.5, Spring Cloud 2024-2025
- Maven, Lombok
- PostgreSQL, Liquibase, Spring Data JPA
- Kafka, Redis
- Keycloak, OAuth2, JWT
- Docker Compose

### Authentication Flow
1. Register: `/register` → Keycloak → Kafka → UserProfile created
2. Login: `/login` → Keycloak tokens → Redis session
3. Access: Cookie `sid` → Redis lookup → Token validation → Backend
4. Refresh: `/refresh` + device headers → New access token
5. Logout: `/logout` → Redis session cleared

---

**For questions or clarifications about this codebase, refer to this document first.**

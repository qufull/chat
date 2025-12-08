# План разработки чат-приложения

## Текущее состояние проекта

**Что уже есть:**
- ✅ Базовая архитектура микросервисов (Eureka, Gateway, User Service)
- ✅ Аутентификация через Keycloak
- ✅ Kafka для событий
- ✅ User Service с автоматической регистрацией профилей
- ✅ Redis для сессий

**Что отсутствует:**
- ❌ Сам функционал чата (отправка/получение сообщений)
- ❌ WebSocket для real-time коммуникации
- ❌ Хранение сообщений
- ❌ Frontend приложение
- ❌ Тесты, мониторинг, документация

---

## 🎯 Roadmap: От MVP до Production

---

# ЭТАП 0: Исправление базовых проблем (1-2 недели)

**Цель:** Стабилизировать текущую инфраструктуру

## 0.1 Инфраструктура (3-4 дня)

### Задачи:
- [ ] Добавить Redis в docker-compose.yml
- [ ] Добавить вторую PostgreSQL БД для user-service в docker-compose
- [ ] Унифицировать версии Spring Boot (выбрать одну для всех сервисов)
- [ ] Настроить зависимости между контейнерами (depends_on, healthchecks)

### Темы для изучения:
- Docker Compose: multi-container applications
- PostgreSQL: multiple databases in one instance
- Docker healthchecks

### Код:
```yaml
# Пример для docker-compose.yml
redis:
  image: redis:7-alpine
  container_name: Redis
  ports:
    - "6379:6379"
  networks:
    - keycloak-network
  restart: unless-stopped

postgres:
  # Добавить создание второй БД через init scripts
  volumes:
    - ./init-db.sql:/docker-entrypoint-initdb.d/init.sql
```

## 0.2 Конфигурация (2-3 дня)

### Задачи:
- [ ] Создать application-dev.yml, application-prod.yml для каждого сервиса
- [ ] Вынести все секреты в environment variables
- [ ] Настроить Spring Profiles
- [ ] Создать .env файл для docker-compose

### Темы для изучения:
- Spring Profiles (dev, test, prod)
- Environment variables в Spring Boot
- Docker secrets management
- 12-Factor App methodology

### Код:
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# application-dev.yml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

## 0.3 User Service - CRUD API (2-3 дня)

### Задачи:
- [ ] Реализовать GET /profile/me (получить свой профиль)
- [ ] Реализовать PUT /profile/me (обновить профиль)
- [ ] Реализовать GET /profile/{userId} (публичный профиль)
- [ ] Добавить валидацию (nickname уникален, длина полей)
- [ ] Обработка ошибок

### Темы для изучения:
- Spring Data JPA: queries, projections
- Bean Validation (@Valid, @NotNull, @Size)
- Spring Security: получение текущего пользователя из JWT
- DTO mapping (MapStruct или ModelMapper)

### Код:
```java
@GetMapping("/me")
public ResponseEntity<ProfileResponse> getCurrentProfile(
    @AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();
    UserProfile profile = userProfileService.getProfile(userId);
    return ResponseEntity.ok(toDto(profile));
}
```

---

# ЭТАП 1: Core Chat Функционал (3-4 недели)

**Цель:** Реализовать базовый функционал чата (1-to-1 сообщения)

## 1.1 Message Service (5-7 дней)

### Задачи:
- [ ] Создать новый микросервис `message-service`
- [ ] Добавить в parent pom.xml
- [ ] Настроить Eureka client
- [ ] Создать базу данных для сообщений

### Структура БД:
```sql
-- messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    sender_id VARCHAR(255) NOT NULL,
    recipient_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    is_read BOOLEAN DEFAULT false,
    is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX idx_sender ON messages(sender_id);
CREATE INDEX idx_recipient ON messages(recipient_id);
CREATE INDEX idx_created_at ON messages(created_at DESC);
```

### Темы для изучения:
- Database design для чатов
- Indexing стратегии для большого объема данных
- Soft delete pattern
- UUID vs BIGINT для ID

### Endpoints:
- POST /messages - отправить сообщение
- GET /messages?userId={id}&page=0&size=20 - получить историю с пользователем
- PUT /messages/{id}/read - пометить как прочитанное
- DELETE /messages/{id} - удалить сообщение

## 1.2 WebSocket Support (4-5 дней)

### Задачи:
- [ ] Добавить Spring WebSocket в message-service
- [ ] Реализовать STOMP over WebSocket
- [ ] Настроить аутентификацию для WebSocket
- [ ] Реализовать отправку сообщений через WebSocket
- [ ] Реализовать получение сообщений в real-time

### Темы для изучения:
- **WebSocket protocol** - как работает, отличия от HTTP
- **STOMP protocol** - messaging protocol over WebSocket
- **Spring WebSocket** + STOMP configuration
- JWT authentication для WebSocket connections
- Message brokers (In-Memory vs External like RabbitMQ)

### Код:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

// Controller
@MessageMapping("/chat/{recipientId}")
public void sendMessage(@DestinationVariable String recipientId,
                        @Payload MessageRequest message,
                        Principal principal) {
    // Сохранить в БД
    Message saved = messageService.save(message, principal.getName());

    // Отправить получателю
    messagingTemplate.convertAndSendToUser(
        recipientId,
        "/queue/messages",
        saved
    );
}
```

### Важные моменты:
- WebSocket connections должны быть аутентифицированы через JWT
- Нужно обработать reconnection логику на клиенте
- Нужно хранить mapping userId → WebSocket session

## 1.3 Notification System (2-3 дня)

### Задачи:
- [ ] Реализовать уведомления о новых сообщениях
- [ ] Счетчик непрочитанных сообщений
- [ ] Typing indicators (опционально)
- [ ] Online/Offline status

### Темы для изучения:
- Server-Sent Events (SSE) как альтернатива WebSocket для уведомлений
- Redis Pub/Sub для координации между инстансами
- Presence detection в распределенных системах

### Endpoints:
- GET /messages/unread/count
- GET /users/{userId}/status (online/offline)

## 1.4 API Gateway Routing (1 день)

### Задачи:
- [ ] Добавить routes для message-service в Gateway
- [ ] Настроить WebSocket routing через Gateway
- [ ] Убедиться что JWT токены пробрасываются

### Код:
```yaml
# api-gateway/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: message-service
          uri: lb://message-service
          predicates:
            - Path=/messages/**
        - id: message-service-ws
          uri: lb:ws://message-service
          predicates:
            - Path=/ws/**
```

### Темы для изучения:
- Spring Cloud Gateway: WebSocket routing
- Load balancing для WebSocket connections

---

# ЭТАП 2: Group Chat (2-3 недели)

**Цель:** Добавить групповые чаты

## 2.1 Chat Service (4-5 дней)

### Задачи:
- [ ] Создать новый сервис `chat-service` или расширить message-service
- [ ] Реализовать управление чатами (создание, удаление, добавление участников)

### Структура БД:
```sql
-- chats table
CREATE TABLE chats (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(50) NOT NULL, -- 'PRIVATE' или 'GROUP'
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    avatar_url VARCHAR(500)
);

-- chat_members table
CREATE TABLE chat_members (
    chat_id UUID REFERENCES chats(id),
    user_id VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER', -- 'ADMIN', 'MEMBER'
    PRIMARY KEY (chat_id, user_id)
);

-- messages table (модифицировать)
ALTER TABLE messages ADD COLUMN chat_id UUID REFERENCES chats(id);
ALTER TABLE messages ALTER COLUMN recipient_id DROP NOT NULL;
```

### Endpoints:
- POST /chats - создать групповой чат
- GET /chats - список моих чатов
- GET /chats/{chatId} - информация о чате
- POST /chats/{chatId}/members - добавить участника
- DELETE /chats/{chatId}/members/{userId} - удалить участника
- PUT /chats/{chatId} - обновить чат (название, аватар)
- DELETE /chats/{chatId} - удалить чат

### Темы для изучения:
- Many-to-Many relationships в JPA
- Permissions и роли в чатах
- Оптимизация запросов (N+1 problem)

## 2.2 Group Messages via WebSocket (2-3 дня)

### Задачи:
- [ ] Модифицировать WebSocket логику для групповых чатов
- [ ] Реализовать broadcast сообщений всем участникам чата
- [ ] Read receipts для групповых чатов

### Темы для изучения:
- Broadcast patterns в WebSocket
- Scaling WebSocket connections (когда участники на разных серверах)
- Redis Pub/Sub для coordination

---

# ЭТАП 3: Media & Files (1-2 недели)

**Цель:** Возможность отправки файлов, изображений, голосовых

## 3.1 File Storage Service (3-4 дня)

### Задачи:
- [ ] Создать `file-service` для хранения файлов
- [ ] Интеграция с S3-compatible storage (MinIO или AWS S3)
- [ ] Генерация thumbnails для изображений
- [ ] Ограничения по размеру и типам файлов

### Структура БД:
```sql
CREATE TABLE files (
    id UUID PRIMARY KEY,
    original_name VARCHAR(500) NOT NULL,
    stored_name VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_by VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    s3_key VARCHAR(1000) NOT NULL
);
```

### Endpoints:
- POST /files/upload - загрузить файл
- GET /files/{fileId} - скачать файл
- GET /files/{fileId}/thumbnail - получить thumbnail

### Темы для изучения:
- **AWS S3 / MinIO** - object storage
- **Multipart file upload** в Spring
- **Image processing** - создание thumbnails (библиотека Thumbnailator)
- **Streaming large files** - как не грузить всю память
- **Pre-signed URLs** для direct upload/download

## 3.2 Расширение Messages (2 дня)

### Задачи:
- [ ] Добавить поле `attachments` в message
- [ ] Реализовать отправку сообщений с файлами
- [ ] Валидация файлов перед отправкой

---

# ЭТАП 4: Frontend Application (4-6 недель)

**Цель:** Создать полноценный web/mobile клиент

## 4.1 Выбор технологии (1 день исследования)

### Варианты:

**Web (SPA):**
- React + TypeScript + Material-UI / Ant Design
- Vue.js + TypeScript + Vuetify
- Angular + Material

**Mobile:**
- React Native (iOS + Android)
- Flutter (iOS + Android)
- Отдельные Native (Swift, Kotlin)

**Рекомендация:** React + TypeScript для начала (можно потом портировать на React Native)

### Темы для изучения:
- Modern JavaScript (ES6+)
- **TypeScript** - статическая типизация
- **React** - компоненты, hooks, state management
- **React Router** - навигация
- **Redux / Zustand / Jotai** - state management
- **WebSocket клиент** - библиотека STOMP.js или SockJS
- **Axios** - HTTP клиент

## 4.2 Authentication Flow (3-5 дней)

### Задачи:
- [ ] Интеграция с Keycloak (keycloak-js библиотека)
- [ ] Login/Logout UI
- [ ] Хранение токенов (localStorage vs sessionStorage vs httpOnly cookies)
- [ ] Auto-refresh tokens
- [ ] Protected routes

### Темы для изучения:
- OAuth2/OpenID Connect flow на клиенте
- Keycloak JavaScript adapter
- JWT декодирование на клиенте
- Security best practices для SPA

### Код:
```typescript
// Keycloak setup
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:9090',
  realm: 'chat',
  clientId: 'chatclient'
});

keycloak.init({ onLoad: 'login-required' }).then(authenticated => {
  if (authenticated) {
    // Store token
    localStorage.setItem('token', keycloak.token);
  }
});
```

## 4.3 Chat UI Components (5-7 дней)

### Задачи:
- [ ] Список чатов (sidebar)
- [ ] Окно переписки (message thread)
- [ ] Input для отправки сообщений
- [ ] Display сообщений (свои/чужие)
- [ ] Avatars, timestamps, read status
- [ ] Scroll to bottom, infinite scroll для истории

### Темы для изучения:
- CSS Flexbox / Grid для layouts
- Virtual scrolling для больших списков (react-window)
- Optimistic UI updates
- Debouncing и throttling

## 4.4 WebSocket Integration (3-4 дня)

### Задачи:
- [ ] Подключение к WebSocket endpoint
- [ ] Отправка сообщений через WebSocket
- [ ] Получение сообщений в real-time
- [ ] Обработка reconnection
- [ ] Loading states, error handling

### Код:
```typescript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  onConnect: () => {
    client.subscribe('/user/queue/messages', (message) => {
      const newMessage = JSON.parse(message.body);
      // Update UI
    });
  }
});

client.activate();
```

### Темы для изучения:
- STOMP over WebSocket на клиенте
- Reconnection strategies
- Heartbeat механизм

## 4.5 Advanced Features (5-7 дней)

### Задачи:
- [ ] Search по сообщениям
- [ ] Emoji picker
- [ ] Markdown support в сообщениях
- [ ] File upload с progress bar
- [ ] Image preview, lightbox
- [ ] Typing indicators
- [ ] Online/Offline status
- [ ] Notifications (browser notifications API)

---

# ЭТАП 5: Advanced Features (3-4 недели)

## 5.1 Search Service (3-5 дней)

### Задачи:
- [ ] Интеграция с Elasticsearch
- [ ] Индексирование сообщений
- [ ] Full-text search
- [ ] Поиск по чатам, пользователям

### Темы для изучения:
- **Elasticsearch** - полнотекстовый поиск
- Spring Data Elasticsearch
- Indexing strategies
- Query DSL

## 5.2 Notifications Service (3-4 дня)

### Задачи:
- [ ] Push notifications для мобильных (FCM)
- [ ] Email notifications
- [ ] Настройки уведомлений для пользователей

### Темы для изучения:
- Firebase Cloud Messaging (FCM)
- SMTP / Email sending (Spring Mail)
- Notification preferences design

## 5.3 Voice/Video Calls (опционально, 1-2 недели)

### Задачи:
- [ ] WebRTC для peer-to-peer звонков
- [ ] Signaling server
- [ ] TURN/STUN серверы

### Темы для изучения:
- **WebRTC** - сложная тема!
- Signaling protocols
- NAT traversal (STUN/TURN)
- Media streams

---

# ЭТАП 6: Testing (2-3 недели)

**Цель:** Покрыть тестами критичную функциональность

## 6.1 Unit Tests (5-7 дней)

### Задачи:
- [ ] Юнит-тесты для service слоя во всех сервисах
- [ ] Моки для внешних зависимостей
- [ ] Code coverage > 70%

### Темы для изучения:
- JUnit 5
- Mockito для моков
- AssertJ для assertions
- Code coverage (JaCoCo)

### Код:
```java
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService service;

    @Test
    void shouldGetProfile_whenUserExists() {
        // given
        String userId = "123";
        UserProfile profile = UserProfile.builder()
            .id(userId)
            .nickname("test")
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(profile));

        // when
        UserProfile result = service.getProfile(userId);

        // then
        assertThat(result.getNickname()).isEqualTo("test");
    }
}
```

## 6.2 Integration Tests (5-7 дней)

### Задачи:
- [ ] Integration тесты с testcontainers (PostgreSQL, Kafka, Redis)
- [ ] REST API тесты через MockMvc / WebTestClient
- [ ] Тесты на правильность работы security

### Темы для изучения:
- **Testcontainers** - Docker containers для тестов
- Spring Boot Test
- @WebMvcTest, @DataJpaTest, @SpringBootTest
- MockMvc / WebTestClient

### Код:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class MessageControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldSendMessage() {
        webTestClient.post()
            .uri("/messages")
            .header("Authorization", "Bearer " + getTestToken())
            .bodyValue(new MessageRequest("Hello", "recipient-id"))
            .exchange()
            .expectStatus().isCreated();
    }
}
```

## 6.3 E2E Tests (3-5 дней, опционально)

### Задачи:
- [ ] End-to-end тесты для критичных флоу
- [ ] Тесты для frontend (Cypress / Playwright)

### Темы для изучения:
- Cypress или Playwright для E2E
- Test scenarios design
- Flaky tests prevention

---

# ЭТАП 7: Observability & Monitoring (1-2 недели)

**Цель:** Видеть что происходит в production

## 7.1 Centralized Logging (2-3 дня)

### Задачи:
- [ ] ELK Stack (Elasticsearch + Logstash + Kibana) или Loki
- [ ] Логи всех сервисов в одно место
- [ ] Структурированное логирование (JSON)
- [ ] Correlation ID для трейсинга запросов

### Темы для изучения:
- ELK Stack или Grafana Loki
- Logback / Log4j2 configuration
- Structured logging (JSON format)
- Correlation ID propagation

### Код:
```xml
<!-- logback-spring.xml -->
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

## 7.2 Metrics & Monitoring (3-4 дня)

### Задачи:
- [ ] Spring Boot Actuator во всех сервисах
- [ ] Prometheus для сбора метрик
- [ ] Grafana для визуализации
- [ ] Алерты на критичные метрики

### Темы для изучения:
- **Spring Boot Actuator** - метрики, healthchecks
- **Prometheus** - time-series database для метрик
- **Grafana** - дашборды
- Micrometer - abstraction для метрик
- Alerting (Alertmanager)

### Метрики для отслеживания:
- Request rate, latency, errors
- WebSocket connections count
- Messages sent/received per second
- Database connection pool metrics
- JVM metrics (heap, GC)

## 7.3 Distributed Tracing (2-3 дня)

### Задачи:
- [ ] Micrometer Tracing (ex-Spring Cloud Sleuth)
- [ ] Zipkin или Jaeger
- [ ] Трейсинг через все микросервисы

### Темы для изучения:
- Distributed tracing concepts
- OpenTelemetry
- Micrometer Tracing
- Zipkin / Jaeger

### Зачем:
- Видеть путь запроса через все микросервисы
- Найти bottlenecks
- Debug production issues

---

# ЭТАП 8: Resilience & Scalability (2-3 недели)

**Цель:** Сделать систему отказоустойчивой и масштабируемой

## 8.1 Circuit Breakers (2-3 дня)

### Задачи:
- [ ] Добавить Resilience4j
- [ ] Circuit breakers для вызовов между сервисами
- [ ] Fallback механизмы
- [ ] Retry policies

### Темы для изучения:
- **Resilience4j** - circuit breaker, retry, rate limiter
- Circuit breaker pattern
- Bulkhead pattern
- Timeout handling

### Код:
```java
@Service
public class UserService {

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    @Retry(name = "userService")
    public User getUser(String id) {
        return restClient.get("/users/" + id);
    }

    private User fallbackGetUser(String id, Exception e) {
        return User.builder()
            .id(id)
            .nickname("Unknown")
            .build();
    }
}
```

## 8.2 Caching (2-3 дня)

### Задачи:
- [ ] Spring Cache abstraction
- [ ] Redis как cache
- [ ] Кэшировать профили пользователей, метаданные чатов
- [ ] Cache eviction стратегии

### Темы для изучения:
- Caching strategies (cache-aside, read-through, write-through)
- Spring Cache (@Cacheable, @CacheEvict)
- Redis as cache vs session store
- TTL и eviction policies

## 8.3 Database Optimization (3-5 дней)

### Задачи:
- [ ] Анализ медленных запросов
- [ ] Добавить недостающие индексы
- [ ] Partitioning для больших таблиц (messages)
- [ ] Connection pooling настройка (HikariCP)

### Темы для изучения:
- PostgreSQL query optimization
- EXPLAIN ANALYZE
- Indexing strategies
- Table partitioning
- Connection pooling (HikariCP configuration)

## 8.4 Rate Limiting (2 дня)

### Задачи:
- [ ] Rate limiting в Gateway
- [ ] Bucket4j или Redis rate limiter
- [ ] Лимиты на API endpoints
- [ ] Защита от спама в чате

### Темы для изучения:
- Rate limiting algorithms (token bucket, leaky bucket)
- Bucket4j library
- Redis для distributed rate limiting

## 8.5 Load Balancing & Scaling (3-4 дня)

### Задачи:
- [ ] Запустить несколько инстансов сервисов
- [ ] Убедиться что load balancing работает через Eureka
- [ ] Sticky sessions для WebSocket (или Redis для координации)
- [ ] Horizontal scaling тестирование

### Темы для изучения:
- Eureka load balancing
- Sticky sessions для WebSocket
- Redis Pub/Sub для cross-instance communication
- Session affinity

---

# ЭТАП 9: Security Hardening (1-2 недели)

**Цель:** Усилить безопасность

## 9.1 Security Audit (3-5 дней)

### Задачи:
- [ ] Включить CSRF где нужно
- [ ] Content Security Policy (CSP)
- [ ] XSS protection
- [ ] SQL injection prevention (проверить параметризованные запросы)
- [ ] Rate limiting на authentication endpoints
- [ ] Secrets rotation strategy

### Темы для изучения:
- OWASP Top 10
- Spring Security best practices
- CSP headers
- Secrets management (Vault, AWS Secrets Manager)

## 9.2 Data Encryption (2-3 дня)

### Задачи:
- [ ] HTTPS everywhere (TLS certificates)
- [ ] Encryption at rest для sensitive data
- [ ] Encrypted WebSocket (wss://)

### Темы для изучения:
- TLS/SSL certificates (Let's Encrypt)
- Data encryption в PostgreSQL
- Spring Security Crypto

---

# ЭТАП 10: DevOps & CI/CD (2-3 недели)

**Цель:** Автоматизировать deployment

## 10.1 CI Pipeline (3-5 дней)

### Задачи:
- [ ] GitHub Actions / GitLab CI / Jenkins
- [ ] Автоматический запуск тестов на каждый PR
- [ ] Build Docker images
- [ ] Code quality checks (SonarQube)

### Темы для изучения:
- CI/CD concepts
- GitHub Actions workflows
- Docker multi-stage builds
- SonarQube для code quality

### Пример GitHub Actions:
```yaml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean test
      - name: Run tests
        run: mvn verify
```

## 10.2 CD Pipeline (3-5 дней)

### Задачи:
- [ ] Автоматический deploy в staging
- [ ] Manual approval для production
- [ ] Blue-green или canary deployment
- [ ] Rollback strategy

### Темы для изучения:
- Continuous Deployment strategies
- Blue-green deployment
- Canary releases
- GitOps (ArgoCD, Flux)

## 10.3 Kubernetes (опционально, 1-2 недели)

### Задачи:
- [ ] Kubernetes cluster setup (Minikube локально, EKS/GKE для production)
- [ ] Helm charts для всех сервисов
- [ ] Ingress controller
- [ ] ConfigMaps и Secrets
- [ ] Auto-scaling (HPA)

### Темы для изучения:
- **Kubernetes** - orchestration (большая тема!)
- Pods, Services, Deployments
- Helm - package manager для K8s
- Ingress controllers
- Persistent Volumes
- ConfigMaps и Secrets
- Horizontal Pod Autoscaler

---

# ЭТАП 11: Production Readiness (1-2 недели)

## 11.1 Documentation (3-5 дней)

### Задачи:
- [ ] OpenAPI/Swagger для всех REST APIs
- [ ] Architecture Decision Records (ADR)
- [ ] Deployment guide
- [ ] User documentation

### Темы для изучения:
- SpringDoc OpenAPI (Swagger UI)
- API documentation best practices
- ADR format

## 11.2 Backup & Disaster Recovery (2-3 дня)

### Задачи:
- [ ] Автоматические backups PostgreSQL
- [ ] Backup стратегия для S3 files
- [ ] Disaster recovery plan
- [ ] Tested restore procedures

### Темы для изучения:
- PostgreSQL backup strategies (pg_dump, WAL archiving)
- S3 versioning и backup
- RTO/RPO concepts

## 11.3 Performance Testing (3-5 дней)

### Задачи:
- [ ] Load testing (Gatling, JMeter, k6)
- [ ] Stress testing
- [ ] WebSocket connections load testing
- [ ] Bottleneck identification

### Темы для изучения:
- Load testing tools (Gatling, k6)
- Performance metrics interpretation
- Capacity planning

---

# 📚 Общие темы для изучения

## Backend
1. **Spring Framework Ecosystem**
   - Spring Boot
   - Spring Security
   - Spring Data JPA
   - Spring Cloud (Gateway, Eureka, Config)
   - Spring WebSocket

2. **Microservices Architecture**
   - Service discovery
   - API Gateway pattern
   - Event-driven architecture
   - Saga pattern
   - CQRS (опционально)

3. **Messaging & Streaming**
   - Apache Kafka
   - RabbitMQ (альтернатива)
   - WebSocket & STOMP

4. **Databases**
   - PostgreSQL (advanced features)
   - Redis (cache, pub/sub)
   - Elasticsearch (search)

5. **Security**
   - OAuth2 / OpenID Connect
   - JWT tokens
   - Keycloak administration

6. **Testing**
   - JUnit 5, Mockito
   - Testcontainers
   - Integration testing

7. **DevOps**
   - Docker & Docker Compose
   - Kubernetes (optional but recommended)
   - CI/CD pipelines

## Frontend
1. **JavaScript/TypeScript**
   - ES6+ features
   - TypeScript basics

2. **React Ecosystem**
   - React hooks
   - React Router
   - State management (Redux/Zustand)
   - React Query (для data fetching)

3. **UI/UX**
   - CSS (Flexbox, Grid)
   - Material-UI / Ant Design
   - Responsive design

4. **Real-time Communication**
   - WebSocket client
   - STOMP.js

## DevOps & Infrastructure
1. **Containerization**
   - Docker
   - Docker Compose
   - Kubernetes (advanced)

2. **Monitoring & Observability**
   - Prometheus, Grafana
   - ELK Stack / Loki
   - Zipkin / Jaeger

3. **CI/CD**
   - GitHub Actions
   - Jenkins / GitLab CI

---

# 📖 Рекомендуемые ресурсы

## Книги
- "Spring in Action" - Craig Walls
- "Microservices Patterns" - Chris Richardson
- "Designing Data-Intensive Applications" - Martin Kleppmann
- "Building Microservices" - Sam Newman

## Онлайн курсы
- Spring Framework на Udemy (курсы от Chad Darby)
- Microservices with Spring Cloud на Pluralsight
- React - The Complete Guide на Udemy
- Kubernetes для начинающих

## Документация
- Spring.io guides - https://spring.io/guides
- Keycloak documentation
- Kafka documentation
- React documentation

## YouTube каналы
- Amigoscode (Spring Boot, microservices)
- Java Brains (Spring, Kafka)
- Web Dev Simplified (React, frontend)

---

# ⏱️ Оценка времени (полный roadmap)

**При работе full-time (8 часов/день):**

| Этап | Время | Описание |
|------|-------|----------|
| 0 | 1-2 недели | Исправление базовых проблем |
| 1 | 3-4 недели | Core chat функционал |
| 2 | 2-3 недели | Group chat |
| 3 | 1-2 недели | Media & files |
| 4 | 4-6 недель | Frontend |
| 5 | 3-4 недели | Advanced features |
| 6 | 2-3 недели | Testing |
| 7 | 1-2 недели | Observability |
| 8 | 2-3 недели | Resilience & scalability |
| 9 | 1-2 недели | Security |
| 10 | 2-3 недели | DevOps & CI/CD |
| 11 | 1-2 недели | Production readiness |

**Итого: 6-9 месяцев** для полного production-ready приложения

**MVP (Этапы 0-4):** 3-4 месяца

**При работе part-time (2-4 часа/день):** умножить на 2-3

---

# 🎯 Рекомендуемый путь обучения

## Вариант 1: "От простого к сложному"
1. Закончить User Service (этап 0.3)
2. Сделать простой 1-to-1 чат (этап 1)
3. Сделать минимальный frontend (этап 4.1-4.4)
4. Дальше по приоритетам

## Вариант 2: "Feature-driven"
1. Выбрать одну фичу (например, 1-to-1 chat)
2. Сделать её полностью: backend + frontend + тесты
3. Deploy в staging
4. Следующая фича

## Вариант 3: "Production-first"
1. Этап 0 (инфраструктура)
2. Этап 6 (базовые тесты)
3. Этап 10 (CI/CD)
4. Потом добавлять фичи инкрементально

---

# ✅ Критерии готовности к production

- [ ] Все сервисы в Docker Compose / Kubernetes
- [ ] Secrets в environment variables или Vault
- [ ] Unit tests coverage > 70%
- [ ] Integration tests для critical paths
- [ ] Monitoring (Prometheus + Grafana)
- [ ] Centralized logging
- [ ] Circuit breakers на external calls
- [ ] Rate limiting
- [ ] HTTPS enabled
- [ ] Backup & restore tested
- [ ] CI/CD pipeline работает
- [ ] Load testing пройден
- [ ] Documentation готова

---

**Успехов в разработке!** 🚀

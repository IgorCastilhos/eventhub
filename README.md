# 🎫 EventHub - Event Management System

## 🚀 Quick Start

**One command to run everything:**

```bash
docker-compose up -d
```

That's it! The application will be available at:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 📋 Table of Contents
- [Quick Start](#quick-start)
- [Requirements](#requirements)
- [Management Commands](#management-commands)
- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [API Documentation](#api-documentation)

---

## 📦 Requirements

- Docker
- Docker Compose

That's all you need!

---

## 🎮 Management Commands

Use the helper script for common tasks:

```bash
./eventhub.sh start    # Start all services
./eventhub.sh stop     # Stop all services
./eventhub.sh restart  # Restart all services
./eventhub.sh logs     # View logs
./eventhub.sh status   # Check service status
./eventhub.sh clean    # Remove all data (WARNING: deletes DB!)
./eventhub.sh rebuild  # Rebuild and restart
```

Or use Docker Compose directly:

```bash
docker-compose up -d        # Start
docker-compose down         # Stop
docker-compose logs -f      # View logs
docker-compose ps           # Status
```

---

## 🏛️ Architecture Overview

This application follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (Controllers, DTOs, Exception        │
│           Handlers)                     │
├─────────────────────────────────────────┤
│         Application Layer               │
│    (Services, Business Logic,           │
│        Use Cases)                       │
├─────────────────────────────────────────┤
│          Domain Layer                   │
│    (Entities, Domain Events,            │
│       Business Rules)                   │
├─────────────────────────────────────────┤
│       Infrastructure Layer              │
│    (Repositories, External              │
│    Services, Database)                  │
└─────────────────────────────────────────┘
```

### Why Clean Architecture?

1. **Testability**: Business logic is independent of frameworks
2. **Maintainability**: Changes in one layer don't cascade to others
3. **Flexibility**: Easy to swap implementations (e.g., database, security)
4. **Scalability**: Clear boundaries enable microservices migration

---

## 🛠️ Technology Stack

### Backend

| Technology | Version | Purpose | Why This Choice? |
|------------|---------|---------|------------------|
| **Java** | 17 LTS | Core Language | Long-term support, modern features (Records, Pattern Matching), production stability |
| **Spring Boot** | 3.2.x | Framework | Industry standard, extensive ecosystem, production-ready features |
| **PostgreSQL** | 16 | Database | ACID compliance, advanced concurrency control, JSON support, excellent for production |
| **Spring Security** | 6.x | Authentication/Authorization | JWT implementation, role-based access, battle-tested |
| **Spring Data JPA** | 3.x | ORM | Reduces boilerplate, supports optimistic/pessimistic locking |
| **Hibernate** | 6.x | JPA Implementation | Advanced caching, lazy loading, query optimization |
| **Flyway** | 9.x | Database Migration | Version control for database, reproducible deployments |
| **Redis** | 7.x | Caching | In-memory performance, pub/sub for events, production-proven |
| **Lombok** | 1.18.x | Code Generation | Reduces boilerplate (getters, setters, constructors) |
| **MapStruct** | 1.5.x | Object Mapping | Compile-time DTO mapping, type-safe, performant |
| **JUnit 5** | 5.10.x | Testing Framework | Modern assertions, parameterized tests, extensions |
| **Mockito** | 5.x | Mocking | Unit test isolation, behavior verification |
| **Testcontainers** | 1.19.x | Integration Testing | Real database for tests, reproducible test environment |
| **SpringDoc OpenAPI** | 2.x | API Documentation | Auto-generated Swagger UI, Spring Boot 3 compatible |
| **SLF4J + Logback** | 2.x | Logging | Structured logging, production-grade log management |

### Frontend

| Technology | Version | Purpose | Why This Choice? |
|------------|---------|---------|------------------|
| **React** | 18.x | UI Framework | Component-based, virtual DOM, huge ecosystem |
| **TypeScript** | 5.x | Type System | Type safety, better IDE support, fewer runtime errors |
| **Vite** | 5.x | Build Tool | Fast HMR, optimized production builds, modern tooling |
| **React Router** | 6.x | Routing | Declarative routing, nested routes, URL management |
| **React Query** | 5.x | Data Fetching | Caching, background updates, optimistic UI |
| **Zustand** | 4.x | State Management | Lightweight, simple API, no boilerplate (vs Redux) |
| **Axios** | 1.x | HTTP Client | Interceptors for auth, request/response transformation |
| **React Hook Form** | 7.x | Form Management | Performance, validation, less re-renders than Formik |
| **Zod** | 3.x | Schema Validation | Type-safe validation, TypeScript integration |
| **TailwindCSS** | 3.x | Styling | Utility-first, rapid development, consistent design |
| **Shadcn/ui** | Latest | Component Library | Accessible, customizable, modern design |
| **date-fns** | 3.x | Date Manipulation | Lightweight (vs moment.js), tree-shakeable, immutable |

### DevOps & Infrastructure

| Technology | Purpose | Why This Choice? |
|------------|---------|------------------|
| **Docker** | Containerization | Consistent environments, easy deployment |
| **Docker Compose** | Multi-container orchestration | Local development, simplified setup |
| **Nginx** | Reverse Proxy | Serve React SPA, API gateway, SSL termination |
| **Ollama** | Local LLM Hosting | Privacy-first AI, no external dependencies |

---

## 🎯 Design Decisions

### 1. **Concurrency Strategy: Optimistic Locking with Event Sourcing**

**Problem**: Two users buying the last ticket simultaneously (overbooking)

**Solution**: Multi-layered approach

```java
@Entity
public class Event {
    @Version  // Optimistic Locking
    private Long version;
    
    private Integer availableCapacity;
}
```

**Why Optimistic Locking?**
- **Performance**: No database locks during read operations
- **Scalability**: Better throughput than pessimistic locks
- **User Experience**: Fast response times under normal load

**Fallback Strategy**:
- Database constraint: `CHECK (available_capacity >= 0)`
- Application-level validation before commit
- Idempotency keys for duplicate request prevention

**Alternative Considered**: Pessimistic Locking
- **Rejected because**: Lower throughput, potential deadlocks, not suitable for distributed systems

### 2. **Security: JWT with Refresh Token Pattern**

**Implementation**:
- Access Token: 15 minutes expiry
- Refresh Token: 7 days expiry, stored in HTTP-only cookie
- Roles: ADMIN, USER

**Why JWT?**
- **Stateless**: No server-side session storage
- **Scalability**: Works in distributed systems
- **Performance**: No database lookup per request

**Security Measures**:
- HTTPS-only cookies
- CSRF protection
- XSS prevention via HTTP-only cookies
- Password hashing with BCrypt (cost factor: 12)

### 3. **Caching Strategy: Multi-Level Cache**

```
┌──────────────┐
│   Browser    │  (HTTP Cache-Control)
└──────┬───────┘
       │
┌──────▼───────┐
│    Redis     │  (Application Cache)
└──────┬───────┘
       │
┌──────▼───────┐
│  Hibernate   │  (L2 Cache)
└──────┬───────┘
       │
┌──────▼───────┐
│  PostgreSQL  │
└──────────────┘
```

**Cache Invalidation**:
- Write-through cache for event updates
- TTL: 5 minutes for event listings
- Cache eviction on event CRUD operations

### 4. **DTO Pattern: Separation of Concerns**

**Why DTOs?**
- **Security**: Don't expose internal entity structure
- **Versioning**: API changes don't affect domain model
- **Performance**: Fetch only needed fields (projection queries)
- **Validation**: Input validation separate from business logic

**Example**:
```java
// Request DTO
public record CreateEventRequest(
    @NotBlank String name,
    @Future LocalDateTime eventDate,
    @NotBlank String location,
    @Positive Integer capacity
) {}

// Response DTO
public record EventResponse(
    UUID id,
    String name,
    LocalDateTime eventDate,
    String location,
    Integer availableCapacity
) {}
```

### 5. **Exception Handling: Centralized Error Responses**

**Custom Exception Hierarchy**:
```
BusinessException (abstract)
├── ResourceNotFoundException
├── InsufficientCapacityException
├── OptimisticLockException
└── UnauthorizedException
```

**Global Exception Handler**:
- Consistent error format across all endpoints
- Proper HTTP status codes
- Detailed error messages for debugging (dev mode only)
- Generic messages for production

### 6. **Event-Driven Architecture: Domain Events**

**When a ticket is purchased**:
```java
// 1. Update database
ticketRepository.save(ticket);

// 2. Publish domain event
eventPublisher.publishEvent(new TicketPurchasedEvent(ticket));

// 3. Event listener sends email (async)
@EventListener
@Async
public void handleTicketPurchase(TicketPurchasedEvent event) {
    emailService.sendConfirmation(event.getTicket());
}
```

**Benefits**:
- Decoupled components
- Easy to add new features (e.g., send SMS, update analytics)
- Supports eventual consistency

---


---

## 🚀 Getting Started

### Prerequisites

- Docker Desktop 24.x or higher
- Docker Compose 2.x or higher
- (Optional) Java 17+ and Node.js 20+ for local development

### Quick Start (Docker) - Recomendado

A forma mais fácil de rodar todo o sistema é usando Docker Compose:

```bash
# Clone the repository
git clone <repository-url>
cd eventhub

# Start all services (PostgreSQL, Redis, Backend, Frontend, Ollama)
docker compose up -d --build

# Wait for services to be healthy (this may take 2-3 minutes on first run)
docker compose ps

# (Optional) Download Ollama model for AI Chat
docker exec eventhub-ollama ollama pull llama3.2:3b

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/api
# Swagger UI: http://localhost:3000/swagger-ui/index.html
# Ollama: http://localhost:11434
```

### Default Credentials

The system comes with pre-configured demo users:

| Role | Username | Password |
|------|----------|----------|
| **Admin** | admin | admin123 |
| **User** | user | user123 |

### Stopping the Application

```bash
# Stop all services (keeps data)
docker compose stop

# Stop and remove containers (keeps volumes/data)
docker compose down

# Stop, remove containers and ALL data
docker compose down -v
```

### Local Development

**Backend**:
```bash
cd backend

# Install dependencies
./mvnw clean install

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests
./mvnw test -Dtest=**/*E2E*    # E2E tests
```

**Frontend**:
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Run tests
npm test

# Build for production
npm run build
```

---

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!@",
  "name": "John Doe"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "john.doe",
  "role": "USER"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "SecurePass123!@"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "john.doe",
  "role": "USER"
}
```

### Event Endpoints

#### Create Event (ADMIN only)
```http
POST /api/events
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Spring Boot Workshop",
  "eventDate": "2026-03-15T14:00:00",
  "location": "Tech Hub, Room 301",
  "capacity": 50,
  "description": "Advanced Spring Boot techniques",
  "price": 299.00,
  "imageUrl": "https://example.com/image.jpg"
}

Response: 201 Created
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Spring Boot Workshop",
  "eventDate": "2026-03-15T14:00:00",
  "location": "Tech Hub, Room 301",
  "capacity": 50,
  "availableTickets": 50,
  "price": 299.00,
  "status": "SCHEDULED",
  "createdAt": "2026-02-04T10:00:00"
}
```

#### Get Event by ID (Public)
```http
GET /api/events/{id}

Example: GET /api/events/123e4567-e89b-12d3-a456-426614174000

Response: 200 OK
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Spring Boot Workshop",
  "description": "Advanced Spring Boot techniques",
  "eventDate": "2026-03-15T14:00:00",
  "location": "Tech Hub, Room 301",
  "capacity": 50,
  "availableTickets": 45,
  "price": 299.00,
  "imageUrl": "https://example.com/image.jpg",
  "status": "SCHEDULED",
  "ticketsSold": 5,
  "soldPercentage": 10.0,
  "isAvailable": true,
  "isPast": false,
  "createdAt": "2026-02-04T10:00:00",
  "updatedAt": "2026-02-04T10:00:00"
}
```

#### List Events (Public)
```http
GET /api/events?page=0&size=10&sort=eventDate,asc

Response: 200 OK
{
  "content": [
    {
      "id": "...",
      "name": "Spring Boot Workshop",
      "eventDate": "2026-03-15T14:00:00",
      "location": "Tech Hub, Room 301",
      "availableTickets": 45,
      "capacity": 50
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

#### Update Event (ADMIN only)
```http
PATCH /api/events/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Spring Boot Advanced Workshop",
  "price": 349.00
}

Response: 200 OK
```

#### Delete Event (ADMIN only)
```http
DELETE /api/events/{id}
Authorization: Bearer <token>

Response: 204 No Content
```

#### Search Events (Public)
```http
GET /api/events/search?q=spring

Response: 200 OK
[
  {
    "id": "...",
    "name": "Spring Boot Workshop",
    ...
  }
]
```

#### Get Upcoming Events (Public)
```http
GET /api/events/upcoming

Response: 200 OK
[
  {
    "id": "...",
    "name": "Spring Boot Workshop",
    "eventDate": "2026-03-15T14:00:00",
    ...
  }
]
```

### Ticket Endpoints

#### Purchase Ticket (Authenticated)
```http
POST /api/tickets/purchase
Authorization: Bearer <token>
Content-Type: application/json

{
  "eventId": "123e4567-e89b-12d3-a456-426614174000",
  "participantName": "John Doe",
  "participantEmail": "john@example.com"
}

Response: 201 Created
{
  "id": "ticket-uuid",
  "confirmationCode": "ABC123XYZ",
  "status": "ACTIVE",
  "purchaseDate": "2026-02-04T10:30:00",
  "participantName": "John Doe",
  "participantEmail": "john@example.com",
  "event": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Spring Boot Workshop",
    "eventDate": "2026-03-15T14:00:00",
    "location": "Tech Hub, Room 301"
  }
}
```

#### Get My Tickets (Authenticated)
```http
GET /api/tickets/my-tickets?page=0&size=10
Authorization: Bearer <token>

Response: 200 OK
{
  "content": [
    {
      "id": "...",
      "confirmationCode": "ABC123XYZ",
      "status": "ACTIVE",
      "event": {...}
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

#### Cancel Ticket (Authenticated)
```http
DELETE /api/tickets/{id}
Authorization: Bearer <token>

Response: 200 OK
{
  "id": "...",
  "status": "CANCELLED",
  "message": "Ingresso cancelado com sucesso"
}
```

---

## 🧪 Testing Strategy

### Test Pyramid

```
         /\
        /  \  E2E (1 test)
       /────\
      / Inte-\  Integration (15 tests)
     / gration\
    /──────────\
   /    Unit    \  Unit (50+ tests)
  /─────────────\
```

### Unit Tests (50+ tests)

**Purpose**: Test business logic in isolation

**Tools**: JUnit 5 + Mockito

**Coverage Goal**: 80%+ for service layer

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    
    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private EventRepository eventRepository;
    
    @InjectMocks
    private TicketService ticketService;
    
    @Test
    @DisplayName("Should purchase ticket when capacity available")
    void shouldPurchaseTicket_WhenCapacityAvailable() {
        // Given
        Event event = createEventWithCapacity(10);
        when(eventRepository.findById(any())).thenReturn(Optional.of(event));
        
        // When
        TicketResponse result = ticketService.purchaseTicket(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(event.getAvailableCapacity()).isEqualTo(9);
        verify(ticketRepository).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when event is sold out")
    void shouldThrowException_WhenEventSoldOut() {
        // Given
        Event event = createEventWithCapacity(0);
        when(eventRepository.findById(any())).thenReturn(Optional.of(event));
        
        // When & Then
        assertThrows(InsufficientCapacityException.class, 
            () -> ticketService.purchaseTicket(request));
    }
}
```

### Integration Tests (15 tests)

**Purpose**: Test component interaction with real database

**Tools**: Testcontainers + Spring Boot Test

**Example**:
```java
@SpringBootTest
@Testcontainers
class TicketPurchaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldPreventOverbooking_WhenConcurrentPurchases() throws Exception {
        // Given: Event with 1 ticket
        createEventWithCapacity(1);
        
        // When: 2 users try to buy simultaneously
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        List<ResponseEntity<TicketResponse>> responses = new ArrayList<>();
        
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                latch.countDown();
                latch.await();
                responses.add(purchaseTicket());
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Then: Only 1 purchase succeeds
        long successful = responses.stream()
            .filter(r -> r.getStatusCode() == HttpStatus.CREATED)
            .count();
        
        assertThat(successful).isEqualTo(1);
    }
}
```

### End-to-End Test (1 test)

**Purpose**: Test complete user journey

**Tools**: Selenium WebDriver + Testcontainers

**Scenario**: User registers → logs in → views events → purchases ticket → receives confirmation

---

## 🔐 Security Implementation

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",
    "roles": ["USER"],
    "iat": 1706637600,
    "exp": 1706638500
  }
}
```

### Password Security

- **Algorithm**: BCrypt
- **Cost Factor**: 12 (2^12 iterations)
- **Salt**: Automatic per-password
- **Storage**: Never store plaintext

### CORS Configuration

```yaml
allowed-origins: http://localhost:3000
allowed-methods: GET, POST, PUT, DELETE
allowed-headers: Authorization, Content-Type
allow-credentials: true
max-age: 3600
```

---

## ⚡ Concurrency Control

### Scenario: Last Ticket Race Condition

**Problem**:
```
Time    User A                  User B
────────────────────────────────────────
T1      GET /events/123         
        available: 1            
T2                              GET /events/123
                                available: 1
T3      POST /tickets/purchase  
        ✅ Success              
T4                              POST /tickets/purchase
                                ❌ Should fail (overbooking!)
```

### Solution: Optimistic Locking

```java
@Entity
public class Event {
    @Id
    private UUID id;
    
    @Version  // Hibernate manages this
    private Long version;
    
    private Integer availableCapacity;
}

// Service layer
@Transactional
public TicketResponse purchaseTicket(PurchaseRequest request) {
    Event event = eventRepository.findById(request.eventId())
        .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    
    // Check capacity
    if (event.getAvailableCapacity() <= 0) {
        throw new InsufficientCapacityException("Event is sold out");
    }
    
    // Decrement capacity
    event.setAvailableCapacity(event.getAvailableCapacity() - 1);
    
    // Save will check version - throws OptimisticLockException if changed
    eventRepository.save(event);
    
    // Create ticket
    Ticket ticket = new Ticket(event, participant);
    return ticketRepository.save(ticket);
}
```

**What happens**:
1. User A reads event (version=1, capacity=1)
2. User B reads event (version=1, capacity=1)
3. User A saves → version becomes 2, capacity=0 ✅
4. User B tries to save → version mismatch (expected 1, actual 2) → OptimisticLockException ❌

### Retry Logic (Client-side)

```typescript
async function purchaseTicketWithRetry(
  request: PurchaseRequest,
  maxRetries = 3
): Promise<Ticket> {
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await ticketService.purchase(request);
    } catch (error) {
      if (error.code === 'OPTIMISTIC_LOCK_ERROR' && attempt < maxRetries) {
        // Wait with exponential backoff
        await sleep(100 * Math.pow(2, attempt));
        continue;
      }
      throw error;
    }
  }
}
```

---

## 🤖 AI Chat Support

### Architecture

```
┌─────────────┐      WebSocket      ┌──────────────┐
│   React     │ ◄──────────────────► │  Spring Boot │
│   Client    │                      │   Backend    │
└─────────────┘                      └───────┬──────┘
                                            │
                                            │ HTTP
                                            ▼
                                     ┌──────────────┐
                                     │   Ollama     │
                                     │  (Llama 3.2) │
                                     └──────────────┘
```

### Why Ollama?

- **Privacy**: All data stays local
- **Cost**: No API fees
- **Customization**: Can fine-tune for EventHub domain
- **Performance**: Fast responses on modern hardware

### Model Choice: Llama 3.2 (3B)

- **Size**: 3 billion parameters
- **Memory**: ~2GB RAM
- **Speed**: ~50 tokens/second on CPU
- **Quality**: Sufficient for customer support


<div align="center">

# ⚙️ TastyBite — Backend

**Spring Boot REST API powering the TastyBite restaurant platform.**
Handles authentication, menu management, orders, reservations, offers, activity logging and an AI-powered chat assistant backed by Google Gemini.

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Gemini](https://img.shields.io/badge/Gemini-2.5_Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

[🌐 Live API](https://tastybite-spye.onrender.com/api/health) · [🐛 Report Bug](https://github.com/Subha035/TastyBite-Frontend/issues)

</div>

---

## 📋 Table of Contents

- [✨ Overview](#-overview)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Project Structure](#️-project-structure)
- [🚀 Getting Started](#-getting-started)
- [⚙️ Configuration](#️-configuration)
- [📡 API Reference](#-api-reference)
- [🗄️ Data Models](#️-data-models)
- [🔐 Security & Auth](#-security--auth)
- [🤖 AI Chat Service](#-ai-chat-service)
- [🗃️ Database Layer](#️-database-layer)
- [🐳 Docker](#-docker)
- [📦 Build & Scripts](#-build--scripts)

---

## ✨ Overview

The TastyBite backend is a **Spring Boot 3.4.3** application that:

- Exposes a **RESTful JSON API** consumed by the React frontend
- Uses **Google Cloud Firestore** as its primary NoSQL database (via Firebase Admin SDK)
- Falls back to **in-memory storage** when Firestore credentials are unavailable (useful for local dev without Firebase setup)
- Issues and validates **JWT tokens** for customer authentication
- Verifies **Firebase ID tokens** for Google OAuth logins
- Powers an **AI chat assistant** using Google Gemini 2.5 Flash with a local RAG keyword fallback
- Includes a **Docker-ready build** for cloud deployment (e.g., Render, Railway, Fly.io)

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| **Spring Boot** | 3.4.3 | Core REST framework |
| **Java** | 21 (LTS) | Runtime language |
| **Spring Security** | 6.x | Request authentication, CORS & filter chain |
| **Firebase Admin SDK** | 9.7.0 | Firestore database + Google ID token verification |
| **Google Cloud Firestore** | 3.30.0 | Primary NoSQL cloud database |
| **jjwt (JWT)** | 0.11.5 | JWT generation & validation |
| **BCrypt** | — | Password hashing via Spring Security Crypto |
| **Lombok** | latest | Boilerplate reduction (`@Data`, `@Builder`, etc.) |
| **Spring Validation** | — | Request body validation |
| **Spring DevTools** | — | Hot reload during development |
| **JUnit 5** | — | Unit & integration testing |
| **Gradle** | wrapper | Build tool |

---

## 🏗️ Project Structure

```
Backend/
├── 📁 src/
│   ├── 📁 main/java/com/example/backend/
│   │   ├── 📁 config/                          # Spring configuration beans
│   │   │   ├── FirebaseConfig.java             # Firebase Admin SDK initialization
│   │   │   ├── SecurityConfig.java             # CORS rules + JWT filter chain
│   │   │   ├── JwtUtils.java                   # JWT token generation & validation
│   │   │   ├── JwtAuthenticationFilter.java    # Per-request JWT extraction filter
│   │   │   └── CorsConfig.java                 # Additional CORS configuration
│   │   │
│   │   ├── 📁 controller/                      # REST API layer (thin, delegates to services)
│   │   │   ├── AuthController.java             # POST /api/auth/{signup,login,google}, GET /api/auth/me
│   │   │   ├── MenuController.java             # CRUD /api/menu/**
│   │   │   ├── OrderController.java            # CRUD /api/orders/**
│   │   │   ├── ReservationController.java      # CRUD /api/reservations/**
│   │   │   ├── OfferController.java            # CRUD /api/offers/**
│   │   │   ├── ChatController.java             # POST /api/chat
│   │   │   ├── AdminController.java            # POST /api/admin/login
│   │   │   ├── ActivityController.java         # GET/POST /api/activities
│   │   │   └── HealthController.java           # GET /api/health
│   │   │
│   │   ├── 📁 service/                         # Business logic layer
│   │   │   ├── AuthService.java                # Signup, login, Google OAuth, JWT issuance
│   │   │   ├── FirebaseService.java            # All Firestore CRUD + in-memory fallback + cache
│   │   │   ├── LlmService.java                 # Gemini API calls + RAG keyword fallback
│   │   │   └── ChatService.java                # Chat orchestration (fetches context, calls LLM)
│   │   │
│   │   ├── 📁 model/                           # Plain Java data models (Lombok)
│   │   │   ├── User.java
│   │   │   ├── MenuItem.java
│   │   │   ├── Order.java                      # Includes nested OrderItem
│   │   │   ├── Reservation.java
│   │   │   ├── Offer.java
│   │   │   ├── ChatMessage.java
│   │   │   └── ActivityLog.java
│   │   │
│   │   └── BackendApplication.java             # Spring Boot entry point (@SpringBootApplication)
│   │
│   └── 📁 test/                                # Unit & integration tests
│
├── Dockerfile                                  # Multi-step Docker build
├── build.gradle                                # Gradle dependency declarations
├── settings.gradle                             # Project name config
├── gradlew / gradlew.bat                       # Gradle wrapper scripts
└── firebase-service-account.json              # Firebase credentials (⚠️ gitignored)
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK) — [Download OpenJDK](https://adoptium.net/)
- **Gradle** — bundled via the `./gradlew` wrapper (no global install needed)
- A **Firebase project** with Firestore enabled — [Firebase Console](https://console.firebase.google.com/)
- *(Optional)* A **Google Gemini API key** — [Get one here](https://aistudio.google.com/app/apikey)

### Local Setup

```bash
# 1. Navigate to the Backend directory
cd Backend

# 2. Place your Firebase service account JSON here:
#    Backend/firebase-service-account.json
#    (Download from Firebase Console → Project Settings → Service Accounts)

# 3. Configure application properties (see Configuration section)

# 4. Run the application
./gradlew bootRun
```

The server starts at **http://localhost:8080**

> 💡 **No Firebase?** The server still runs with in-memory fallback storage. All endpoints work — data just resets on restart.

---

## ⚙️ Configuration

Edit `src/main/resources/application.properties` or set these as environment variables:

```properties
# Server
server.port=8080

# Google Gemini AI (optional — RAG fallback used if not set)
genai.api.key=${GENAI_API_KEY:}
genai.model=${GENAI_MODEL:gemini-2.5-flash}

# JWT
jwt.secret=${JWT_SECRET:your-super-secret-key-change-in-production}
jwt.expiration=86400000

# Admin credentials
admin.username=${ADMIN_USERNAME:admin}
admin.password=${ADMIN_PASSWORD:admin123}

# Firebase service account path
firebase.service.account.path=${FIREBASE_SERVICE_ACCOUNT:firebase-service-account.json}
```

### Environment Variables Reference

| Variable | Required | Default | Description |
|---|---|---|---|
| `GENAI_API_KEY` | ❌ Optional | *(empty)* | Google Gemini API key for AI responses |
| `GENAI_MODEL` | ❌ Optional | `gemini-2.5-flash` | Gemini model name |
| `JWT_SECRET` | ✅ Production | — | HS256 signing secret (min 32 chars) |
| `JWT_EXPIRATION` | ❌ Optional | `86400000` | Token TTL in milliseconds (24h default) |
| `ADMIN_USERNAME` | ✅ Production | `admin` | Admin panel login username |
| `ADMIN_PASSWORD` | ✅ Production | `admin123` | Admin panel login password |
| `FIREBASE_SERVICE_ACCOUNT` | ❌ Optional | `firebase-service-account.json` | Path to Firebase credentials JSON |

---

## 📡 API Reference

**Base URL:** `http://localhost:8080/api` (local) · `https://tastybite-spye.onrender.com/api` (production)

---

### 🔑 Auth — `/api/auth`

#### `POST /auth/signup`
Register a new user.

**Request body:**
```json
{ "name": "John Doe", "email": "john@example.com", "password": "secret123" }
```
**Response `200`:**
```json
{ "token": "<JWT>", "user": { "id": "...", "name": "John Doe", "email": "...", "role": "USER", "authProvider": "local" } }
```

---

#### `POST /auth/login`
Login with email and password.

**Request body:**
```json
{ "email": "john@example.com", "password": "secret123" }
```
**Response `200`:** Same as signup.
**Response `401`:** `{ "message": "Invalid email or password." }`

---

#### `POST /auth/google`
Login or register via Google OAuth.

**Request body:**
```json
{ "idToken": "<Firebase ID Token>", "email": "...", "name": "...", "picture": "..." }
```
**Response `200`:** Same as signup.

---

#### `GET /auth/me`
Get the currently authenticated user's profile.

**Headers:** `Authorization: Bearer <JWT>`
**Response `200`:** `{ "user": { ... } }`
**Response `401`:** `{ "message": "Missing token" }`

---

### 🍽️ Menu — `/api/menu`

| Method | Endpoint | Body | Auth | Description |
|---|---|---|---|---|
| `GET` | `/menu` | — | Public | List all menu items |
| `POST` | `/menu` | `MenuItem` JSON | Admin JWT | Create menu item |
| `PUT` | `/menu/{id}` | `MenuItem` JSON | Admin JWT | Update menu item |
| `DELETE` | `/menu/{id}` | — | Admin JWT | Delete menu item |

**MenuItem fields:** `title`, `desc`, `price`, `category`, `imageUrl`, `isVeg`, `available`

---

### 📋 Orders — `/api/orders`

| Method | Endpoint | Body | Auth | Description |
|---|---|---|---|---|
| `GET` | `/orders` | — | Admin JWT | List all orders |
| `POST` | `/orders` | `Order` JSON | JWT | Place a new order |
| `PUT` | `/orders/{id}/status` | `{ "status": "PREPARING" }` | Admin JWT | Update order status |
| `PUT` | `/orders/{id}/payment-status` | `{ "paymentStatus": "PAID" }` | Admin JWT | Update payment status |

**Order status values:** `PENDING` → `PREPARING` → `SERVED` → `COMPLETED` · `CANCELLED`

---

### 🪑 Reservations — `/api/reservations`

| Method | Endpoint | Body | Auth | Description |
|---|---|---|---|---|
| `GET` | `/reservations` | — | Admin JWT | List all reservations |
| `POST` | `/reservations` | `Reservation` JSON | Public | Create reservation |
| `PUT` | `/reservations/{id}/status` | `{ "status": "CONFIRMED" }` | Admin JWT | Approve/reject |
| `DELETE` | `/reservations/{id}` | — | Admin JWT | Delete reservation |

---

### 🎁 Offers — `/api/offers`

| Method | Endpoint | Body | Auth | Description |
|---|---|---|---|---|
| `GET` | `/offers` | — | Public | List all offers |
| `POST` | `/offers` | `Offer` JSON | Admin JWT | Create offer |
| `PUT` | `/offers/{id}` | `Offer` JSON | Admin JWT | Update offer |
| `DELETE` | `/offers/{id}` | — | Admin JWT | Delete offer |

---

### 💬 Chat — `/api/chat`

#### `POST /chat`
Send a message to the AI assistant.

**Request body:**
```json
{ "message": "What veg dishes do you have?" }
```
**Response `200`:**
```json
{ "response": "Here are our vegetarian options: ..." }
```

---

### 🔒 Admin — `/api/admin`

#### `POST /admin/login`
Authenticate as admin.

**Request body:**
```json
{ "username": "admin", "password": "admin123" }
```
**Response `200`:** `{ "token": "<admin-jwt>", "role": "ADMIN" }`
**Response `401`:** `{ "message": "Invalid credentials" }`

---

### 📊 Activities — `/api/activities`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/activities` | Fetch recent activity log entries |
| `POST` | `/activities` | Log a new activity event |

**Activity body:** `{ "title": "New order placed", "category": "order" }`

---

### 🏥 Health — `/api/health`

```
GET /api/health  →  200 OK  →  "TastyBite Backend is running!"
```

---

## 🗄️ Data Models

### `User`
```
id            String    (Firestore document ID)
name          String
email         String    (unique)
passwordHash  String    (BCrypt, null for Google users)
role          String    USER | ADMIN
authProvider  String    local | google
avatar        String    (URL)
createdAt     String
```

### `MenuItem`
```
id          String
title       String
desc        String
price       Double
category    String
imageUrl    String
isVeg       Boolean
available   Boolean
```

### `Order`
```
id              String
orderId         String
items           List<OrderItem>   (id, title, price, quantity, specialNotes)
tableNumber     String
totalAmount     Double
status          String   PENDING | PREPARING | SERVED | COMPLETED | CANCELLED
customerName    String
customerPhone   String
paymentStatus   String
paymentMethod   String
qrCodeUrl       String
createdAt       String
```

### `Reservation`
```
id           String
name         String
email        String
phone        String
date         String
timeSlot     String
guests       Integer
status       String   PENDING | CONFIRMED | REJECTED
createdAt    String
```

### `Offer`
```
id       String
title    String
code     String   (promo code)
desc     String
```

---

## 🔐 Security & Auth

### JWT Authentication Filter

Every request passes through [`JwtAuthenticationFilter`](./src/main/java/com/example/backend/config/JwtAuthenticationFilter.java):

```
Incoming Request
      │
      ▼
Extract "Authorization: Bearer <token>" header
      │
      ├── No token? → Continue as anonymous
      │
      └── Token present? → JwtUtils.validateToken()
              │
              ├── Invalid / Expired → 401 Unauthorized
              │
              └── Valid → Set SecurityContext → Continue
```

### CORS Policy

Configured in [`SecurityConfig`](./src/main/java/com/example/backend/config/SecurityConfig.java):

- **Allowed origins:** `*` (all origins — restrict in production)
- **Allowed methods:** `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`
- **Allowed headers:** `*`
- **Credentials:** `true`

### Password Security

- Passwords are hashed using **BCrypt** via Spring Security's `PasswordEncoder`
- Raw passwords are never stored or logged
- Google OAuth users have no password hash (`null`)

---

## 🤖 AI Chat Service

The chat system is implemented in [`LlmService.java`](./src/main/java/com/example/backend/service/LlmService.java).

### Two-Tier Response Strategy

```
User Message
     │
     ▼
ChatService fetches live Menu + Offers from Firestore
     │
     ▼
LlmService.buildSystemPrompt()
  ┌─ Injects live menu items (name, price, category, veg flag)
  ├─ Injects active offers (title, code, description)
  └─ Adds restaurant hours & booking instructions
     │
     ▼
Tier 1: callGenAiApi() ──► Google Gemini 2.5 Flash
     │                           │
     │   (if key missing or      │
     │    API call fails)        ▼
     │                    Gemini Response ✅
     │
     ▼
Tier 2: generateRagFallbackResponse()
  ┌─ Keyword detection (veg, non-veg, pizza, burger, coffee, dessert...)
  ├─ Searches live Firestore data by title/category/description
  ├─ Handles offers, hours, reservations queries
  └─ Returns formatted, emoji-rich response ✅
```

### Supported Query Types (RAG Fallback)
- Menu item search by name, category or description
- Veg / Non-veg filtering
- Active offers & discount codes
- Restaurant opening hours
- Table reservation instructions

---

## 🗃️ Database Layer

[`FirebaseService`](./src/main/java/com/example/backend/service/FirebaseService.java) is the single data access layer for all Firestore operations.

### Smart Caching

| Collection | Cache TTL | Strategy |
|---|---|---|
| Menu items | 60 seconds | In-memory `CopyOnWriteArrayList` |
| Offers | 60 seconds | In-memory `CopyOnWriteArrayList` |

- Cache is **pre-warmed on startup** via `@PostConstruct` in a background thread — first user request is instant
- **Write-through**: mutations go to Firestore and immediately invalidate the cache

### In-Memory Fallback

When `firebase-service-account.json` is missing or Firestore is unreachable, all operations fall back to thread-safe `ConcurrentHashMap` stores. The server starts seeded with sample menu items and offers — **no crash, no config required for local development**.

### Firestore Collections

| Collection | Document Model |
|---|---|
| `users` | `User` |
| `menu` | `MenuItem` |
| `orders` | `Order` |
| `reservations` | `Reservation` |
| `offers` | `Offer` |
| `activities` | `ActivityLog` |

---

## 🐳 Docker

```dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean bootJar
EXPOSE 8080
CMD ["java", "-jar", "build/libs/Backend-0.0.1-SNAPSHOT.jar"]
```

### Build & Run

```bash
# Build the image
docker build -t tastybite-backend .

# Run with environment variables
docker run -p 8080:8080 \
  -e GENAI_API_KEY=your_gemini_key \
  -e JWT_SECRET=your_jwt_secret_min_32_chars \
  -e ADMIN_USERNAME=myadmin \
  -e ADMIN_PASSWORD=mypassword \
  tastybite-backend
```

> ⚠️ Mount your `firebase-service-account.json` via a volume or inject its content as an environment variable for production deployments.

---

## 📦 Build & Scripts

```bash
# Start the dev server with hot reload
./gradlew bootRun

# Compile and run all tests
./gradlew build

# Build the production fat JAR (→ build/libs/Backend-0.0.1-SNAPSHOT.jar)
./gradlew clean bootJar

# Run tests only
./gradlew test

# Clean build artifacts
./gradlew clean
```

**Java version check:**
```bash
java -version   # Must be 21+
```

---

<div align="center">

**Part of the [TastyBite](https://github.com/Subha035/TastyBite-Frontend) restaurant platform**

⭐ Star the repo if you find it useful!

</div>

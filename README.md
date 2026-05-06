# 🕹️ Enterprise Arcade Leaderboard API

## Architecture Overview
This project is a scalable, stateless backend designed to handle high-traffic arcade leaderboard submissions. It replaces traditional server-side sessions with cryptographic JSON Web Tokens (JWT) for secure, stateless authorization. To protect the PostgreSQL database from heavy read operations during high traffic, the `/top` endpoints are routed through a Redis caching layer using a proxy pattern, ensuring instant response times.

---

## 🛠️ Tech Stack
* **Backend:** Java, Spring Boot, Spring Security
* **Frontend:** React.js, Fetch API
* **Database:** PostgreSQL (Source of Truth)
* **Caching:** Redis (High-Speed Read Layer)
* **Authentication:** JWT (JSON Web Tokens)
* **Infrastructure:** Docker & Docker Compose

---

## 🏗️ Core System Design

### 1. Stateless Security (JWT)
Instead of storing heavy server-side sessions, this API uses a custom Spring Security Filter Chain. 
* When a user logs in, the system verifies their credentials against PostgreSQL and issues a mathematically signed **JWT Bearer Token**.
* For subsequent requests (like adding a high score), a custom `JwtRequestFilter` intercepts the request, verifies the RSA signature and expiration, and temporarily authorizes the thread using `ThreadLocal` via the `SecurityContextHolder`.

### 2. High-Performance Caching (Redis Proxy)
To handle massive spikes in read traffic (e.g., thousands of users viewing the leaderboard simultaneously):
* The `GET /top` endpoint is intercepted by a Spring `@Cacheable` proxy and served directly from **Redis** in milliseconds, completely bypassing the database.
* To prevent stale data, `POST` and `DELETE` operations utilize `@CacheEvict` to instantly wipe the Redis cache the moment the PostgreSQL database is updated.

---

## 🚀 How to Run Locally

This project uses Docker to instantly spin up the database and cache infrastructure. You do not need to install PostgreSQL or Redis on your machine!

### Prerequisites
* Docker Desktop installed and running
* Java 17+ installed
* Node.js installed

### Step-by-Step Setup

**1. Clone the repository**
```bash
git clone [https://github.com/YOUR-USERNAME/arcade-leaderboard-api.git](https://github.com/YOUR-USERNAME/arcade-leaderboard-api.git)
cd arcade-leaderboard-api
```
**2. Start the Infrastructure**

Start the PostgreSQL database and Redis cache in the background:
```bash
docker-compose up -d
```
**3. Start the Spring Boot Backend**

Open the project in your IDE (IntelliJ/VS Code) and run the main application file, or run:
```bash
./mvnw spring-boot:run
```
**4. Start the React Frontend**

Open a new terminal window and navigate to the frontend folder:
```bash
cd frontend-arcade
npm install
npm start
```

The application will now be running securely on http://localhost:5173

---

## 📡 Key API Endpoints

| Method | Endpoint | Description | Auth Required? |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Authenticates user and returns a signed JWT | No |
| `GET` | `/api/players/top` | Fetches the Top 10 high scores (Optimized via Redis) | No |
| `POST` | `/api/players` | Submits a new score to PostgreSQL and evicts stale cache | **Yes (JWT)** |
| `DELETE` | `/api/players/{id}` | Removes a player record (Admin cleanup) | **Yes (JWT)** |

### 🛠️ Example Authenticated Request
To interact with protected endpoints, include the JWT in the Authorization header:
```bash
Authorization: Bearer <your_jwt_token_here>

# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

智慧物业管理平台 - A property management platform with Spring Boot 3 backend and Vue 3 frontend.

## Quick Start Commands

### Local Development Setup

Backend (Spring Boot):
```bash
# Run backend with local MySQL (requires MySQL on localhost:3306)
cd backend
mvn spring-boot:run

# Run backend with skip tests
cd backend
mvn -DskipTests spring-boot:run

# Build backend JAR
cd backend
mvn clean package

# Run specific test class
cd backend
mvn test -Dtest=YourTestClass

# Run tests with coverage
cd backend
mvn clean test jacoco:report
```

Frontend (Vue 3 + Vite):
```bash
# Install dependencies
cd frontend
npm install

# Run dev server (http://localhost:5173)
cd frontend
npm run dev

# Build for production
cd frontend
npm run build

# Preview production build
cd frontend
npm run preview
```

Docker Deployment:
```bash
# Start all services (MySQL, Backend, Frontend)
docker compose up --build

# Start in detached mode
docker compose up -d --build

# Stop all services
docker compose down

# Stop and remove volumes (clean database)
docker compose down -v

# View logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

## Architecture Overview

### Backend Architecture (Spring Boot 3)

The backend follows a layered architecture with clear separation of concerns:

```
backend/src/main/java/com/example/propertymanagement/
├── controller/          # REST endpoints - handles HTTP requests/responses
├── service/            # Business logic layer - orchestrates operations
├── repository/         # Data access layer - JPA repositories  
├── model/             # JPA entities - database table mappings
├── dto/               # Data Transfer Objects - API request/response schemas
│   ├── auth/         # Authentication DTOs (Login, Register, Token)
│   ├── property/     # Property management DTOs
│   ├── lease/        # Lease agreement DTOs
│   ├── payment/      # Payment record DTOs
│   └── maintenance/  # Maintenance request DTOs
├── security/          # JWT authentication and authorization
│   ├── JwtAuthenticationFilter   # Validates tokens from cookies/headers
│   ├── JwtTokenProvider          # Creates and validates JWT tokens
│   └── CustomUserDetailsService  # Loads user from database
├── exception/         # Custom exceptions and global error handling
├── config/           # Application configuration
└── mapper/           # Entity-to-DTO conversion utilities
```

**Key Patterns:**
- **Authentication**: JWT tokens via HttpOnly cookies or Authorization header. Access token expires in 15 minutes, refresh token in 7 days.
- **API Design**: RESTful endpoints under `/api/*` with standard HTTP verbs
- **Pagination**: Spring Data Pageable for all list endpoints
- **Validation**: Jakarta Bean Validation on DTOs
- **Error Handling**: Global exception handler returns consistent error responses
- **Role-Based Access**: Three roles - ADMIN, OWNER, TENANT with different permissions

### Frontend Architecture (Vue 3 + Vite)

```
frontend/src/
├── views/              # Page components (routed views)
│   ├── LoginView.vue        # Authentication pages
│   ├── RegisterView.vue     
│   ├── DashboardView.vue    # Main dashboard
│   ├── PropertiesView.vue   # Property management
│   ├── LeasesView.vue       # Lease management
│   ├── PaymentsView.vue     # Payment tracking
│   └── MaintenanceView.vue  # Maintenance requests
├── api/
│   └── http.js        # Axios instance with auth interceptor
├── stores/
│   └── auth.js        # Pinia store for authentication state
├── router/
│   └── index.js       # Vue Router configuration
└── App.vue            # Root component
```

**Key Patterns:**
- **State Management**: Pinia for global state (auth, user info)
- **API Communication**: Axios with automatic token refresh on 401
- **Routing**: Vue Router with auth guards
- **Development Proxy**: Vite proxy forwards `/api` to `http://localhost:8080`

### Database Schema

The application uses MySQL 8.0 with the following main entities:
- **users**: User accounts with roles (ADMIN/OWNER/TENANT)
- **properties**: Property records with owner relationships
- **leases**: Lease agreements linking properties to tenants
- **payments**: Payment records for rent and other fees
- **maintenance_requests**: Service requests with status tracking

JPA auto-generates schema on startup (`spring.jpa.hibernate.ddl-auto=update`).

## API Endpoints

All endpoints prefixed with `/api`:

**Authentication** (Public):
- `POST /api/auth/register` - Create new account
- `POST /api/auth/login` - Login with credentials
- `POST /api/auth/refresh-token` - Refresh access token
- `POST /api/auth/logout` - Clear auth cookies
- `GET /api/auth/me` - Get current user info

**Protected Endpoints** (Require authentication):
- `GET/POST/PUT/DELETE /api/properties` - Property CRUD
- `GET/POST/PUT/DELETE /api/leases` - Lease management
- `GET/POST /api/payments` - Payment records
- `GET/POST/PUT /api/maintenance-requests` - Maintenance requests

## Development Environment Configuration

### Backend Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/property_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

# JWT
JWT_SECRET=your-secret-key
JWT_ACCESS_TOKEN_EXPIRATION_MINUTES=15
JWT_REFRESH_TOKEN_EXPIRATION_DAYS=7

# CORS
APP_ALLOWED_ORIGINS=http://localhost:5173
```

### Frontend Development
The Vite dev server (port 5173) proxies `/api` requests to `http://localhost:8080`. This is configured in `vite.config.js`.

## Common Development Tasks

### Adding a New API Endpoint
1. Create DTOs in `backend/src/main/java/com/example/propertymanagement/dto/`
2. Add repository interface extending `JpaRepository`
3. Implement service layer with business logic
4. Create controller with REST mappings
5. Update frontend API calls in relevant Vue components

### Debugging Authentication Issues
- Check browser DevTools for cookies: `ACCESS_TOKEN` and `REFRESH_TOKEN`
- Verify JWT secret matches between backend and environment
- Check CORS configuration allows frontend origin
- Review `JwtAuthenticationFilter` logs for token validation

### Database Operations
```bash
# Connect to MySQL container
docker exec -it smart-property-db mysql -u root -p

# Connect to local MySQL
mysql -u root -p property_db
```

## Production Deployment Notes

1. Change JWT_SECRET from default `change-me-in-prod`
2. Configure proper database credentials
3. Update CORS allowed origins for production domain
4. Build frontend with `npm run build` and serve static files
5. Configure HTTPS/TLS for secure cookie transmission
6. Set appropriate JVM heap size for backend container

## Testing Strategy

Currently no automated tests are implemented. When adding tests:

Backend:
- Unit tests: Test services with mocked repositories
- Integration tests: Test controllers with `@SpringBootTest`
- Use `@DataJpaTest` for repository layer

Frontend:
- Component tests with Vue Test Utils
- E2E tests with Cypress or Playwright
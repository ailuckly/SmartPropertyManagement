# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

智慧物业管理平台 (Smart Property Management Platform) - A full-stack property management system built with Spring Boot 3 + Vue 3.

**Tech Stack:**
- Backend: Spring Boot 3 (Java 17), Spring Security, JWT, JPA/Hibernate, MySQL 8.0
- Frontend: Vue 3 (Composition API), Vite, Pinia, Vue Router, Element Plus, ECharts
- Deployment: Docker + Docker Compose + Nginx

**Core Business Modules:**
- User authentication & role-based access control (Admin, Owner, Tenant)
- Property management (CRUD + image upload + batch operations)
- Lease management (lifecycle tracking + expiration alerts)
- Maintenance request system (work orders + status tracking)
- Payment records (rent payment logging & queries)
- Notification system (14 notification types)
- Dashboard (statistics + ECharts visualization)
- Excel export (5 data types)
- File management (upload/preview/delete)

## Development Commands

### Backend (Spring Boot)

**Local development:**
```powershell
cd backend
mvn -DskipTests spring-boot:run
```

**Build JAR:**
```powershell
cd backend
mvn clean package -DskipTests
```

**Run tests:**
```powershell
cd backend
mvn test
```

**Database setup:**
```powershell
# Connect to MySQL
mysql -u root -p

# Execute scripts in order
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/00_db_drop_and_create.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/01_db_schema.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/02_db_init_admin.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/03_db_mock_data.sql
```

**Default backend port:** 8080

### Frontend (Vue 3 + Vite)

**Local development:**
```powershell
cd frontend
npm install
npm run dev
```

**Production build:**
```powershell
cd frontend
npm run build
```

**Preview production build:**
```powershell
cd frontend
npm run preview
```

**Default dev server:** http://localhost:5173 (proxies `/api` to backend)

### Docker Deployment

**Start all services:**
```powershell
docker compose up --build
```

**Stop all services:**
```powershell
docker compose down
```

**View logs:**
```powershell
docker compose logs -f [service_name]
```

**Services after startup:**
- Frontend: http://localhost/
- Backend API: http://localhost:8080/
- MySQL: localhost:3306 (db: `property_db`, user: `property`, password: `property`)

## Architecture

### Backend Architecture

**Package Structure:**
```
com.example.propertymanagement/
├── config/              # Configuration classes (Security, JWT, File storage)
├── controller/          # REST API endpoints (@RestController)
├── dto/                # Data Transfer Objects for API requests/responses
├── exception/          # Custom exceptions & GlobalExceptionHandler
├── mapper/             # Entity ↔ DTO converters
├── model/              # JPA entities (@Entity)
├── repository/         # Spring Data JPA repositories
├── security/           # JWT authentication filter, UserDetailsService
├── service/            # Business logic layer
└── util/               # Utility classes (Cookie, Security helpers)
```

**Key Controllers:**
- `AuthController` - `/api/auth/*` (register, login, refresh, logout, me)
- `PropertyController` - `/api/properties/*` (CRUD with pagination)
- `LeaseController` - `/api/leases/*` (lease lifecycle management)
- `PaymentController` - `/api/payments/*` (rent payment records)
- `MaintenanceRequestController` - `/api/maintenance-requests/*` (work orders)
- `NotificationController` - `/api/notifications/*` (system notifications)
- `UserController` - `/api/users/*` (user management, admin only)
- `FileController` - `/api/files/*` (file upload/download/delete)
- `DashboardController` - `/api/dashboard/*` (statistics)

**Domain Model:**
- `User` - users table with BCrypt password encryption
- `Role` - three roles: ROLE_ADMIN, ROLE_OWNER, ROLE_TENANT
- `Property` - property listings with status (AVAILABLE, LEASED, UNDER_MAINTENANCE)
- `Lease` - rental agreements with status (ACTIVE, EXPIRED, TERMINATED)
- `Payment` - rent payment records with BigDecimal amounts
- `MaintenanceRequest` - maintenance work orders (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- `Notification` - 14 notification types
- `RefreshToken` - JWT refresh token storage

**Database:**
- Database name: `smart_property_system`
- Follows Alibaba MySQL specification:
  - No physical foreign keys (soft references only)
  - Audit fields: `gmt_create`, `gmt_modified`, `is_deleted`
  - Table names are singular
  - Logical deletion via `is_deleted` flag
  - Indexes on foreign key fields and status columns

### Frontend Architecture

**Directory Structure:**
```
frontend/src/
├── api/                # Axios instance & API service modules
├── components/         # Reusable Vue components
├── router/             # Vue Router configuration with guards
├── stores/             # Pinia stores (auth, notifications, etc.)
├── styles/             # Global CSS
├── utils/              # Utility functions
├── views/              # Page components
├── App.vue             # Root component with navigation
└── main.js             # Application entry point
```

**Key Routes:**
- `/` - Dashboard (requires auth)
- `/properties` - Property management (all roles)
- `/maintenance` - Maintenance requests (all roles)
- `/leases` - Lease management (all roles)
- `/payments` - Payment records (admin/owner only)
- `/notifications` - Notification center (requires auth)
- `/users` - User management (admin only)
- `/profile` - User profile (requires auth)
- `/login` - Login page (guest only)
- `/register` - Registration page (guest only)

**State Management (Pinia):**
- `auth` store: manages user session, roles, and authentication state
- Role checking: `hasRole()`, `hasAnyRole()`, `isAdmin()`, `isOwner()`, `isTenant()`

**API Integration:**
- Axios instance with `withCredentials: true` for cookie-based JWT
- Automatic 401 handling with token refresh via `/api/auth/refresh-token`
- Request retry after successful token refresh

### Security Architecture

**Authentication Flow:**
1. User logs in via `/api/auth/login`
2. Backend generates JWT access token (15min expiry) and refresh token (7 days)
3. Tokens stored in HttpOnly cookies (`ACCESS_TOKEN`, `REFRESH_TOKEN`)
4. All requests include cookies automatically
5. On 401, frontend attempts token refresh, then retries original request
6. On refresh failure, user is redirected to login

**Authorization:**
- `JwtAuthenticationFilter` extracts JWT from `Authorization: Bearer` header or cookies
- `SecurityConfig` enforces role-based access control
- Controllers use `@PreAuthorize` for method-level security
- Frontend router guards enforce client-side role checks

**Key Security Classes:**
- `SecurityConfig` - Security filter chain, CORS, stateless session
- `JwtTokenProvider` - JWT generation and validation
- `JwtAuthenticationFilter` - Request interceptor for JWT extraction
- `CustomUserDetailsService` - Load user by username
- `UserPrincipal` - User authentication principal

**Configuration Properties:**
- `JWT_SECRET` - JWT signing key (default: `change-me`, **MUST** change in production)
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES` - Access token TTL (default: 15)
- `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` - Refresh token TTL (default: 7)
- `APP_ALLOWED_ORIGINS` - CORS whitelist (default: `http://localhost:5173`)

## Important Patterns & Conventions

### Backend Patterns

**Layered Architecture:**
```
Controller → Service → Repository → Database
     ↓          ↓
    DTO ←─── Mapper ←─── Entity
```

**Error Handling:**
- Use `@RestControllerAdvice` via `GlobalExceptionHandler`
- Custom exceptions extend `RuntimeException`
- Return consistent error response format

**DTO Usage:**
- Never expose entities directly in API responses
- Use mappers to convert: Entity ↔ DTO
- Request DTOs have `@Valid` annotations for Bean Validation

**Pagination:**
- Use `PageRequest.of(page, size, Sort.by(...))`
- Return `PageResponse<T>` with content, pagination metadata
- Default page size: typically 10-20

**File Upload:**
- Files stored in `uploads/` directory
- Subdirectories: `properties/`, `contracts/`, `maintenance/`
- Max file size: 10MB (configurable via `file.storage.max-file-size`)
- Images limited to 5MB

**Transaction Management:**
- Service methods that modify multiple entities should be `@Transactional`
- Read-only queries can use `@Transactional(readOnly = true)`

### Frontend Patterns

**Component Composition:**
- Use Composition API with `<script setup>`
- Extract reusable logic to composables
- Props validation with TypeScript-style comments

**State Management:**
- Use Pinia stores for cross-component state
- Keep component local state in `ref()` or `reactive()`
- Computed properties via `computed()`

**API Calls:**
- Import services from `@/api/` modules
- Handle loading states with reactive flags
- Show user feedback via Element Plus notifications

**Router Guards:**
- `beforeEach` checks authentication status
- `meta.requiresAuth` for protected routes
- `meta.roles` for role-specific routes
- `meta.guestOnly` for login/register pages

**Form Handling:**
- Use Element Plus form components with validation rules
- Call `formRef.value.validate()` before submission
- Reset forms with `formRef.value.resetFields()`

## Common Development Tasks

### Adding a New Entity

1. Create JPA entity in `backend/.../model/`
2. Create repository interface in `backend/.../repository/`
3. Create DTO classes in `backend/.../dto/`
4. Create mapper in `backend/.../mapper/`
5. Create service in `backend/.../service/`
6. Create controller in `backend/.../controller/`
7. Update database schema in `docs/database/01_db_schema.sql`
8. Add frontend API service in `frontend/src/api/`
9. Create Vue view in `frontend/src/views/`
10. Add route in `frontend/src/router/index.js`

### Testing Backend Endpoints

PowerShell test scripts are available in `backend/`:
- `test_file_upload.ps1` - File upload testing
- `test_local_upload.ps1` - Local file operations
- `test_preview.ps1` - File preview functionality

Example manual test:
```powershell
# Login
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}' `
  -SessionVariable session

# Make authenticated request
Invoke-WebRequest -Uri "http://localhost:8080/api/properties" `
  -Method GET `
  -WebSession $session
```

### Debugging Tips

**Backend:**
- Enable SQL logging: `spring.jpa.show-sql=true` (already enabled)
- Check console for JWT validation errors
- Use `@Slf4j` + `log.debug()` for detailed logging
- Verify database connection in `application.properties`

**Frontend:**
- Check browser DevTools console for errors
- Network tab shows API requests/responses
- Vue DevTools extension for component inspection
- Pinia DevTools for state inspection

**Common Issues:**
- **CORS errors**: Verify `APP_ALLOWED_ORIGINS` matches frontend URL exactly
- **401 loops**: Check JWT secret consistency, cookie domain settings
- **Database connection**: Ensure MySQL is running, credentials are correct
- **File upload fails**: Check `uploads/` directory exists with write permissions

## Configuration

### Environment Variables

**Backend (production overrides):**
- `SPRING_DATASOURCE_URL` - Database connection string
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing key (**CRITICAL**: change from default)
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES` - Access token TTL
- `JWT_REFRESH_TOKEN_EXPIRATION_DAYS` - Refresh token TTL
- `APP_ALLOWED_ORIGINS` - CORS whitelist (comma-separated)

**Frontend:**
- Backend URL configured in `vite.config.js` proxy settings for dev
- Production build expects backend at same origin or `/api` path

### Default Test Accounts

After running database initialization scripts:

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Owner | owner_wang | admin123 |
| Owner | owner_li | admin123 |
| Owner | owner_zhang | admin123 |
| Tenant | tenant_zhao | admin123 |
| Tenant | tenant_chen | admin123 |
| (more tenants...) | tenant_* | admin123 |

## Production Deployment Checklist

- [ ] Change `JWT_SECRET` to a strong random value
- [ ] Update `MYSQL_ROOT_PASSWORD` in docker-compose.yml
- [ ] Configure `APP_ALLOWED_ORIGINS` with production domain
- [ ] Remove or secure database mock data scripts
- [ ] Enable HTTPS (update nginx.conf, add SSL certificates)
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`)
- [ ] Configure proper file backup for `uploads/` directory
- [ ] Set up database backups
- [ ] Review and restrict exposed ports in docker-compose.yml
- [ ] Enable production logging configuration
- [ ] Set up monitoring and alerting

## Additional Documentation

- `README.md` - Project overview and quick start
- `docs/database/README.md` - Database schema documentation
- `docs/system/module_overview.md` - Detailed module descriptions
- `docs/frontend/frontend-development-guide.md` - Frontend development guide
- `docs/CHANGES_V2.0.md` - Version 2.0 changelog
- `frontend/README_FILE_UPLOAD.md` - File upload feature documentation

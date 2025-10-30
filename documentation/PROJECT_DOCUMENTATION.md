# AgriBackup Project Documentation

## Overview

AgriBackup is a comprehensive agricultural management platform designed to connect farmers, exporters, and agricultural stakeholders in a streamlined digital ecosystem. The platform facilitates produce listing, pickup scheduling, zone management, and export verification processes.

## Architecture

### System Components

1. **Frontend (farmer-portal-frontend)**
   - Vue.js 2.x application with Vuetify UI framework
   - Progressive Web App (PWA) capabilities
   - Responsive design for mobile and desktop

2. **Backend (farmers-portal-apis)**
   - Kotlin/Spring Boot microservices architecture
   - RESTful APIs with OpenAPI/Swagger documentation
   - JPA/Hibernate for data persistence

3. **Infrastructure**
   - AWS S3 for file storage
   - JWT-based authentication
   - Liquibase for database migrations

## Technology Stack

### Frontend
- **Framework**: Vue.js 2.x
- **UI Library**: Vuetify
- **Build Tool**: Vue CLI
- **State Management**: Vuex
- **HTTP Client**: Axios
- **Routing**: Vue Router
- **PWA**: Workbox

### Backend
- **Language**: Kotlin
- **Framework**: Spring Boot 3.x
- **Database**: MySQL/PostgreSQL
- **ORM**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **Documentation**: OpenAPI 3.0
- **Migration**: Liquibase
- **File Storage**: AWS S3

### DevOps & Tools
- **Build**: Maven (Backend), npm/yarn (Frontend)
- **Containerization**: Docker
- **Version Control**: Git
- **CI/CD**: GitHub Actions (planned)
- **Monitoring**: Spring Boot Actuator

## User Roles & Permissions

### 1. Farmer
- Register and manage farm profile
- List produce for sale
- Track pickup schedules
- View transaction history
- Receive notifications

### 2. Exporter
- Manage export operations
- Verify export licenses
- Oversee zone supervisors
- Approve farmer registrations
- Manage pickup routes

### 3. Zone Supervisor
- Manage assigned zones
- Schedule farmer pickups
- Monitor zone performance
- Report issues

### 4. System Admin
- System-wide administration
- User management
- Analytics and reporting
- System configuration

## Key Features

### Core Functionality

#### 1. User Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Multi-tenant architecture
- Secure password management

#### 2. Farmer Management
- Farmer registration and profiling
- Farm location mapping
- Produce inventory management
- Harvest prediction integration

#### 3. Produce Listing & Trading
- Digital marketplace for agricultural produce
- Real-time pricing
- Quality verification
- Transaction management

#### 4. Export License Verification
- Two-step verification process
- Document upload (PDF/JPG/PNG)
- AWS S3 storage integration
- Admin review workflow
- License ID is now optional (nullable in database)

#### 5. Zone & Route Management
- Geographic zone definition
- Farmer-zone assignments
- Optimized pickup routing
- Real-time tracking

#### 6. Dashboard & Analytics
- Role-specific dashboards
- Performance metrics
- Data visualization
- Export capabilities

### Recent Implementations

#### Export License Document Upload
- **Status**: ✅ Completed
- **Features**:
  - File validation (size <10MB, formats: PDF/JPG/PNG)
  - S3 storage with organized prefixes
  - Status workflow: PENDING → UNDER_REVIEW → VERIFIED
  - User-friendly progress indicators

#### Verification Workflow
- **Status**: ✅ Completed
- **Flow**:
  1. Initial registration (no license required)
  2. Post-login verification completion
  3. Document submission and review
  4. Admin verification and approval

## Database Schema

### Core Entities

#### Users & Authentication
- `user_profiles`: Base user information
- `roles`: System roles (FARMER, EXPORTER, ZONE_SUPERVISOR, SYSTEM_ADMIN)
- `permissions`: Granular permissions
- `role_permissions`: Role-permission mappings

#### Agricultural Management
- `farmers`: Farmer profiles and farm details
- `exporters`: Exporter company information (license_id nullable)
- `zones`: Geographic operational zones
- `farmer_exporter_relationships`: Farmer-zone assignments

#### Produce & Trading
- `produce_listings`: Available produce for sale
- `pickup_schedules`: Scheduled farmer pickups
- `pickup_routes`: Optimized delivery routes
- `pickup_route_stops`: Individual stops in routes

#### Administrative
- `system_admins`: System administrator profiles
- `zone_supervisors`: Zone management personnel

### Key Relationships
- Exporter → Zones (1:many)
- Zone → Farmers (many:many via relationships)
- Farmer → Produce Listings (1:many)
- Zone Supervisor → Zones (many:many)

## API Architecture

### Base URL Structure
```
/api/{service-name}/{resource}
```

### Service Endpoints

#### Authentication Service
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Token refresh

#### Farmer Service
- `GET /api/farmer-service/farmers` - List farmers
- `POST /api/farmer-service/farmers` - Register farmer
- `PUT /api/farmer-service/farmers/{id}` - Update farmer
- `POST /api/farmer-service/produce` - Create produce listing

#### Exporter Service
- `GET /api/exporters-service/exporter/{id}` - Get exporter details
- `PUT /api/exporters-service/exporter/{id}` - Update exporter
- `POST /api/exporters-service/exporter/submit-license-document` - Submit license + document
- `PUT /api/exporters-service/exporter/{id}/verify` - Verify exporter

#### Admin Service
- `GET /api/admin-service/zones` - List zones
- `POST /api/admin-service/zones` - Create zone
- `GET /api/admin-service/system-admins` - List system admins

#### Zone Supervisor Service
- `GET /api/zone-supervisor-service/farmers` - List farmers in zones
- `POST /api/zone-supervisor-service/pickups` - Schedule pickup

### Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully"
}
```

## File Storage Architecture

### AWS S3 Organization
```
/produce-images/{uuid}-{filename}     # Farmer produce photos
/license-documents/{uuid}-{filename}  # Export license documents
```

### Upload Validation
- **Max Size**: 10MB per file
- **Allowed Types**: PDF, JPG, JPEG, PNG
- **Naming**: UUID prefix for uniqueness
- **Access**: Private with signed URLs

## Development Setup

### Prerequisites
- Java 17+
- Node.js 16+
- MySQL/PostgreSQL
- AWS Account (for S3)
- Docker (optional)

### Backend Setup
```bash
cd farmers-portal-apis
./mvnw clean install
./mvnw spring-boot:run
```

### Frontend Setup
```bash
cd farmer-portal-frontend
npm install
npm run serve
```

### Environment Configuration
```yaml
# application.yml
aws:
  accessKey: ${AWS_ACCESS_KEY}
  secretKey: ${AWS_SECRET_KEY}
  region: ${AWS_REGION}
  bucketName: ${S3_BUCKET_NAME}

database:
  url: ${DB_URL}
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}
```

## Deployment Architecture

### Development Environment
- Local development with H2/in-memory DB
- Hot reload for frontend
- Live reload for backend

### Production Environment
- Docker containerization
- AWS ECS/Kubernetes orchestration
- RDS for database
- CloudFront CDN
- S3 for static assets

## Security Considerations

### Authentication
- JWT tokens with expiration
- Refresh token rotation
- Password hashing with BCrypt

### Authorization
- Method-level security
- Role-based permissions
- API rate limiting

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- File upload security

## Testing Strategy

### Unit Tests
- Service layer testing
- Repository testing
- Utility function testing

### Integration Tests
- API endpoint testing
- Database integration
- External service mocking

### E2E Tests
- User workflow testing
- Critical path validation
- Cross-browser testing

## Monitoring & Logging

### Application Metrics
- Spring Boot Actuator endpoints
- Custom business metrics
- Performance monitoring

### Logging
- Structured logging with SLF4J
- Log aggregation (ELK stack planned)
- Error tracking and alerting

## Current Development Status

### ✅ Completed Features
- [x] User authentication and role management
- [x] Basic farmer and exporter registration
- [x] Export license document upload
- [x] Verification workflow (PENDING → UNDER_REVIEW → VERIFIED)
- [x] Dashboard layouts for different roles
- [x] Zone management system
- [x] Basic produce listing functionality

### 🚧 In Progress
- [ ] Advanced analytics dashboard
- [ ] Real-time notifications
- [ ] Mobile app optimization
- [ ] Payment integration

### 📋 Planned Features
- [ ] AI-powered harvest prediction
- [ ] Advanced routing optimization
- [ ] Multi-language support
- [ ] Advanced reporting system
- [ ] Integration with agricultural APIs

## Contributing Guidelines

### Code Standards
- Kotlin: Follow official Kotlin coding conventions
- Vue.js: Follow Vue.js style guide
- Commit messages: Use conventional commits
- Branch naming: feature/, bugfix/, hotfix/

### Pull Request Process
1. Create feature branch from `dev`
2. Implement changes with tests
3. Update documentation
4. Create PR with detailed description
5. Code review and approval
6. Merge to `dev` branch

## Support & Contact

### Development Team
- **Project Lead**: [Name]
- **Backend Lead**: [Name]
- **Frontend Lead**: [Name]
- **DevOps**: [Name]

### Documentation Updates
This document should be updated whenever:
- New features are implemented
- Architecture changes occur
- Dependencies are updated
- Security policies change
- Deployment procedures change

---

**Last Updated**: October 16, 2025
**Version**: 1.0.0
**Status**: Active Development
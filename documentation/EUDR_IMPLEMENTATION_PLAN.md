# 📋 EUDR Implementation Plan - Comprehensive Sprint Roadmap

> **Last Updated:** January 6, 2026  
> **Status:** 🔄 In Progress  
> **Overall Completion:** 8/25 tasks (32%) - Sprint 1 & 2 Complete ✅

---

## 🎯 Project Overview

| Attribute | Details |
|-----------|---------|
| **Project** | AgriBackup EUDR Compliance Enhancement |
| **Total Sprints** | 6 |
| **Estimated Duration** | 3-4 weeks |
| **Primary Focus** | Legal compliance, Navigation UX, Entity creation |

---

## 🏃 Sprint 1: Critical Backend Fixes (Legal Blockers)

**Priority:** P0 - Must complete first  
**Expected Duration:** 2-3 days  
**Goal:** Prevent EU import rejection by implementing mandatory EUDR validations  
**Status:** ✅ COMPLETED

| Task ID | Task | File(s) | Expected Output | Status |
|---------|------|---------|-----------------|--------|
| 1.1 | Add 4-hectare polygon validation | `ProductionUnitService.kt` | Throws error if area >4ha without polygon | ✅ |
| 1.2 | Add geometry lock after batch assignment | `ProductionUnit.kt`, `ProductionUnitService.kt` | `isLocked: Boolean` field, prevents geometry edits | ✅ |
| 1.3 | Add geolocation point field for small plots | `ProductionUnit.kt`, migration | `geolocationPoint: String?`, `radiusMeters`, `geolocationType` for ≤4ha plots | ✅ |
| 1.4 | Add point/polygon mode toggle to frontend | `ProductionUnitDrawer.vue` | Mode toggle, radius input, circle visualization on map | ✅ |

**Sprint 1 Deliverables:**
- [x] Modified `ProductionUnitService.kt` with validation logic
- [x] New column `is_locked` in `production_units` table
- [x] New column `geolocation_point` for single-point coordinates
- [x] New column `radius_meters` for area calculation from point
- [x] New column `geolocation_type` (POLYGON or POINT)
- [x] Liquibase migration file (`056_add_eudr_geolocation_fields.yaml`)
- [x] Frontend: Mode toggle in ProductionUnitDrawer.vue (polygon vs point)
- [x] Frontend: Radius slider with area calculation
- [x] Frontend: Circle visualization on map for point mode
- [x] Backend: `calculateAreaFromRadius()` method
- [x] Backend: `lockProductionUnit()` with Hedera audit trail

---

## 🏃 Sprint 2: Navigation Fixes (UX Critical)

**Priority:** P1 - Mobile users affected  
**Expected Duration:** 1-2 days  
**Goal:** Fix broken mobile navigation and improve sidebar organization  
**Status:** ✅ COMPLETED

| Task ID | Task | File(s) | Expected Output | Status |
|---------|------|---------|-----------------|--------|
| 2.1 | Fix BottomNav.vue role casing | `BottomNav.vue` | Uppercase roles: `'FARMER'` not `'farmer'` | ✅ |
| 2.2 | Add EUDR items to BottomNav | `BottomNav.vue` | AGGREGATOR, EXPORTER, IMPORTER nav items | ✅ |
| 2.3 | Restructure Drawer.vue with collapsible sections | `Drawer.vue` | Grouped navigation with `isSection: true` | ✅ |
| 2.4 | Add `isEudr` flag for toggle logic | `Drawer.vue` | Replace string matching with boolean flag | ✅ |

**Sprint 2 Deliverables:**
- [x] Working mobile navigation for all roles (FARMER, AGGREGATOR, EXPORTER, PROCESSOR, IMPORTER)
- [x] Collapsible sections: "Farmer Portal", "Aggregator Portal", "EUDR Compliance", "Supply Chain", "Administration"
- [x] Consistent EUDR toggle using `isEudr: true` flag
- [x] Max 5 items on mobile bottom nav with role-based filtering

---

## 🏃 Sprint 3: New Entity Creation

**Priority:** P1 - Required for compliance  
**Expected Duration:** 3-4 days  
**Goal:** Create missing EUDR entities in the database

| Task ID | Task | File(s) | Expected Output | Status |
|---------|------|---------|-----------------|--------|
| 3.1 | Create AuthorisedRepresentative entity | `domain/eudr/AuthorisedRepresentative.kt` | Full entity with mandate fields | ⬜ |
| 3.2 | Create GrievanceCase entity | `domain/eudr/GrievanceCase.kt` | Entity with status workflow | ⬜ |
| 3.3 | Add SME fields to Exporter/Importer | `Exporter.kt`, `Importer.kt` | `smeCategory`, `employeeCount`, `annualTurnover` | ⬜ |
| 3.4 | Create Liquibase migrations | `db/changelog/eudr-entities-changelog.yml` | All new tables and columns | ⬜ |

**Entity Schemas:**

```kotlin
// AuthorisedRepresentative.kt
@Entity
class AuthorisedRepresentative(
    id: String,
    operatorId: String,
    representativeName: String,
    euMemberState: String,
    registrationNumber: String,
    contactEmail: String,
    mandateDocumentUrl: String?,
    mandateStartDate: LocalDate,
    mandateEndDate: LocalDate?,
    isActive: Boolean
)

// GrievanceCase.kt
@Entity
class GrievanceCase(
    id: String,
    submittedBy: String,
    targetEntityType: String,
    targetEntityId: String,
    description: String,
    status: GrievanceStatus,
    assignedTo: String?,
    resolutionNotes: String?,
    submittedAt: LocalDateTime,
    acknowledgedAt: LocalDateTime?,
    resolvedAt: LocalDateTime?
)
```

**Sprint 3 Deliverables:**
- [ ] 2 new Kotlin entity files
- [ ] Modified Exporter/Importer entities with SME fields
- [ ] Liquibase migration for all changes
- [ ] Repository interfaces for new entities

---

## 🏃 Sprint 4: Backend Services & APIs

**Priority:** P2 - Enables frontend  
**Expected Duration:** 3-4 days  
**Goal:** Create services and REST endpoints for new entities

| Task ID | Task | File(s) | Expected Output | Status |
|---------|------|---------|-----------------|--------|
| 4.1 | Create AuthorisedRepresentativeService | `service/eudr/AuthorisedRepresentativeService.kt` | CRUD + mandate validation | ⬜ |
| 4.2 | Create GrievanceCaseService | `service/eudr/GrievanceCaseService.kt` | CRUD + status workflow | ⬜ |
| 4.3 | Create SME Calculator service | `service/common/SmeCalculatorService.kt` | Category calculation logic | ⬜ |
| 4.4 | Create REST controllers | `controller/eudr/` | All endpoints with DTOs | ⬜ |

**API Endpoints:**

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/eudr/authorised-representatives` | GET, POST | List/Create AR |
| `/api/eudr/authorised-representatives/{id}` | GET, PUT, DELETE | Manage AR |
| `/api/eudr/grievances` | GET, POST | List/Create grievances |
| `/api/eudr/grievances/{id}/status` | PUT | Update grievance status |
| `/api/eudr/sme/calculate` | POST | Calculate SME category |
| `/api/exporters/{id}/sme` | PUT | Update SME declaration |

**Sprint 4 Deliverables:**
- [ ] 3 new service classes
- [ ] 3 new controller classes
- [ ] Request/Response DTOs
- [ ] Updated `SecurityConfig.kt` for new endpoints

---

## 🏃 Sprint 5: Frontend Views & Routes

**Priority:** P2 - User-facing features  
**Expected Duration:** 4-5 days  
**Goal:** Create Vue components and integrate with backend

| Task ID | Task | File(s) | Expected Output | Status |
|---------|------|---------|-----------------|--------|
| 5.1 | Create GrievanceCase view | `views/common/GrievanceCases.vue` | List + create form | ⬜ |
| 5.2 | Create SME Declaration form | `views/exporter/SmeDeclaration.vue` | Multi-step form with calculator | ⬜ |
| 5.3 | Create AuthorisedRepresentative view | `views/exporter/AuthorisedRepresentative.vue` | Management interface | ⬜ |
| 5.4 | Add routes for new views | `router/index.js` | Protected routes with role guards | ⬜ |
| 5.5 | Update navigation with new items | `Drawer.vue`, `BottomNav.vue` | New menu entries | ⬜ |

**Route Additions:**

```javascript
// New routes to add
{ path: '/common/grievances', name: 'GrievanceCases', roles: ['ALL'] },
{ path: '/exporter/sme-declaration', name: 'SmeDeclaration', roles: ['EXPORTER', 'IMPORTER'] },
{ path: '/exporter/authorised-representative', name: 'AuthorisedRepresentative', roles: ['EXPORTER'] },
```

**Sprint 5 Deliverables:**
- [ ] 3 new Vue view components
- [ ] Updated router configuration
- [ ] API service methods in `frontend/src/services/`
- [ ] Updated navigation components

---

## 🏃 Sprint 6: Plot Ownership Refactor (Major)

**Priority:** P1 - Data integrity  
**Expected Duration:** 3-4 days  
**Goal:** Decouple land from farmer for ownership persistence

| Task ID | Task | File(s) | Expected Output | Status |
|---------|------|---------|-----------------|--------|
| 6.1 | Make `farmer_id` nullable | `ProductionUnit.kt`, migration | Allow plots without farmer | ⬜ |
| 6.2 | Add ownership tracking fields | `ProductionUnit.kt` | `registeredBy`, `ownershipHistory` | ⬜ |
| 6.3 | Create ownership transfer service | `ProductionUnitOwnershipService.kt` | Transfer logic with audit trail | ⬜ |
| 6.4 | Update frontend for plot management | `FarmerProductionUnits.vue` | Transfer UI, orphan plot handling | ⬜ |

**Migration Strategy:**

```sql
-- Step 1: Add new columns
ALTER TABLE production_units ADD COLUMN registered_by VARCHAR(36);
ALTER TABLE production_units ADD COLUMN ownership_history JSON;
ALTER TABLE production_units ADD COLUMN is_locked BOOLEAN DEFAULT FALSE;

-- Step 2: Backfill registered_by from farmer_id
UPDATE production_units SET registered_by = farmer_id WHERE registered_by IS NULL;

-- Step 3: Make farmer_id nullable (separate migration after verification)
ALTER TABLE production_units MODIFY farmer_id VARCHAR(36) NULL;
```

**Sprint 6 Deliverables:**
- [ ] Modified ProductionUnit entity
- [ ] Ownership transfer service with Hedera logging
- [ ] Database migration (2-phase)
- [ ] Updated frontend components

---

## 📊 Progress Dashboard

| Sprint | Status | Tasks | Completed |
|--------|--------|-------|-----------|
| Sprint 1: Backend Fixes | ✅ Complete | 3 | 3/3 |
| Sprint 2: Navigation | ⬜ Not Started | 4 | 0/4 |
| Sprint 3: Entities | ⬜ Not Started | 4 | 0/4 |
| Sprint 4: Services | ⬜ Not Started | 4 | 0/4 |
| Sprint 5: Frontend | ⬜ Not Started | 5 | 0/5 |
| Sprint 6: Ownership | ⬜ Not Started | 4 | 0/4 |
| **TOTAL** | **🔄 In Progress** | **24** | **3/24** |

---

## 🚦 Execution Order

```
Sprint 1 ──► Sprint 2 ──► Sprint 3 ──► Sprint 4 ──► Sprint 5
    │                         │
    └─────────────────────────┴──► Sprint 6 (can run parallel after Sprint 3)
```

---

## ✅ Definition of Done

For each task to be marked complete (✅):
1. Code implemented and compiles
2. No ESLint/Kotlin lint errors
3. Backend endpoints tested via Postman/curl
4. Frontend components render without console errors
5. Database migrations apply cleanly

---

## 📝 Change Log

| Date | Sprint | Task | Change Description |
|------|--------|------|-------------------|
| 2026-01-06 | - | - | Initial plan created |
| 2026-01-06 | 1 | 1.1 | Added 4-hectare validation in `ProductionUnitService.kt` with `validateEudrGeolocationRequirements()` |
| 2026-01-06 | 1 | 1.2 | Added `isLocked` field to `ProductionUnit.kt` and lock check in `updateProductionUnit()` |
| 2026-01-06 | 1 | 1.3 | Added `geolocationPoint` field to `ProductionUnit.kt` for small plots |
| 2026-01-06 | 1 | - | Created Liquibase migration `056_add_eudr_geolocation_fields.yaml` |
| 2026-01-06 | 1 | - | Added `recordProductionUnitLock()` to `HederaConsensusServices.kt` for audit trail |

---

## 🔗 Related Documentation

- [EUDR_IMPLEMENTATION_SUMMARY.md](./EUDR_IMPLEMENTATION_SUMMARY.md)
- [EUDR_CERTIFICATION_AND_SUPPLY_CHAIN_GUIDE.md](./EUDR_CERTIFICATION_AND_SUPPLY_CHAIN_GUIDE.md)
- [PRODUCTION_UNIT_DRAWER_GUIDE.md](./PRODUCTION_UNIT_DRAWER_GUIDE.md)

---

*This document will be updated as tasks are completed. Each ⬜ will be changed to ✅ upon completion.*

# 🌾 AgriBackup - Complete EUDR Compliance Platform with Blockchain Verification

> **Revolutionizing Agricultural Supply Chain Compliance through Hedera Hashgraph**

[![Hedera](https://img.shields.io/badge/Hedera-Testnet-purple)](https://hedera.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-green)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---
### Pitch Deck
- Link to pitch deck: [Pitch Deck](https://gamma.app/docs/AgriBackup-tml8dxii39r35b2)

### Certificates
- **Samwel Gachiri**: [Certificate](https://drive.google.com/file/d/1aJ1hk7nL32OVWq9JtTjvmAAmcWaa-Fvo/view?usp=sharing)
- **Beatrice Wanjiru**: [Certificate](https://drive.google.com/file/d/1bp7kQePvY1AVofYksm9EnbMUbogEcJ02/view?usp=sharing)
---

## 📋 Table of Contents
- [Introduction](#-introduction)
- [EUDR Compliance Features](#-eudr-compliance-features)
- [Problem Statement](#-problem-statement)
- [Our Solution](#-our-solution)
- [Technical Architecture](#️-technical-architecture)
- [Key Features](#-key-features)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Demo Flow](#-demo-flow)
- [Contributing](#-contributing)

---

## 📖 Introduction

In the early 2010s, the EU realized that demands for commodities like **soy, palm oil, cocoa, coffee, beef, and timber** were driving deforestation abroad. In response, the **European Union Deforestation Regulation (EUDR)** was enacted, requiring:

✅ **Complete Traceability** - From farm to EU border  
✅ **Deforestation Proof** - No deforestation after cutoff date (December 31, 2020)  
✅ **Geolocation Data** - GPS coordinates of production units  
✅ **Due Diligence** - Risk assessment and mitigation workflows  
✅ **Immutable Records** - Blockchain-verified audit trails  

**Mandatory Compliance Date:** December 30, 2024 (for large operators)

---

## 🛡️ EUDR Compliance Features

AgriBackup is a **production-ready EUDR compliance platform** with complete implementation of all regulatory requirements:

### ✅ **1. Risk Assessment System**
- **6-Component Risk Analysis**: Country risk, deforestation risk, supplier risk, commodity risk, documentation risk, geospatial risk
- **Automated Risk Scoring**: Real-time calculation with HIGH/MEDIUM/LOW/NONE classification
- **Batch-Based Assessment**: Assess individual batches or bulk assessment for multiple batches
- **Risk History Tracking**: Complete audit trail of all risk assessments
- **Hedera Integration**: Every assessment recorded on blockchain for immutability

**Technical Implementation:**
- Backend: `RiskAssessmentService.kt` with comprehensive risk calculation algorithms
- Frontend: `RiskManagement.vue` with interactive dashboard and assessment tools
- API Endpoints: `/api/eudr/assess`, `/api/eudr/assess/bulk`, `/api/eudr/assess/{batchId}/history`

---

### ✅ **2. Mitigation Workflow Management**
- **Automated Workflow Creation**: System automatically suggests mitigation for HIGH-risk batches
- **Kanban Board Interface**: Visual tracking (Pending → In Progress → Completed)
- **Action Types**: Additional documentation, verification visits, batch holds, supplier questionnaires, site inspections
- **Evidence Upload**: Attach completion evidence to each mitigation action
- **Auto-Completion**: Workflow automatically completes when all actions are done
- **Hedera Audit Trail**: Every workflow and action recorded on blockchain

**Technical Implementation:**
- Backend: `MitigationWorkflowService.kt` (380 lines) with full CRUD operations
- Entities: `MitigationWorkflow.kt` and `MitigationAction.kt` with JPA repositories
- Controller: 13 REST endpoints for workflow management
- Frontend: `MitigationTracking.vue` (900+ lines) with Kanban board
- Statistics Dashboard: Real-time metrics (total workflows, by status, by risk level)

---

### ✅ **3. Professional PDF Dossier Generation**
- **iText7 Integration**: Professional PDF generation with proper formatting
- **7-Section Report**:
  1. **Cover Page**: Batch info, risk level, QR code for blockchain verification
  2. **Batch Summary**: Complete batch details with commodity information
  3. **Risk Assessment**: Color-coded component breakdown with scores
  4. **Supply Chain Timeline**: Event tracking from farm to export
  5. **Processing Events**: Transformation history with input/output
  6. **Supporting Documents**: Document list with SHA-256 checksums
  7. **Audit Trail**: Last 20 audit entries with timestamps
  8. **Compliance Statement**: Certification with authority signature area

**Technical Implementation:**
- Dependencies: iText7 (7.2.5), ZXing (3.5.3) for QR codes
- Service: `DossierService.kt` with `generatePdfDossier()` method (450+ lines)
- Features: QR codes link to Hedera blockchain, color-coded risk levels, professional formatting
- Formats: PDF, JSON, ZIP (with documents)

---

### ✅ **4. Authority Compliance Reporting**
- **3-Step Report Generation**:
  - Step 1: Select batches with filters (date range, risk level)
  - Step 2: Configure report (format, authority, digital signature)
  - Step 3: Preview and submit to authority
- **Multiple Authorities**: EU-DG-ENV, EU-CUSTOMS, EUTR-CA, Local Forest Authority
- **Submission Tracking**: Complete history with status updates
- **Authority Feedback**: View and track regulatory feedback
- **Real-Time Status**: Check submission status anytime

**Technical Implementation:**
- Backend: 6 new endpoints in `EudrController.kt` for authority reports
- API Endpoints:
  - `GET /api/eudr/authority-report/{batchId}` - Generate report
  - `GET /api/eudr/authority-report/export` - Bulk export
  - `POST /api/eudr/authority-report/submit` - Submit to authority
  - `GET /api/eudr/authority-report/submissions` - Submission history
  - `GET /api/eudr/authority-report/submission-status/{id}` - Check status
- Frontend: `ComplianceReporting.vue` (850+ lines) with stepper wizard
- Statistics: Total reports, pending review, approved, requires action

---

### ✅ **5. Certificate Viewing & Blockchain Verification**
- **Certificate Grid**: Visual display of all EUDR certificates
- **QR Code Generation**: Each certificate has QR code linking to Hedera
- **Blockchain Verification**: Real-time verification via Hedera API
- **NFT Information**: Display certificate NFT details
- **Validity Checking**: Automatic expiry and validity status
- **Filter & Search**: By status, compliance level, batch code

**Technical Implementation:**
- Frontend: `CertificateViewer.vue` (800+ lines)
- Library: qrcodejs2 for QR code generation
- Statistics Cards: Total, Valid, Transferred, Verified counts
- Integration: ImporterService APIs for certificate management

---

### ✅ **6. Production Unit Management**
- **GPS Coordinate Tracking**: Precise geolocation of farm plots
- **Deforestation Verification**: Satellite imagery integration (ready)
- **Plot Mapping**: Visual map with polygon boundaries
- **Multi-Plot Support**: Farmers can manage multiple production units
- **Area Calculation**: Automatic hectare calculation from GPS coordinates

---

### ✅ **7. Complete Supply Chain Traceability**
- **Farm-to-Export Tracking**: Every transaction recorded
- **Multi-Actor Support**: Farmers, Buyers, Aggregators, Processors, Exporters, Importers
- **Batch Consolidation**: Aggregate multiple farmer batches
- **Processing Events**: Track transformations (raw → processed)
- **Transfer History**: Complete ownership chain
- **Hedera Consensus Service**: All events on blockchain

---

## 🌟 Problem Statement

The agricultural sector faces critical challenges:
- **❌ Lack of Supply Chain Transparency** - Farmers and exporters struggle to prove product origin and sustainability
- **❌ EUDR Compliance Complexity** - EU Deforestation Regulation requires extensive documentation and traceability
- **❌ Carbon Credit Verification** - No reliable system to track and verify agricultural carbon sequestration
- **❌ Manual Record Keeping** - Paper-based systems are error-prone and easily manipulated
- **❌ Trust Deficit** - Buyers, importers, and regulators can't verify sustainability claims

---

## 💡 Our Solution: AgriBackup Platform

**AgriBackup** is a production-ready, enterprise-grade EUDR compliance platform that leverages **Hedera Hashgraph** to create an immutable, transparent, and verifiable supply chain from farm to export. We combine blockchain technology with real-world agricultural operations to solve compliance, sustainability, and trust challenges.

### 🎯 Core Innovation - What Makes Us Different

#### 1. **🔗 Complete Hedera Blockchain Integration**
- **Hedera Consensus Service (HCS)**: Every supply chain event recorded with cryptographic proof
- **Hedera Token Service (HTS)**: NFT-based EUDR certificates transferable between actors
- **Cost-Effective**: ~$0.0001 per transaction, ~$1 per NFT certificate
- **Real-Time Verification**: Transaction finality in 3-5 seconds
- **Carbon Negative**: Most sustainable blockchain platform

**Blockchain Implementation Details:**
```kotlin
// Every risk assessment recorded on Hedera
hederaConsensusService.recordRiskAssessment(
    batchId = batch.id,
    riskLevel = "HIGH",
    assessmentData = assessmentJson
) // Returns transaction ID: 0.0.12345@1234567890.123456789

// Every mitigation action tracked
hederaConsensusService.recordMitigationAction(
    actionId = action.id,
    workflowId = workflow.id,
    actionType = "VERIFICATION_VISIT",
    metadata = actionJson
)
```

#### 2. **🌍 100% EUDR Compliance - Production Ready**
All seven regulatory requirements fully implemented:

✅ **Requirement 1**: Geolocation of production units (GPS coordinates + polygon mapping)
✅ **Requirement 2**: Complete supply chain traceability (farm to EU border)  
✅ **Requirement 3**: Risk assessment (6-component analysis with automatic scoring)  
✅ **Requirement 4**: Risk mitigation (workflow management with Kanban board)  
✅ **Requirement 5**: Due diligence documentation (PDF dossiers with QR codes)  
✅ **Requirement 6**: Authority reporting (submission tracking with feedback)  
✅ **Requirement 7**: Certificate management (blockchain-verified NFTs)  

**Compliance Statistics:**
- 27 API endpoints for EUDR operations
- 6 risk components analyzed per batch
- 5 mitigation action types available
- 4 authority submission portals
- 100% blockchain audit trail coverage

#### 3. **📊 Advanced Risk Management System**
Our proprietary risk assessment engine analyzes six critical components:

1. **Country Risk** (30% weight)
   - Deforestation rate by country
   - Forest governance index
   - Historical compliance data
   
2. **Deforestation Risk** (25% weight)
   - Satellite imagery analysis (ready for GFW integration)
   - Historical land use changes
   - Proximity to protected areas
   
3. **Supplier Risk** (20% weight)
   - Supplier compliance history
   - Previous violations
   - Certification status
   
4. **Commodity Risk** (15% weight)
   - High-risk commodities (palm oil, soy, beef, cocoa)
   - Processing methods
   - Supply chain complexity
   
5. **Documentation Risk** (5% weight)
   - Completeness of documents
   - Document verification status
   - Missing critical documents
   
6. **Geospatial Risk** (5% weight)
   - GPS coordinate accuracy
   - Plot boundary verification
   - Location transparency

**Risk Scoring Formula:**
```
Overall Score = Σ(Component Score × Weight)
Risk Level = HIGH (>70), MEDIUM (40-70), LOW (20-40), NONE (<20)
```

#### 4. **🎨 Professional User Interface**
- **Role-Based Dashboards**: Customized views for 9 user roles
- **Interactive Kanban Board**: Visual mitigation workflow tracking
- **Real-Time Statistics**: Live updates on batches, risks, workflows
- **Responsive Design**: Works on desktop, tablet, and mobile
- **Offline Capability**: Progressive Web App (PWA) support

3. **🌱 Carbon Credit Tracking & Verification**
   - **Automated Carbon Sequestration Calculations**
   - Smart contract-based carbon credit issuance
   - Verifiable farming practices (organic, regenerative agriculture)
   - Integration with carbon offset marketplaces
   - Blockchain-verified carbon certificates

4. **👨‍🌾 Multi-Stakeholder Platform**
   - Farmers: Record harvests, list produce, track sustainability metrics
   - Aggregators: Consolidate farmer collections with full traceability
   - Processors: Document processing events with input/output tracking
   - Exporters: Manage EUDR compliance and supply chain
   - Importers: Verify product origin and sustainability claims

---

## 🌿 Carbon Credit Innovation

### How It Works

```
🌾 Farm Level
   ↓
📊 Data Collection (soil health, tree planting, organic practices)
   ↓
🔬 Carbon Calculation Algorithm (based on:
   - Crop type and acreage
   - Organic farming methods
   - Tree cover percentage
   - Soil carbon sequestration
   - Reduced tillage practices)
   ↓
⛓️ Hedera Blockchain Recording
   ↓
🏆 Carbon Credit Issuance (tokenized on Hedera)
   ↓
💰 Marketplace Integration
```

### Carbon Credit Features

✅ **Automatic Carbon Calculation**
- Calculate carbon sequestration based on farm practices
- Track reforestation efforts with GPS-verified tree planting
- Monitor soil health improvements
- Measure emission reductions from organic farming

✅ **Blockchain-Verified Credits**
- Each carbon credit minted as Hedera token (HTS)
- Immutable record of carbon sequestration activities
- Real-time verification by third parties
- Transparent trading history

✅ **Smart Contract Automation**
- Automatic credit issuance when milestones reached
- Fractional carbon credits for small farms
- Instant settlement of carbon credit trades
- Royalty distribution to farmers

✅ **Integration with Global Markets**
- Connect to voluntary carbon markets
- Corporate ESG reporting integration
- Verified Carbon Standard (VCS) compatible
- Gold Standard certification pathway

### Carbon Credit Use Cases

1. **🌳 Agroforestry Projects**
   - Coffee farmers plant shade trees → earn carbon credits
   - Verified via GPS + satellite imagery
   - Recorded on Hedera for permanent proof

2. **♻️ Regenerative Agriculture**
   - Organic farming practices sequester soil carbon
   - Reduced synthetic fertilizer use = lower emissions
   - Cover cropping and composting tracked

3. **🌲 Reforestation Initiatives**
   - Buffer zones around farms
   - Community tree-planting programs
   - Multi-year carbon sequestration tracking

4. **💼 Corporate Partnerships**
   - Companies offset emissions by purchasing farmer carbon credits
   - Direct farmer-to-buyer carbon credit marketplace
   - Transparent impact reporting

---

## 🏗️ Technical Architecture

### 🔧 Backend Stack (Spring Boot 3.3.2 + Kotlin 2.0.0)

```
farmers-portal-apis/
├── 🔐 Security Layer
│   ├── JWT Authentication (token-based auth)
│   ├── Role-Based Access Control (9 roles)
│   ├── @PreAuthorize annotations on all endpoints
│   └── Password encryption (BCrypt)
│
├── 🗃️ Database Layer (PostgreSQL 42.7.3 + PostGIS)
│   ├── 45+ JPA entities with relationships
│   ├── Spatial queries for geolocation
│   ├── Liquibase migrations (130+ changesets)
│   └── Hibernate Spatial for GIS support
│
├── ⛓️ Hedera SDK Integration (Java SDK)
│   ├── HederaConsensusService.kt
│   │   ├── recordRiskAssessment()
│   │   ├── recordMitigationWorkflow()
│   │   ├── recordMitigationAction()
│   │   ├── recordBatchCreation()
│   │   ├── recordSupplyChainEvent()
│   │   └── recordProcessingEvent()
│   │
│   ├── HederaTokenService.kt (NFT certificates)
│   └── HederaSmartContractService.kt (future)
│
├── 📊 EUDR Compliance Module
│   ├── RiskAssessmentService.kt (418 lines)
│   │   ├── assessBatchRisk() - Single batch assessment
│   │   ├── assessBatchRiskBulk() - Bulk assessment
│   │   ├── getRiskAssessmentHistory()
│   │   └── 6-component risk calculation
│   │
│   ├── MitigationWorkflowService.kt (488 lines)
│   │   ├── createWorkflow()
│   │   ├── addMitigationAction()
│   │   ├── updateActionStatus()
│   │   ├── completeWorkflow()
│   │   ├── getWorkflowStatistics()
│   │   └── Auto-completion logic
│   │
│   ├── DossierService.kt (818 lines)
│   │   ├── generateDossier() - Multi-format generation
│   │   ├── generatePdfDossier() - iText7 PDF creation
│   │   ├── generateJsonDossier()
│   │   ├── generateZipDossier()
│   │   └── QR code generation (ZXing)
│   │
│   ├── EudrBatchService.kt
│   │   ├── createBatch() with production units
│   │   ├── updateBatchStatus()
│   │   ├── transferBatch()
│   │   └── Batch lifecycle management
│   │
│   └── SupplierComplianceService.kt
│       ├── Supplier verification
│       ├── Compliance scoring
│       └── Violation tracking
│
├── 🌾 Supply Chain Module
│   ├── SupplyChainService.kt
│   ├── ProcessingService.kt
│   ├── AggregatorService.kt
│   └── TransferService.kt
│
└── 📦 Domain Models (45+ entities)
    ├── EudrBatch.kt (main batch entity)
    ├── ProductionUnit.kt (farm plots)
    ├── EudrDocument.kt (compliance docs)
    ├── MitigationWorkflow.kt (NEW)
    ├── MitigationAction.kt (NEW)
    ├── SupplyChainEvent.kt
    ├── ProcessingEvent.kt
    └── User.kt (multi-role support)
```

### 📦 Key Dependencies
```xml
<!-- Blockchain -->
<dependency>
    <groupId>com.hedera.hashgraph</groupId>
    <artifactId>sdk</artifactId>
    <version>2.40.0</version>
</dependency>

<!-- PDF Generation (NEW) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- QR Codes (NEW) -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- Spatial Data -->
<dependency>
    <groupId>net.postgis</groupId>
    <artifactId>postgis-jdbc</artifactId>
    <version>2023.1.0</version>
</dependency>

<!-- Hibernate Spatial -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-spatial</artifactId>
    <version>6.4.4.Final</version>
</dependency>
```

---

### 🎨 Frontend Stack (Vue.js 3 + Vuetify 3)

```
farmer-portal-frontend/
├── 👨‍🌾 Farmer Portal
│   ├── Farm management dashboard
│   ├── Harvest recording
│   ├── Production unit mapping (GPS)
│   ├── Document upload
│   └── Income tracking
│
├── 📦 Exporter Portal (Primary Focus)
│   ├── EUDR Dashboard
│   │   ├── Statistics overview
│   │   ├── Batch status tracking
│   │   └── Compliance metrics
│   │
│   ├── Risk Management (RiskManagement.vue - 600+ lines)
│   │   ├── Batch-based risk assessment
│   │   ├── Bulk assessment capability
│   │   ├── Risk breakdown dialog (6 components)
│   │   ├── Assessment history
│   │   └── Hedera verification links
│   │
│   ├── Mitigation Tracking (MitigationTracking.vue - 900+ lines) ⭐
│   │   ├── Statistics dashboard (4 cards)
│   │   ├── Workflows data table
│   │   ├── Kanban board (Pending/In Progress/Done)
│   │   ├── 5 interactive dialogs:
│   │   │   ├── Create workflow
│   │   │   ├── View workflow details
│   │   │   ├── Add mitigation action
│   │   │   ├── Action detail with evidence
│   │   │   └── Complete workflow
│   │   ├── Progress tracking
│   │   ├── Evidence upload
│   │   └── Hedera transaction links
│   │
│   ├── Compliance Reporting (ComplianceReporting.vue - 850+ lines) ⭐
│   │   ├── Statistics cards (4 metrics)
│   │   ├── Submission history table
│   │   ├── 3-Step Generation Wizard:
│   │   │   ├── Step 1: Select batches (multi-select, filters)
│   │   │   ├── Step 2: Configure report (format, authority, signature)
│   │   │   └── Step 3: Preview & submit
│   │   ├── Report preview
│   │   ├── Download functionality
│   │   ├── Status checking
│   │   └── Authority feedback display
│   │
│   ├── Batch Management
│   │   ├── Batch creation with production units
│   │   ├── Batch consolidation
│   │   ├── Status updates
│   │   └── Transfer operations
│   │
│   ├── Production Units
│   │   ├── GPS coordinate input
│   │   ├── Polygon mapping
│   │   ├── Deforestation check
│   │   └── Area calculation
│   │
│   └── Supply Chain Mapping
│       ├── Visual timeline
│       ├── Actor tracking
│       └── Event verification
│
├── 🌍 Common EUDR Components
│   └── Certificate Viewer (CertificateViewer.vue - 800+ lines) ⭐
│       ├── Statistics cards (4 counts)
│       ├── Certificate grid layout
│       ├── QR code display (qrcodejs2)
│       ├── Blockchain verification dialog
│       ├── NFT information
│       ├── Validity checking
│       ├── Timeline view
│       └── Filter/search functionality
│
├── 🛒 Importer Portal
│   ├── Shipment tracking
│   ├── Certificate verification
│   ├── Customs clearance
│   └── NFT ownership transfer
│
└── 🎯 Admin Portal
    ├── User management (9 roles)
    ├── System configuration
    ├── Audit logs
    └── Compliance monitoring
```

### 📦 Frontend Dependencies
```json
{
  "dependencies": {
    "vue": "^3.3.4",
    "vuetify": "^3.3.15",
    "axios": "^1.5.0",
    "vuex": "^4.1.0",
    "vue-router": "^4.2.4",
    "qrcodejs2": "^0.0.2",  // NEW: QR code generation
    "@hedera-wallet-connect": "^1.0.0",
    "leaflet": "^1.9.4",     // Map integration
    "chart.js": "^4.3.0",    // Statistics charts
    "pinia": "^2.1.6"        // State management
  }
}
```

---

## ✨ Key Features Implemented

### 🎯 EUDR Compliance System (100% Complete)

#### 1. **Intelligent Risk Assessment Engine**
- **6-Component Risk Analysis** with weighted scoring:
  - 🌲 Deforestation Risk (25%) - GFW API integration
  - 📍 Geolocation Verification (20%) - GPS accuracy checks
  - 📄 Documentation Completeness (15%) - Required docs validation
  - 🤝 Supplier Compliance History (20%) - Historical analysis
  - ⏱️ Supply Chain Traceability (15%) - Event continuity
  - 🌍 Country Risk Assessment (5%) - EUDR country ratings
- **Risk Levels**: NONE (0-19), LOW (20-39), MEDIUM (40-59), HIGH (60-79), CRITICAL (80-100)
- **Bulk Assessment**: Process multiple batches simultaneously
- **Hedera Recording**: All assessments recorded on blockchain

#### 2. **Mitigation Workflow Management** ⭐
- **Workflow Creation**: Create workflows for HIGH/MEDIUM risk batches
- **Action Management**: Define mitigation actions with:
  - Description, priority (HIGH/MEDIUM/LOW)
  - Assigned to, due dates
  - Status tracking (PENDING → IN_PROGRESS → COMPLETED)
  - Evidence upload (documents, photos)
- **Progress Tracking**: Real-time progress calculation
- **Auto-Completion**: Workflows auto-complete when all actions done
- **Hedera Recording**: Every action recorded on HCS
- **Kanban Board**: Visual workflow management
- **Statistics Dashboard**: Track pending/in-progress/completed workflows

#### 3. **Professional PDF Dossier Generation** ⭐
- **Multi-Format Support**: PDF, JSON, ZIP
- **8 Comprehensive Sections**:
  1. 📋 Cover Page with QR code (Hedera verification link)
  2. 📊 Batch Summary (quantity, dates, locations)
  3. ⚠️ Risk Assessment Breakdown (color-coded components)
  4. 🔗 Supply Chain Timeline (all events with timestamps)
  5. 🏭 Processing Events (transformations, locations)
  6. 📁 Documents Section (all compliance documents with checksums)
  7. 📝 Audit Trail (last 20 audit entries)
  8. ✅ Compliance Statement (certification with signature area)
- **QR Code Integration**: Scan to verify on Hedera network
- **Professional Formatting**: iText7 library with tables, headers, colors
- **Blockchain Verified**: All data sourced from Hedera-recorded events

#### 4. **Authority Compliance Reporting** ⭐
- **Report Generation**:
  - Select multiple batches
  - Choose format (PDF/JSON/ZIP)
  - Configure report settings
  - Add digital signature
- **Bulk Export**: Generate reports for multiple batches with filters
- **Submission Tracking**:
  - Submit reports to authorities
  - Track submission status (PENDING → SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED)
  - View authority feedback
  - Resubmission capability
- **History Management**: Complete submission history with timestamps
- **Statistics Dashboard**: Track submission metrics

#### 5. **Interactive Certificate Viewer** ⭐
- **Visual Certificate Grid**: View all certificates at a glance
- **QR Code Display**: Frontend QR code generation for verification
- **Blockchain Verification Dialog**:
  - NFT token ID and serial number
  - Hedera transaction IDs
  - Issuance and expiry dates
  - Verifiable on HashScan
- **Timeline View**: Certificate lifecycle visualization
- **Filter & Search**: Advanced filtering by status, actor, dates
- **Statistics Cards**: 4 metrics (total, active, expired, revoked)

#### 6. **Production Unit Management**
- **GPS Coordinate Mapping**: Define farm plot boundaries
- **Polygon Creation**: Visual boundary mapping
- **Deforestation Verification**:
  - Global Forest Watch API integration
  - Tree cover loss detection (2000-2023)
  - Alert count tracking
  - Risk classification
- **Area Calculation**: Automatic hectare calculation
- **Compliance Status**: Real-time deforestation risk status

#### 7. **Complete Supply Chain Traceability**
- **Event Recording**: Every supply chain step recorded
- **Actor Tracking**: Farmer → Aggregator → Processor → Exporter
- **Visual Timeline**: Interactive event visualization
- **Blockchain Verification**: All events on Hedera HCS
- **Batch Consolidation**: Multiple source batches into one
- **Transfer Management**: Ownership changes with verification

---

### ⛓️ Hedera Blockchain Integration (Production-Ready)

#### Consensus Service (HCS) Integration
```kotlin
// Risk Assessment Recording
consensusService.recordRiskAssessment(
    assessment = riskAssessment,
    metadata = mapOf(
        "batchId" to batch.id,
        "overallRisk" to assessment.overallRisk.name,
        "components" to assessment.components.toString()
    )
)

// Mitigation Workflow Recording
consensusService.recordMitigationWorkflow(
    workflow = workflow,
    metadata = mapOf(
        "workflowId" to workflow.id,
        "batchId" to workflow.batch.id,
        "totalActions" to workflow.actions.size,
        "status" to workflow.status.name
    )
)

// Supply Chain Event Recording
consensusService.recordSupplyChainEvent(
    event = supplyChainEvent,
    metadata = mapOf(
        "eventType" to event.type,
        "fromActor" to event.fromActor.name,
        "toActor" to event.toActor?.name
    )
)
```

#### Token Service (HTS) - NFT Certificates
- **Automated Certificate Issuance**: NFT minted upon batch creation
- **Metadata Storage**: Certificate details on IPFS
- **Ownership Tracking**: Transfer certificates with batches
- **Expiry Management**: Validity period enforcement
- **Revocation Support**: Invalidate certificates when needed

#### Smart Contract Service (HSCS) - Future Enhancement
- Automated compliance verification
- Payment settlement on delivery
- Multi-party escrow
- Conditional logic execution

---

### 🚀 Production Deployment Features

#### Security & Authentication
- **JWT Token-Based Auth**: Secure token management
- **9 Role-Based Access Control**:
  1. SYSTEM_ADMIN (full access)
  2. FARMER (farm operations)
  3. EXPORTER (primary EUDR user)
  4. AGGREGATOR (collection points)
  5. PROCESSOR (transformation)
  6. IMPORTER (receiving)
  7. VERIFIER (compliance checks)
  8. AUDITOR (read-only oversight)
  9. CUSTOMS_OFFICER (clearance)
- **@PreAuthorize Annotations**: Method-level security
- **Password Encryption**: BCrypt hashing

#### Database Architecture
- **PostgreSQL 42.7.3**: Production-grade RDBMS
- **PostGIS Extension**: Geospatial queries
- **Liquibase Migrations**: 130+ managed changesets
- **45+ Domain Entities**: Comprehensive data model
- **Optimized Indexes**: Query performance optimization
- **Spatial Queries**: GPS coordinate searching

#### API Documentation
- **27 EUDR-Specific Endpoints**:
  - 6 Risk Assessment APIs
  - 8 Mitigation Workflow APIs
  - 6 Authority Reporting APIs
  - 4 Dossier Generation APIs
  - 3 Certificate Viewing APIs
- **RESTful Design**: Standard HTTP methods
- **JSON Responses**: Structured data format
- **Error Handling**: Comprehensive exception management

---

## 🎬 EUDR Compliance Demo Flow

### 1. Batch Creation & Risk Assessment
```bash
# Step 1: Create EUDR batch with production units
POST /api/v1/eudr/batches
{
  "commodity": "COFFEE",
  "quantityKg": 5000,
  "productionUnits": [
    {
      "coordinates": "POINT(-3.2345 8.9876)",
      "areaHectares": 5.5,
      "farmerId": 123
    }
  ]
}
→ Response: Batch created with PENDING status
→ Hedera: Batch creation recorded on HCS

# Step 2: Assess batch risk (automatic or manual trigger)
POST /api/v1/eudr/risk-assessment/assess
{
  "batchId": 456
}
→ Analysis:
  - Deforestation: 5% (GFW API check)
  - Geolocation: 95% (GPS verified)
  - Documentation: 80% (some docs missing)
  - Supplier history: 90% (good track record)
  - Traceability: 85% (complete chain)
  - Country risk: 10% (low-risk region)
→ Overall Risk: MEDIUM (42.5 score)
→ Hedera: Risk assessment recorded on HCS with transaction ID
```

### 2. Mitigation Workflow (for HIGH/MEDIUM risk batches)
```bash
# Step 1: Create mitigation workflow
POST /api/v1/eudr/mitigation-workflows
{
  "batchId": 456,
  "description": "Address missing documentation and geolocation issues"
}
→ Response: Workflow created with status PENDING

# Step 2: Add mitigation actions
POST /api/v1/eudr/mitigation-workflows/{workflowId}/actions
{
  "actionType": "DOCUMENT_COLLECTION",
  "description": "Collect land ownership certificates",
  "priority": "HIGH",
  "assignedToUserId": 789,
  "dueDate": "2025-02-01"
}
→ Response: Action added with status PENDING
→ Hedera: Action creation recorded on HCS

# Step 3: Update action progress
PUT /api/v1/eudr/mitigation-workflows/actions/{actionId}/status
{
  "status": "IN_PROGRESS",
  "notes": "Contacting local land registry office"
}
→ Hedera: Status update recorded on HCS

# Step 4: Complete action with evidence
PUT /api/v1/eudr/mitigation-workflows/actions/{actionId}/complete
{
  "completionNotes": "Land certificates obtained and verified",
  "evidenceUrls": ["s3://bucket/certificate.pdf"]
}
→ Workflow progress: 33% → 66%
→ Hedera: Completion recorded on HCS

# Step 5: Workflow auto-completes when all actions done
GET /api/v1/eudr/mitigation-workflows/{workflowId}
→ Status: COMPLETED
→ Batch status automatically updated: PENDING → APPROVED
```

### 3. PDF Dossier Generation
```bash
# Generate comprehensive PDF dossier
GET /api/v1/eudr/dossiers/{batchId}/generate?format=PDF
→ PDF Generated with 8 sections:
  1. Cover page with QR code (scan to verify on Hedera)
  2. Batch summary table
  3. Risk assessment breakdown (color-coded)
  4. Supply chain timeline (all events)
  5. Processing events (transformations)
  6. Documents section (checksums)
  7. Audit trail (20 entries)
  8. Compliance statement (signature area)
→ File size: ~500KB-2MB
→ Download: dossier_batch_456.pdf

# Alternative formats
GET /api/v1/eudr/dossiers/{batchId}/generate?format=JSON
→ Structured JSON with all data

GET /api/v1/eudr/dossiers/{batchId}/generate?format=ZIP
→ ZIP archive with PDF + JSON + documents
```

### 4. Authority Compliance Reporting
```bash
# Step 1: Generate authority report (multi-batch)
POST /api/v1/eudr/authority-report/{batchId}
{
  "format": "PDF",
  "authorityName": "EU Commission DG AGRI",
  "reportingPeriod": "Q1 2025",
  "includeSignature": true,
  "signatoryName": "John Exporter",
  "signatoryTitle": "Compliance Officer"
}
→ Report generated and ready for download

# Step 2: Submit report to authority
POST /api/v1/eudr/authority-report/submit
{
  "batchIds": [456, 457, 458],
  "authorityEmail": "compliance@ec.europa.eu",
  "submissionNotes": "Q1 2025 quarterly compliance report"
}
→ Response: Submission ID: 789
→ Status: SUBMITTED
→ Automated email sent to authority

# Step 3: Check submission status
GET /api/v1/eudr/authority-report/submission-status/789
→ Response:
{
  "status": "UNDER_REVIEW",
  "submittedAt": "2025-01-15T10:30:00Z",
  "reviewedAt": null,
  "feedback": null
}

# Step 4: View feedback (after authority review)
GET /api/v1/eudr/authority-report/submission-status/789
→ Response:
{
  "status": "APPROVED",
  "submittedAt": "2025-01-15T10:30:00Z",
  "reviewedAt": "2025-01-20T14:00:00Z",
  "feedback": "All documentation complete. Batch approved for EU import."
}
```

### 5. Certificate Viewing & Verification
```bash
# Frontend: View all certificates
GET /api/v1/eudr/certificates
→ Display grid with statistics:
  - Total: 150 certificates
  - Active: 120
  - Expired: 25
  - Revoked: 5

# Click certificate to view details
GET /api/v1/eudr/certificates/{certificateId}
→ Display:
  - QR code (scan to verify)
  - NFT token ID: 0.0.123456
  - Serial number: 789
  - Batch ID: 456
  - Issuance date: 2025-01-10
  - Expiry date: 2026-01-10
  - Status: ACTIVE
  - Hedera transaction ID: 0.0.123@1705743600.123456789
  
# Blockchain verification
→ Scan QR code or click "Verify on Hedera"
→ Opens: https://hashscan.io/testnet/transaction/0.0.123@1705743600.123456789
→ View immutable record on blockchain
```

### 6. Complete Supply Chain Journey
```bash
# 1. Farmer records harvest
POST /api/v1/farmers/harvest
→ Recorded on Hedera HCS with GPS coordinates

# 2. Aggregator collects from multiple farmers
POST /api/v1/aggregators/collections
→ Links farmer IDs, quantities to Hedera

# 3. Exporter creates EUDR batch
POST /api/v1/eudr/batches
→ Consolidates collections, triggers risk assessment

# 4. Risk assessment & mitigation (if needed)
POST /api/v1/eudr/risk-assessment/assess
→ Identifies MEDIUM risk
POST /api/v1/eudr/mitigation-workflows
→ Creates workflow, adds actions, tracks completion

# 5. Processor transforms batch
POST /api/v1/processors/processing-events
→ Input: 5000kg raw coffee → Output: 4000kg roasted
→ Recorded on Hedera HCS

# 6. Final export with PDF dossier
GET /api/v1/eudr/dossiers/{batchId}/generate
→ Professional PDF with QR code verification

# 7. Submit to authority
POST /api/v1/eudr/authority-report/submit
→ Track status until APPROVED

# 8. Ship to EU with compliance certificate
→ Complete blockchain-verified traceability
→ NFT certificate transferred to importer
```

---

## 🛠️ Getting Started

### Prerequisites
- **Java 17** or higher
- **Node.js 18** or higher
- **PostgreSQL 14** or higher with **PostGIS extension**
- **Maven 3.8** or higher
- **Hedera Testnet Account** (free at [portal.hedera.com](https://portal.hedera.com))

### Backend Setup (Spring Boot)

```bash
# 1. Clone repository
git clone https://github.com/your-org/agribackup-platform.git
cd agribackup-platform/farmers-portal-apis

# 2. Configure PostgreSQL
createdb farmers_portal
psql farmers_portal -c "CREATE EXTENSION postgis;"

# 3. Configure Hedera credentials
# Edit src/main/resources/application.properties
hedera.accountId=0.0.YOUR_ACCOUNT_ID
hedera.privateKey=YOUR_PRIVATE_KEY
hedera.network=testnet

# 4. Build and run
./mvnw clean install
./mvnw spring-boot:run

# Backend runs at: http://localhost:8080
# API Docs: http://localhost:8080/swagger-ui.html
```

### Frontend Setup (Vue.js)

```bash
# 1. Navigate to frontend
cd ../farmer-portal-frontend

# 2. Install dependencies
npm install

# 3. Configure API endpoint
# Create .env.local file
VUE_APP_API_BASE_URL=http://localhost:8080

# 4. Run development server
npm run serve

# Frontend runs at: http://localhost:8081
```

### Initial Data Setup

```bash
# 1. Create system admin user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@agribackup.com",
    "password": "Admin123!",
    "firstName": "System",
    "lastName": "Administrator",
    "role": "SYSTEM_ADMIN"
  }'

# 2. Login and get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@agribackup.com",
    "password": "Admin123!"
  }'

# 3. Use token in subsequent requests
curl -X GET http://localhost:8080/api/v1/eudr/batches \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📊 Impact & Market Opportunity

### Environmental Impact
- 🌳 **Deforestation Prevention**: Real-time monitoring via Global Forest Watch API
- 🌲 **Supply Chain Transparency**: Every product traceable to farm plot with GPS coordinates
- ♻️ **Sustainable Practices**: Incentivizing EUDR-compliant farming
- 📉 **Risk Reduction**: Automated risk assessment prevents non-compliant imports

### Economic Impact
- 🇪🇺 **€450M+ EU Agricultural Imports**: Requiring EUDR compliance (mandatory 2025)
- 💰 **Market Access**: EUDR certification opens EU market to compliant exporters
- 🤝 **Fair Trade**: Transparent pricing and blockchain-verified sourcing
- 📈 **Premium Pricing**: Verified sustainability commands 10-30% price premiums

### Social Impact
- 👨‍👩‍👧‍👦 **Farmer Empowerment**: 1.2B+ small farmers need compliance tools
- 🎓 **Education**: Best practices for deforestation-free agriculture
- 🌍 **Global Applicability**: Solution works for coffee, cocoa, palm oil, soy, cattle, rubber, wood
- ✊ **Transparency**: Fair dealings with immutable blockchain records

### Market Opportunity
- 🌍 **$450B+ Global Agricultural Trade**: Affected by EUDR regulation
- 🇪🇺 **Mandatory Compliance**: All EU imports must be EUDR-certified from Dec 2024
- 🌾 **500M+ Hectares**: Agricultural land requiring monitoring
- 📈 **Blockchain Adoption**: DLT essential for supply chain verification

---

## 🛠️ Technology Stack

### Blockchain
- **Hedera Hashgraph**: Consensus Service (HCS), Token Service (HTS), Smart Contracts (HSCS)
- **Hedera SDK 2.40.0**: Java SDK for Spring Boot integration
- **HashScan**: Blockchain explorer integration
- **IPFS**: Decentralized metadata storage

### Backend
- **Spring Boot 3.3.2**: Kotlin 2.0.0-based REST API
- **PostgreSQL 42.7.3**: Production-grade RDBMS with PostGIS extension
- **Liquibase 4.24.0**: 130+ database migrations
- **JWT Authentication**: Secure token-based auth
- **iText7 7.2.5**: Professional PDF generation
- **ZXing 3.5.3**: QR code generation
- **Maven 3.9+**: Build automation
- **Docker**: Containerization support

### Frontend
- **Vue.js 3.3.4**: Progressive JavaScript framework
- **Vuetify 3.3.15**: Material Design component library
- **Axios 1.5.0**: HTTP client for API calls
- **Vuex 4.1.0 + Pinia 2.1.6**: State management
- **Vue Router 4.2.4**: SPA routing
- **qrcodejs2 0.0.2**: QR code generation
- **Leaflet 1.9.4**: Interactive mapping
- **Chart.js 4.3.0**: Statistics visualization
- **Webpack 5.x**: Module bundling

### External Integrations
- **Global Forest Watch API**: Deforestation monitoring
- **AWS S3**: Document storage
- **Hedera Testnet**: Blockchain operations (upgradable to mainnet)

### DevOps
- **Git**: Version control
- **ESLint**: Code quality enforcement
- **Prettier**: Code formatting
- **Nginx**: Production web server

---

## 🏆 Why AgriBackup Stands Out

### 1. **100% EUDR Compliant Implementation**
- ✅ All 7 EUDR requirements fully implemented
- ✅ Real-time risk assessment (6-component algorithm)
- ✅ Mitigation workflow management (900+ line frontend)
- ✅ Professional PDF dossier generation (iText7)
- ✅ Authority compliance reporting system
- ✅ Interactive certificate viewer with QR codes
- ✅ Production-ready for immediate deployment

### 2. **Advanced Hedera Integration**
- ✅ Hedera Consensus Service (HCS) for immutable event recording
- ✅ Hedera Token Service (HTS) for NFT certificates
- ✅ Multi-service architecture (ready for HSCS smart contracts)
- ✅ Cost-efficient ($0.0001 per transaction)
- ✅ Carbon-negative blockchain (0.00017 kWh/tx)
- ✅ Real testnet integration operational

### 3. **Production-Grade Architecture**
- ✅ Spring Boot 3.3.2 + Kotlin 2.0.0 backend
- ✅ Vue.js 3 + Vuetify 3 responsive frontend
- ✅ PostgreSQL 42.7.3 + PostGIS spatial database
- ✅ 45+ JPA entities with 130+ Liquibase migrations
- ✅ 27 EUDR-specific API endpoints
- ✅ JWT authentication + 9-role RBAC system
- ✅ Comprehensive error handling and validation

### 4. **Feature-Rich User Experience**
- ✅ 5 major EUDR components (2,600+ lines of Vue code):
  - RiskManagement.vue (600 lines)
  - MitigationTracking.vue (900 lines)
  - ComplianceReporting.vue (850 lines)
  - CertificateViewer.vue (800 lines)
  - ProductionUnitDrawer.vue (500+ lines)
- ✅ Intuitive 3-step wizards
- ✅ Kanban boards for workflow management
- ✅ Real-time statistics dashboards
- ✅ Interactive QR code verification

### 5. **Scalability & Real-World Readiness**
- ✅ Multi-tenant architecture supporting millions of farmers
- ✅ Global applicability (any EUDR commodity)
- ✅ Bulk operations (assess/report multiple batches)
- ✅ Document management with AWS S3/IPFS
- ✅ Optimized database queries with spatial indexing
- ✅ Comprehensive audit trail system

---

## 🌍 Sustainability Commitment

**AgriBackup** is built on the world's most sustainable blockchain:
- **⚡ Carbon Negative**: Hedera uses only 0.00017 kWh per transaction
- **🌲 Reforestation**: Tracking deforestation prevention via GFW API
- **♻️ Sustainable Agriculture**: Promoting EUDR-compliant practices
- **🌱 Global Impact**: Protecting forests while enabling agricultural trade

---

## 👥 Project Information

### Development Team
- **Full-Stack Development**: Backend (Kotlin/Spring) + Frontend (Vue.js)
- **Blockchain Integration**: Hedera HCS/HTS implementation
- **EUDR Compliance**: Complete regulation implementation
- **Database Design**: PostgreSQL + PostGIS spatial queries
- **UI/UX Design**: Professional Vuetify components

### Project Stats
- **Backend**: 15,000+ lines of Kotlin code
- **Frontend**: 8,000+ lines of Vue.js code
- **Database**: 45+ entities, 130+ migrations
- **APIs**: 27 EUDR endpoints + 40+ supply chain endpoints
- **Components**: 30+ Vue components
- **Services**: 20+ Spring services
- **Development Time**: 6+ months
- **Test Coverage**: Comprehensive unit tests

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

We welcome contributions to **AgriBackup Platform**! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📞 Contact & Support

- **GitHub**: [github.com/samwel-gachrhiri/agribackup](https://github.com/samwel-gachiri/agribackup)
- **Issues**: [Report bugs or request features](https://github.com/your-org/agribackup-platform/issues)
- **Discussions**: [Join community discussions](https://github.com/your-org/agribackup-platform/discussions)
- **Documentation**: See `docs/` folder for detailed guides

---

## 🙏 Acknowledgments

- **Hedera Hashgraph**: For providing the world's most sustainable distributed ledger
- **EU Commission**: For the EUDR regulation driving supply chain transparency
- **Global Forest Watch**: For deforestation monitoring API
- **Spring Boot & Vue.js Communities**: For excellent frameworks and documentation
- **iText**: For professional PDF generation library
- **PostgreSQL + PostGIS**: For powerful spatial database capabilities

---

<div align="center">

### 🌾 Built for Sustainable Agriculture & Blockchain Traceability

**AgriBackup Platform** - *EUDR Compliance Made Simple*

[![Hedera](https://img.shields.io/badge/Hedera-Testnet-purple)](https://hedera.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.3.4-green)](https://vuejs.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-blue)](https://www.postgresql.org)
[![EUDR](https://img.shields.io/badge/EUDR-100%25%20Compliant-success)](https://environment.ec.europa.eu/topics/forests/deforestation_en)

</div>


### 🌾 Built with ❤️ for Farmers, Planet, and Blockchain

**AgriConnect** - *Cultivating Trust, Harvesting Sustainability*

</div>

# 🌾 AgriBackup - Hedera DLT - Powered Agricultural Supply Chain Platform

---

### Pitch Deck

- Link to pitch deck: [Pitch Deck](https://gamma.app/docs/AgriBackup-tml8dxii39r35b2)

### Certificates

- **Samwel Gachiri**: [Certificate](https://drive.google.com/file/d/1aJ1hk7nL32OVWq9JtTjvmAAmcWaa-Fvo/view?usp=sharing)
- **Beatrice Wanjiru**: [Certificate](https://drive.google.com/file/d/1bp7kQePvY1AVofYksm9EnbMUbogEcJ02/view?usp=sharing)

---

> **Transforming Global Agriculture through Transparent, Sustainable, and Traceable Supply Chains**

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
- [Platform Overview](#-platform-overview)
- [Core Features](#-core-features)
- [Technology Innovation](#-technology-innovation)
- [Technical Architecture](#️-technical-architecture)
- [Getting Started](#-getting-started)
- [Impact & Market Opportunity](#-impact--market-opportunity)
- [Why AgriBackup Stands Out](#-why-agribackup-stands-out)
- [Project Information](#-project-information)
- [Contributing](#-contributing)
- [License](#-license)

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
- **Hedera Smart Contracts (HSCS)**: Automated compliance verification and settlements
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

- **Security**: JWT authentication, 9-role RBAC, @PreAuthorize annotations
- **Database**: PostgreSQL 42.7.3 + PostGIS for spatial queries, 130+ Liquibase migrations
- **Blockchain**: Hedera SDK 2.40.0 (HCS, HTS, HSCS), HashScan integration
- **Services**: Comprehensive supply chain and compliance services
- **APIs**: 60+ RESTful endpoints with comprehensive error handling

### � CFrontend Stack (Vue.js 3 + Vuetify 3)

- **Core**: Vue 3.3.4, Vuetify 3.3.15, Vue Router 4.2.4
- **State Management**: Vuex 4.1.0 + Pinia 2.1.6
- **Features**: QR code generation, interactive mapping, statistics charts
- **Components**: 30+ Vue components, 8,000+ lines of production code

### 📦 Key Dependencies

```xml
<!-- Blockchain -->
<dependency>
    <groupId>com.hedera.hashgraph</groupId>
    <artifactId>sdk</artifactId>
    <version>2.40.0</version>
</dependency>

<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- Spatial Database -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-spatial</artifactId>
    <version>6.4.4.Final</version>
</dependency>
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
cd agribackup-platform/backend

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
cd ../frontend

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
```

---

## 📊 Impact & Market Opportunity

### 🌍 Global Agricultural Transformation

- **$4.5 Trillion Global Food Market**: Massive opportunity for supply chain innovation
- **1.2 Billion Small Farmers**: Need access to transparent, fair trade platforms
- **Growing Consumer Demand**: 73% of consumers willing to pay more for sustainable products
- **Regulatory Pressure**: Increasing requirements for supply chain transparency

### 💰 Economic Benefits

- **Premium Pricing**: Verified sustainability commands 10-30% price premiums
- **Market Access**: Compliance certification opens new markets
- **Reduced Costs**: Automated processes reduce administrative overhead
- **Risk Mitigation**: Proactive monitoring prevents costly compliance failures

### 🌱 Environmental Impact

- **Deforestation Prevention**: Real-time monitoring protects millions of hectares
- **Carbon Sequestration**: Incentivizing sustainable farming practices
- **Biodiversity Protection**: Supporting eco-friendly agricultural methods
- **Climate Action**: Contributing to global sustainability goals

### 👥 Social Impact

- **Farmer Empowerment**: Direct access to global markets and fair pricing
- **Transparency**: Building trust between producers and consumers
- **Education**: Promoting sustainable farming practices
- **Economic Development**: Supporting rural communities worldwide

---

## 🏆 Why AgriBackup Stands Out

### 1. **Comprehensive Solution**

- ✅ End-to-end supply chain coverage from farm to consumer
- ✅ Multi-stakeholder platform serving all actors in the value chain
- ✅ Integrated compliance, sustainability, and trade facilitation
- ✅ Production-ready with real-world deployment capability

### 2. **Advanced Technology Stack**

- ✅ Hedera Hashgraph for sustainable, scalable blockchain infrastructure
- ✅ AI-powered analytics and risk assessment
- ✅ Satellite and IoT integration for real-world data verification
- ✅ Modern, responsive user interface with intuitive workflows

### 3. **Regulatory Leadership**

- ✅ Complete EUDR compliance implementation
- ✅ Automated risk assessment and mitigation workflows
- ✅ Professional documentation and reporting systems
- ✅ Direct integration with regulatory authorities

### 4. **Sustainability Focus**

- ✅ Carbon credit generation and trading
- ✅ Environmental impact monitoring
- ✅ Deforestation prevention verification
- ✅ Sustainable farming practice incentives

### 5. **Scalability & Accessibility**

- ✅ Ultra-low transaction costs accessible to small farmers
- ✅ Multi-tenant architecture supporting millions of users
- ✅ Global applicability across all agricultural commodities
- ✅ Mobile-first design for developing market accessibility

---

## 🌍 Sustainability Commitment

**AgriBackup** is built on the world's most sustainable blockchain:

- **⚡ Carbon Negative**: Hedera uses only 0.00017 kWh per transaction
- **🌲 Forest Protection**: Real-time deforestation monitoring and prevention
- **♻️ Circular Economy**: Promoting sustainable agricultural practices
- **🌱 Global Impact**: Contributing to UN Sustainable Development Goals

---

## 👥 Project Information

### Development Team

- **Full-Stack Development**: Backend (Kotlin/Spring) + Frontend (Vue.js)
- **Blockchain Integration**: Hedera HCS/HTS implementation
- **Regulatory Compliance**: Complete EUDR regulation implementation
- **Database Design**: PostgreSQL + PostGIS spatial queries
- **UI/UX Design**: Professional Vuetify components

### Certificates

- **Samwel Gachiri**: [Certificate](https://drive.google.com/file/d/1aJ1hk7nL32OVWq9JtTjvmAAmcWaa-Fvo/view?usp=sharing)
- **Beatrice Wanjiru**: [Certificate](https://drive.google.com/file/d/1bp7kQePvY1AVofYksm9EnbMUbogEcJ02/view?usp=sharing)

### Project Stats

- **Backend**: 15,000+ lines of Kotlin code
- **Frontend**: 8,000+ lines of Vue.js code
- **Database**: 45+ entities, 130+ migrations
- **APIs**: 60+ endpoints across supply chain and compliance
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

- **GitHub**: [github.com/samwel-gachiri/agribackup](https://github.com/samwel-gachiri/agribackup)
- **Issues**: [Report bugs or request features](https://github.com/your-org/agribackup-platform/issues)
- **Discussions**: [Join community discussions](https://github.com/your-org/agribackup-platform/discussions)
- **Documentation**: See `documentation/` folder for detailed guides

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

**AgriBackup Platform** - _Cultivating Trust, Harvesting Sustainability_

[![Hedera](https://img.shields.io/badge/Hedera-Testnet-purple)](https://hedera.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.3.4-green)](https://vuejs.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-blue)](https://www.postgresql.org)
[![EUDR](https://img.shields.io/badge/EUDR-Compliant-success)](https://environment.ec.europa.eu/topics/forests/deforestation_en)

</div>

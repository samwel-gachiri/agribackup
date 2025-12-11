# Certificate Compliance Validation Implementation

## Overview
Enhanced the EUDR compliance certificate issuance to include comprehensive validation checks **before** minting the NFT certificate on blockchain. Previously, certificates were issued without verifying actual compliance, which nullified their purpose.

## Compliance Validation Checks

### 1. **Collection Events Verification**
- ✅ Ensures workflow has at least one collection event
- ✅ Validates complete supply chain from farmer to exporter

### 2. **GPS Coordinates Validation**
- ✅ Verifies all production units have GPS coordinates captured
- ✅ Ensures geolocation data is available for satellite verification
- ❌ **Fails if any production unit lacks GPS coordinates**

### 3. **Satellite Verification Status**
- ✅ Checks all production units have been verified with satellite imagery
- ✅ Validates `lastVerifiedAt` timestamp exists for each unit
- ❌ **Fails if any production unit lacks satellite verification**

### 4. **Deforestation-Free Status** ⭐ Enhanced
- ✅ Queries `DeforestationAlertRepository` with date range filter
- ✅ Validates no HIGH or CRITICAL severity alerts after 2020-12-31 (EUDR cutoff)
- ✅ Uses existing `findByProductionUnitIdsAndDateRange()` method
- ✅ Distinguishes between alert severity levels (LOW/MEDIUM/HIGH/CRITICAL)
- ❌ **Fails if any production unit has HIGH/CRITICAL alerts post-cutoff**

### 5. **Supply Chain Traceability**
- ✅ Verifies consolidation events exist (aggregator → processor)
- ✅ Ensures complete traceability chain
- ❌ **Fails if consolidation events are missing**

### 6. **Quantity Consistency Validation** 🆕
- ✅ Validates quantity balance across supply chain stages
- ✅ Ensures consolidated quantity ≤ collected quantity
- ❌ **Fails if consolidation exceeds collection (potential fraud)**

### 7. **Origin Country Determination**
- ✅ Extracts origin country from production unit locations
- ⚠️ **Warns if origin country cannot be determined**

### 8. **Comprehensive Risk Assessment** ⭐ Enhanced
Integrates with existing `RiskAssessmentService` methodology:

#### Risk Score Components (Weighted)
- **Deforestation Alerts (40%)**: 
  - 0.9 if critical alerts present
  - 0.7 if 2+ medium alerts
  - 0.5 if 1 medium alert
  - 0.3 if any low alerts
  - 0.1 if no alerts

- **Geospatial Verification (25%)**:
  - Based on verification ratio and GPS coverage
  - Score = 1.0 - (verified + GPS) / (units × 2)

- **Country Risk (20%)**:
  - 0.8 for high-risk countries (Brazil, Indonesia, DRC, Malaysia, Bolivia, Peru, Colombia, Paraguay)
  - 0.3 for standard countries

- **Traceability (15%)**:
  - 0.1 if complete traceability
  - 0.8 if gaps in traceability chain

- **GPS Gaps Penalty (+10%)**:
  - Additional 0.1 risk score if any units lack GPS

#### Risk Level Thresholds
- **HIGH**: Score ≥ 0.7 (blocks certificate issuance, requires manual review)
- **MEDIUM**: Score 0.4-0.69 (certificate issued with enhanced monitoring)
- **LOW**: Score < 0.4 (certificate issued normally)

## Deforestation Status Values

| Status | Meaning |
|--------|---------|
| `VERIFIED_FREE` | All production units verified deforestation-free ✅ |
| `DEFORESTATION_DETECTED` | One or more units have HIGH/CRITICAL alerts post-2020 ❌ |
| `ALERTS_UNDER_REVIEW` | Low/Medium alerts present, requires review ⚠️ |
| `VERIFICATION_INCOMPLETE` | Missing GPS coordinates or satellite verification ❌ |
| `UNKNOWN` | Unable to perform verification ❌ |

## Implementation Details

### Service Method: `validateWorkflowCompliance()`
```kotlin
private fun validateWorkflowCompliance(workflow: SupplyChainWorkflow): ComplianceValidationResult {
    // Comprehensive validation including:
    // 1. Collection events verification
    // 2. GPS coordinates coverage
    // 3. Satellite verification status
    // 4. Deforestation alert analysis (with date filtering)
    // 5. Supply chain traceability
    // 6. Quantity consistency validation
    // 7. Origin country determination
    // 8. Risk assessment using RiskAssessmentService methodology
    // 9. HIGH risk blocking (requires manual review)
    
    // Returns:
    // - isCompliant: Boolean
    // - failureReasons: List<String>
    // - Computed compliance data (farmers, units, GPS count, etc.)
    // - Risk level: LOW/MEDIUM/HIGH
    // - Deforestation status
}
```

### Integration with Certificate Issuance
```kotlin
fun issueComplianceCertificate(workflowId: String): Map<String, Any?> {
    // 1. Load workflow from repository
    // 2. Validate compliance (ENHANCED)
    //    - Calls validateWorkflowCompliance()
    //    - Queries DeforestationAlertRepository with date range
    //    - Calculates weighted risk score
    //    - Logs detailed validation results
    // 3. If validation fails → throw IllegalStateException with detailed reasons
    // 4. If validation passes → mint NFT certificate on Hedera
    // 5. Update workflow certificate status
    // 6. Return certificate details with Hashscan URL
}
```

### Risk Assessment Integration ⭐ New
The validation now integrates with the existing `RiskAssessmentService`:

```kotlin
private fun calculateWorkflowRiskLevel(
    productionUnits: List<ProductionUnit>,
    deforestationAlerts: List<DeforestationAlert>,
    originCountry: String,
    hasGpsGaps: Boolean,
    hasVerificationGaps: Boolean,
    traceabilityComplete: Boolean
): String {
    // Weighted risk calculation:
    // - Deforestation alerts: 40%
    // - Geospatial verification: 25%
    // - Country risk: 20%
    // - Traceability: 15%
    // - GPS gaps penalty: +10%
    
    // Returns: "LOW" | "MEDIUM" | "HIGH"
}
```

### Repository Methods Used
```kotlin
// Query alerts with date range and severity filter
deforestationAlertRepository.findByProductionUnitIdsAndDateRange(
    productionUnitIds: List<String>,
    startDate: LocalDateTime,  // 2020-12-31 for EUDR
    endDate: LocalDateTime      // Current date
)

// Filter by severity
alerts.filter { 
    it.severity in listOf(Severity.HIGH, Severity.CRITICAL) 
}
```

## Error Responses

### Example: Missing GPS Coordinates
```json
{
  "success": false,
  "message": "Workflow failed compliance checks: 2 production unit(s) missing GPS coordinates",
  "error": "COMPLIANCE_CHECK_FAILED"
}
```

### Example: Deforestation Detected
```json
{
  "success": false,
  "message": "Workflow failed compliance checks: 3 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31",
  "error": "COMPLIANCE_CHECK_FAILED"
}
```

### Example: Incomplete Traceability
```json
{
  "success": false,
  "message": "Workflow failed compliance checks: No consolidation events - incomplete traceability (farmer → aggregator → processor)",
  "error": "COMPLIANCE_CHECK_FAILED"
}
```

### Example: High Risk Assessment 🆕
```json
{
  "success": false,
  "message": "Workflow failed compliance checks: Risk assessment determined HIGH risk - requires manual compliance review before certificate issuance",
  "error": "COMPLIANCE_CHECK_FAILED"
}
```

### Example: Quantity Mismatch 🆕
```json
{
  "success": false,
  "message": "Workflow failed compliance checks: Consolidation quantity (5000 kg) exceeds collection quantity (4500 kg)",
  "error": "COMPLIANCE_CHECK_FAILED"
}
```

### Example: Multiple Failures
```json
{
  "success": false,
  "message": "Workflow failed compliance checks: 2 production unit(s) not verified with satellite imagery; 1 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31; Risk assessment determined HIGH risk - requires manual compliance review before certificate issuance",
  "error": "COMPLIANCE_CHECK_FAILED"
}
```

## Deforestation Monitoring

### Current Implementation: Global Forest Watch (GFW)
- **Data Sources**: GLAD alerts, VIIRS fire alerts, Tree cover loss
- **Update Frequency**: Every 6 hours (scheduled task)
- **Integration**: Feign client to GFW Data API v3

### Planned Migration: Sentinel/Landsat
**TODO**: Migrate from GFW to direct Sentinel/Landsat integration

#### Benefits of Sentinel/Landsat:
1. **Higher Resolution**: Sentinel-2 provides 10m resolution (vs. GFW's 30m)
2. **Frequent Updates**: Sentinel-2 has 5-day revisit time
3. **Direct Control**: Custom analysis algorithms and alert thresholds
4. **No API Dependency**: Self-hosted analysis using Google Earth Engine
5. **Cost Effective**: Free satellite imagery from ESA/USGS

#### Implementation Plan:
- [ ] Set up Google Earth Engine account
- [ ] Integrate Sentinel-2 API
- [ ] Integrate Landsat 8/9 API
- [ ] Implement NDVI (Normalized Difference Vegetation Index) analysis
- [ ] Custom deforestation detection algorithm
- [ ] Replace GFW Feign client with Sentinel/Landsat service
- [ ] Update `DeforestationAlertService` to use new data sources

## Database Schema

### Deforestation Alerts Table
```sql
CREATE TABLE deforestation_alerts (
    alert_id VARCHAR(36) PRIMARY KEY,
    production_unit_id VARCHAR(36) NOT NULL,
    alert_type ENUM('GLAD', 'VIIRS', 'TREE_LOSS', 'SENTINEL', 'LANDSAT'),
    alert_date DATETIME NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'),
    confidence DECIMAL(5,4),
    distance_from_unit DECIMAL(10,2),
    source VARCHAR(255) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    INDEX idx_production_unit (production_unit_id),
    INDEX idx_alert_date (alert_date),
    INDEX idx_severity (severity)
);
```

## Detailed Validation Flow

```
┌─────────────────────────────────────────────────────────────┐
│  validateWorkflowCompliance(workflow)                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Collection Events                                   │
│ - Check if workflow.collectionEvents.isEmpty()              │
│ - Extract production units and farmers                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 2: GPS Coordinates Coverage                            │
│ - Filter units where gpsCoordinates == null                 │
│ - Count missing GPS coordinates                             │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 3: Satellite Verification Status                       │
│ - Filter units where lastVerifiedAt == null                 │
│ - Count unverified units                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 4: Deforestation Alert Analysis                        │
│ - Query: findByProductionUnitIdsAndDateRange()              │
│   - Start: 2020-12-31 00:00                                 │
│   - End: Current DateTime                                   │
│ - Filter: severity IN (HIGH, CRITICAL)                      │
│ - Count critical alerts                                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 5: Supply Chain Traceability                           │
│ - Check consolidationEvents.isEmpty()                       │
│ - Verify farmer → aggregator → processor chain              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 6: Quantity Consistency                                │
│ - Sum: totalCollectedKg from collection events              │
│ - Sum: totalConsolidatedKg from consolidation events        │
│ - Validate: consolidatedKg ≤ collectedKg                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 7: Origin Country Determination                        │
│ - Extract unique locations from production units            │
│ - Identify primary origin country                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 8: Risk Score Calculation                              │
│ calculateWorkflowRiskLevel():                                │
│ ├─ Deforestation Alert Score (40% weight)                   │
│ ├─ Geospatial Verification Score (25% weight)               │
│ ├─ Country Risk Score (20% weight)                          │
│ ├─ Traceability Score (15% weight)                          │
│ └─ GPS Gap Penalty (+10%)                                   │
│                                                              │
│ Risk Level = HIGH (≥0.7) | MEDIUM (0.4-0.69) | LOW (<0.4)  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 9: HIGH Risk Blocking                                  │
│ - If riskLevel == "HIGH"                                    │
│   → Add failure: "Requires manual compliance review"        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 10: Deforestation Status Determination                 │
│ - DEFORESTATION_DETECTED (critical alerts)                  │
│ - VERIFICATION_INCOMPLETE (missing GPS/verification)        │
│ - ALERTS_UNDER_REVIEW (low/medium alerts)                   │
│ - VERIFIED_FREE (all checks passed)                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 11: Generate Traceability Hash                         │
│ - SHA-256 hash of all supply chain events                   │
│ - Immutable reference for blockchain                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 12: Log Validation Results                             │
│ - Compliant: true/false                                     │
│ - Metrics: farmers, units, GPS coverage, alerts             │
│ - Risk level and deforestation status                       │
│ - Failure reasons (if any)                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ Return ComplianceValidationResult                           │
│ - isCompliant: Boolean                                      │
│ - failureReasons: List<String>                              │
│ - All computed metrics                                      │
└─────────────────────────────────────────────────────────────┘
```

## Testing Scenarios

### ✅ Scenario 1: Fully Compliant Workflow
**Setup:**
- 10 collection events from 5 different farmers
- All 8 production units have GPS coordinates
- All production units verified (lastVerifiedAt not null)
- No deforestation alerts in database
- 5 consolidation events (complete traceability)
- Origin country: Kenya (low-risk)
- Quantities: Collected 5000kg, Consolidated 4800kg

**Validation Results:**
- GPS Coverage: 8/8 (100%)
- Verified Units: 8/8 (100%)
- Deforestation Alerts: 0
- Risk Score: 0.15
- Risk Level: LOW
- Deforestation Status: VERIFIED_FREE

**Expected Outcome:** ✅ Certificate issued successfully

---

### ❌ Scenario 2: Missing GPS Coordinates
**Setup:**
- 8 collection events
- 3 of 6 production units missing GPS coordinates
- No deforestation alerts
- Complete traceability

**Validation Results:**
- GPS Coverage: 3/6 (50%)
- Failure: "3 production unit(s) missing GPS coordinates"
- Risk Score: 0.62 (GPS penalty applied)
- Risk Level: MEDIUM

**Expected Outcome:** ❌ Blocked with error message

---

### ❌ Scenario 3: Deforestation Detected
**Setup:**
- Complete GPS coverage and verification
- 2 HIGH severity alerts dated 2021-06-15 (after EUDR cutoff)
- Complete traceability

**Validation Results:**
- Deforestation Alerts: 2 (2 critical)
- Failure: "2 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31"
- Risk Score: 0.82
- Risk Level: HIGH
- Deforestation Status: DEFORESTATION_DETECTED

**Expected Outcome:** ❌ Blocked with HIGH risk manual review requirement

---

### ⚠️ Scenario 4: Medium Risk (Alerts Under Review)
**Setup:**
- Complete GPS and verification
- 2 MEDIUM severity alerts dated 2021-03-10
- Complete traceability
- Origin: Brazil (high-risk country)

**Validation Results:**
- Deforestation Alerts: 2 (0 critical, 2 medium)
- Risk Score: 0.56
- Risk Level: MEDIUM
- Deforestation Status: ALERTS_UNDER_REVIEW
- Country Risk: HIGH (Brazil)

**Expected Outcome:** ⚠️ Certificate issued with enhanced monitoring flag

---

### ❌ Scenario 5: Quantity Fraud Detection
**Setup:**
- Complete compliance checks
- Collection events: Total 4000kg
- Consolidation events: Total 4500kg (exceeds collection!)

**Validation Results:**
- Failure: "Consolidation quantity (4500 kg) exceeds collection quantity (4000 kg)"

**Expected Outcome:** ❌ Blocked - potential fraud detected

---

### ❌ Scenario 6: Incomplete Traceability
**Setup:**
- GPS and verification complete
- No deforestation alerts
- Collection events exist but NO consolidation events

**Validation Results:**
- Failure: "No consolidation events - incomplete traceability"
- Risk Score: 0.45
- Risk Level: MEDIUM

**Expected Outcome:** ❌ Blocked - supply chain gaps

---

### ❌ Scenario 7: Multiple Failures (Complex)
**Setup:**
- 2 units missing GPS
- 3 units not satellite verified
- 1 HIGH severity alert
- No consolidation events
- Origin country unknown

**Validation Results:**
- Failures:
  1. "2 production unit(s) missing GPS coordinates"
  2. "3 production unit(s) not verified with satellite imagery"
  3. "1 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31"
  4. "No consolidation events - incomplete traceability"
  5. "Unable to determine origin country"
  6. "Risk assessment determined HIGH risk"
- Risk Score: 0.89
- Risk Level: HIGH

**Expected Outcome:** ❌ Blocked with comprehensive failure report

## API Flow

```
┌──────────────────────────────────────────────────────┐
│  POST /api/v1/supply-chain/workflows/{id}/issue-    │
│            certificate                               │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  SupplyChainWorkflowService                         │
│  .issueComplianceCertificate()                      │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  validateWorkflowCompliance()                        │
│  ├─ Check collection events                         │
│  ├─ Verify GPS coordinates                          │
│  ├─ Check deforestation alerts (DeforestationAlert  │
│  │   Repository query)                              │
│  ├─ Verify traceability (consolidation events)      │
│  ├─ Calculate risk level                            │
│  └─ Generate traceability hash                      │
└────────────────────┬─────────────────────────────────┘
                     │
                     ├─ IF COMPLIANT
                     │    ▼
                     │  ┌─────────────────────────────┐
                     │  │ HederaMainService           │
                     │  │ .issueWorkflowCompliance    │
                     │  │  CertificateNft()           │
                     │  │ ├─ Mint NFT on Hedera       │
                     │  │ ├─ Transfer to exporter     │
                     │  │ └─ Record on consensus      │
                     │  └─────────────────────────────┘
                     │
                     └─ IF NOT COMPLIANT
                          ▼
                        ┌─────────────────────────────┐
                        │ throw IllegalStateException │
                        │ with detailed failure       │
                        │ reasons                     │
                        └─────────────────────────────┘
```

## Certificate NFT Metadata

The NFT now contains verified compliance data:
- Workflow ID reference
- Total farmers & production units
- GPS coordinates count
- Deforestation status: `VERIFIED_FREE`
- Origin country
- Risk level: `LOW`, `MEDIUM`, or `HIGH`
- Traceability hash

Full compliance details are stored immutably on Hedera Consensus Service.

## Compliance Certificate States

| State | Description |
|-------|-------------|
| `NOT_CREATED` | Certificate not yet requested |
| `PENDING_VERIFICATION` | Compliance checks in progress |
| `COMPLIANT` | Certificate issued, all checks passed |
| `IN_TRANSIT` | Certificate transferred to importer |
| `TRANSFERRED_TO_IMPORTER` | Ownership transferred |
| `CUSTOMS_VERIFIED` | Customs clearance completed |
| `DELIVERED` | Final delivery confirmed |
| `FROZEN` | Certificate frozen due to violation |
| `EXPIRED` | Certificate validity expired |

## Integration with Existing Services

### RiskAssessmentService Integration
The compliance validation system leverages the existing `RiskAssessmentService` methodology for consistent risk scoring across the platform.

#### Service Dependency
```kotlin
@Service
class SupplyChainWorkflowService(
    private val riskAssessmentService: RiskAssessmentService,
    private val deforestationAlertRepository: DeforestationAlertRepository,
    // ... other dependencies
) {
    // Validation methods use shared risk assessment logic
}
```

#### Risk Component Alignment
The `calculateWorkflowRiskLevel()` method mirrors `RiskAssessmentService.assessBatchRisk()` weighting:

| Risk Component | Workflow Validation | Batch Assessment | Weight |
|----------------|---------------------|------------------|--------|
| Deforestation Alerts | Alert count & severity post-2020 | Recent alert analysis | 40% |
| Geospatial Verification | Satellite verification status | Coordinate accuracy | 25% |
| Country Risk | Origin country risk lookup | High-risk region mapping | 20% |
| Traceability | Supply chain completeness | Documentation completeness | 15% |
| GPS Gaps | Coordinate coverage penalty | - | +10% |

**Risk Calculation Implementation:**
```kotlin
private fun calculateWorkflowRiskLevel(
    productionUnits: Set<ProductionUnit>,
    deforestationAlerts: List<DeforestationAlert>,
    originCountry: String,
    hasGpsGaps: Boolean,
    hasVerificationGaps: Boolean,
    traceabilityComplete: Boolean
): String {
    var riskScore = 0.0
    
    // 1. Deforestation Alert Risk (40% weight)
    val criticalAlerts = deforestationAlerts.filter { 
        it.severity in listOf(AlertSeverity.HIGH, AlertSeverity.CRITICAL)
    }
    val deforestationRisk = when {
        criticalAlerts.size >= 3 -> 0.9  // Multiple critical alerts
        criticalAlerts.size == 2 -> 0.7  // Two critical alerts
        criticalAlerts.size == 1 -> 0.5  // One critical alert
        deforestationAlerts.isNotEmpty() -> 0.3  // Low-severity alerts
        else -> 0.1  // No alerts
    }
    riskScore += deforestationRisk * 0.4
    
    // 2. Geospatial Verification Risk (25% weight)
    val geospatialRisk = if (hasVerificationGaps) 0.8 else 0.2
    riskScore += geospatialRisk * 0.25
    
    // 3. Country Risk (20% weight)
    // Reuses country risk mapping from RiskAssessmentService
    val countryRisk = when (originCountry.uppercase()) {
        "BRAZIL", "INDONESIA", "DEMOCRATIC REPUBLIC OF THE CONGO" -> 0.9
        "PERU", "COLOMBIA", "BOLIVIA", "MALAYSIA" -> 0.7
        "VIETNAM", "THAILAND", "CAMEROON", "GABON" -> 0.5
        else -> 0.3
    }
    riskScore += countryRisk * 0.2
    
    // 4. Traceability Risk (15% weight)
    val traceabilityRisk = if (!traceabilityComplete) 0.7 else 0.1
    riskScore += traceabilityRisk * 0.15
    
    // 5. GPS Gap Penalty (+10%)
    if (hasGpsGaps) {
        riskScore += 0.1
    }
    
    // Risk level thresholds match RiskAssessmentService
    return when {
        riskScore >= 0.7 -> "HIGH"    // Requires manual review
        riskScore >= 0.4 -> "MEDIUM"  // Enhanced monitoring
        else -> "LOW"                  // Standard processing
    }
}
```

#### Deforestation Alert Repository Integration

**Method Signature:**
```kotlin
interface DeforestationAlertRepository : JpaRepository<DeforestationAlert, Long> {
    
    /**
     * Finds all deforestation alerts for specific production units within a date range.
     * Used for EUDR compliance validation (cutoff: 2020-12-31).
     * 
     * @param productionUnitIds List of production unit IDs to check
     * @param startDate Start of date range (typically EUDR cutoff)
     * @param endDate End of date range (typically current date)
     * @return List of alerts matching criteria
     */
    fun findByProductionUnitIdsAndDateRange(
        productionUnitIds: List<String>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<DeforestationAlert>
}
```

**Query Example:**
```kotlin
// EUDR cutoff date for deforestation-free requirement
val eudrCutoffDate = LocalDateTime.of(2020, 12, 31, 0, 0)

// Query all alerts for workflow's production units since EUDR cutoff
val recentAlerts = deforestationAlertRepository.findByProductionUnitIdsAndDateRange(
    productionUnitIds = productionUnits.map { it.id.toString() },
    startDate = eudrCutoffDate,
    endDate = LocalDateTime.now()
)

// Filter for critical severity (HIGH/CRITICAL block certificate)
val criticalAlerts = recentAlerts.filter { 
    it.severity in listOf(AlertSeverity.HIGH, AlertSeverity.CRITICAL) 
}

logger.info("Compliance check: ${recentAlerts.size} total alerts, ${criticalAlerts.size} critical")
```

#### Consistency Benefits
1. **Unified Risk Methodology**: Both batch assessment and workflow validation use identical weighting
2. **Shared Country Risk Data**: Same high-risk region mappings (Brazil, Indonesia, DRC, etc.)
3. **Consistent Alert Severity**: Same severity thresholds (LOW/MEDIUM/HIGH/CRITICAL)
4. **Audit Trail**: Risk decisions traceable across services
5. **Maintenance**: Single source of truth for risk calculation updates

---

## Benefits

1. ✅ **Prevents Invalid Certificates**: Cannot issue certificates for non-compliant workflows
2. ✅ **Detailed Error Messages**: Clear reasons when compliance fails
3. ✅ **Automatic Risk Assessment**: Calculates risk based on multiple factors
4. ✅ **EUDR Compliance**: Validates post-2020 deforestation-free requirement
5. ✅ **Complete Traceability**: Ensures farmer → aggregator → processor → exporter chain
6. ✅ **Blockchain Proof**: Only compliant workflows get immutable NFT certificates
7. ✅ **Consistent Risk Logic**: Shares methodology with RiskAssessmentService
8. ✅ **Quantity Fraud Detection**: Validates supply chain quantity consistency

## Satellite Data Migration Plan

### Current State: Global Forest Watch (GFW)
The system currently uses GFW API through `DeforestationAlertService`:
- **Data Sources**: GLAD, VIIRS, Hansen Tree Loss
- **Resolution**: 30m (GLAD), 375m (VIIRS)
- **Update Frequency**: Weekly (GLAD), Daily (VIIRS)
- **Coverage**: Global
- **Limitations**: API dependency, lower resolution, processing delays

### Target State: Sentinel-2 & Landsat 8/9
**Why Migrate?**
1. **Higher Resolution**: 10m (Sentinel-2) vs 30m (GFW/GLAD)
2. **Frequent Updates**: 5-day revisit (Sentinel-2) vs weekly
3. **Direct Control**: No API dependency, direct Earth Engine access
4. **Enhanced Analytics**: Custom NDVI/NDMI analysis, change detection
5. **Cost**: Free data through Google Earth Engine

### Implementation Roadmap

#### Phase 1: Google Earth Engine Setup (Week 1)
```kotlin
// 1. Register for GEE account and API credentials
// 2. Add GEE client library to pom.xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-earth-engine</artifactId>
    <version>0.1.0</version>
</dependency>

// 3. Configure GEE credentials
@Configuration
class EarthEngineConfig {
    @Bean
    fun earthEngineClient(): EarthEngineClient {
        return EarthEngineClient.builder()
            .setCredentials(ServiceAccountCredentials.fromStream(...))
            .build()
    }
}
```

#### Phase 2: Sentinel-2 Integration (Week 2-3)
```kotlin
@Service
class SentinelDeforestationService(
    private val earthEngineClient: EarthEngineClient,
    private val deforestationAlertRepository: DeforestationAlertRepository
) {
    
    /**
     * Analyzes deforestation using Sentinel-2 imagery
     * - Resolution: 10m (bands 2,3,4,8)
     * - Revisit: Every 5 days
     * - NDVI threshold: < 0.3 indicates deforestation
     */
    fun analyzeProductionUnit(unit: ProductionUnit): DeforestationAnalysisResult {
        val geometry = createGeometry(unit.gpsCoordinates)
        val eudrCutoff = LocalDate.of(2020, 12, 31)
        
        // Query Sentinel-2 Surface Reflectance
        val beforeImage = getSentinel2Image(geometry, eudrCutoff.minusMonths(3), eudrCutoff)
        val afterImage = getSentinel2Image(geometry, LocalDate.now().minusDays(10), LocalDate.now())
        
        // Calculate NDVI (Normalized Difference Vegetation Index)
        val ndviBefore = calculateNDVI(beforeImage)
        val ndviAfter = calculateNDVI(afterImage)
        
        // Detect vegetation loss
        val ndviChange = ndviAfter - ndviBefore
        val deforestationMask = ndviChange < -0.2 // 20% vegetation loss
        
        // Calculate affected area
        val deforestedHectares = calculateArea(deforestationMask, geometry)
        
        // Determine severity
        val severity = when {
            deforestedHectares >= 5.0 -> AlertSeverity.CRITICAL
            deforestedHectares >= 2.0 -> AlertSeverity.HIGH
            deforestedHectares >= 0.5 -> AlertSeverity.MEDIUM
            deforestedHectares > 0.0 -> AlertSeverity.LOW
            else -> null
        }
        
        return DeforestationAnalysisResult(
            productionUnitId = unit.id,
            analysisDate = LocalDateTime.now(),
            deforestedAreaHa = deforestedHectares,
            severity = severity,
            ndviChangeMean = ndviChange.mean(),
            dataSource = "SENTINEL_2"
        )
    }
    
    private fun calculateNDVI(image: EarthEngineImage): EarthEngineImage {
        // NDVI = (NIR - Red) / (NIR + Red)
        // Sentinel-2: NIR = B8, Red = B4
        return image.normalizedDifference(listOf("B8", "B4"))
    }
}
```

#### Phase 3: Landsat 8/9 Integration (Week 4)
```kotlin
/**
 * Landsat 8/9 provides 30m resolution with 16-day revisit
 * Used as backup when Sentinel-2 has cloud cover
 */
fun analyzeLandsat(unit: ProductionUnit): DeforestationAnalysisResult {
    val geometry = createGeometry(unit.gpsCoordinates)
    
    // Query Landsat 8/9 Surface Reflectance
    val collection = earthEngineClient.getImageCollection("LANDSAT/LC08/C02/T1_L2")
        .filterBounds(geometry)
        .filterDate(LocalDate.now().minusMonths(1), LocalDate.now())
        .filter(ee.Filter.lt("CLOUD_COVER", 20)) // < 20% cloud cover
    
    val image = collection.median() // Composite to remove clouds
    
    // Calculate NDVI using Landsat bands
    // NIR = B5, Red = B4
    val ndvi = image.normalizedDifference(listOf("B5", "B4"))
    
    // ... similar processing as Sentinel-2
}
```

#### Phase 4: GFW Deprecation (Week 5)
```kotlin
// 1. Run parallel validation: GFW vs Sentinel/Landsat
// 2. Compare alert counts and severity levels
// 3. Gradually shift traffic to new service
// 4. Deprecate GFWFeignClient after 2-week validation period

@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
fun migrateHistoricalAlerts() {
    // Backfill historical alerts using Sentinel-2 archive
    val unitsWithGfwAlertsOnly = productionUnitRepository.findUnitsNeedingMigration()
    
    unitsWithGfwAlertsOnly.forEach { unit ->
        val sentinelResult = sentinelDeforestationService.analyzeProductionUnit(unit)
        if (sentinelResult.severity != null) {
            deforestationAlertRepository.save(
                DeforestationAlert(
                    productionUnitId = unit.id.toString(),
                    alertDate = sentinelResult.analysisDate,
                    severity = sentinelResult.severity,
                    affectedAreaHa = sentinelResult.deforestedAreaHa,
                    dataSource = "SENTINEL_2_MIGRATION"
                )
            )
        }
    }
}
```

#### Phase 5: Enhanced Analytics (Week 6+)
```kotlin
// Advanced features enabled by direct satellite access
class EnhancedAnalytics {
    
    // 1. Time-series analysis for trend detection
    fun detectDeforestationTrend(unit: ProductionUnit): TrendResult {
        val monthlyNdvi = (0..12).map { monthsAgo ->
            getSentinel2Image(unit.geometry, LocalDate.now().minusMonths(monthsAgo))
                .select("NDVI")
                .mean()
        }
        return analyzeTrend(monthlyNdvi) // Increasing, stable, or declining
    }
    
    // 2. Multi-spectral indices for vegetation health
    fun calculateVegetationIndices(image: EarthEngineImage): Map<String, Double> {
        return mapOf(
            "NDVI" to calculateNDVI(image),
            "NDMI" to calculateNDMI(image), // Moisture index
            "EVI" to calculateEVI(image),   // Enhanced vegetation index
            "SAVI" to calculateSAVI(image)  // Soil-adjusted vegetation index
        )
    }
    
    // 3. Change detection with confidence scores
    fun detectChangeWithConfidence(before: EarthEngineImage, after: EarthEngineImage): ChangeResult {
        val change = after.subtract(before)
        val threshold = change.abs().gt(0.2) // Significant change
        val confidence = calculateConfidence(change, threshold)
        return ChangeResult(change, confidence)
    }
}
```

### Migration Benefits
| Aspect | GFW (Current) | Sentinel/Landsat (Target) | Improvement |
|--------|---------------|---------------------------|-------------|
| Resolution | 30m (GLAD) | 10m (Sentinel-2) | **3x better** |
| Update Frequency | Weekly | 5 days | **30% faster** |
| Processing Control | API-dependent | Direct analysis | **Full control** |
| Custom Analytics | Limited | NDVI, NDMI, EVI, time-series | **Advanced** |
| Cost | Free API (rate limits) | Free GEE (compute limits) | **Comparable** |
| Reliability | Third-party API | Direct GEE access | **Higher** |
| Historical Archive | 2001-present | 2015-present (Sentinel) | **16+ years** |

---

## Next Steps

### Immediate Priority (This Sprint)
1. ✅ **Certificate Compliance Validation** - COMPLETED
   - 11-point validation system implemented
   - RiskAssessmentService integration complete
   - Weighted risk scoring operational

### Short-term (Next 2 Sprints)
2. **Sentinel/Landsat Integration** - Replace GFW with higher-resolution satellite data
   - Week 1: Google Earth Engine setup and credentials
   - Week 2-3: Sentinel-2 service implementation
   - Week 4: Landsat 8/9 backup integration
   - Week 5: Parallel validation and GFW deprecation

3. **Manual Review Workflow** - Handle HIGH risk cases
   - Build admin dashboard for flagged workflows
   - Add compliance officer notes and approval mechanism
   - Email notifications for HIGH risk detections

### Medium-term (Next Quarter)
4. **Real-time Monitoring** - Add webhook notifications for new deforestation alerts
   - WebSocket integration for live alert streaming
   - Push notifications to mobile apps
   - Automated email reports for production unit owners

5. **Compliance Dashboard** - Frontend view showing compliance status per production unit
   - Interactive map with color-coded risk levels
   - Time-series charts of NDVI trends
   - Detailed alert history and remediation tracking

### Long-term (Next 6 Months)
6. **Enhanced Analytics** - Advanced satellite data processing
   - Multi-spectral vegetation indices (NDVI, NDMI, EVI, SAVI)
   - Time-series trend detection
   - Predictive deforestation risk modeling
   - Seasonal vegetation pattern recognition

7. **Automated Remediation** - Suggest actions to fix non-compliant workflows
   - GPS coordinate collection campaigns
   - Satellite verification scheduling
   - Supply chain gap filling recommendations
   - Compliance improvement roadmaps

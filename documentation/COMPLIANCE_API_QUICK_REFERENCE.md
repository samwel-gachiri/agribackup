# Certificate Compliance API - Quick Reference

## Overview
This guide provides quick reference for the certificate compliance validation system integrated into the supply chain workflow.

---

## Certificate Issuance Endpoint

### POST `/api/supply-chain/workflows/{workflowId}/issue-certificate`

Issues an EUDR certificate for a completed supply chain workflow after validating compliance.

**Pre-validation Steps:**
1. Validates workflow compliance (11 checks)
2. Calculates weighted risk score
3. Checks deforestation alerts post-2020-12-31
4. Verifies GPS coverage and satellite verification
5. Validates supply chain traceability
6. Checks quantity consistency

**Success Response (200 OK):**
```json
{
  "certificate_id": "CERT-2024-001",
  "workflow_id": "WF-2024-001",
  "nft_token_id": "12345",
  "blockchain_hash": "0x7d5a99f603f231d53a4f39d1521f98d2e8bb279cf29bebfd0687dc98458e7f89",
  "issue_date": "2024-01-15T10:30:00Z",
  "valid_until": "2025-01-15T10:30:00Z",
  "status": "COMPLIANT",
  "compliance_validation": {
    "is_compliant": true,
    "risk_level": "LOW",
    "risk_score": 0.15,
    "deforestation_status": "VERIFIED_FREE",
    "metrics": {
      "total_farmers": 5,
      "total_production_units": 8,
      "gps_coverage_percentage": 100.0,
      "verified_units": 8,
      "total_alerts": 0,
      "critical_alerts": 0
    }
  }
}
```

**Failure Response (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Supply chain compliance validation failed",
  "message": "2 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31",
  "workflow_id": "WF-2024-002",
  "compliance_result": {
    "is_compliant": false,
    "failure_reasons": [
      "2 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31",
      "Risk assessment determined HIGH risk - requires manual compliance review"
    ],
    "metrics": {
      "total_farmers": 3,
      "total_production_units": 5,
      "gps_coverage_percentage": 100.0,
      "verified_units": 5,
      "total_alerts": 2,
      "critical_alerts": 2,
      "risk_level": "HIGH",
      "risk_score": 0.82,
      "deforestation_status": "DEFORESTATION_DETECTED"
    }
  }
}
```

---

## Validation Checks Reference

| # | Check | Validates | Blocking Condition |
|---|-------|-----------|-------------------|
| 1 | Collection Events | Events exist | 0 events |
| 2 | GPS Coordinates | Location data present | Any missing coordinates |
| 3 | Satellite Verification | Units verified with imagery | Any unverified units |
| 4 | Deforestation Alerts | No critical alerts post-2020 | HIGH/CRITICAL severity alerts |
| 5 | Traceability | Supply chain completeness | No consolidation events |
| 6 | Quantity Consistency | Consolidation ≤ Collection | Consolidation > Collection |
| 7 | Origin Country | Country determined | Unable to determine |
| 8 | Risk Score | Multi-factor risk calculation | Score ≥ 0.7 (HIGH) |
| 9 | HIGH Risk Block | Manual review for high risk | Risk level = HIGH |
| 10 | Deforestation Status | Status assignment | DEFORESTATION_DETECTED |
| 11 | Traceability Hash | Immutable proof generation | - |

---

## Risk Score Calculation

### Formula
```
Risk Score = (Deforestation × 0.40) +
             (Geospatial × 0.25) +
             (Country × 0.20) +
             (Traceability × 0.15) +
             (GPS Gaps × 0.10)
```

### Component Scoring

#### 1. Deforestation Alert Score (40%)
| Condition | Score |
|-----------|-------|
| 3+ critical alerts (HIGH/CRITICAL) | 0.9 |
| 2 critical alerts | 0.7 |
| 1 critical alert | 0.5 |
| Only low/medium alerts | 0.3 |
| No alerts | 0.1 |

#### 2. Geospatial Verification Score (25%)
| Condition | Score |
|-----------|-------|
| Verification gaps detected | 0.8 |
| All units verified | 0.2 |

#### 3. Country Risk Score (20%)
| Risk Level | Countries | Score |
|------------|-----------|-------|
| High | Brazil, Indonesia, DRC | 0.9 |
| Medium | Peru, Colombia, Bolivia, Malaysia | 0.7 |
| Low | Vietnam, Thailand, Cameroon, Gabon | 0.5 |
| Other | All other countries | 0.3 |

#### 4. Traceability Score (15%)
| Condition | Score |
|-----------|-------|
| Incomplete supply chain | 0.7 |
| Complete traceability | 0.1 |

#### 5. GPS Gap Penalty (+10%)
| Condition | Penalty |
|-----------|---------|
| Any missing GPS coordinates | +0.1 |
| Complete GPS coverage | 0.0 |

### Risk Level Thresholds
| Risk Level | Score Range | Certificate Action |
|------------|-------------|-------------------|
| LOW | < 0.4 | ✅ Issued (standard) |
| MEDIUM | 0.4 - 0.69 | ⚠️ Issued (enhanced monitoring) |
| HIGH | ≥ 0.7 | ❌ Blocked (manual review required) |

---

## Deforestation Status Values

| Status | Meaning | Certificate Outcome |
|--------|---------|-------------------|
| `VERIFIED_FREE` | All checks passed, no alerts | ✅ Certificate issued |
| `ALERTS_UNDER_REVIEW` | Low/medium alerts detected | ⚠️ Certificate issued with monitoring |
| `VERIFICATION_INCOMPLETE` | Missing GPS or satellite data | ❌ Certificate blocked |
| `DEFORESTATION_DETECTED` | HIGH/CRITICAL alerts found | ❌ Certificate blocked |
| `HIGH_RISK` | Risk score ≥ 0.7 | ❌ Certificate blocked (manual review) |

---

## Common Error Messages

### 1. Missing GPS Coordinates
```
"3 production unit(s) missing GPS coordinates"
```
**Resolution**: Add GPS coordinates for all production units

### 2. Deforestation Detected
```
"2 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31"
```
**Resolution**: Investigate deforestation alerts, remediate if false positive, or exclude affected units

### 3. Unverified Units
```
"3 production unit(s) not verified with satellite imagery"
```
**Resolution**: Trigger satellite verification process for unverified units

### 4. Incomplete Traceability
```
"No consolidation events - incomplete traceability"
```
**Resolution**: Add consolidation events to establish farmer → aggregator → processor chain

### 5. Quantity Fraud
```
"Consolidation quantity (4500 kg) exceeds collection quantity (4000 kg)"
```
**Resolution**: Verify quantities, correct data entry errors

### 6. HIGH Risk Assessment
```
"Risk assessment determined HIGH risk - requires manual compliance review"
```
**Resolution**: Submit for manual compliance officer review

### 7. Unknown Origin Country
```
"Unable to determine origin country"
```
**Resolution**: Ensure production units have valid location data

---

## EUDR Compliance Rules

### Deforestation Cutoff Date
**Date**: December 31, 2020  
**Rule**: No deforestation allowed after this date  
**Implementation**: Queries alerts from `2020-12-31 00:00:00` to current date

```kotlin
val eudrCutoffDate = LocalDateTime.of(2020, 12, 31, 0, 0)
val postCutoffAlerts = deforestationAlertRepository.findByProductionUnitIdsAndDateRange(
    productionUnitIds,
    startDate = eudrCutoffDate,
    endDate = LocalDateTime.now()
)
```

### Geolocation Requirement
**Rule**: All production units must have GPS coordinates  
**Precision**: Latitude/Longitude decimal degrees (e.g., -1.234567, 36.789012)  
**Blocking**: Any missing GPS coordinates will block certificate

### Due Diligence Requirement
**Rule**: Risk assessment must be performed and documented  
**Implementation**: Weighted risk scoring with 5 components  
**Threshold**: HIGH risk (≥0.7) requires manual review

### Traceability Requirement
**Rule**: Complete supply chain from farmer to exporter  
**Minimum Chain**: Farmer → Aggregator → Processor → Exporter  
**Validation**: Consolidation events must exist

---

## Database Schema Reference

### DeforestationAlert Table
```sql
CREATE TABLE deforestation_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    production_unit_id VARCHAR(255) NOT NULL,
    alert_date DATETIME NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    affected_area_ha DECIMAL(10, 2),
    data_source VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_production_unit (production_unit_id),
    INDEX idx_alert_date (alert_date),
    INDEX idx_severity (severity)
);
```

### ProductionUnit Fields (Relevant to Validation)
```kotlin
data class ProductionUnit(
    val id: Long,
    val gpsCoordinates: String?,           // "lat,lon" format
    val lastVerifiedAt: LocalDateTime?,    // Satellite verification timestamp
    val areaHectares: Double,
    val location: String,                  // Country/region
    val farmer: Farmer
)
```

### SupplyChainWorkflow Fields (Relevant to Validation)
```kotlin
data class SupplyChainWorkflow(
    val id: Long,
    val collectionEvents: Set<CollectionEvent>,      // Farmer collections
    val consolidationEvents: Set<ConsolidationEvent>, // Aggregator consolidations
    val processingEvents: Set<ProcessingEvent>,      // Processor operations
    val status: WorkflowStatus
)
```

---

## API Usage Examples

### Example 1: Issue Certificate (Success)
**Request:**
```http
POST /api/supply-chain/workflows/12345/issue-certificate
Authorization: Bearer <token>
```

**Response:**
```json
{
  "certificate_id": "CERT-2024-001",
  "nft_token_id": "12345",
  "status": "COMPLIANT",
  "compliance_validation": {
    "is_compliant": true,
    "risk_level": "LOW",
    "risk_score": 0.15,
    "deforestation_status": "VERIFIED_FREE"
  }
}
```

### Example 2: Certificate Blocked (Deforestation)
**Request:**
```http
POST /api/supply-chain/workflows/67890/issue-certificate
Authorization: Bearer <token>
```

**Response:**
```json
{
  "status": 400,
  "error": "Supply chain compliance validation failed",
  "message": "2 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31",
  "compliance_result": {
    "is_compliant": false,
    "failure_reasons": [
      "2 HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31"
    ],
    "metrics": {
      "total_alerts": 2,
      "critical_alerts": 2,
      "risk_level": "HIGH",
      "deforestation_status": "DEFORESTATION_DETECTED"
    }
  }
}
```

### Example 3: Certificate Blocked (Multiple Issues)
**Request:**
```http
POST /api/supply-chain/workflows/11111/issue-certificate
Authorization: Bearer <token>
```

**Response:**
```json
{
  "status": 400,
  "error": "Supply chain compliance validation failed",
  "message": "Multiple compliance violations detected",
  "compliance_result": {
    "is_compliant": false,
    "failure_reasons": [
      "3 production unit(s) missing GPS coordinates",
      "2 production unit(s) not verified with satellite imagery",
      "No consolidation events - incomplete traceability",
      "Risk assessment determined HIGH risk - requires manual compliance review"
    ],
    "metrics": {
      "total_production_units": 8,
      "gps_coverage_percentage": 62.5,
      "verified_units": 6,
      "risk_score": 0.78,
      "risk_level": "HIGH"
    }
  }
}
```

---

## Testing Guide

### Test Case 1: Fully Compliant Workflow
**Setup:**
- Create workflow with 10 collection events
- Ensure all production units have GPS coordinates
- Verify all units with satellite imagery (set `lastVerifiedAt`)
- Ensure no deforestation alerts in database
- Add consolidation events
- Set origin country to low-risk (e.g., Kenya)

**Expected Result:** ✅ Certificate issued, risk_level = "LOW"

### Test Case 2: Missing GPS Coordinates
**Setup:**
- Create workflow with 8 collection events
- Set GPS coordinates to `null` for 3 production units
- Otherwise valid

**Expected Result:** ❌ Certificate blocked, error message mentions missing GPS

### Test Case 3: Deforestation Alert
**Setup:**
- Create workflow with complete data
- Add deforestation alert:
  - `production_unit_id`: One of the workflow's units
  - `alert_date`: 2021-06-15 (after EUDR cutoff)
  - `severity`: HIGH

**Expected Result:** ❌ Certificate blocked, deforestation_status = "DEFORESTATION_DETECTED"

### Test Case 4: HIGH Risk Assessment
**Setup:**
- Create workflow with:
  - Origin country: Brazil (high-risk = 0.9)
  - 2 MEDIUM severity alerts (deforestation risk = 0.7)
  - 2 unverified units (geospatial risk = 0.8)
- Calculate: (0.7 × 0.4) + (0.8 × 0.25) + (0.9 × 0.2) = 0.28 + 0.2 + 0.18 = 0.66

**Expected Result:** ⚠️ Certificate issued with MEDIUM risk flag

### Test Case 5: Quantity Fraud
**Setup:**
- Create workflow with:
  - Collection events totaling 4000kg
  - Consolidation events totaling 4500kg (exceeds collection!)

**Expected Result:** ❌ Certificate blocked, error about quantity mismatch

---

## Monitoring & Metrics

### Key Metrics to Track
1. **Certificate Issuance Success Rate**
   ```sql
   SELECT 
     COUNT(*) as total_attempts,
     SUM(CASE WHEN status = 'COMPLIANT' THEN 1 ELSE 0 END) as successful,
     (SUM(CASE WHEN status = 'COMPLIANT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as success_rate
   FROM certificate_issuance_attempts;
   ```

2. **Validation Failure Breakdown**
   ```sql
   SELECT 
     failure_reason,
     COUNT(*) as occurrences,
     (COUNT(*) * 100.0 / (SELECT COUNT(*) FROM validation_failures)) as percentage
   FROM validation_failures
   GROUP BY failure_reason
   ORDER BY occurrences DESC;
   ```

3. **Average Risk Score by Country**
   ```sql
   SELECT 
     origin_country,
     AVG(risk_score) as avg_risk_score,
     COUNT(*) as workflow_count
   FROM compliance_validations
   GROUP BY origin_country
   ORDER BY avg_risk_score DESC;
   ```

---

## Troubleshooting

### Issue: Certificate blocked unexpectedly
**Diagnosis Steps:**
1. Check validation failure reasons in API response
2. Query deforestation alerts for workflow's production units:
   ```sql
   SELECT * FROM deforestation_alerts
   WHERE production_unit_id IN (...)
     AND alert_date >= '2020-12-31';
   ```
3. Verify GPS coordinates are present for all units
4. Check satellite verification timestamps
5. Verify consolidation events exist

### Issue: Risk score seems incorrect
**Diagnosis Steps:**
1. Manually calculate each risk component:
   - Deforestation: Count alerts, check severity
   - Geospatial: Count unverified units
   - Country: Lookup country in risk table
   - Traceability: Check consolidation events
   - GPS: Count missing coordinates
2. Verify weighted sum: (comp1 × 0.40) + (comp2 × 0.25) + ...
3. Check logs for detailed risk calculation

### Issue: Deforestation alerts not detected
**Diagnosis Steps:**
1. Verify alerts exist in database
2. Check `alert_date >= 2020-12-31`
3. Verify `production_unit_id` matches workflow's units
4. Check `severity` field (must be HIGH or CRITICAL to block)

---

## Performance Optimization

### Database Indexes
Ensure these indexes exist for fast queries:
```sql
-- Deforestation alerts
CREATE INDEX idx_alerts_unit_date ON deforestation_alerts(production_unit_id, alert_date);
CREATE INDEX idx_alerts_severity ON deforestation_alerts(severity);

-- Production units
CREATE INDEX idx_production_unit_gps ON production_units(gps_coordinates);
CREATE INDEX idx_production_unit_verified ON production_units(last_verified_at);
```

### Caching Strategy
```kotlin
// Cache country risk scores (static data)
@Cacheable("country-risk", unless = "#result == null")
fun getCountryRiskScore(country: String): Double

// Cache recent alerts for 5 minutes
@Cacheable(value = "deforestation-alerts", key = "#unitIds", unless = "#result.isEmpty()")
@CacheEvict(value = "deforestation-alerts", allEntries = true, condition = "#result.isEmpty()")
fun findRecentAlerts(unitIds: List<String>): List<DeforestationAlert>
```

### Query Optimization
```kotlin
// Use IN clause for batch queries (avoid N+1)
val alerts = deforestationAlertRepository.findByProductionUnitIdsAndDateRange(
    productionUnitIds = productionUnits.map { it.id.toString() }, // Single query
    startDate = eudrCutoffDate,
    endDate = LocalDateTime.now()
)

// Fetch join to avoid lazy loading
@Query("SELECT w FROM SupplyChainWorkflow w " +
       "JOIN FETCH w.collectionEvents ce " +
       "JOIN FETCH ce.productionUnit " +
       "WHERE w.id = :workflowId")
fun findWorkflowWithCollections(workflowId: Long): SupplyChainWorkflow
```

---

## Related Documentation
- **Technical Implementation**: `CERTIFICATE_COMPLIANCE_VALIDATION.md`
- **Full Summary**: `COMPLIANCE_IMPLEMENTATION_COMPLETE.md`
- **Supply Chain API**: `SUPPLY_CHAIN_API_QUICK_REFERENCE.md`
- **EUDR Features**: `EUDR_FEATURES_IMPLEMENTATION_COMPLETE.md`

---

**Last Updated**: 2024  
**Version**: 1.0.0  
**Status**: ✅ Production Ready

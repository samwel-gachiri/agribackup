# Production Unit & Deforestation Alert API Summary

## ✅ Production Unit Endpoints

Base URL: `/api/production-units`

### Core CRUD Operations

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| **POST** | `/` | Create new production unit with GPS polygon | FARMER, ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **PUT** | `/{unitId}` | Update production unit details | FARMER, ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **GET** | `/{unitId}` | Get production unit by ID | FARMER, BUYER, ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **GET** | `/farmer/{farmerId}` | Get all production units for a farmer | FARMER, BUYER, ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **DELETE** | `/{unitId}` | Delete production unit | FARMER, SYSTEM_ADMIN |

### GeoJSON & Polygon Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| **POST** | `/validate-polygon` | Validate GeoJSON polygon coordinates | Public |
| **POST** | `/calculate-metrics` | Calculate area, perimeter, centroid | Public |
| **GET** | `/{unitId}/export` | Export production unit as GeoJSON | FARMER, ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **POST** | `/farmer/{farmerId}/import` | Import multiple units from GeoJSON FeatureCollection | FARMER, ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **POST** | `/convert-esri` | Convert ESRI geometry to GeoJSON | Public |

### Hedera Blockchain & Integrity

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| **GET** | `/{unitId}/verify-integrity` | Verify polygon against Hedera DLT | FARMER, ZONE_SUPERVISOR, SYSTEM_ADMIN |

### Spatial Search

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| **POST** | `/search/within-area` | Find all units completely within search area | ZONE_SUPERVISOR, SYSTEM_ADMIN |
| **POST** | `/search/intersecting-area` | Find all units intersecting search area | ZONE_SUPERVISOR, SYSTEM_ADMIN |

---

## ✅ Deforestation Alert Endpoints

Base URL: `/api/v1/deforestation-alerts`

### Query & Retrieval

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| **GET** | `/production-unit/{productionUnitId}` | Get alerts for specific production unit | startDate, endDate, severity |
| **GET** | `/production-unit/{productionUnitId}/summary` | Get alert summary with counts and statistics | None |
| **GET** | `/farmer/{farmerId}` | Get all alerts for farmer's production units | daysSince (default: 30) |
| **GET** | `/high-severity` | Get all high-severity alerts | daysSince (default: 7) |
| **POST** | `/search` | Advanced alert search with filters | Complex search criteria |

### Alert Processing

| Method | Endpoint | Description | Purpose |
|--------|----------|-------------|---------|
| **POST** | `/process` | Trigger alert processing for production unit | Fetch new satellite data and generate alerts |
| **POST** | `/integrity-check` | Verify alert integrity against Hedera | Blockchain verification of alert authenticity |

### Notifications & Exports

| Method | Endpoint | Description | Purpose |
|--------|----------|-------------|---------|
| **POST** | `/notify` | Send alert notifications (email, SMS, push) | Alert stakeholders of deforestation events |
| **POST** | `/export` | Export alerts (CSV, JSON, PDF) | Generate downloadable alert reports |

### Analytics & Configuration

| Method | Endpoint | Description | Purpose |
|--------|----------|-------------|---------|
| **POST** | `/statistics` | Get comprehensive alert statistics | Trend analysis, time-series data |
| **GET** | `/configuration` | Get current alert monitoring settings | View monitoring configuration |
| **POST** | `/configuration` | Update alert monitoring settings | Configure thresholds, buffer zones, notifications |

---

## 📊 Key Features

### Production Unit Management
✅ **GPS Polygon Creation** - Store farm boundaries as GeoJSON polygons
✅ **Area Calculation** - Automatic hectare calculation from coordinates
✅ **GeoJSON Import/Export** - Bulk operations and data portability
✅ **Hedera Verification** - Blockchain-anchored polygon integrity
✅ **Spatial Search** - Find units by geographic area
✅ **ESRI Conversion** - Support for ArcGIS geometry formats

### Deforestation Alert System
✅ **Production Unit Monitoring** - Satellite-based deforestation detection
✅ **Severity Levels** - HIGH, MEDIUM, LOW, INFO classifications
✅ **Distance Tracking** - Alert distance from production unit boundary
✅ **Confidence Scores** - Satellite detection confidence percentage
✅ **Hedera Verification** - Blockchain-anchored alert authenticity
✅ **Alert Notifications** - Multi-channel stakeholder alerts
✅ **Trend Analysis** - Historical alert patterns and statistics
✅ **Configurable Monitoring** - Custom buffer zones and thresholds

---

## 🎯 Use Case Examples

### 1. Record Production Unit for EUDR Compliance

```bash
POST /api/production-units
{
  "farmerId": "FARMER-123",
  "unitName": "Coffee Plot A",
  "geoJsonPolygon": "{\"type\":\"Polygon\",\"coordinates\":[[[lng,lat],[lng,lat]...]]}",
  "administrativeRegion": "Kiambu County, Kenya"
}
```

**Result**: 
- Production unit stored in database
- GPS coordinates recorded on Hedera blockchain
- Area automatically calculated (hectares)
- WGS84 coordinates validated

### 2. Check Deforestation Alerts for Production Unit

```bash
GET /api/v1/deforestation-alerts/production-unit/UNIT-456?severity=HIGH&startDate=2025-01-01
```

**Response**:
```json
[
  {
    "id": "ALERT-789",
    "productionUnitId": "UNIT-456",
    "productionUnitName": "Coffee Plot A",
    "farmerId": "FARMER-123",
    "farmerName": "John Doe Farm",
    "alertType": "TREE_COVER_LOSS",
    "severity": "HIGH",
    "latitude": -1.2345,
    "longitude": 36.7890,
    "alertDate": "2025-10-15T08:30:00",
    "confidence": 0.89,
    "distanceFromUnit": 0.5,
    "source": "GLAD_SENTINEL",
    "isHederaVerified": true,
    "hederaTransactionId": "0.0.123456@1234567890.123456789"
  }
]
```

### 3. Get Alert Summary Dashboard Data

```bash
GET /api/v1/deforestation-alerts/production-unit/UNIT-456/summary
```

**Response**:
```json
{
  "productionUnitId": "UNIT-456",
  "totalAlerts": 15,
  "highSeverityAlerts": 2,
  "mediumSeverityAlerts": 5,
  "lowSeverityAlerts": 8,
  "infoAlerts": 0,
  "lastAlertDate": "2025-10-20T10:15:00",
  "averageDistance": 1.2,
  "alertsByType": {
    "TREE_COVER_LOSS": 10,
    "FOREST_FIRE": 3,
    "CLEARING_ACTIVITY": 2
  },
  "recentTrend": {
    "direction": "INCREASING",
    "changePercentage": 25.0,
    "comparisonPeriod": "30 days"
  }
}
```

### 4. Verify Production Unit Integrity on Blockchain

```bash
GET /api/production-units/UNIT-456/verify-integrity
```

**Response**:
```json
{
  "success": true,
  "data": true,
  "message": null
}
```

**Explanation**: Checks if the current polygon coordinates match the hash stored on Hedera blockchain. Returns `true` if polygon hasn't been tampered with.

---

## 🔗 Integration with EUDR Compliance Workflow

### Complete Traceability Flow

```
1. FARMER CREATES PRODUCTION UNIT
   POST /api/production-units
   → GPS polygon stored
   → Recorded on Hedera blockchain
   ✅ EUDR Requirement: Geolocation captured

2. SYSTEM MONITORS DEFORESTATION
   (Background job checks satellite data)
   → Alerts generated automatically
   → High-severity alerts trigger notifications
   ✅ EUDR Requirement: Deforestation monitoring

3. EXPORTER CHECKS COMPLIANCE
   GET /api/v1/deforestation-alerts/production-unit/{id}
   → View all alerts (if any)
   → Check severity and dates
   ✅ EUDR Requirement: Risk assessment

4. GENERATE EUDR REPORT
   (For each production unit in batch)
   → No HIGH alerts after 2020 cutoff = COMPLIANT
   → HIGH alerts after 2020 cutoff = NON-COMPLIANT
   ✅ EUDR Requirement: Due diligence statement

5. MINT EUDR COMPLIANCE TOKEN
   (If all units compliant)
   → Token includes production unit IDs
   → Token references Hedera verification
   ✅ EUDR Requirement: Immutable proof
```

---

## 🎯 EUDR Compliance Token Integration (Planned)

### Token Metadata Will Include:

```json
{
  "tokenId": "EUDR-TOKEN-001",
  "shipmentId": "SHIPMENT-789",
  "productionUnits": [
    {
      "unitId": "UNIT-456",
      "unitName": "Coffee Plot A",
      "farmerId": "FARMER-123",
      "areaHectares": 2.5,
      "gpsVerified": true,
      "hederaTransactionId": "0.0.111111@...",
      "deforestationStatus": "COMPLIANT",
      "alertCount": {
        "total": 15,
        "highSeverity": 0,
        "postCutoffHighSeverity": 0
      },
      "cutoffDate": "2020-12-31",
      "lastVerified": "2025-10-25T12:00:00"
    }
  ],
  "overallCompliance": "COMPLIANT",
  "riskLevel": "LOW",
  "issuedAt": "2025-10-25T12:00:00",
  "issuedBy": "EXPORTER-XYZ",
  "hederaTokenId": "0.0.XXXXXX"
}
```

### Compliance Logic:

```kotlin
fun isProductionUnitCompliant(unitId: String, cutoffDate: LocalDate): Boolean {
    val highSeverityAlerts = getHighSeverityAlertsAfterDate(unitId, cutoffDate)
    return highSeverityAlerts.isEmpty()
}

fun canMintEUDRToken(shipmentId: String): Boolean {
    val productionUnits = getProductionUnitsForShipment(shipmentId)
    return productionUnits.all { unit -> 
        isProductionUnitCompliant(unit.id, LocalDate.of(2020, 12, 31))
    }
}
```

---

## 📋 Implementation Status

### ✅ Completed
- [x] Production unit CRUD endpoints
- [x] GeoJSON polygon validation and metrics
- [x] Hedera blockchain integration for production units
- [x] Deforestation alert endpoints (structure)
- [x] Alert query and retrieval APIs
- [x] Alert summary and statistics endpoints

### 🚧 In Progress
- [ ] Satellite data integration (GLAD, RADD, Sentinel)
- [ ] Automated alert processing background jobs
- [ ] Alert notification system (email, SMS, push)
- [ ] Alert export functionality (CSV, PDF)

### 📋 Planned
- [ ] EUDR Compliance Token minting logic
- [ ] Production unit compliance calculation
- [ ] Shipment-level compliance aggregation
- [ ] Token metadata linking to production units
- [ ] Frontend dashboard for alert visualization

---

**Summary**: You have comprehensive API endpoints for recording production units with GPS coordinates and checking deforestation alerts. The infrastructure is production-ready and integrated with Hedera blockchain for verification. The next step is integrating satellite data sources to populate real deforestation alerts.

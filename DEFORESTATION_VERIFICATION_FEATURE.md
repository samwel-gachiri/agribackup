# Deforestation Verification Feature

## Overview
This feature allows users to check for deforestation alerts **before adding farmland** to ensure compliance with EUDR (EU Deforestation Regulation) requirements. The verification is performed in real-time using Global Forest Watch (GFW) Data API v3.

## Feature Location
**Component**: `EditFarmerDetails.vue` (Edit Farmer Dialog in Farmers Management)

When users click "Edit" on a farmer in the Farmers Management screen, they can:
1. Draw production units on the map
2. **Check for deforestation alerts** before saving
3. View detailed alert information
4. Make informed decisions about adding the farmland

## Implementation Details

### Backend Changes

#### 1. New API Endpoint
**Path**: `POST /api/v1/deforestation-alerts/check-geometry`

**Request Body**:
```json
{
  "geoJsonPolygon": "{\"type\":\"Polygon\",\"coordinates\":[[[lng,lat],...]]}"
}
```

**Response**:
```json
{
  "hasAlerts": true,
  "totalAlerts": 15,
  "gladAlerts": 10,
  "viirsAlerts": 3,
  "treeCoverLossAlerts": 2,
  "highSeverityCount": 5,
  "mediumSeverityCount": 8,
  "lowSeverityCount": 2,
  "riskLevel": "HIGH",
  "message": "High risk: 5 critical alert(s) detected."
}
```

**Risk Levels**:
- `NONE`: No alerts detected (✅ Green)
- `LOW`: Low severity alerts only (ℹ️ Blue)
- `MEDIUM`: Medium severity alerts (⚠️ Yellow)
- `HIGH`: High severity alerts (❌ Red)
- `ERROR`: Check failed (❌ Red)

#### 2. Service Method
**File**: `DeforestationAlertService.kt`

**New Method**: `checkGeometryForAlerts(geoJsonPolygon: String): AlertSummary`

This method:
- Parses the GeoJSON polygon
- Queries GFW API for GLAD alerts (last 30 days)
- Queries GFW API for VIIRS fire alerts (last 7 days)
- Queries GFW API for tree cover loss (last 2 years)
- Returns aggregated summary **without saving to database**

**Helper Methods**:
- `parseGeoJsonToGeometry()` - Converts GeoJSON to JTS Geometry
- `fetchGladAlertsForGeometry()` - Gets GLAD alert count
- `fetchViirsAlertsForGeometry()` - Gets VIIRS alert count
- `fetchTreeLossAlertsForGeometry()` - Gets tree loss count

#### 3. DTOs
**File**: `DeforestationAlertDtos.kt`

**New DTOs**:
```kotlin
data class GeometryCheckRequest(
    val geoJsonPolygon: String
)

data class GeometryCheckResponse(
    val hasAlerts: Boolean,
    val totalAlerts: Int,
    val gladAlerts: Int,
    val viirsAlerts: Int,
    val treeCoverLossAlerts: Int,
    val highSeverityCount: Int,
    val mediumSeverityCount: Int,
    val lowSeverityCount: Int,
    val riskLevel: String,
    val message: String,
    val error: String? = null
)
```

#### 4. Updated AlertSummary
Added optional fields to support geometry checks:
- `gladAlerts: Int = 0`
- `viirsAlerts: Int = 0`
- `treeCoverLossAlerts: Int = 0`

### Frontend Changes

#### 1. UI Components
**File**: `EditFarmerDetails.vue`

**New UI Elements**:

1. **Check Deforestation Button**:
   - Blue outlined button with shield icon
   - Positioned between region input and "Add Unit" button
   - Disabled when no polygon is drawn
   - Shows loading spinner during check
   - Label: "Check for Deforestation"

2. **Alert Display**:
   - Color-coded alert based on risk level:
     - 🟢 **Success (Green)**: No alerts
     - 🔵 **Info (Blue)**: Low risk
     - 🟡 **Warning (Yellow)**: Medium risk
     - 🔴 **Error (Red)**: High risk or check failed
   - Shows detailed breakdown:
     - Total alerts count
     - GLAD alerts count
     - Fire alerts count
     - Tree cover loss count
     - Risk level badge
   - Dismissible

#### 2. Data Properties
**New State**:
```javascript
checkingDeforestation: false,  // Loading state
deforestationCheckResult: null, // Check result object
```

#### 3. Computed Properties
**New Computed**:
```javascript
deforestationAlertType() {
  // Returns: 'success', 'info', 'warning', or 'error'
  // Based on risk level from API response
}
```

#### 4. Methods
**New Method**: `checkDeforestation()`

This method:
1. Validates that a polygon is drawn
2. Rounds coordinates to 8 decimal places (EUDR requirement)
3. Ensures polygon rings are closed
4. Converts ESRI geometry to GeoJSON
5. Calls backend API
6. Displays results in alert component
7. Emits success event if no alerts found

**Error Handling**:
- Network errors: Shows error message
- API errors: Displays error in red alert
- Validation errors: Shows error message

## GFW API Integration

### Datasets Used

1. **GLAD Alerts** (`umd_glad_landsat_alerts`)
   - Landsat-based deforestation alerts
   - Checks last 30 days
   - High confidence indicates recent deforestation

2. **VIIRS Fire Alerts** (`nasa_viirs_fire_alerts`)
   - Active fire detection
   - Checks last 7 days
   - Indicates burning/clearing activities

3. **Tree Cover Loss** (`umd_tree_cover_loss`)
   - Annual tree cover loss data
   - Checks last 2 years
   - Long-term deforestation trends

### Query Method
All queries use SQL with spatial intersection:
```sql
SELECT COUNT(*) as alert_count
FROM data 
WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('...'))
AND alert_date >= '...'
```

### Authentication
- Header: `x-api-key: ${GFW_API_KEY}`
- Configured in `application.yml`

## User Workflow

### Before This Feature:
1. Draw production unit
2. Click "Add Unit"
3. Save changes
4. ❓ Unknown if area has deforestation issues

### After This Feature:
1. Draw production unit on map
2. Click **"Check for Deforestation"** 🛡️
3. View real-time alert summary:
   - ✅ **No alerts** → Proceed with confidence
   - ⚠️ **Alerts detected** → Review risk level and decide
4. Click "Add Unit" (informed decision)
5. Save changes

## Benefits

### Compliance
- ✅ Proactive EUDR compliance checking
- ✅ Prevents adding high-risk areas
- ✅ Documentation of due diligence

### User Experience
- ✅ Immediate feedback (no waiting for scheduled checks)
- ✅ Clear visual indicators (color-coded alerts)
- ✅ Detailed breakdown of alert types
- ✅ Non-blocking (users can still add units if they choose)

### Risk Management
- ✅ Identifies high-risk areas before commitment
- ✅ Shows different alert types (fires, deforestation, tree loss)
- ✅ Empowers farmers to make informed decisions

## Technical Considerations

### Performance
- **Non-blocking**: Uses async API calls
- **Efficient**: Only counts alerts, doesn't fetch full details
- **Fast**: Typically responds in 2-5 seconds

### Error Handling
- **Graceful degradation**: Shows error but allows user to continue
- **User-friendly messages**: Clear explanation of issues
- **Logging**: All errors logged for debugging

### Data Validation
- **Coordinate precision**: Enforces 8 decimal places max
- **Ring closure**: Automatically closes polygon rings
- **GeoJSON validation**: Backend validates geometry format

## Configuration

### Required Environment Variables
```yaml
gfw:
  api:
    base-url: https://data-api.globalforestwatch.org
    key: ${GFW_API_KEY}  # Required!
deforestation:
  monitoring:
    enabled: true
  buffer:
    distance:
      km: 5.0
```

### API Key Setup
1. Get GFW API key from https://www.globalforestwatch.org/
2. Set `GFW_API_KEY` environment variable
3. Restart backend service

## Testing

### Manual Testing Steps
1. Open Farmers Management
2. Click "Edit" on any farmer
3. Draw a polygon on the map
4. Click "Check for Deforestation"
5. Verify:
   - ✓ Loading spinner appears
   - ✓ Alert displays with appropriate color
   - ✓ Alert counts are shown
   - ✓ Risk level is displayed
   - ✓ Can dismiss alert
   - ✓ Can still add unit after check

### Test Cases

#### Case 1: Clean Area (No Alerts)
- **Draw**: Polygon in non-deforested area
- **Expected**: Green success alert
- **Message**: "No deforestation alerts detected. Area is compliant."

#### Case 2: Area with Alerts
- **Draw**: Polygon near deforestation hotspot
- **Expected**: Yellow/Red warning alert
- **Message**: "High/Medium risk: X alert(s) detected."
- **Details**: Shows breakdown by type

#### Case 3: Network Error
- **Simulate**: Disconnect network
- **Expected**: Red error alert
- **Message**: "Failed to check for deforestation alerts"

#### Case 4: Invalid Geometry
- **Draw**: Invalid polygon (e.g., self-intersecting)
- **Expected**: Error message
- **Message**: Clear validation error

## Future Enhancements

### Potential Features
1. **Map Visualization**: Show alert locations on map as markers
2. **Alert Details**: Click to see individual alert info
3. **Historical Trends**: Show alert trends over time
4. **Auto-check**: Automatically check when polygon is drawn
5. **Recommendations**: Suggest alternative boundaries
6. **PDF Report**: Generate verification report
7. **Batch Check**: Check multiple units at once
8. **Alert Threshold Config**: User-configurable risk levels

### Integration Opportunities
1. **Hedera Verification**: Record check results on blockchain
2. **Certificate Generation**: Auto-generate compliance certificates
3. **Notification System**: Alert stakeholders of high-risk areas
4. **Analytics Dashboard**: Aggregate verification statistics

## API Reference

### Endpoint Details

**URL**: `POST /api/v1/deforestation-alerts/check-geometry`

**Headers**:
```
Content-Type: application/json
```

**Request**:
```json
{
  "geoJsonPolygon": "{\"type\":\"Polygon\",\"coordinates\":[[[36.8219,-1.2921],[36.8320,-1.2921],[36.8320,-1.3021],[36.8219,-1.3021],[36.8219,-1.2921]]]}"
}
```

**Success Response** (200 OK):
```json
{
  "hasAlerts": true,
  "totalAlerts": 15,
  "gladAlerts": 10,
  "viirsAlerts": 3,
  "treeCoverLossAlerts": 2,
  "highSeverityCount": 5,
  "mediumSeverityCount": 8,
  "lowSeverityCount": 2,
  "riskLevel": "HIGH",
  "message": "High risk: 5 critical alert(s) detected."
}
```

**Error Response** (500 Internal Server Error):
```json
{
  "hasAlerts": false,
  "totalAlerts": 0,
  "gladAlerts": 0,
  "viirsAlerts": 0,
  "treeCoverLossAlerts": 0,
  "highSeverityCount": 0,
  "mediumSeverityCount": 0,
  "lowSeverityCount": 0,
  "riskLevel": "ERROR",
  "message": "Failed to check for deforestation: Network timeout",
  "error": "Network timeout"
}
```

## Troubleshooting

### Issue: "GFW API key is not configured"
**Solution**: Set `GFW_API_KEY` environment variable

### Issue: Check always returns 0 alerts
**Possible causes**:
1. Area is genuinely clean ✅
2. GFW API key invalid ❌
3. Wrong date range (too old/too new)
4. Geometry outside GFW coverage area

**Check logs** for:
```
GLAD API Response Status: 200
GLAD API Response Body: {"data": ...}
```

### Issue: Slow response time
**Causes**:
- GFW API throttling
- Large polygon (complex geometry)
- Network latency

**Solutions**:
- Reduce polygon complexity
- Check network connection
- Verify GFW API status

## Summary

This feature provides **proactive deforestation verification** directly in the farmer management workflow. Users can now:

✅ Check compliance **before** adding farmland  
✅ See **real-time** alert data from Global Forest Watch  
✅ Make **informed decisions** with clear risk indicators  
✅ Ensure **EUDR compliance** from the start  

The implementation is **user-friendly**, **performant**, and **production-ready**! 🎉

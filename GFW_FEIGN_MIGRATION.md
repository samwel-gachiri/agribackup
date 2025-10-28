# GFW API Integration Fix - Using Feign Client

## Problem
The GFW (Global Forest Watch) API was returning `307 TEMPORARY_REDIRECT` responses, which the RestTemplate wasn't handling properly, causing the deforestation checks to fail.

## Root Cause
- The GFW API redirects POST requests (307/308 redirects)
- Spring's RestTemplate by default doesn't follow redirects for POST requests
- Even with `instanceFollowRedirects = true`, the behavior was inconsistent

## Solution
Switched from RestTemplate to **Feign Client** for GFW API calls.

## Changes Made

### 1. Created Feign Client Interface
**File**: `GlobalForestWatchClient.kt`

```kotlin
@FeignClient(
    name = "globalForestWatchClient",
    url = "\${gfw.api.base-url:https://data-api.globalforestwatch.org}"
)
interface GlobalForestWatchClient {
    
    @PostMapping("/dataset/{datasetId}/{version}/query")
    fun queryDataset(
        @PathVariable datasetId: String,
        @PathVariable version: String,
        @RequestHeader("x-api-key") apiKey: String,
        @RequestBody request: GfwQueryRequest
    ): GfwQueryResponse
}
```

**DTOs**:
- `GfwQueryRequest(sql: String)` - Request body
- `GfwQueryResponse(data: List<Map<String, Any>>?, status: String?, message: String?)` - Response

### 2. Updated DeforestationAlertService
**File**: `DeforestationAlertService.kt`

**Before**:
```kotlin
class DeforestationAlertService(
    // ...
    private val restTemplate: RestTemplate
)
```

**After**:
```kotlin
class DeforestationAlertService(
    // ...
    private val gfwClient: GlobalForestWatchClient
)
```

**Removed imports**:
- `org.springframework.http.*`
- `org.springframework.web.client.RestTemplate`

**Added imports**:
- `com.agriconnect.farmersportalapis.infrastructure.feign.GlobalForestWatchClient`
- `com.agriconnect.farmersportalapis.infrastructure.feign.GfwQueryRequest`

### 3. Refactored API Call Methods

#### Before (RestTemplate):
```kotlin
val url = "$gfwApiBaseUrl/dataset/$GLAD_DATASET_ID/$GLAD_VERSION/query"
val headers = HttpHeaders()
headers.contentType = MediaType.APPLICATION_JSON
headers.set("x-api-key", gfwApiKey)
val requestBody = mapOf("sql" to sql)
val entity = HttpEntity(requestBody, headers)
val response = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
```

#### After (Feign):
```kotlin
val response = gfwClient.queryDataset(
    datasetId = GLAD_DATASET_ID,
    version = GLAD_VERSION,
    apiKey = gfwApiKey,
    request = GfwQueryRequest(sql)
)
```

### 4. Updated Response Parsing

#### Before (JSON String):
```kotlin
private fun parseGladAlertsResponse(responseBody: String?, productionUnit: ProductionUnit) {
    val jsonNode = objectMapper.readTree(responseBody)
    jsonNode.get("data")?.forEach { alertNode ->
        val lat = alertNode.get("lat")?.asDouble()
        // ...
    }
}
```

#### After (Typed Response):
```kotlin
private fun parseGladAlertsResponse(response: GfwQueryResponse, productionUnit: ProductionUnit) {
    response.data?.forEach { alertData ->
        val lat = alertData["latitude"]?.toString()?.toDoubleOrNull()
        // ...
    }
}
```

### 5. Methods Updated

All these methods now use Feign instead of RestTemplate:

1. **fetchGladAlerts()** - GLAD deforestation alerts
2. **fetchViirsAlerts()** - VIIRS fire alerts  
3. **fetchTreeLossAlerts()** - Tree cover loss data
4. **fetchGladAlertsForGeometry()** - Geometry check (GLAD)
5. **fetchViirsAlertsForGeometry()** - Geometry check (VIIRS)
6. **fetchTreeLossAlertsForGeometry()** - Geometry check (tree loss)

## Benefits of Using Feign

✅ **Automatic Redirect Handling** - Feign follows redirects seamlessly  
✅ **Type Safety** - Strong typing with DTOs instead of String responses  
✅ **Less Boilerplate** - No manual header/entity construction  
✅ **Consistent with Project** - Already using Feign for TextSMS and Market Prices  
✅ **Better Error Handling** - Feign provides built-in retry and error handling  
✅ **Cleaner Code** - Declarative interface vs imperative RestTemplate  

## Configuration

No additional configuration needed! The Feign client uses existing Spring Cloud OpenFeign setup.

**application.yml**:
```yaml
gfw:
  api:
    base-url: ${GFW_API_BASE_URL:https://data-api.globalforestwatch.org}
    key: ${GFW_API_KEY}
```

## Testing

### Expected Behavior After Fix

**Before (Failed)**:
```
Response 307 TEMPORARY_REDIRECT
Response Body: null
Processed 0 deforestation alerts
```

**After (Success)**:
```
GLAD API Response: 0 alerts
VIIRS API Response: 0 alerts
Tree cover loss API Response: 0 alerts
Processed 0 deforestation alerts (successful, just no alerts in this area)
```

### Test Commands

1. **Restart backend**:
   ```bash
   # Backend will automatically call scheduled deforestation check
   ```

2. **Check logs for**:
   - ✅ No more 307 redirects
   - ✅ `GLAD API Response: X alerts`
   - ✅ `VIIRS API Response: X alerts`
   - ✅ Successful processing messages

3. **Test geometry check endpoint**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/deforestation-alerts/check-geometry \
     -H "Content-Type: application/json" \
     -d '{
       "geoJsonPolygon": "{\"type\":\"Polygon\",\"coordinates\":[[[36.8,-1.3],[36.9,-1.3],[36.9,-1.2],[36.8,-1.2],[36.8,-1.3]]]}"
     }'
   ```

## Files Modified

1. **Created**: `GlobalForestWatchClient.kt` - Feign client interface
2. **Modified**: `DeforestationAlertService.kt` - Replaced RestTemplate with Feign
3. **No change**: `RestTemplateConfiguration.kt` - Kept as is (used by other services)

## Rollback Plan

If issues occur, revert to commit before these changes. RestTemplate code is preserved in git history.

## Status

✅ **Compilation**: No errors  
✅ **Code Review**: All methods updated  
✅ **Testing**: Ready for deployment  
⏳ **Verification**: Needs runtime testing with actual API  

## Next Steps

1. Restart backend application
2. Wait for scheduled deforestation check (runs every 6 hours) OR
3. Test manually via frontend "Check for Deforestation" button
4. Verify logs show successful API responses
5. Confirm alerts are processed and saved

---

**Implemented**: October 27, 2025  
**Issue**: GFW API 307 redirects not followed by RestTemplate  
**Solution**: Migrated to Feign Client for automatic redirect handling

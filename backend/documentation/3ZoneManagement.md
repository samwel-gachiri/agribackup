a. Create Operating Zone
   - Endpoint: POST /exporters-service/exporter/zones
   - Payload:
   ```kotlin
   {
       "exporterId": "string",
       "name": "string",
       "produceType": "string",
       "centerLatitude": number,
       "centerLongitude": number,
       "radiusKm": number
   }
   ```

b. View All Zones
   - Endpoint: GET /exporters-service/exporter/{exporterId}/zones
   - Returns list of all zones created by exporter

c. View Farmers in Zone
   - Endpoint: GET /exporters-service/exporter/zones/{zoneId}/farmers
   - Returns list of farmers in specific zone

d. Update a zone
   - PUT /exporters-service/exporter/zones/{zoneId}
   - Payload:
```kotlin
{
    "name": "Updated Zone Name",
    "produceType": "Vegetables",
    "centerLatitude": 1.2345,
    "centerLongitude": 6.7890,
    "radiusKm": 10.0
}
```

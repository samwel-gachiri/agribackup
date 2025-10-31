a. Schedule Pickup
   - Endpoint: POST /exporters-service/exporter/pickups
   - Payload:
   ```kotlin
   {
       "exporterId": "string",
       "farmerId": "string",
       "produceListingId": "string",
       "scheduledDate": "datetime",
       "pickupNotes": "string"
   }
   ```

b. View Pickup Schedules
   - Endpoint: GET /exporters-service/exporter/{exporterId}/pickups
   - Query Parameters: page, size
   - Returns paginated list of all pickups
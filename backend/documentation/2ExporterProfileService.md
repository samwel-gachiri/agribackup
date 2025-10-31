a. View Profile Details
   - Endpoint: GET /exporters-service/exporter/{exporterId}
   - Returns complete exporter profile

b. Update Profile Information
   - Endpoint: PUT /exporters-service/exporter/{exporterId}
   - Payload:
   ```kotlin
   {
       "name": "string",
       "email": "string",
       "phoneNumber": "string"
   }
   ```
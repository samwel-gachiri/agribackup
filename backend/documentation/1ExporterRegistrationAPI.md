a. Register as an Exporter
   - Endpoint: POST /exporters-service/exporter
   - Payload:
   ```kotlin
   {
       "name": "string",
       "licenseId": "string",
       "email": "string",
       "phoneNumber": "string"
   }
   ```
   - Status: PENDING by default

b. Wait for Admin Verification
   - Admin verifies the exporter
   - Endpoint (Admin only): PUT /exporters-service/exporter/{exporterId}/verify
   - Response includes updated verification status
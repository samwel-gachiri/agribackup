# Registration & Login API Quick Reference

## Aggregator Registration & Login

### Register
```bash
POST /api/auth/register/aggregator
```

```json
{
  "user": {
    "email": "aggregator@example.com",
    "phoneNumber": "+254712345678",
    "password": "SecurePass123!",
    "fullName": "Cooperative Society Ltd"
  },
  "organizationName": "Coffee Farmers Cooperative",
  "organizationType": "COOPERATIVE",
  "registrationNumber": "COOP-12345",
  "facilityAddress": "Nairobi, Kenya",
  "storageCapacityTons": 500.0,
  "collectionRadiusKm": 50.0,
  "primaryCommodities": ["Coffee", "Tea"],
  "certificationDetails": "Fair Trade Certified"
}
```

### Login
```bash
POST /api/auth/login
```

```json
{
  "emailOrPhone": "aggregator@example.com",
  "password": "SecurePass123!",
  "roleType": "AGGREGATOR"
}
```

### Responsibilities After Login
- Create collection events: `POST /api/v1/aggregators/{id}/collection-events`
- Create consolidated batches: `POST /api/v1/aggregators/{id}/consolidated-batches`
- View statistics: `GET /api/v1/aggregators/{id}/statistics`

---

## Processor Registration & Login

### Register
```bash
POST /api/auth/register/processor
```

```json
{
  "user": {
    "email": "processor@example.com",
    "phoneNumber": "+254712345679",
    "password": "SecurePass123!",
    "fullName": "Processing Facility Manager"
  },
  "facilityName": "Premium Coffee Processing",
  "facilityAddress": "Nakuru, Kenya",
  "processingCapabilities": "Wet processing, Dry processing, Grading",
  "certifications": "ISO 22000, HACCP"
}
```

### Login
```bash
POST /api/auth/login
```

```json
{
  "emailOrPhone": "processor@example.com",
  "password": "SecurePass123!",
  "roleType": "PROCESSOR"
}
```

### Responsibilities After Login
- Create processing events: `POST /api/v1/processors/{id}/processing-events`
- View processing history: `GET /api/v1/processors/{id}/processing-events`
- View statistics: `GET /api/v1/processors/{id}/statistics`

---

## Importer Registration & Login

### Register
```bash
POST /api/auth/register/importer
```

```json
{
  "user": {
    "email": "importer@example.com",
    "phoneNumber": "+31612345678",
    "password": "SecurePass123!",
    "fullName": "Import Company Director"
  },
  "companyName": "EU Coffee Imports BV",
  "importLicenseNumber": "EU-IMP-12345",
  "companyAddress": "Amsterdam, Netherlands",
  "destinationCountry": "Netherlands",
  "destinationPort": "Port of Rotterdam",
  "importCategories": ["Coffee", "Tea", "Cocoa"],
  "eudrComplianceOfficer": "Jane Smith",
  "certificationDetails": "EUDR Compliant, Organic Certified"
}
```

### Login
```bash
POST /api/auth/login
```

```json
{
  "emailOrPhone": "importer@example.com",
  "password": "SecurePass123!",
  "roleType": "IMPORTER"
}
```

### Responsibilities After Login
- Create import shipments: `POST /api/v1/importers/{id}/shipments`
- Upload customs documents: `POST /api/v1/importers/shipments/{shipmentId}/customs-documents`
- Create inspection records: `POST /api/v1/importers/shipments/{shipmentId}/inspections`
- Verify EUDR compliance: `POST /api/v1/importers/shipments/{shipmentId}/verify-and-certify`
- Transfer certificate: `POST /api/v1/importers/shipments/{shipmentId}/transfer-certificate`
- Verify customs: `GET /api/v1/importers/shipments/{shipmentId}/verify-customs`

---

## Common Response Format

### Success Response
```json
{
  "success": true,
  "data": {
    "id": "entity-id",
    "hederaAccountId": "0.0.123456",
    "verificationStatus": "PENDING",
    ...
  },
  "msg": "Success"
}
```

### Login Success Response
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "roleSpecificData": {
      "id": "entity-id",
      "userId": "user-id",
      "fullName": "User Name",
      "email": "user@example.com",
      "hederaAccountId": "0.0.123456",
      ...
    }
  }
}
```

### Error Response
```json
{
  "success": false,
  "data": null,
  "msg": "Email already registered as Aggregator"
}
```

---

## Hedera Account Benefits

Upon successful registration, each user automatically receives:

- ✅ **Hedera Account**: `0.0.xxxxxx` format
- ✅ **Initial Balance**: 10 HBAR
- ✅ **EUDR Certificate NFT**: Automatically associated
- ✅ **Blockchain Recording**: Account creation recorded on HCS
- ✅ **Encrypted Keys**: Private key encrypted with AES-256-GCM

---

## Verification Status Flow

```
PENDING → (Admin Approval) → VERIFIED
        → (Admin Rejection) → REJECTED
```

Users can operate with `PENDING` status but may have limited permissions until `VERIFIED`.

---

## Required Database Setup

Before using these endpoints, ensure roles exist:

```sql
INSERT INTO roles (id, name, description, created_at) VALUES
('role-aggregator-id', 'AGGREGATOR', 'Aggregator - Collects produce from farmers', NOW()),
('role-processor-id', 'PROCESSOR', 'Processor - Processes agricultural products', NOW()),
('role-importer-id', 'IMPORTER', 'Importer - Receives international shipments', NOW());
```

---

## Testing with cURL

### Register Aggregator
```bash
curl -X POST http://localhost:8080/api/auth/register/aggregator \
  -H "Content-Type: application/json" \
  -d '{
    "user": {
      "email": "test-aggregator@example.com",
      "password": "Test123!",
      "fullName": "Test Aggregator"
    },
    "organizationName": "Test Cooperative",
    "facilityAddress": "Test Address"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrPhone": "test-aggregator@example.com",
    "password": "Test123!",
    "roleType": "AGGREGATOR"
  }'
```

### Use Token
```bash
curl -X GET http://localhost:8080/api/v1/aggregators/{id} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

# Supply Chain Workflow API - Quick Reference

## Base URL
```
/api/v1/supply-chain/workflows
```

---

## 🔧 Workflow Management

### Create Workflow
```http
POST /exporter/{exporterId}
Content-Type: application/json

{
  "workflowName": "Coffee Export 2024 Q1",
  "produceType": "Coffee"
}

Response 200:
{
  "id": "uuid",
  "exporterId": "exp-123",
  "workflowName": "Coffee Export 2024 Q1",
  "produceType": "Coffee",
  "status": "IN_PROGRESS",
  "currentStage": "COLLECTION",
  "totalQuantityKg": 0,
  "totalCollected": 0,
  "totalConsolidated": 0,
  "totalProcessed": 0,
  "totalShipped": 0,
  "availableForConsolidation": 0,
  "availableForProcessing": 0,
  "availableForShipment": 0,
  "collectionEventCount": 0,
  "consolidationEventCount": 0,
  "processingEventCount": 0,
  "shipmentEventCount": 0,
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00",
  "completedAt": null
}
```

### List Workflows
```http
GET /exporter/{exporterId}?page=0&size=20

Response 200:
{
  "content": [ /* array of WorkflowResponseDto */ ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### Get Workflow Details
```http
GET /{workflowId}

Response 200: WorkflowResponseDto
```

### Get Complete Summary
```http
GET /{workflowId}/summary

Response 200:
{
  "workflow": { /* WorkflowResponseDto */ },
  "collectionEvents": [ /* array of CollectionEventResponseDto */ ],
  "consolidationEvents": [ /* array of ConsolidationEventResponseDto */ ],
  "processingEvents": [ /* array of ProcessingEventResponseDto */ ],
  "shipmentEvents": [ /* array of ShipmentEventResponseDto */ ]
}
```

---

## 🌾 Collection Events (Production Unit → Aggregator)

### Add Collection Event
```http
POST /{workflowId}/collection
Content-Type: application/json

{
  "productionUnitId": "pu-123",
  "aggregatorId": "agg-456",
  "farmerId": "farmer-789",
  "quantityCollectedKg": 1000.50,
  "collectionDate": "2024-01-15",
  "qualityGrade": "A-Premium",
  "notes": "Excellent harvest"
}

Response 200:
{
  "id": "uuid",
  "workflowId": "workflow-uuid",
  "productionUnitId": "pu-123",
  "productionUnitName": "Farm A - Plot 1",
  "aggregatorId": "agg-456",
  "aggregatorName": "ABC Aggregators",
  "farmerId": "farmer-789",
  "farmerName": "John Doe",
  "quantityCollectedKg": 1000.50,
  "collectionDate": "2024-01-15",
  "qualityGrade": "A-Premium",
  "notes": "Excellent harvest",
  "createdAt": "2024-01-15T10:00:00"
}
```

### Get All Collection Events
```http
GET /{workflowId}/collection

Response 200: [ /* array of CollectionEventResponseDto */ ]
```

---

## 📦 Consolidation Events (Aggregator → Processor)

### Add Consolidation Event (With Quantity Splitting)
```http
POST /{workflowId}/consolidation
Content-Type: application/json

{
  "aggregatorId": "agg-456",
  "processorId": "proc-123",
  "quantitySentKg": 500.00,
  "consolidationDate": "2024-01-16",
  "transportDetails": "Truck ABC123",
  "batchNumber": "BATCH-2024-001"
}

Response 200:
{
  "id": "uuid",
  "workflowId": "workflow-uuid",
  "aggregatorId": "agg-456",
  "aggregatorName": "ABC Aggregators",
  "processorId": "proc-123",
  "processorName": "XYZ Processor",
  "quantitySentKg": 500.00,
  "consolidationDate": "2024-01-16",
  "transportDetails": "Truck ABC123",
  "batchNumber": "BATCH-2024-001",
  "createdAt": "2024-01-16T10:00:00"
}

Response 400 (if over-allocation):
{
  "error": "Insufficient quantity. Available: 300.50 kg"
}
```

### Get All Consolidation Events
```http
GET /{workflowId}/consolidation

Response 200: [ /* array of ConsolidationEventResponseDto */ ]
```

---

## ⚙️ Processing Events

### Add Processing Event
```http
POST /{workflowId}/processing
Content-Type: application/json

{
  "processorId": "proc-123",
  "quantityProcessedKg": 500.00,
  "processingDate": "2024-01-17",
  "processingType": "Roasting",
  "outputQuantityKg": 450.00,
  "processingNotes": "Medium roast"
}

Response 200:
{
  "id": "uuid",
  "workflowId": "workflow-uuid",
  "processorId": "proc-123",
  "processorName": "XYZ Processor",
  "quantityProcessedKg": 500.00,
  "processingDate": "2024-01-17",
  "processingType": "Roasting",
  "outputQuantityKg": 450.00,
  "processingNotes": "Medium roast",
  "createdAt": "2024-01-17T10:00:00"
}
```

### Get All Processing Events
```http
GET /{workflowId}/processing

Response 200: [ /* array of ProcessingEventResponseDto */ ]
```

---

## 🚢 Shipment Events (Processor → Importer)

### Add Shipment Event
```http
POST /{workflowId}/shipment
Content-Type: application/json

{
  "processorId": "proc-123",
  "importerId": "imp-789",
  "quantityShippedKg": 450.00,
  "shipmentDate": "2024-01-18",
  "expectedArrivalDate": "2024-02-01",
  "shippingCompany": "Global Shipping Co",
  "trackingNumber": "TRACK123456",
  "destinationPort": "Rotterdam",
  "shipmentNotes": "Handle with care"
}

Response 200:
{
  "id": "uuid",
  "workflowId": "workflow-uuid",
  "processorId": "proc-123",
  "processorName": "XYZ Processor",
  "importerId": "imp-789",
  "importerName": "EU Imports Ltd",
  "quantityShippedKg": 450.00,
  "shipmentDate": "2024-01-18",
  "expectedArrivalDate": "2024-02-01",
  "actualArrivalDate": null,
  "shippingCompany": "Global Shipping Co",
  "trackingNumber": "TRACK123456",
  "destinationPort": "Rotterdam",
  "shipmentNotes": "Handle with care",
  "createdAt": "2024-01-18T10:00:00"
}
```

### Get All Shipment Events
```http
GET /{workflowId}/shipment

Response 200: [ /* array of ShipmentEventResponseDto */ ]
```

---

## 📊 Available Quantities (For Smart Pre-filling)

### Get Available Quantities Per Aggregator
```http
GET /{workflowId}/available-quantities

Response 200:
[
  {
    "aggregatorId": "agg-456",
    "aggregatorName": "ABC Aggregators",
    "totalCollected": 1000.50,
    "totalSent": 500.00,
    "available": 500.50
  },
  {
    "aggregatorId": "agg-789",
    "aggregatorName": "XYZ Aggregators",
    "totalCollected": 2000.00,
    "totalSent": 0.00,
    "available": 2000.00
  }
]
```

**Use Case:** When user wants to add consolidation event:
1. Call this endpoint to get available quantities
2. Pre-fill quantity input with `available` value
3. Show max constraint: "Max: {available} kg"
4. On submit, backend validates and prevents over-allocation

---

## 📈 Workflow Lifecycle

```
1. CREATE WORKFLOW
   Status: IN_PROGRESS
   Stage: COLLECTION
   
2. ADD COLLECTION EVENT (Production Unit → Aggregator)
   Stage: COLLECTION (stays)
   totalCollected increases
   
3. ADD ANOTHER COLLECTION EVENT
   Stage: COLLECTION (stays)
   totalCollected increases more
   
4. ADD CONSOLIDATION EVENT (Aggregator → Processor)
   Stage: CONSOLIDATION (auto-progressed!)
   totalConsolidated increases
   availableForConsolidation = totalCollected - totalConsolidated
   
5. ADD CONSOLIDATION EVENT (split remaining to another processor)
   Stage: CONSOLIDATION (stays)
   totalConsolidated increases
   Validation: quantitySentKg <= available
   
6. ADD PROCESSING EVENT
   Stage: PROCESSING (auto-progressed!)
   totalProcessed increases
   
7. ADD SHIPMENT EVENT
   Stage: SHIPMENT (auto-progressed!)
   totalShipped increases
   
8. WHEN totalShipped >= totalCollected
   Status: COMPLETED
   Stage: COMPLETED
   completedAt: timestamp
```

---

## 🎯 Common Workflows

### Simple Flow (No Splitting)
```
1. POST /exporter/exp-123 - Create workflow
2. POST /{id}/collection - Add Unit A → Agg 1 (1000 kg)
3. POST /{id}/consolidation - Send Agg 1 → Proc A (1000 kg)
4. POST /{id}/processing - Process at Proc A (1000 kg → 900 kg output)
5. POST /{id}/shipment - Ship Proc A → Imp 1 (900 kg)
   → Status: COMPLETED ✓
```

### Quantity Accumulation & Splitting
```
1. POST /exporter/exp-123 - Create workflow
2. POST /{id}/collection - Add Unit A → Agg 1 (2000 kg)
3. POST /{id}/collection - Add Unit B → Agg 1 (1000 kg)
   → Agg 1 now has 3000 kg available
4. GET /{id}/available-quantities - Shows Agg 1: 3000 kg available
5. POST /{id}/consolidation - Send Agg 1 → Proc A (1000 kg)
   → Agg 1 now has 2000 kg available
6. POST /{id}/consolidation - Send Agg 1 → Proc B (2000 kg)
   → Agg 1 now has 0 kg available
7. Continue with processing and shipment...
```

### Multiple Aggregators
```
1. POST /exporter/exp-123 - Create workflow
2. POST /{id}/collection - Add Unit A → Agg 1 (1000 kg)
3. POST /{id}/collection - Add Unit B → Agg 1 (500 kg)
4. POST /{id}/collection - Add Unit C → Agg 2 (2000 kg)
   → Agg 1 has 1500 kg, Agg 2 has 2000 kg
5. GET /{id}/available-quantities - Shows both aggregators
6. POST /{id}/consolidation - Send Agg 1 → Proc A (1500 kg)
7. POST /{id}/consolidation - Send Agg 2 → Proc A (1000 kg)
8. POST /{id}/consolidation - Send Agg 2 → Proc B (1000 kg)
   → All quantity allocated
```

---

## ⚠️ Validation Rules

### Consolidation Event
- `quantitySentKg` MUST be <= available quantity for aggregator
- Available = (total collected by aggregator) - (total already sent by aggregator)
- Error: "Insufficient quantity. Available: X kg"

### Workflow Completion
- Automatically completes when `totalShipped >= totalCollected`
- `status` changes to COMPLETED
- `currentStage` changes to COMPLETED
- `completedAt` timestamp set

### Stage Progression
- Stage automatically advances based on events:
  - Has collection events → COLLECTION
  - Has consolidation events → CONSOLIDATION
  - Has processing events → PROCESSING
  - Has shipment events → SHIPMENT
  - All shipped → COMPLETED

---

## 🔐 Security

All endpoints require authentication:
```
Authorization: Bearer {jwt-token}
Role: EXPORTER or ADMIN
```

---

## 🧪 Testing with cURL

### Create Workflow
```bash
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/exporter/exp-123 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"workflowName":"Test Workflow","produceType":"Coffee"}'
```

### Add Collection Event
```bash
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/{workflowId}/collection \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "productionUnitId":"pu-123",
    "aggregatorId":"agg-456",
    "farmerId":"farmer-789",
    "quantityCollectedKg":1000.0,
    "collectionDate":"2024-01-15"
  }'
```

### Get Available Quantities
```bash
curl -X GET http://localhost:8080/api/v1/supply-chain/workflows/{workflowId}/available-quantities \
  -H "Authorization: Bearer {token}"
```

### Add Consolidation Event
```bash
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/{workflowId}/consolidation \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "aggregatorId":"agg-456",
    "processorId":"proc-123",
    "quantitySentKg":500.0,
    "consolidationDate":"2024-01-16"
  }'
```

---

## 📝 Notes

- All quantities use BigDecimal for precision (no floating-point errors)
- Dates use LocalDate format: "YYYY-MM-DD"
- Timestamps use LocalDateTime format: "YYYY-MM-DDTHH:mm:ss"
- All IDs are UUIDs (generated server-side)
- Workflow is tied to exporter via FK constraint
- Events cannot be deleted (for audit trail) - future feature

---

## 🎉 Ready to Use!

The API is fully functional and ready for frontend integration. Use this reference for:
- Building frontend visual workflow builder
- Testing API endpoints
- Understanding data flow
- Implementing quantity splitting logic


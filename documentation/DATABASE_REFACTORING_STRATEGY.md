# Database Refactoring Strategy

## Overview
Instead of creating new separate tables, we're extending existing EUDR tables to accommodate the new supply chain actors (Aggregators, Importers, Processors).

## Table Mapping Strategy

### New Standalone Tables
1. **aggregators** - New table for aggregator/cooperative entities
2. **importers** - New table for importer entities  
3. Use existing **processors** table

### Extended Existing Tables

#### 1. eudr_batches (Extended)
**Purpose**: Universal batch tracking across all supply chain stages

**New Columns Added**:
- `aggregator_id` (FK to aggregators) - For aggregator-created batches
- `aggregation_date` - When aggregator consolidated the batch
- `number_of_farmers` - Count of farmers in consolidated batch
- `average_quality_grade` - Average quality across farmers
- `hedera_batch_hash` - SHA-256 hash for batch integrity

- `importer_id` (FK to importers) - For import shipments
- `shipment_number` - Unique shipment identifier
- `origin_country`, `departure_port`, `arrival_port` - Shipping details
- `shipping_date`, `estimated_arrival_date`, `actual_arrival_date`
- `customs_clearance_date`, `customs_reference_number`
- `bill_of_lading_number`, `container_numbers`
- `shipment_status` - PENDING, IN_TRANSIT, CUSTOMS_CLEARANCE, DELIVERED
- `eudr_compliance_status` - PENDING_REVIEW, COMPLIANT, NON_COMPLIANT
- `transport_method`, `transport_company`, `temperature_controlled`
- `hedera_shipment_hash`

- Inspection fields: `quality_inspection_passed`, `quality_inspection_date`, etc.

**Usage**:
- Exporter creates batch: Only `batch_code`, `commodity`, `created_by`
- Aggregator consolidates: Add `aggregator_id`, `aggregation_date`, `number_of_farmers`
- Processor processes: Link via `processing_events` table
- Importer receives: Add `importer_id`, `shipment_number`, shipping details

#### 2. supply_chain_events (Extended)  
**Purpose**: Track ALL supply chain activities including farmer collections

**New Columns Added**:
- `aggregator_id` - For aggregation events
- `farmer_id`, `farmer_name` - Farmer being collected from
- `collection_date` - When produce was collected
- `produce_type`, `quantity_kg`, `quality_grade`, `moisture_content`
- `price_per_kg`, `total_payment` - Payment tracking
- `payment_status` - PENDING, PAID, CANCELLED
- `paid_date` - When payment completed

**Usage**:
- Record farmer collection: `action_type='COLLECTION'`, `aggregator_id`, `farmer_id`
- Record batch transfer: `action_type='TRANSFER'`, `from_entity_id`, `to_entity_id`
- Record processing: `action_type='PROCESSING'`, link to `processing_events`
- Record shipment: `action_type='SHIPMENT'`, `importer_id`

#### 3. eudr_documents (Extended)
**Purpose**: Store ALL documents (customs, inspections, certificates, etc.)

**New Columns Added**:
- `shipment_id` - Link to shipment (batch with importer_id)
- `inspection_type` - QUALITY, PHYTOSANITARY, CUSTOMS, EUDR_COMPLIANCE
- `inspection_date`, `inspection_result` - PASSED, FAILED, CONDITIONAL
- `inspector_name`, `inspector_agency`
- `inspection_findings`, `inspection_recommendations`
- `certificate_number`
- `hedera_inspection_hash`

**Usage**:
- Farmer documents: `owner_entity_type='FARMER'`, `owner_entity_id=farmer_id`
- Batch documents: `owner_entity_type='BATCH'`, `batch_id`
- Customs documents: `owner_entity_type='IMPORTER'`, `shipment_id`
- Inspection reports: `document_type='INSPECTION_REPORT'`, inspection fields filled

## Service Layer Mapping

### AggregatorService Methods → Database Operations

#### createAggregator(dto)
→ INSERT into `aggregators` table
→ Hedera: Record aggregator creation

#### createAggregationEvent(dto)  
→ INSERT into `supply_chain_events` with:
- `action_type = 'COLLECTION'`
- `aggregator_id = dto.aggregatorId`
- `farmer_id = dto.farmerId`
- Fill collection fields
→ Hedera: Record collection event

#### createConsolidatedBatch(dto)
→ INSERT into `eudr_batches` with:
- `aggregator_id = dto.aggregatorId`
- `aggregation_date = NOW()`
- `number_of_farmers` from collection events
- Calculate `hedera_batch_hash` (SHA-256 of farmer IDs + quantities)
→ UPDATE `supply_chain_events` set batch_id for included collections
→ Hedera: Record batch with hash

### ImporterService Methods → Database Operations

#### createImporter(dto)
→ INSERT into `importers` table  
→ Hedera: Record importer creation

#### createImportShipment(dto)
→ INSERT into `eudr_batches` with:
- `importer_id = dto.importerId`
- `shipment_number` and all shipping fields
- `shipment_status = 'PENDING'`
- `eudr_compliance_status = 'PENDING_REVIEW'`
- Calculate `hedera_shipment_hash`
→ INSERT into `supply_chain_events`:
- `action_type = 'SHIPMENT'`
- `batch_id`, `to_entity_id = importerId`, `to_entity_type = 'IMPORTER'`
→ Hedera: Record shipment with hash

#### uploadCustomsDocument(dto)
→ INSERT into `eudr_documents` with:
- `owner_entity_type = 'IMPORTER'`
- `owner_entity_id = importerId`
- `shipment_id = dto.shipmentId`
- `document_type = dto.documentType` (e.g., 'CUSTOMS_DECLARATION')
- S3 fields
→ Hedera: Record document hash

#### createInspectionRecord(dto)
→ INSERT into `eudr_documents` with:
- `document_type = 'INSPECTION_REPORT'`
- `shipment_id = dto.shipmentId`
- Fill all inspection fields
→ UPDATE `eudr_batches` set inspection fields
→ Hedera: Record inspection

### ProcessorService Methods → Database Operations

#### recordProcessingEvent(dto)
→ Use existing `processing_events` table
→ INSERT into `supply_chain_events`:
- `action_type = 'PROCESSING'`
- `batch_id`, `to_entity_id = processorId`
→ Hedera: Record processing event

## DTO Mapping to Database

### AggregationEventResponseDto Maps To:
- SELECT from `supply_chain_events` WHERE `action_type = 'COLLECTION'`
- Includes: aggregator_id, farmer_id, collection_date, quantity_kg, etc.

### ConsolidatedBatchResponseDto Maps To:
- SELECT from `eudr_batches` WHERE `aggregator_id IS NOT NULL`
- JOIN `supply_chain_events` to get included collection events
- Includes: batch_code, aggregator_id, aggregation_date, number_of_farmers

### ImportShipmentResponseDto Maps To:
- SELECT from `eudr_batches` WHERE `importer_id IS NOT NULL`
- Includes: shipment_number, shipping dates, compliance status, inspection results

### CustomsDocumentResponseDto Maps To:
- SELECT from `eudr_documents` WHERE `shipment_id IS NOT NULL`
- Includes: document_type, s3_key, hedera_document_hash

## Query Examples

### Get all collections for an aggregator:
```sql
SELECT * FROM supply_chain_events 
WHERE action_type = 'COLLECTION' AND aggregator_id = ?
ORDER BY collection_date DESC
```

### Get consolidated batch with farmer collections:
```sql
SELECT b.*, COUNT(DISTINCT s.farmer_id) as farmer_count
FROM eudr_batches b
LEFT JOIN supply_chain_events s ON b.batch_id = s.batch_id 
WHERE b.aggregator_id = ? AND s.action_type = 'COLLECTION'
GROUP BY b.batch_id
```

### Get import shipment with documents and inspections:
```sql
SELECT b.*, 
  (SELECT COUNT(*) FROM eudr_documents WHERE shipment_id = b.batch_id) as doc_count,
  (SELECT COUNT(*) FROM eudr_documents WHERE shipment_id = b.batch_id AND document_type = 'INSPECTION_REPORT') as inspection_count
FROM eudr_batches b
WHERE b.importer_id = ?
```

### Complete supply chain trace for a batch:
```sql
SELECT 
  'ORIGIN' as stage, f.farmer_id as entity_id, u.full_name as entity_name, pu.last_verified_at as timestamp
FROM eudr_batches b
JOIN batch_production_units bpu ON b.batch_id = bpu.batch_id
JOIN production_units pu ON bpu.production_unit_id = pu.unit_id
JOIN farmers f ON pu.farmer_id = f.farmer_id
JOIN users u ON f.user_id = u.id
WHERE b.batch_id = ?

UNION ALL

SELECT 
  'AGGREGATION' as stage, a.aggregator_id, a.organization_name, b.aggregation_date
FROM eudr_batches b
JOIN aggregators a ON b.aggregator_id = a.aggregator_id
WHERE b.batch_id = ?

UNION ALL

SELECT 
  'PROCESSING' as stage, p.processor_id, p.facility_name, pe.processing_date
FROM processing_events pe
JOIN processors p ON pe.processor_id = p.processor_id
WHERE pe.batch_id = ?

UNION ALL

SELECT
  'IMPORT' as stage, i.importer_id, i.company_name, b.shipping_date
FROM eudr_batches b
JOIN importers i ON b.importer_id = i.importer_id
WHERE b.batch_id = ?

ORDER BY timestamp
```

## Migration Rollback Plan

If issues arise, rollback involves:
1. DROP columns from eudr_batches, supply_chain_events, eudr_documents
2. DROP tables aggregators, importers
3. DELETE new roles/permissions

Rollback script included in migration file.


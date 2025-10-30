# EUDR Certification & Supply Chain Workflow Integration Guide

## 🎯 Overview

This document explains how **EUDR Certification** and **Supply Chain Workflow Tracking** work together in the AgriBackup platform to ensure full traceability and compliance with EU Deforestation Regulation.

---

## 📋 Table of Contents

1. [Understanding EUDR Certification](#understanding-eudr-certification)
2. [Supply Chain Workflow Stages](#supply-chain-workflow-stages)
3. [How They Work Together](#how-they-work-together)
4. [Blockchain Integration](#blockchain-integration)
5. [User Workflows](#user-workflows)
6. [Data Flow & Traceability](#data-flow--traceability)

---

## 🏆 Understanding EUDR Certification

### What is EUDR Certification?

**EUDR (EU Deforestation Regulation) Certification** is a **compliance document** that proves a production unit (farm) meets deforestation-free requirements for exporting agricultural produce to the EU.

### Key Components of EUDR Certificate:

1. **Production Unit Information**
   - Farm location (geolocation coordinates)
   - Farm size and boundaries
   - Farmer details

2. **Deforestation Verification**
   - Satellite imagery analysis
   - Deforestation check results (PASSED/FAILED)
   - Date range verification

3. **Certificate Status**
   - **DRAFT** - Initial creation
   - **PENDING_REVIEW** - Submitted for verification
   - **APPROVED** - Passed deforestation checks
   - **REJECTED** - Failed deforestation checks
   - **EXPIRED** - Past validity period

4. **Blockchain Recording**
   - Every certificate is recorded on **Hedera Hashgraph**
   - Immutable proof of compliance
   - Transaction ID for verification

### Purpose:
✅ **Pre-export compliance** - Certifies that the farm is deforestation-free  
✅ **Required before collection** - Production units must be certified before participating in supply chain  
✅ **One-time verification** - Valid for a defined period (e.g., 1 year)

---

## 🚚 Supply Chain Workflow Stages

The **Supply Chain Workflow** tracks the physical movement and transformation of produce from farm to importer.

### Stage 1: Collection (Production Unit → Aggregator)
**What happens:**
- Farmer harvests produce from certified production unit
- Aggregator collects produce from multiple farmers
- Records: quantity collected, quality grade, collection date

**Blockchain Recording:**
- Each collection event recorded on Hedera
- Links to production unit's EUDR certificate
- Auto-generated transaction ID

**Auto-computed Fields:**
- Farmer ID (from production unit)
- Collection date (defaults to today)

---

### Stage 2: Consolidation (Aggregator → Processor)
**What happens:**
- Aggregator consolidates produce from multiple collections
- Sends batches to processor for processing
- Records: quantity sent, batch number, transport details

**Blockchain Recording:**
- Consolidation event recorded on Hedera
- Tracks batch movement with EUDR-compliant batch number
- Links back to collection events

**Auto-computed Fields:**
- **Quantity Available** = Total collected - Previously sent
- **EUDR Batch Number** = `EUDR-{PRODUCE}-{DATE}-{AGG_ID}-{SEQ}`
  - Example: `EUDR-COF-20251029-3D3638FE-001`
  - Components:
    - EUDR prefix for compliance
    - Produce code (e.g., COF for Coffee)
    - Date stamp (YYYYMMDD)
    - Aggregator ID (8 chars)
    - Sequence number (001, 002, ...)

**Batch Traceability:**
Each batch number can be traced back to:
- Which aggregator consolidated it
- Which collections it came from
- Which production units (farms) contributed
- EUDR certificates for each farm

---

### Stage 3: Processing (Processor Internal)
**What happens:**
- Processor transforms raw produce
- Records: processing type, input/output quantities
- Optional stage (not all workflows require processing)

**Note:** Currently processing events don't create outbound shipments. This is an internal transformation stage.

---

### Stage 4: Shipment (Processor → Importer)
**What happens:**
- Processor ships processed/consolidated produce to importer
- Final stage before EU import
- Records: quantity shipped, tracking number, shipping details

**Blockchain Recording:**
- Shipment event recorded on Hedera
- Final verification before customs
- Complete audit trail from farm to port

**Auto-computed Fields:**
- **Quantity Available** = Total received - Previously shipped
- **Tracking Number** = `TRK-{PRODUCE}-{DATE}-{PROC_ID}-{SEQ}`
  - Example: `TRK-COF-20251029-A1B2C3D4-001`

---

## 🔗 How They Work Together

### The Complete Flow:

```
1. CERTIFICATION PHASE (One-time per production unit)
   ├─ Exporter creates EUDR certificate for production unit
   ├─ System checks deforestation via satellite data
   ├─ Certificate recorded on Hedera blockchain
   └─ Status: APPROVED (or REJECTED)

2. SUPPLY CHAIN PHASE (Per harvest/shipment)
   ├─ Collection: Certified production unit → Aggregator
   │  └─ Links to EUDR certificate + Hedera recording
   ├─ Consolidation: Aggregator → Processor (EUDR batches)
   │  └─ Batch number contains EUDR compliance + Hedera recording
   ├─ Processing: Internal transformation (optional)
   │  └─ Records processing details
   └─ Shipment: Processor → Importer
      └─ Final blockchain verification + Tracking number
```

### Key Relationships:

| Entity | EUDR Certificate | Supply Chain Events |
|--------|-----------------|---------------------|
| **Production Unit** | ✅ Must have APPROVED certificate | Source of collections |
| **Aggregator** | ❌ No certificate needed | Consolidates from multiple units |
| **Processor** | ❌ No certificate needed | Processes and ships |
| **Importer** | ❌ No certificate needed | Final destination |

---

## 🛡️ Blockchain Integration

### What Gets Recorded on Hedera?

**1. EUDR Certificates:**
```json
{
  "certificateId": "uuid",
  "productionUnitId": "uuid",
  "farmerId": "uuid",
  "deforestationStatus": "PASSED",
  "certificateStatus": "APPROVED",
  "timestamp": "2025-10-29T10:00:00Z"
}
```

**2. Collection Events:**
```json
{
  "eventId": "uuid",
  "productionUnitId": "uuid",
  "aggregatorId": "uuid",
  "farmerId": "uuid",
  "produceType": "Coffee",
  "quantityKg": 150.0,
  "collectionDate": "2025-10-29"
}
```

**3. Consolidation Events:**
```json
{
  "eventId": "uuid",
  "aggregatorId": "uuid",
  "processorId": "uuid",
  "produceType": "Coffee",
  "quantityKg": 500.0,
  "batchNumber": "EUDR-COF-20251029-3D3638FE-001",
  "consolidationDate": "2025-10-29"
}
```

**4. Shipment Events:**
```json
{
  "eventId": "uuid",
  "processorId": "uuid",
  "importerId": "uuid",
  "produceType": "Coffee",
  "quantityKg": 1000.0,
  "trackingNumber": "TRK-COF-20251029-A1B2C3D4-001",
  "shipmentDate": "2025-10-29"
}
```

### Verification Process:

1. **At Collection:** System checks if production unit has valid EUDR certificate
2. **At Each Stage:** Event recorded on blockchain with transaction ID
3. **At Import:** Importer can verify entire chain using batch number or tracking number
4. **Full Audit Trail:** From EUDR certificate → Collection → Consolidation → Shipment

---

## 👤 User Workflows

### For Exporters:

**Step 1: Certify Production Units**
```
1. Navigate to "EUDR Certificates"
2. Create certificate for each production unit
3. Wait for deforestation verification (automated)
4. Certificate status changes to APPROVED
5. Production unit now eligible for collections
```

**Step 2: Create Supply Chain Workflow**
```
1. Navigate to "Supply Chain Workflow"
2. Create new workflow (e.g., "Coffee Export Q4 2025")
3. Select produce type (Coffee, Tea, Cocoa, etc.)
4. Workflow canvas opens
```

**Step 3: Record Collections**
```
1. Select production unit (must be certified)
2. Select aggregator
3. System auto-populates:
   - Farmer ID
   - Today's date
4. Enter quantity collected
5. Select quality grade (optional)
6. Create collection event
7. ✅ Recorded on blockchain automatically
```

**Step 4: Record Consolidations**
```
1. Select aggregator
2. Select processor
3. System auto-computes:
   - Available quantity (collected - sent)
   - EUDR batch number
4. Verify/adjust quantity
5. Create consolidation event
6. ✅ Recorded on blockchain with batch number
```

**Step 5: Record Shipments**
```
1. Select processor
2. Select importer
3. System auto-computes:
   - Available quantity (received - shipped)
   - Tracking number
4. Enter shipping details
5. Create shipment event
6. ✅ Recorded on blockchain with tracking number
```

### For Importers:

**Verification at Customs:**
```
1. Receive shipment with tracking number (e.g., TRK-COF-20251029-A1B2C3D4-001)
2. Use tracking number to query blockchain
3. View complete audit trail:
   - Shipment event details
   - Consolidation batch numbers
   - Collection events
   - EUDR certificates for each production unit
4. Verify deforestation compliance
5. Confirm quantities match documentation
```

---

## 📊 Data Flow & Traceability

### Forward Traceability (Farm → Import):

```
EUDR Certificate
    ↓
Production Unit (Farm)
    ↓ Collection Event [Blockchain ✓]
Aggregator
    ↓ Consolidation Event [Blockchain ✓] [EUDR Batch]
Processor
    ↓ Shipment Event [Blockchain ✓] [Tracking Number]
Importer
```

### Backward Traceability (Import → Farm):

**Given: Tracking Number** `TRK-COF-20251029-A1B2C3D4-001`

```
1. Query shipment event by tracking number
   └─ Find: Processor ID, Importer ID, Quantity, Date

2. Query consolidation events for processor
   └─ Find: EUDR batch numbers, Aggregator IDs

3. Query collection events by batch numbers
   └─ Find: Production Unit IDs, Farmer IDs

4. Query EUDR certificates for production units
   └─ Find: Deforestation status, Geolocation, Verification date
```

**Result:** Complete chain from import back to specific farms with proof of compliance.

---

## 🎓 Key Concepts Explained

### Why Two Systems?

| EUDR Certification | Supply Chain Workflow |
|-------------------|----------------------|
| **Static compliance** | **Dynamic operations** |
| One-time verification | Continuous tracking |
| Answers: "Is farm compliant?" | Answers: "Where did produce go?" |
| Valid for extended period | Event-by-event recording |
| Pre-requisite for export | Actual export process |

### EUDR Batch Numbers - Why They Matter:

**Problem:** How to link consolidated produce back to original farms?

**Solution:** EUDR-compliant batch numbers that:
- ✅ Identify the consolidation event uniquely
- ✅ Can be traced to collection events
- ✅ Link to EUDR certificates
- ✅ Provide EU customs with verification path
- ✅ Recorded on immutable blockchain

### Blockchain - Why Use It?

**Traditional System:**
- Database can be modified
- Records can be deleted
- Timestamps can be changed
- No independent verification

**Blockchain System:**
- ✅ Immutable records
- ✅ Tamper-proof timestamps
- ✅ Independent verification (anyone can check)
- ✅ Distributed trust (not controlled by one party)
- ✅ Audit trail preserved forever

---

## 🚀 What This Means for Users

### For Farmers:
- Get certified once, participate in multiple supply chains
- Blockchain proof increases trust and potentially price
- Transparent tracking of their produce journey

### For Aggregators:
- Auto-generated EUDR batch numbers simplify paperwork
- Clear visibility of what's been collected vs. sent
- Blockchain verification reduces disputes

### For Processors:
- Auto-calculated available quantities prevent errors
- Tracking numbers link shipments to source batches
- Complete audit trail for compliance officers

### For Importers:
- One tracking number reveals entire supply chain
- Instant verification of deforestation compliance
- Reduced risk of customs issues or fines

### For EU Customs:
- Independent blockchain verification
- No need to trust exporter's database
- EUDR batch numbers provide clear traceability path
- Can verify compliance without lengthy investigations

---

## 📝 Summary

**EUDR Certification** = **Passport for the Farm**  
(One-time compliance check proving deforestation-free status)

**Supply Chain Workflow** = **Journey Tracker for the Produce**  
(Event-by-event recording of physical movement and transformation)

**Blockchain** = **Notary Public**  
(Independent, immutable witness to all events)

**EUDR Batch Numbers** = **DNA of the Shipment**  
(Unique identifier linking final product back to certified farms)

**Result:** Complete, verifiable, tamper-proof traceability from EU supermarket shelf back to the exact farms where produce was grown, with proof those farms are deforestation-free. ✅

---

## 🔍 Technical Implementation Notes

### Database Schema:
- `eudr_certificates` table - stores certification data
- `workflow_collection_events` table - with `hedera_transaction_id`
- `workflow_consolidation_events` table - with `hedera_transaction_id` and `batch_number`
- `workflow_shipment_events` table - with `hedera_transaction_id` and `tracking_number`

### Blockchain Recording:
- Uses Hedera Consensus Service (HCS)
- Each event gets unique transaction ID
- Stored in `hedera_transaction_id` column
- Non-blocking (failures logged but don't stop workflow)

### Auto-computed Fields:
- **Farmer ID** - from production unit relationship
- **Dates** - default to current timestamp
- **Quantities** - calculated from previous events
- **Batch Numbers** - EUDR-compliant format with sequence
- **Tracking Numbers** - unique per processor with sequence

---

*Last Updated: October 29, 2025*

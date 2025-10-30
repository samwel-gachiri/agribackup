# Supply Chain Workflow System - Visual Guide

## 🎯 System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SUPPLY CHAIN WORKFLOW SYSTEM                         │
│                    (Exporter Creates & Manages)                         │
└─────────────────────────────────────────────────────────────────────────┘

                         WORKFLOW LIFECYCLE
                                
  CREATE          COLLECTION         CONSOLIDATION       PROCESSING        SHIPMENT
┌─────────┐     ┌─────────┐        ┌─────────┐        ┌─────────┐      ┌─────────┐
│         │     │         │        │         │        │         │      │         │
│ Exporter│────→│Production│──────→│Aggregator│──────→│Processor│─────→│ Importer│
│ Creates │     │  Units  │ Collect│  Nodes  │ Send  │  Sites  │ Ship │         │
│Workflow │     │         │        │         │        │         │      │         │
└─────────┘     └─────────┘        └─────────┘        └─────────┘      └─────────┘
                     ↓                  ↓                  ↓                ↓
                 Collection         Consolidation      Processing       Shipment
                   Event              Event             Event           Event
                     ↓                  ↓                  ↓                ↓
                ┌─────────────────────────────────────────────────────────────┐
                │          WORKFLOW (Tracks Quantities & Stage)               │
                │                                                             │
                │  Status: IN_PROGRESS → COMPLETED                            │
                │  Stage: COLLECTION → CONSOLIDATION → PROCESSING → SHIPMENT  │
                │                                                             │
                │  Quantities:                                                │
                │  - Total Collected: 5000 kg                                 │
                │  - Total Consolidated: 5000 kg                              │
                │  - Total Processed: 4500 kg (10% loss)                      │
                │  - Total Shipped: 4500 kg                                   │
                │                                                             │
                │  When totalShipped >= totalCollected → Status: COMPLETED    │
                └─────────────────────────────────────────────────────────────┘
```

---

## 📊 Data Flow Example

### Scenario: Coffee Export from 3 Farms to 2 Importers

```
STEP 1: CREATE WORKFLOW
========================
Exporter: "ABC Coffee Exports"
Creates: "Coffee Export 2024 Q1"
Produce Type: "Arabica Coffee"

POST /api/v1/supply-chain/workflows/exporter/exp-123
{
  "workflowName": "Coffee Export 2024 Q1",
  "produceType": "Arabica Coffee"
}

Result:
┌─────────────────────────────────────────┐
│ Workflow ID: wf-12345                   │
│ Status: IN_PROGRESS                     │
│ Stage: COLLECTION                       │
│ Total Quantity: 0 kg                    │
└─────────────────────────────────────────┘


STEP 2: COLLECTION EVENTS (Production Units → Aggregators)
============================================================
Farm A produces 2000 kg → Aggregator 1
Farm B produces 1000 kg → Aggregator 1
Farm C produces 2000 kg → Aggregator 2

POST /api/v1/supply-chain/workflows/wf-12345/collection
Event 1: { productionUnitId: "farm-a", aggregatorId: "agg-1", quantityCollectedKg: 2000 }
Event 2: { productionUnitId: "farm-b", aggregatorId: "agg-1", quantityCollectedKg: 1000 }
Event 3: { productionUnitId: "farm-c", aggregatorId: "agg-2", quantityCollectedKg: 2000 }

Result:
┌──────────────┐
│   Farm A     │        ┌──────────────┐
│   2000 kg    │───────→│              │
└──────────────┘        │ Aggregator 1 │
                        │   3000 kg    │
┌──────────────┐        │  Available   │
│   Farm B     │───────→│              │
│   1000 kg    │        └──────────────┘
└──────────────┘

┌──────────────┐
│   Farm C     │        ┌──────────────┐
│   2000 kg    │───────→│ Aggregator 2 │
└──────────────┘        │   2000 kg    │
                        │  Available   │
                        └──────────────┘

Workflow Updated:
┌─────────────────────────────────────────┐
│ Status: IN_PROGRESS                     │
│ Stage: COLLECTION                       │
│ Total Collected: 5000 kg                │
└─────────────────────────────────────────┘


STEP 3: CHECK AVAILABLE QUANTITIES
====================================
GET /api/v1/supply-chain/workflows/wf-12345/available-quantities

Response:
[
  {
    "aggregatorId": "agg-1",
    "aggregatorName": "ABC Aggregators",
    "totalCollected": 3000,
    "totalSent": 0,
    "available": 3000  ← Use this for pre-filling
  },
  {
    "aggregatorId": "agg-2",
    "aggregatorName": "XYZ Aggregators",
    "totalCollected": 2000,
    "totalSent": 0,
    "available": 2000  ← Use this for pre-filling
  }
]


STEP 4: CONSOLIDATION EVENTS (Aggregators → Processors) WITH SPLITTING
========================================================================
Aggregator 1 (3000 kg) → Split to 2 processors:
  - Send 1000 kg to Processor A
  - Send 2000 kg to Processor B

Aggregator 2 (2000 kg) → Send all to Processor A

POST /api/v1/supply-chain/workflows/wf-12345/consolidation
Event 1: { aggregatorId: "agg-1", processorId: "proc-a", quantitySentKg: 1000 }
  ✓ Success (1000 <= 3000 available)
  
Event 2: { aggregatorId: "agg-1", processorId: "proc-b", quantitySentKg: 2000 }
  ✓ Success (2000 <= 2000 remaining)
  
Event 3: { aggregatorId: "agg-2", processorId: "proc-a", quantitySentKg: 2000 }
  ✓ Success (2000 <= 2000 available)

Result:
┌──────────────┐                       ┌──────────────┐
│ Aggregator 1 │──── 1000 kg ────────→ │ Processor A  │
│   3000 kg    │                       │   3000 kg    │
│              │                       │  (1000+2000) │
│ Available: 0 │──── 2000 kg ───┐      └──────────────┘
└──────────────┘                │
                                │      ┌──────────────┐
                                └────→ │ Processor B  │
┌──────────────┐                       │   2000 kg    │
│ Aggregator 2 │──── 2000 kg ────────→ └──────────────┘
│   2000 kg    │                            ↑
│              │                            │
│ Available: 0 │────────────────────────────┘
└──────────────┘

Workflow Updated:
┌─────────────────────────────────────────┐
│ Status: IN_PROGRESS                     │
│ Stage: CONSOLIDATION ← Auto-advanced!   │
│ Total Collected: 5000 kg                │
│ Total Consolidated: 5000 kg             │
└─────────────────────────────────────────┘


STEP 5: PROCESSING EVENTS
===========================
Processor A processes 3000 kg → 2700 kg output (10% loss)
Processor B processes 2000 kg → 1800 kg output (10% loss)

POST /api/v1/supply-chain/workflows/wf-12345/processing
Event 1: { processorId: "proc-a", quantityProcessedKg: 3000, outputQuantityKg: 2700 }
Event 2: { processorId: "proc-b", quantityProcessedKg: 2000, outputQuantityKg: 1800 }

Result:
┌──────────────┐
│ Processor A  │  Processing: Roasting
│   3000 kg    │  Output: 2700 kg
│              │  Loss: 300 kg (10%)
└──────────────┘

┌──────────────┐
│ Processor B  │  Processing: Roasting
│   2000 kg    │  Output: 1800 kg
│              │  Loss: 200 kg (10%)
└──────────────┘

Workflow Updated:
┌─────────────────────────────────────────┐
│ Status: IN_PROGRESS                     │
│ Stage: PROCESSING ← Auto-advanced!      │
│ Total Processed: 5000 kg (input)        │
│ Output: 4500 kg (for shipment)          │
└─────────────────────────────────────────┘


STEP 6: SHIPMENT EVENTS (Processors → Importers)
==================================================
Processor A (2700 kg) → Importer 1
Processor B (1800 kg) → Importer 2

POST /api/v1/supply-chain/workflows/wf-12345/shipment
Event 1: { 
  processorId: "proc-a", 
  importerId: "imp-1", 
  quantityShippedKg: 2700,
  trackingNumber: "TRACK123"
}

Event 2: { 
  processorId: "proc-b", 
  importerId: "imp-2", 
  quantityShippedKg: 1800,
  trackingNumber: "TRACK456"
}

Result:
┌──────────────┐                       ┌──────────────┐
│ Processor A  │──── 2700 kg ────────→ │  Importer 1  │
│  2700 kg     │  Tracking: TRACK123   │  EU Markets  │
└──────────────┘                       └──────────────┘

┌──────────────┐                       ┌──────────────┐
│ Processor B  │──── 1800 kg ────────→ │  Importer 2  │
│  1800 kg     │  Tracking: TRACK456   │  US Markets  │
└──────────────┘                       └──────────────┘

Workflow Updated:
┌─────────────────────────────────────────┐
│ Status: COMPLETED ← Auto-completed!     │
│ Stage: COMPLETED                        │
│ Total Shipped: 4500 kg                  │
│ Completed At: 2024-01-20T15:30:00       │
└─────────────────────────────────────────┘
```

---

## 🔄 Quantity Tracking Logic

### Available Quantity Calculation

```
For each Aggregator in a Workflow:

  Available Quantity = Total Collected - Total Sent
  
  Where:
  - Total Collected = SUM(collection events for this aggregator)
  - Total Sent = SUM(consolidation events from this aggregator)

Example:
  Aggregator 1 collected:
    - 2000 kg from Farm A
    - 1000 kg from Farm B
    → Total Collected = 3000 kg
    
  Aggregator 1 sent:
    - 1000 kg to Processor A
    - 1500 kg to Processor B
    → Total Sent = 2500 kg
    
  Available = 3000 - 2500 = 500 kg ✓
  
  Next consolidation event CANNOT send more than 500 kg!
```

### Validation on Consolidation

```kotlin
// Backend validation in addConsolidationEvent()

val availableQuantity = getAvailableQuantityForAggregator(workflowId, aggregatorId)

if (request.quantitySentKg > availableQuantity) {
    throw IllegalArgumentException(
        "Insufficient quantity. Available: $availableQuantity kg, " +
        "Requested: ${request.quantitySentKg} kg"
    )
}
```

---

## 🎨 Frontend Visual Workflow Builder Design

### Layout Concept

```
┌─────────────────────────────────────────────────────────────────────────┐
│ Supply Chain Workflow Builder                        [+ Create Workflow]│
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  Active Workflow: Coffee Export 2024 Q1                                  │
│  ┌────────┬────────┬────────┬────────┬────────┐                         │
│  │   ●    │   ●    │   ●    │   ○    │   ○    │  Progress Indicator     │
│  │Collect │Consol. │Process │ Ship   │Complete│                         │
│  └────────┴────────┴────────┴────────┴────────┘                         │
│                                                                           │
│  Total: 5000 kg  |  Stage: Processing  |  Status: In Progress            │
│                                                                           │
│ ┌───────────────────────────────────────────────────────────────────┐   │
│ │                      WORKFLOW CANVAS                              │   │
│ │                                                                   │   │
│ │  ┌──────────┐         ┌──────────┐         ┌──────────┐         │   │
│ │  │Production│         │          │         │          │         │   │
│ │  │  Units   │         │Aggregators│         │Processors│         │   │
│ │  │──────────│         │──────────│         │──────────│         │   │
│ │  │          │         │          │         │          │         │   │
│ │  │ ○ Farm A │         │ ○ Agg 1  │         │ ○ Proc A │         │   │
│ │  │ 2000 kg  │────────→│ 3000 kg  │────────→│ 3000 kg  │         │   │
│ │  │          │    ↑    │ 0 avail  │   ↑     │          │         │   │
│ │  │          │    │    │          │   │     │          │         │   │
│ │  │ ○ Farm B │────┘    │          │   │     │          │         │   │
│ │  │ 1000 kg  │         │          │   │     │ ○ Proc B │         │   │
│ │  │          │         │          │   └────→│ 2000 kg  │         │   │
│ │  │          │         │          │         │          │         │   │
│ │  │ ○ Farm C │         │ ○ Agg 2  │─────────┘         │          │   │
│ │  │ 2000 kg  │────────→│ 2000 kg  │                   │          │   │
│ │  │          │         │ 0 avail  │                   │          │   │
│ │  └──────────┘         └──────────┘         └──────────┘         │   │
│ │                                                                   │   │
│ │  Click two nodes to create connection with quantity              │   │
│ └───────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  [Add Collection] [Add Consolidation] [Add Processing] [Add Shipment]    │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Connection Dialog

```
┌─────────────────────────────────────────┐
│ Add Connection: Agg 1 → Proc A          │
├─────────────────────────────────────────┤
│                                         │
│ Aggregator: ABC Aggregators (Agg 1)    │
│ Available: 3000 kg                      │
│                                         │
│ Processor: XYZ Processor (Proc A)       │
│                                         │
│ Quantity (kg): [1000____]               │
│                ▲                        │
│                │                        │
│            Pre-filled                   │
│         with available                  │
│          or max value                   │
│                                         │
│ Max: 3000 kg                            │
│                                         │
│ Consolidation Date: [2024-01-16]        │
│                                         │
│ Transport Details:                      │
│ [____________________________]          │
│                                         │
│ Batch Number (optional):                │
│ [____________________________]          │
│                                         │
│           [Cancel]  [Submit]            │
└─────────────────────────────────────────┘
```

---

## 🎯 Key Features Visualization

### 1. Quantity Accumulation

```
Farm A (2000 kg) ─┐
                  ├──→ Aggregator 1 (3000 kg total)
Farm B (1000 kg) ─┘
```

### 2. Quantity Splitting

```
                    ┌──→ Processor A (1000 kg)
Aggregator 1 (3000) ┤
                    └──→ Processor B (2000 kg)
```

### 3. Validation

```
Aggregator 1: 3000 kg available
├─ Send 1000 kg to Proc A → ✓ OK (remaining: 2000)
├─ Send 2000 kg to Proc B → ✓ OK (remaining: 0)
└─ Send 500 kg to Proc C  → ✗ ERROR! (available: 0)
```

### 4. Auto-Stage Progression

```
Workflow Timeline:

Create        → Stage: COLLECTION
Add Collection → Stage: COLLECTION (stays)
Add Collection → Stage: COLLECTION (stays)
Add Consolidation → Stage: CONSOLIDATION (advances!)
Add Processing → Stage: PROCESSING (advances!)
Add Shipment  → Stage: SHIPMENT (advances!)
All Shipped   → Stage: COMPLETED (final!)
```

---

## 💡 Implementation Tips

### Frontend State Management

```javascript
// Vue.js example
data() {
  return {
    workflow: null,
    availableQuantities: [],
    selectedSource: null,
    selectedTarget: null,
    collectionEvents: [],
    consolidationEvents: [],
    processingEvents: [],
    shipmentEvents: []
  }
}

methods: {
  async loadWorkflow(workflowId) {
    // Load complete summary
    const response = await axios.get(`/api/v1/supply-chain/workflows/${workflowId}/summary`)
    this.workflow = response.data.workflow
    this.collectionEvents = response.data.collectionEvents
    this.consolidationEvents = response.data.consolidationEvents
    this.processingEvents = response.data.processingEvents
    this.shipmentEvents = response.data.shipmentEvents
  },
  
  async loadAvailableQuantities(workflowId) {
    const response = await axios.get(`/api/v1/supply-chain/workflows/${workflowId}/available-quantities`)
    this.availableQuantities = response.data
  },
  
  async addConsolidation(workflowId, data) {
    try {
      await axios.post(`/api/v1/supply-chain/workflows/${workflowId}/consolidation`, data)
      await this.loadWorkflow(workflowId)  // Refresh
      await this.loadAvailableQuantities(workflowId)  // Refresh
    } catch (error) {
      // Show error: "Insufficient quantity..."
    }
  }
}
```

### Drawing Connections

```javascript
// Canvas connection drawing
drawConnection(sourceNode, targetNode, quantity) {
  const canvas = this.$refs.workflowCanvas
  const ctx = canvas.getContext('2d')
  
  // Get node positions
  const sourcePos = this.getNodePosition(sourceNode)
  const targetPos = this.getNodePosition(targetNode)
  
  // Draw line
  ctx.beginPath()
  ctx.moveTo(sourcePos.x, sourcePos.y)
  ctx.lineTo(targetPos.x, targetPos.y)
  ctx.strokeStyle = '#4CAF50'
  ctx.lineWidth = 2
  ctx.stroke()
  
  // Draw quantity label
  const midX = (sourcePos.x + targetPos.x) / 2
  const midY = (sourcePos.y + targetPos.y) / 2
  ctx.fillStyle = '#000'
  ctx.font = '12px Arial'
  ctx.fillText(`${quantity} kg`, midX, midY)
}
```

---

## 🚀 Next Steps

1. **Run Database Migration**
   - Execute `026_create_supply_chain_workflow_table.yml`
   
2. **Test Backend API**
   - Use Postman/Swagger to test all endpoints
   - Verify validation works (try over-allocation)
   
3. **Build Frontend**
   - Create SupplyChainWorkflow.vue
   - Implement visual canvas
   - Add connection drawing
   - Add quantity input dialogs
   
4. **Integration Testing**
   - Test complete flow: create → collect → consolidate → process → ship
   - Test quantity splitting
   - Test validation

5. **Launch!** 🎉


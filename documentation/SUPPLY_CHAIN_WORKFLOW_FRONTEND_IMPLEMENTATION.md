# Supply Chain Workflow - Frontend Implementation Plan

## Overview
The frontend needs to be updated to use the new workflow API endpoints instead of the old consolidated batches approach.

## Current State
- File: `farmer-portal-frontend/src/views/exporter/SupplyChainWorkflow.vue`
- Uses old API: `/api/v1/aggregators/.../consolidated-batches`
- Shows workflows from OLD system (batches from all exporters)

## Target State
- Use new API: `/api/v1/supply-chain/workflows/...`
- Show only exporter's own workflows
- Visual workflow builder with connections
- Quantity tracking and splitting

---

## Implementation Steps

### Step 1: Update Data Loading ✅ IN PROGRESS

**Change from:**
```javascript
const response = await axios.get(`/api/v1/supply-chain/workflows/exporter/${exporterId}`);
```

**Update workflow card to show:**
- `workflow.workflowName` (not `workflow.name`)
- `workflow.status` (IN_PROGRESS, COMPLETED, CANCELLED)
- `workflow.currentStage` (COLLECTION, CONSOLIDATION, etc.)
- `workflow.totalCollected`
- `workflow.totalShipped`
- Event counts

### Step 2: Update Workflow Creation

**Change from creating consolidated batch to:**
```javascript
async createWorkflow() {
  const exporterId = this.$store.state.auth?.user?.id;
  
  const response = await axios.post(
    `/api/v1/supply-chain/workflows/exporter/${exporterId}`,
    {
      workflowName: this.newWorkflow.workflowName,
      produceType: this.newWorkflow.produceType
    }
  );
  
  await this.loadWorkflows();
  this.selectWorkflow(response.data);
}
```

### Step 3: Load Workflow Summary

**When workflow is selected:**
```javascript
async selectWorkflow(workflow) {
  this.selectedWorkflow = workflow;
  
  // Load complete summary with all events
  const response = await axios.get(
    `/api/v1/supply-chain/workflows/${workflow.id}/summary`
  );
  
  this.workflowSummary = response.data;
  this.collectionEvents = response.data.collectionEvents;
  this.consolidationEvents = response.data.consolidationEvents;
  this.processingEvents = response.data.processingEvents;
  this.shipmentEvents = response.data.shipmentEvents;
  
  // Load available quantities for splitting
  await this.loadAvailableQuantities(workflow.id);
  
  this.showWorkflowDetails = true;
}
```

### Step 4: Load Available Quantities

```javascript
async loadAvailableQuantities(workflowId) {
  try {
    const response = await axios.get(
      `/api/v1/supply-chain/workflows/${workflowId}/available-quantities`
    );
    this.availableQuantities = response.data;
  } catch (error) {
    console.error('Failed to load available quantities:', error);
  }
}
```

### Step 5: Update Collection Event Creation

```javascript
async addCollectionEvent(data) {
  try {
    await axios.post(
      `/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/collection`,
      {
        productionUnitId: data.productionUnitId,
        aggregatorId: data.aggregatorId,
        farmerId: data.farmerId,
        quantityCollectedKg: data.quantityCollectedKg,
        collectionDate: data.collectionDate,
        qualityGrade: data.qualityGrade,
        notes: data.notes
      }
    );
    
    // Refresh workflow
    await this.selectWorkflow(this.selectedWorkflow);
    this.showSnackbar('Collection event added successfully', 'success');
  } catch (error) {
    this.showSnackbar(error.response?.data?.message || 'Failed to add collection event', 'error');
  }
}
```

### Step 6: Update Consolidation Event Creation (With Validation)

```javascript
async addConsolidationEvent(data) {
  try {
    // Validation already handled by backend, but we can show available quantity
    const available = this.availableQuantities.find(
      q => q.aggregatorId === data.aggregatorId
    );
    
    if (data.quantitySentKg > available.available) {
      this.showSnackbar(
        `Insufficient quantity. Available: ${available.available} kg`,
        'error'
      );
      return;
    }
    
    await axios.post(
      `/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/consolidation`,
      {
        aggregatorId: data.aggregatorId,
        processorId: data.processorId,
        quantitySentKg: data.quantitySentKg,
        consolidationDate: data.consolidationDate,
        transportDetails: data.transportDetails,
        batchNumber: data.batchNumber
      }
    );
    
    // Refresh workflow and available quantities
    await this.selectWorkflow(this.selectedWorkflow);
    this.showSnackbar('Consolidation event added successfully', 'success');
  } catch (error) {
    this.showSnackbar(error.response?.data?.message || 'Failed to add consolidation event', 'error');
  }
}
```

### Step 7: Update Processing Event Creation

```javascript
async addProcessingEvent(data) {
  try {
    await axios.post(
      `/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/processing`,
      {
        processorId: data.processorId,
        quantityProcessedKg: data.quantityProcessedKg,
        processingDate: data.processingDate,
        processingType: data.processingType,
        outputQuantityKg: data.outputQuantityKg,
        processingNotes: data.processingNotes
      }
    );
    
    await this.selectWorkflow(this.selectedWorkflow);
    this.showSnackbar('Processing event added successfully', 'success');
  } catch (error) {
    this.showSnackbar(error.response?.data?.message || 'Failed to add processing event', 'error');
  }
}
```

### Step 8: Update Shipment Event Creation

```javascript
async addShipmentEvent(data) {
  try {
    await axios.post(
      `/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/shipment`,
      {
        processorId: data.processorId,
        importerId: data.importerId,
        quantityShippedKg: data.quantityShippedKg,
        shipmentDate: data.shipmentDate,
        expectedArrivalDate: data.expectedArrivalDate,
        shippingCompany: data.shippingCompany,
        trackingNumber: data.trackingNumber,
        destinationPort: data.destinationPort,
        shipmentNotes: data.shipmentNotes
      }
    );
    
    await this.selectWorkflow(this.selectedWorkflow);
    this.showSnackbar('Shipment event added successfully', 'success');
  } catch (error) {
    this.showSnackbar(error.response?.data?.message || 'Failed to add shipment event', 'error');
  }
}
```

---

## Visual Canvas Implementation (Future Enhancement)

### Option 1: Simple List View (Immediate)
Show events in a timeline/list format:
- Collection Events (Production Unit → Aggregator)
- Consolidation Events (Aggregator → Processor)
- Processing Events
- Shipment Events (Processor → Importer)

### Option 2: Visual Workflow Builder (Advanced)
Implement drag-and-drop canvas with:
- 4 columns: Production Units | Aggregators | Processors | Importers
- Connection lines between entities
- Quantity labels on connections
- Click-to-connect interaction

---

## Changes Summary

### Files to Modify
1. `SupplyChainWorkflow.vue` - Main workflow page

### API Endpoints to Use
- `POST /api/v1/supply-chain/workflows/exporter/{exporterId}` - Create workflow
- `GET /api/v1/supply-chain/workflows/exporter/{exporterId}` - List workflows
- `GET /api/v1/supply-chain/workflows/{id}` - Get workflow
- `GET /api/v1/supply-chain/workflows/{id}/summary` - Get complete summary
- `POST /api/v1/supply-chain/workflows/{id}/collection` - Add collection event
- `POST /api/v1/supply-chain/workflows/{id}/consolidation` - Add consolidation event
- `POST /api/v1/supply-chain/workflows/{id}/processing` - Add processing event
- `POST /api/v1/supply-chain/workflows/{id}/shipment` - Add shipment event
- `GET /api/v1/supply-chain/workflows/{id}/available-quantities` - Get available quantities

### Key Changes
1. Change workflow data structure to match new API response
2. Update workflow creation to use new endpoint
3. Update event creation to use new endpoints
4. Add available quantities loading for validation
5. Show proper error messages from backend validation

---

## Testing Plan

1. Test workflow creation
2. Test workflow listing (should only show exporter's workflows)
3. Test adding collection events
4. Test adding consolidation events (with quantity splitting)
5. Test validation (try to send more than available)
6. Test adding processing events
7. Test adding shipment events
8. Test auto-stage progression (verify stage changes automatically)
9. Test auto-completion (verify workflow completes when all shipped)

---

## Next Steps

1. Update SupplyChainWorkflow.vue with new API endpoints
2. Test basic workflow creation and event addition
3. Add better UI for viewing events
4. Consider implementing visual canvas (future enhancement)


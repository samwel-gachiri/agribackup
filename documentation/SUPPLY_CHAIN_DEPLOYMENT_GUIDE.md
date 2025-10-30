# Supply Chain Workflow - Deployment Guide

## 🚀 Step-by-Step Deployment

### Prerequisites
- [ ] Database backup completed
- [ ] Backend application stopped (if running)
- [ ] All tests passing
- [ ] Database credentials configured

---

## Step 1: Database Migration

### Option A: Liquibase Auto-Migration (Recommended)

The migration will run automatically when you start the Spring Boot application.

1. **Verify migration file exists:**
   ```
   farmers-portal-apis/src/main/resources/db/changelog/
     └── 026_create_supply_chain_workflow_table.yml
   ```

2. **Ensure it's included in master changelog:**
   ```yaml
   # db/changelog/db.changelog-master.yaml
   databaseChangeLog:
     # ... existing changesets
     - include:
         file: db/changelog/026_create_supply_chain_workflow_table.yml
   ```

3. **Start application:**
   ```bash
   cd farmers-portal-apis
   ./mvnw spring-boot:run
   ```

4. **Verify migration in logs:**
   ```
   Liquibase: Successfully acquired change log lock
   Liquibase: Creating database history table with name: DATABASECHANGELOG
   Liquibase: Reading from DATABASECHANGELOG
   Liquibase: Running Changeset: db/changelog/026_create_supply_chain_workflow_table.yml::026::agribackup
   Liquibase: New row inserted into DATABASECHANGELOG
   Liquibase: Successfully released change log lock
   ```

### Option B: Manual Liquibase Execution

If you want to run migration separately:

```bash
cd farmers-portal-apis

# Run migration
./mvnw liquibase:update

# Verify migration status
./mvnw liquibase:status
```

### Option C: Generate SQL and Execute Manually

If you need to review SQL before execution:

```bash
cd farmers-portal-apis

# Generate SQL script
./mvnw liquibase:updateSQL > migration-026.sql

# Review the generated SQL
cat migration-026.sql

# Execute manually if approved
mysql -u username -p database_name < migration-026.sql
```

---

## Step 2: Verify Database Changes

### Check Tables Created

```sql
-- Connect to your database
mysql -u username -p database_name

-- Verify all 5 tables exist
SHOW TABLES LIKE 'workflow%';
-- Expected output:
-- workflow_collection_events
-- workflow_consolidation_events
-- workflow_processing_events
-- workflow_shipment_events

SHOW TABLES LIKE 'supply_chain_workflows';
-- Expected output:
-- supply_chain_workflows

-- Check table structure
DESCRIBE supply_chain_workflows;
DESCRIBE workflow_collection_events;
DESCRIBE workflow_consolidation_events;
DESCRIBE workflow_processing_events;
DESCRIBE workflow_shipment_events;

-- Verify foreign key constraints
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'your_database_name'
    AND TABLE_NAME IN (
        'supply_chain_workflows',
        'workflow_collection_events',
        'workflow_consolidation_events',
        'workflow_processing_events',
        'workflow_shipment_events'
    )
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Verify indexes
SHOW INDEX FROM supply_chain_workflows;
SHOW INDEX FROM workflow_collection_events;
SHOW INDEX FROM workflow_consolidation_events;
SHOW INDEX FROM workflow_processing_events;
SHOW INDEX FROM workflow_shipment_events;
```

---

## Step 3: Start Backend Application

### Development Mode

```bash
cd farmers-portal-apis
./mvnw spring-boot:run
```

### Production Mode

```bash
cd farmers-portal-apis

# Build JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/farmers-portal-apis-0.0.1-SNAPSHOT.jar
```

### Verify Application Started

Check logs for:
```
Started FarmersPortalApisApplication in X.XXX seconds
```

---

## Step 4: Test API Endpoints

### Using cURL

#### 1. Create Workflow
```bash
# Replace with actual exporter ID and JWT token
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/exporter/YOUR_EXPORTER_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "workflowName": "Test Workflow 1",
    "produceType": "Coffee"
  }'

# Expected response (200 OK):
{
  "id": "uuid-here",
  "exporterId": "YOUR_EXPORTER_ID",
  "workflowName": "Test Workflow 1",
  "produceType": "Coffee",
  "status": "IN_PROGRESS",
  "currentStage": "COLLECTION",
  "totalQuantityKg": 0,
  ...
}

# Save the workflow ID for next steps
WORKFLOW_ID="uuid-from-response"
```

#### 2. Add Collection Event
```bash
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/$WORKFLOW_ID/collection \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productionUnitId": "YOUR_PRODUCTION_UNIT_ID",
    "aggregatorId": "YOUR_AGGREGATOR_ID",
    "farmerId": "YOUR_FARMER_ID",
    "quantityCollectedKg": 1000.0,
    "collectionDate": "2024-01-15",
    "qualityGrade": "A-Premium",
    "notes": "Excellent harvest"
  }'

# Expected response (200 OK):
{
  "id": "uuid-here",
  "workflowId": "...",
  "quantityCollectedKg": 1000.0,
  ...
}
```

#### 3. Get Available Quantities
```bash
curl -X GET http://localhost:8080/api/v1/supply-chain/workflows/$WORKFLOW_ID/available-quantities \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected response (200 OK):
[
  {
    "aggregatorId": "...",
    "aggregatorName": "...",
    "totalCollected": 1000.0,
    "totalSent": 0.0,
    "available": 1000.0
  }
]
```

#### 4. Add Consolidation Event
```bash
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/$WORKFLOW_ID/consolidation \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "aggregatorId": "YOUR_AGGREGATOR_ID",
    "processorId": "YOUR_PROCESSOR_ID",
    "quantitySentKg": 500.0,
    "consolidationDate": "2024-01-16",
    "transportDetails": "Truck ABC123",
    "batchNumber": "BATCH-001"
  }'

# Expected response (200 OK):
{
  "id": "uuid-here",
  "workflowId": "...",
  "quantitySentKg": 500.0,
  ...
}
```

#### 5. Test Over-Allocation (Should Fail)
```bash
curl -X POST http://localhost:8080/api/v1/supply-chain/workflows/$WORKFLOW_ID/consolidation \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "aggregatorId": "YOUR_AGGREGATOR_ID",
    "processorId": "YOUR_PROCESSOR_ID",
    "quantitySentKg": 1000.0,
    "consolidationDate": "2024-01-16"
  }'

# Expected response (400 Bad Request):
{
  "error": "Insufficient quantity. Available: 500.0 kg"
}
```

#### 6. Get Workflow Summary
```bash
curl -X GET http://localhost:8080/api/v1/supply-chain/workflows/$WORKFLOW_ID/summary \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected response (200 OK):
{
  "workflow": { ... },
  "collectionEvents": [ ... ],
  "consolidationEvents": [ ... ],
  "processingEvents": [],
  "shipmentEvents": []
}
```

### Using Swagger UI

1. Open browser: `http://localhost:8080/swagger-ui.html`
2. Find "Supply Chain Workflow" section
3. Click "Authorize" and enter JWT token
4. Test each endpoint interactively

---

## Step 5: Rollback Plan (If Needed)

### Option A: Liquibase Rollback

```bash
cd farmers-portal-apis

# Rollback last changeset
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Or rollback to specific tag
./mvnw liquibase:rollback -Dliquibase.rollbackTag=before-workflow-tables
```

### Option B: Manual Rollback

```sql
-- Connect to database
mysql -u username -p database_name

-- Drop tables in reverse order (to respect FK constraints)
DROP TABLE IF EXISTS workflow_shipment_events;
DROP TABLE IF EXISTS workflow_processing_events;
DROP TABLE IF EXISTS workflow_consolidation_events;
DROP TABLE IF EXISTS workflow_collection_events;
DROP TABLE IF EXISTS supply_chain_workflows;

-- Remove from changelog
DELETE FROM DATABASECHANGELOG WHERE id = '026';
```

---

## Step 6: Monitoring

### Check Application Logs

```bash
# View logs in real-time
tail -f logs/application.log

# Search for errors
grep ERROR logs/application.log

# Search for workflow operations
grep "SupplyChainWorkflow" logs/application.log
```

### Monitor Database

```sql
-- Check record counts
SELECT COUNT(*) FROM supply_chain_workflows;
SELECT COUNT(*) FROM workflow_collection_events;
SELECT COUNT(*) FROM workflow_consolidation_events;
SELECT COUNT(*) FROM workflow_processing_events;
SELECT COUNT(*) FROM workflow_shipment_events;

-- Check recent workflows
SELECT 
    id, 
    workflow_name, 
    status, 
    current_stage, 
    total_quantity_kg,
    created_at
FROM supply_chain_workflows
ORDER BY created_at DESC
LIMIT 10;

-- Check workflow with events
SELECT 
    w.workflow_name,
    w.status,
    w.current_stage,
    COUNT(DISTINCT c.id) as collection_count,
    COUNT(DISTINCT co.id) as consolidation_count,
    COUNT(DISTINCT p.id) as processing_count,
    COUNT(DISTINCT s.id) as shipment_count
FROM supply_chain_workflows w
LEFT JOIN workflow_collection_events c ON w.id = c.workflow_id
LEFT JOIN workflow_consolidation_events co ON w.id = co.workflow_id
LEFT JOIN workflow_processing_events p ON w.id = p.workflow_id
LEFT JOIN workflow_shipment_events s ON w.id = s.workflow_id
GROUP BY w.id, w.workflow_name, w.status, w.current_stage;
```

---

## Step 7: Documentation

### Update API Documentation

1. Access Swagger UI: `http://localhost:8080/swagger-ui.html`
2. Verify all endpoints documented
3. Export OpenAPI spec if needed

### Update User Documentation

- [ ] Add workflow creation guide
- [ ] Add quantity splitting tutorial
- [ ] Add troubleshooting section
- [ ] Create video tutorial (optional)

---

## ✅ Deployment Checklist

### Pre-Deployment
- [ ] Database backup completed
- [ ] Migration file reviewed
- [ ] All tests passing locally
- [ ] Code reviewed and approved

### Deployment
- [ ] Migration executed successfully
- [ ] All 5 tables created
- [ ] Foreign keys created
- [ ] Indexes created
- [ ] Application started successfully
- [ ] No errors in logs

### Post-Deployment Verification
- [ ] Can create workflow
- [ ] Can add collection event
- [ ] Can get available quantities
- [ ] Can add consolidation event
- [ ] Validation works (over-allocation prevented)
- [ ] Can get workflow summary
- [ ] Auto-stage progression works
- [ ] Auto-completion works

### Monitoring
- [ ] Application logs monitored
- [ ] Database queries perform well
- [ ] No memory leaks
- [ ] API response times acceptable

---

## 🆘 Troubleshooting

### Migration Fails

**Problem:** "Table 'supply_chain_workflows' already exists"
**Solution:** Table was created in previous run. Either:
1. Drop table and re-run migration
2. Mark changeset as executed: `UPDATE DATABASECHANGELOG SET ...`

**Problem:** "Foreign key constraint fails"
**Solution:** Referenced table doesn't exist. Check:
1. Exporters table exists
2. Aggregators table exists
3. Processors table exists
4. Importers table exists
5. Production units table exists
6. Farmer profiles table exists

### Application Won't Start

**Problem:** "Bean creation failed"
**Solution:** Check:
1. All repository interfaces exist
2. All entity classes exist
3. All DTOs exist
4. Service class properly annotated with `@Service`
5. Controller class properly annotated with `@RestController`

### API Returns 404

**Problem:** Endpoint not found
**Solution:** Check:
1. Application started successfully
2. Controller mapped to correct path
3. Security allows access
4. JWT token valid

### API Returns 500

**Problem:** Internal server error
**Solution:** Check:
1. Application logs for stack trace
2. Database connection working
3. Referenced entities exist
4. Request body matches DTO structure

---

## 📞 Support

If you encounter issues:

1. **Check logs:** `tail -f logs/application.log`
2. **Check database:** Verify tables and data
3. **Check API:** Test with Postman/cURL
4. **Review code:** Check for typos or missing files
5. **Rollback if needed:** Use rollback plan above

---

## 🎉 Success!

If all checks pass, your Supply Chain Workflow system is now live!

**Next steps:**
1. Build frontend visual workflow builder
2. Train users
3. Monitor adoption
4. Collect feedback
5. Iterate and improve


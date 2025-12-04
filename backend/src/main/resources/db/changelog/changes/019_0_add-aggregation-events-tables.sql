--liquibase formatted sql
-- ADD MISSING AGGREGATION EVENTS AND CONSOLIDATED BATCHES TABLES

--changeset samwel:create-aggregation-events-table
CREATE TABLE aggregation_events (
    event_id VARCHAR(36) PRIMARY KEY,
    aggregator_id VARCHAR(36) NOT NULL,
    farmer_id VARCHAR(36) NOT NULL,
    farmer_name VARCHAR(255),
    produce_type VARCHAR(255) NOT NULL,
    quantity_kg DECIMAL(15,2) NOT NULL,
    quality_grade VARCHAR(50),
    price_per_kg DECIMAL(10,2),
    total_payment DECIMAL(15,2),
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    collection_date TIMESTAMP NOT NULL,
    collection_location_gps VARCHAR(255),
    moisture_content DECIMAL(5,2),
    impurity_percentage DECIMAL(5,2),
    notes TEXT,
    hedera_transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consolidated_batch_id VARCHAR(36),
    CONSTRAINT fk_aggregation_events_aggregator FOREIGN KEY (aggregator_id) REFERENCES aggregators(aggregator_id) ON DELETE CASCADE,
    CONSTRAINT fk_aggregation_events_farmer FOREIGN KEY (farmer_id) REFERENCES farmers(farmer_id) ON DELETE CASCADE
);

--changeset samwel:create-aggregation-events-indexes
CREATE INDEX idx_aggregation_events_aggregator ON aggregation_events(aggregator_id);
CREATE INDEX idx_aggregation_events_farmer ON aggregation_events(farmer_id);
CREATE INDEX idx_aggregation_events_payment_status ON aggregation_events(payment_status);
CREATE INDEX idx_aggregation_events_collection_date ON aggregation_events(collection_date);
CREATE INDEX idx_aggregation_events_consolidated_batch ON aggregation_events(consolidated_batch_id);

--changeset samwel:create-consolidated-batches-table
CREATE TABLE consolidated_batches (
    batch_id VARCHAR(36) PRIMARY KEY,
    aggregator_id VARCHAR(36) NOT NULL,
    batch_number VARCHAR(100) NOT NULL UNIQUE,
    produce_type VARCHAR(255) NOT NULL,
    total_quantity_kg DECIMAL(15,2) NOT NULL,
    number_of_farmers INT NOT NULL DEFAULT 0,
    average_quality_grade VARCHAR(50),
    consolidation_date TIMESTAMP NOT NULL,
    destination_entity_id VARCHAR(36),
    destination_entity_type VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    shipment_date TIMESTAMP,
    delivery_date TIMESTAMP,
    transport_details TEXT,
    hedera_transaction_id VARCHAR(100),
    hedera_batch_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_consolidated_batches_aggregator FOREIGN KEY (aggregator_id) REFERENCES aggregators(aggregator_id) ON DELETE CASCADE
);

--changeset samwel:create-consolidated-batches-indexes
CREATE INDEX idx_consolidated_batches_aggregator ON consolidated_batches(aggregator_id);
CREATE INDEX idx_consolidated_batches_batch_number ON consolidated_batches(batch_number);
CREATE INDEX idx_consolidated_batches_status ON consolidated_batches(status);
CREATE INDEX idx_consolidated_batches_produce_type ON consolidated_batches(produce_type);
CREATE INDEX idx_consolidated_batches_consolidation_date ON consolidated_batches(consolidation_date);

--changeset samwel:add-consolidated-batch-fk-to-aggregation-events
ALTER TABLE aggregation_events
ADD CONSTRAINT fk_aggregation_events_consolidated_batch
FOREIGN KEY (consolidated_batch_id) REFERENCES consolidated_batches(batch_id) ON DELETE SET NULL;

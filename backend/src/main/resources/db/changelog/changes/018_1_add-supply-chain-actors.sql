--liquibase formatted sql
-- OPTIMIZED SUPPLY CHAIN ACTORS MIGRATION
-- Strategy: Normalize data using separate tables instead of adding 30+ columns to eudr_batches

--changeset samwel:create-aggregators-table
CREATE TABLE aggregators (
    aggregator_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    organization_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100) NOT NULL UNIQUE,
    aggregator_type VARCHAR(50) NOT NULL DEFAULT 'COOPERATIVE',
    operating_region VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    storage_capacity_kg DECIMAL(15,2),
    number_of_members INT,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMP NULL,
    hedera_account_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_aggregators_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

--changeset samwel:create-aggregators-indexes
CREATE INDEX idx_aggregators_user_id ON aggregators(user_id);
CREATE INDEX idx_aggregators_verification_status ON aggregators(verification_status);

--changeset samwel:create-importers-table
CREATE TABLE importers (
    importer_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    import_license_number VARCHAR(100) NOT NULL UNIQUE,
    company_address TEXT NOT NULL,
    destination_country VARCHAR(100) NOT NULL,
    destination_port VARCHAR(255),
    eudr_compliance_officer VARCHAR(255),
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMP NULL,
    hedera_account_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_importers_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

--changeset samwel:create-importers-indexes
CREATE INDEX idx_importers_user_id ON importers(user_id);
CREATE INDEX idx_importers_verification_status ON importers(verification_status);
CREATE INDEX idx_importers_destination_country ON importers(destination_country);


--changeset samwel:create-batch-shipments-table
-- Separate table for shipment details instead of adding 20+ columns to eudr_batches
CREATE TABLE batch_shipments (
    shipment_id VARCHAR(36) PRIMARY KEY,
    batch_id VARCHAR(36) NOT NULL UNIQUE,
    importer_id VARCHAR(36) NOT NULL,
    shipment_number VARCHAR(100) NOT NULL UNIQUE,
    origin_country VARCHAR(100) NOT NULL,
    departure_port VARCHAR(255),
    arrival_port VARCHAR(255),
    shipping_date DATE NOT NULL,
    estimated_arrival_date DATE,
    actual_arrival_date DATE,
    shipment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    customs_reference_number VARCHAR(100),
    customs_clearance_date DATE,
    bill_of_lading_number VARCHAR(100),
    container_numbers TEXT,
    transport_method VARCHAR(100),
    transport_company VARCHAR(255),
    temperature_controlled BOOLEAN DEFAULT FALSE,
    eudr_compliance_status VARCHAR(50) DEFAULT 'PENDING_REVIEW',
    hedera_shipment_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES eudr_batches(batch_id) ON DELETE CASCADE,
    FOREIGN KEY (importer_id) REFERENCES importers(importer_id) ON DELETE CASCADE
);

--changeset samwel:create-batch-shipments-indexes
CREATE INDEX idx_batch_shipments_batch_id ON batch_shipments(batch_id);
CREATE INDEX idx_batch_shipments_importer_id ON batch_shipments(importer_id);
CREATE INDEX idx_batch_shipments_status ON batch_shipments(shipment_status);
CREATE INDEX idx_batch_shipments_number ON batch_shipments(shipment_number);

--changeset samwel:create-batch-inspections-table
-- Separate table for inspection records instead of adding columns to eudr_batches
CREATE TABLE batch_inspections (
    inspection_id VARCHAR(36) PRIMARY KEY,
    batch_id VARCHAR(36),
    shipment_id VARCHAR(36),
    inspection_type VARCHAR(100) NOT NULL,
    inspection_date DATE NOT NULL,
    inspector_name VARCHAR(255) NOT NULL,
    inspector_agency VARCHAR(255),
    inspection_result VARCHAR(50) NOT NULL,
    findings TEXT,
    recommendations TEXT,
    certificate_number VARCHAR(100),
    hedera_inspection_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (batch_id) REFERENCES eudr_batches(batch_id) ON DELETE CASCADE,
    FOREIGN KEY (shipment_id) REFERENCES batch_shipments(shipment_id) ON DELETE CASCADE,
    CHECK (batch_id IS NOT NULL OR shipment_id IS NOT NULL)
);

--changeset samwel:create-batch-inspections-indexes
CREATE INDEX idx_batch_inspections_batch_id ON batch_inspections(batch_id);
CREATE INDEX idx_batch_inspections_shipment_id ON batch_inspections(shipment_id);
CREATE INDEX idx_batch_inspections_result ON batch_inspections(inspection_result);
CREATE INDEX idx_batch_inspections_date ON batch_inspections(inspection_date);

--changeset samwel:add-minimal-aggregator-columns-to-eudr-batches
-- Only add essential FK columns to eudr_batches
ALTER TABLE eudr_batches 
ADD COLUMN aggregator_id VARCHAR(36) AFTER created_by,
ADD COLUMN processor_id VARCHAR(36) AFTER aggregator_id;

--changeset samwel:add-aggregator-fk-to-eudr-batches
ALTER TABLE eudr_batches
ADD CONSTRAINT fk_eudr_batches_aggregator
FOREIGN KEY (aggregator_id) REFERENCES aggregators(aggregator_id) ON DELETE SET NULL;

--changeset samwel:add-processor-fk-to-eudr-batches
ALTER TABLE eudr_batches
ADD CONSTRAINT fk_eudr_batches_processor
FOREIGN KEY (processor_id) REFERENCES processors(processor_id) ON DELETE SET NULL;

--changeset samwel:create-actor-indexes-on-eudr-batches
CREATE INDEX idx_eudr_batches_aggregator_id ON eudr_batches(aggregator_id);
CREATE INDEX idx_eudr_batches_processor_id ON eudr_batches(processor_id);

--changeset samwel:create-farmer-collections-table
-- New table for tracking individual farmer collections (replaces supply_chain_events extension)
CREATE TABLE farmer_collections (
    collection_id VARCHAR(36) PRIMARY KEY,
    aggregator_id VARCHAR(36) NOT NULL,
    farmer_id VARCHAR(36) NOT NULL,
    batch_id VARCHAR(36),
    collection_date TIMESTAMP NOT NULL,
    produce_type VARCHAR(255) NOT NULL,
    quantity_kg DECIMAL(15,2) NOT NULL,
    quality_grade VARCHAR(50),
    moisture_content DECIMAL(5,2),
    price_per_kg DECIMAL(10,2),
    total_amount DECIMAL(15,2),
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_date TIMESTAMP NULL,
    receipt_number VARCHAR(100),
    collection_location VARCHAR(255),
    notes TEXT,
    hedera_transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aggregator_id) REFERENCES aggregators(aggregator_id) ON DELETE CASCADE,
    FOREIGN KEY (farmer_id) REFERENCES farmers(farmer_id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES eudr_batches(batch_id) ON DELETE SET NULL
);

--changeset samwel:create-farmer-collections-indexes
CREATE INDEX idx_farmer_collections_aggregator_id ON farmer_collections(aggregator_id);
CREATE INDEX idx_farmer_collections_farmer_id ON farmer_collections(farmer_id);
CREATE INDEX idx_farmer_collections_batch_id ON farmer_collections(batch_id);
CREATE INDEX idx_farmer_collections_payment_status ON farmer_collections(payment_status);
CREATE INDEX idx_farmer_collections_date ON farmer_collections(collection_date);

--changeset samwel:add-new-roles
INSERT INTO roles (id, name, description) VALUES
('role_aggregator', 'ROLE_AGGREGATOR', 'Cooperative or collection center managing farmer produce aggregation'),
('role_processor', 'ROLE_PROCESSOR', 'Facility that processes agricultural products'),
('role_importer', 'ROLE_IMPORTER', 'Company importing products into EU market')
ON DUPLICATE KEY UPDATE description = VALUES(description);

--changeset samwel:add-aggregator-permissions
INSERT INTO permissions (id, name, description) VALUES
('perm_manage_collections', 'MANAGE_COLLECTIONS', 'Can record and manage farmer collection events'),
('perm_create_batches', 'CREATE_CONSOLIDATED_BATCHES', 'Can create consolidated batches from collections'),
('perm_view_farmers', 'VIEW_FARMERS', 'Can view farmer information in their region'),
('perm_manage_payments', 'MANAGE_FARMER_PAYMENTS', 'Can update payment status for farmers')
ON DUPLICATE KEY UPDATE description = VALUES(description);

--changeset samwel:add-importer-permissions
INSERT INTO permissions (id, name, description) VALUES
('perm_manage_shipments', 'MANAGE_IMPORT_SHIPMENTS', 'Can create and manage import shipments'),
('perm_upload_customs_docs', 'UPLOAD_CUSTOMS_DOCUMENTS', 'Can upload customs and regulatory documents'),
('perm_record_inspections', 'RECORD_INSPECTIONS', 'Can record inspection results'),
('perm_update_compliance', 'UPDATE_EUDR_COMPLIANCE', 'Can update EUDR compliance status')
ON DUPLICATE KEY UPDATE description = VALUES(description);

--changeset samwel:add-processor-permissions
INSERT INTO permissions (id, name, description) VALUES
('perm_record_processing', 'RECORD_PROCESSING_EVENTS', 'Can record processing events'),
('perm_view_batches', 'VIEW_BATCHES', 'Can view batches for processing')
ON DUPLICATE KEY UPDATE description = VALUES(description);

--changeset samwel:link-aggregator-role-permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
('role_aggregator', 'perm_manage_collections'),
('role_aggregator', 'perm_create_batches'),
('role_aggregator', 'perm_view_farmers'),
('role_aggregator', 'perm_manage_payments')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

--changeset samwel:link-importer-role-permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
('role_importer', 'perm_manage_shipments'),
('role_importer', 'perm_upload_customs_docs'),
('role_importer', 'perm_record_inspections'),
('role_importer', 'perm_update_compliance')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

--changeset samwel:link-processor-role-permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
('role_processor', 'perm_record_processing'),
('role_processor', 'perm_view_batches')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);


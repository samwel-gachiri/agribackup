--liquibase formatted sql

--changeset samwel:make-aggregator-registration-number-nullable
-- Allow registration_number to be NULL to support optional registration during signup
ALTER TABLE aggregators 
MODIFY COLUMN registration_number VARCHAR(100) NULL;

--changeset samwel:drop-aggregator-registration-number-unique-constraint
-- Drop UNIQUE constraint to allow NULL values and avoid issues with optional registration
ALTER TABLE aggregators 
DROP INDEX registration_number;

--changeset samwel:add-aggregator-registration-number-unique-constraint-for-non-null
-- Add conditional UNIQUE constraint only for non-NULL values
ALTER TABLE aggregators 
ADD CONSTRAINT uq_aggregator_registration_number UNIQUE (registration_number);

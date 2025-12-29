package com.agriconnect.farmersportalapis.domain.auth

enum class RoleType {
    FARMER,
    BUYER,
    EXPORTER,
    AGGREGATOR,
    PROCESSOR,
    IMPORTER,
    SUPPLIER, // Generic supplier role (type determined by supplier_type in supply_chain_suppliers)
    ADMIN,
    SYSTEM_ADMIN,
    ZONE_SUPERVISOR,
    USER
}
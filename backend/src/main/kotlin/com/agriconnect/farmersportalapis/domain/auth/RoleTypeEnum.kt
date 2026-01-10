package com.agriconnect.farmersportalapis.domain.auth

enum class RoleType {
    FARMER,
    BUYER,
    EXPORTER,
    AGGREGATOR,
    PROCESSOR,
    IMPORTER,
    SUPPLIER, // Generic supplier role (type determined by supplier_type in supply_chain_suppliers)
    AUTHORISED_REPRESENTATIVE, // EUDR Article 6 - EU-based AR for non-EU operators
    ADMIN,
    SYSTEM_ADMIN,
    ZONE_SUPERVISOR,
    USER
}
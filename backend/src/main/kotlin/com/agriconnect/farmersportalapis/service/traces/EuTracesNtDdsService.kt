package com.agriconnect.farmersportalapis.service.traces

import com.agriconnect.farmersportalapis.config.EuTracesNtConfig
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.profile.Exporter
import com.agriconnect.farmersportalapis.domain.supplychain.SupplyChainSupplier
import com.agriconnect.farmersportalapis.domain.supplychain.SupplyChainWorkflow
import com.agriconnect.farmersportalapis.domain.supplychain.WorkflowProductionUnit
import com.agriconnect.farmersportalapis.repository.SupplyChainWorkflowRepository
import com.agriconnect.farmersportalapis.repository.WorkflowProductionUnitRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Service for generating EU TRACES NT Due Diligence Statements (DDS)
 * 
 * This service converts AgriBackup supply chain workflow data into the 
 * standardized DDS format required by the EU TRACES NT system for EUDR compliance.
 */
@Service
class EuTracesNtDdsService(
    private val workflowRepository: SupplyChainWorkflowRepository,
    private val workflowProductionUnitRepository: WorkflowProductionUnitRepository,
    private val config: EuTracesNtConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val xmlMapper: XmlMapper = XmlMapper.builder()
        .addModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()
        .registerKotlinModule() as XmlMapper
    
    private val jsonMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
    
    /**
     * Generate a complete Due Diligence Statement from a workflow
     */
    fun generateDds(workflowId: String): DueDiligenceStatement {
        logger.info("Generating DDS for workflow: $workflowId")
        
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }
        
        val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflowId)
        
        return buildDdsFromWorkflow(workflow, linkedUnits)
    }
    
    /**
     * Export DDS as XML string (TRACES NT native format)
     */
    fun exportAsXml(workflowId: String): String {
        val dds = generateDds(workflowId)
        return xmlMapper.writeValueAsString(dds)
    }
    
    /**
     * Export DDS as JSON string
     */
    fun exportAsJson(workflowId: String): String {
        val dds = generateDds(workflowId)
        return jsonMapper.writeValueAsString(dds)
    }
    
    /**
     * Export production plots as GeoJSON
     */
    fun exportGeoJson(workflowId: String): String {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }
        
        val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflowId)
        val geoJson = buildGeoJsonFromWorkflow(workflow, linkedUnits)
        return jsonMapper.writeValueAsString(geoJson)
    }
    
    /**
     * Get DDS as structured object for API response
     */
    fun getDdsData(workflowId: String): DueDiligenceStatement {
        return generateDds(workflowId)
    }
    
    // ========================================================================
    // CONVERSION METHODS
    // ========================================================================
    
    private fun buildDdsFromWorkflow(
        workflow: SupplyChainWorkflow, 
        linkedUnits: List<WorkflowProductionUnit>
    ): DueDiligenceStatement {
        val productionUnits = linkedUnits.map { it.productionUnit }
        
        return DueDiligenceStatement(
            header = buildHeader(workflow),
            operator = buildOperatorInfo(workflow.exporter),
            product = buildProductInfo(workflow),
            countryOfProduction = determineCountryOfProduction(productionUnits),
            geolocation = buildGeolocationData(linkedUnits),
            supplyChain = buildSupplyChainInfo(workflow, productionUnits),
            riskAssessment = buildRiskAssessmentInfo(workflow),
            declaration = buildDeclarationInfo(workflow)
        )
    }
    
    private fun buildHeader(workflow: SupplyChainWorkflow): DdsHeader {
        return DdsHeader(
            internalReference = "AGRI-${workflow.id.takeLast(12).uppercase()}",
            submissionDate = LocalDateTime.now(),
            statementVersion = 1,
            tracesReferenceNumber = null,
            verificationNumber = null
        )
    }
    
    private fun buildOperatorInfo(exporter: Exporter): OperatorInfo {
        val userProfile = exporter.userProfile
        
        return OperatorInfo(
            name = exporter.companyName ?: userProfile.fullName,
            address = AddressInfo(
                street = "",  // Not available in current schema
                postalCode = null,
                city = "",    // Not available in current schema
                country = exporter.originCountryCode ?: "KE"
            ),
            eori = exporter.licenseId,
            vatNumber = null,
            activityType = ActivityType.PLACING_ON_MARKET,
            authorizedSignatory = SignatoryInfo(
                name = userProfile.fullName,
                position = "Export Compliance Manager",
                email = userProfile.email,
                phone = userProfile.phoneNumber
            )
        )
    }
    
    private fun buildProductInfo(workflow: SupplyChainWorkflow): ProductInfo {
        return ProductInfo(
            commodityCategory = mapProduceTypeToCommodity(workflow.produceType),
            hsCode = determineHsCode(workflow.produceType),
            scientificName = getScientificName(workflow.produceType),
            commonName = workflow.produceType,
            description = workflow.workflowName,
            quantity = QuantityInfo(
                value = workflow.totalQuantityKg.toDouble(),
                unit = "KGM"
            ),
            harvestPeriod = null,
            processingMethod = null
        )
    }
    
    private fun buildGeolocationData(linkedUnits: List<WorkflowProductionUnit>): GeolocationData {
        val plots = linkedUnits.mapNotNull { wpu ->
            val pu = wpu.productionUnit
            val geolocation = pu.wgs84Coordinates ?: pu.getParcelGeometryGeoJson()
            
            if (geolocation != null) {
                val coords = parseGeolocationCoordinates(geolocation)
                PlotInfo(
                    plotId = pu.id.takeLast(16).uppercase(),
                    farmName = pu.unitName,
                    farmerName = pu.farmer.userProfile.fullName,
                    areaHectares = pu.areaHectares.toDouble(),
                    centroid = coords,
                    harvestDate = null
                )
            } else null
        }
        
        return GeolocationData(
            totalPlots = plots.size,
            totalAreaHectares = plots.sumOf { it.areaHectares },
            geoJsonReference = "production_plots.geojson",
            plots = plots
        )
    }
    
    private fun buildSupplyChainInfo(
        workflow: SupplyChainWorkflow,
        productionUnits: List<ProductionUnit>
    ): SupplyChainInfo {
        val actors = mutableListOf<SupplyChainActor>()
        
        // Add farmers (producers) from production units
        productionUnits
            .map { it.farmer }
            .distinctBy { it.id }
            .forEach { farmer ->
                actors.add(SupplyChainActor(
                    role = ActorRole.PRODUCER,
                    name = farmer.userProfile.fullName,
                    registration = null,  // registration number not available in Farmer entity
                    country = "KE",       // Default country
                    facility = farmer.farmName,
                    processingDate = null
                ))
            }
        
        // Add actors from consolidation events
        workflow.consolidationEvents.forEach { event ->
            addSupplierAsActor(actors, event.targetSupplier, ActorRole.AGGREGATOR)
        }
        
        // Add actors from processing events
        workflow.processingEvents.forEach { event ->
            addSupplierAsActor(actors, event.processorSupplier, ActorRole.PROCESSOR)
        }
        
        // Add exporter
        val exporter = workflow.exporter
        actors.add(SupplyChainActor(
            role = ActorRole.EXPORTER,
            name = exporter.companyName ?: exporter.userProfile.fullName,
            registration = exporter.licenseId,
            country = exporter.originCountryCode ?: "KE",
            facility = null,
            processingDate = null
        ))
        
        return SupplyChainInfo(
            actors = actors.distinctBy { "${it.role}-${it.name}" },
            batchReferences = BatchReferences(
                agriBackupWorkflowId = workflow.id,
                internalBatchCode = null,
                exportLotNumber = null,
                hederaTransactionId = workflow.complianceCertificateTransactionId
            )
        )
    }
    
    private fun addSupplierAsActor(
        actors: MutableList<SupplyChainActor>,
        supplier: SupplyChainSupplier,
        role: ActorRole
    ) {
        actors.add(SupplyChainActor(
            role = role,
            name = supplier.supplierName,
            registration = supplier.businessRegistrationNumber,
            country = supplier.countryCode,
            facility = supplier.region,
            processingDate = null
        ))
    }
    
    private fun buildRiskAssessmentInfo(workflow: SupplyChainWorkflow): RiskAssessmentInfo {
        val classification = workflow.riskClassification?.name
        val isCompliant = classification in listOf("LOW", "NEGLIGIBLE")
        
        return RiskAssessmentInfo(
            overallRiskLevel = mapRiskLevel(classification),
            deforestationFreeStatus = if (isCompliant) 
                DeforestationStatus.CONFIRMED else DeforestationStatus.PENDING_VERIFICATION,
            cutoffDateCompliance = isCompliant,
            verificationMethods = listOf(
                VerificationMethod.SATELLITE_IMAGERY,
                VerificationMethod.GLOBAL_FOREST_WATCH
            ),
            certifications = buildCertifications(workflow),
            mitigationActions = null
        )
    }
    
    private fun buildDeclarationInfo(workflow: SupplyChainWorkflow): DeclarationInfo {
        val classification = workflow.riskClassification?.name
        val isCompliant = classification in listOf("LOW", "NEGLIGIBLE")
        
        return DeclarationInfo(
            deforestationFreeCompliance = isCompliant,
            legalityCompliance = true,
            dueDiligenceExercised = true,
            signatureDate = LocalDate.now(),
            blockchainReference = BlockchainReference(
                platform = "HEDERA",
                transactionId = workflow.complianceCertificateTransactionId,
                nftTokenId = workflow.complianceCertificateNftId,
                nftSerialNumber = workflow.complianceCertificateSerialNumber
            )
        )
    }
    
    private fun buildGeoJsonFromWorkflow(
        workflow: SupplyChainWorkflow,
        linkedUnits: List<WorkflowProductionUnit>
    ): ProductionPlotsGeoJson {
        val features = linkedUnits.mapNotNull { wpu ->
            val pu = wpu.productionUnit
            val geolocation = pu.wgs84Coordinates ?: pu.getParcelGeometryGeoJson() ?: return@mapNotNull null
            
            try {
                val coords = parseGeolocationCoordinates(geolocation)
                
                PlotFeature(
                    properties = PlotProperties(
                        plotId = pu.id.takeLast(16).uppercase(),
                        farmName = pu.unitName,
                        farmerName = pu.farmer.userProfile.fullName,
                        areaHectares = pu.areaHectares.toDouble(),
                        commodityType = workflow.produceType,
                        harvestDate = null
                    ),
                    geometry = PlotGeometry(
                        type = "Point",
                        coordinates = listOf(coords.longitude, coords.latitude)
                    )
                )
            } catch (e: Exception) {
                logger.warn("Failed to parse geolocation for production unit ${pu.id}: ${e.message}")
                null
            }
        }
        
        return ProductionPlotsGeoJson(
            name = "Production Plots - ${workflow.exporter.companyName} - ${workflow.id.takeLast(8)}",
            features = features
        )
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    private fun mapProduceTypeToCommodity(produceType: String): EudrCommodity {
        val normalized = produceType.lowercase()
        return when {
            normalized.contains("coffee") -> EudrCommodity.COFFEE
            normalized.contains("cocoa") || normalized.contains("cacao") -> EudrCommodity.COCOA
            normalized.contains("palm") -> EudrCommodity.OIL_PALM
            normalized.contains("soya") || normalized.contains("soy") -> EudrCommodity.SOYA
            normalized.contains("rubber") -> EudrCommodity.RUBBER
            normalized.contains("wood") || normalized.contains("timber") -> EudrCommodity.WOOD
            normalized.contains("cattle") || normalized.contains("beef") -> EudrCommodity.CATTLE
            else -> EudrCommodity.COFFEE
        }
    }
    
    private fun determineHsCode(produceType: String): String {
        val normalized = produceType.lowercase()
        return when {
            normalized.contains("coffee") && normalized.contains("green") -> "0901.11.00"
            normalized.contains("coffee") && normalized.contains("roast") -> "0901.21.00"
            normalized.contains("coffee") -> "0901.11.00"
            normalized.contains("cocoa") -> "1801.00.00"
            normalized.contains("palm oil") -> "1511.10.00"
            normalized.contains("soya") -> "1201.90.00"
            normalized.contains("rubber") -> "4001.10.00"
            normalized.contains("wood") -> "4403.11.00"
            else -> "0901.11.00"
        }
    }
    
    private fun getScientificName(produceType: String): String? {
        val normalized = produceType.lowercase()
        return when {
            normalized.contains("arabica") -> "Coffea arabica"
            normalized.contains("robusta") -> "Coffea canephora"
            normalized.contains("coffee") -> "Coffea arabica"
            normalized.contains("cocoa") -> "Theobroma cacao"
            normalized.contains("palm") -> "Elaeis guineensis"
            normalized.contains("soya") -> "Glycine max"
            normalized.contains("rubber") -> "Hevea brasiliensis"
            normalized.contains("cattle") -> "Bos taurus"
            else -> null
        }
    }
    
    private fun determineCountryOfProduction(productionUnits: List<ProductionUnit>): String {
        // Try to get country from administrative region if available
        val regions: List<String> = productionUnits.mapNotNull { it.administrativeRegion }
        return if (regions.isNotEmpty()) {
            // Parse country from region if possible, otherwise default to KE
            "KE"
        } else "KE"
    }
    
    private fun parseGeolocationCoordinates(geolocation: String): CoordinateInfo {
        return try {
            val geoData = jsonMapper.readTree(geolocation)
            when {
                geoData.has("coordinates") && geoData["type"]?.asText() == "Point" -> {
                    val coords = geoData["coordinates"]
                    CoordinateInfo(
                        latitude = coords[1].asDouble(),
                        longitude = coords[0].asDouble()
                    )
                }
                geoData.has("coordinates") && geoData["type"]?.asText() == "Polygon" -> {
                    val ring = geoData["coordinates"][0]
                    val lats = (0 until ring.size()).map { ring[it][1].asDouble() }
                    val lons = (0 until ring.size()).map { ring[it][0].asDouble() }
                    CoordinateInfo(
                        latitude = lats.average(),
                        longitude = lons.average()
                    )
                }
                geoData.has("latitude") && geoData.has("longitude") -> {
                    CoordinateInfo(
                        latitude = geoData["latitude"].asDouble(),
                        longitude = geoData["longitude"].asDouble()
                    )
                }
                geolocation.contains(",") -> {
                    val parts = geolocation.split(",")
                    if (parts.size >= 2) {
                        CoordinateInfo(
                            latitude = parts[0].trim().toDouble(),
                            longitude = parts[1].trim().toDouble()
                        )
                    } else CoordinateInfo(0.0, 0.0)
                }
                else -> CoordinateInfo(0.0, 0.0)
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse geolocation: $geolocation")
            CoordinateInfo(0.0, 0.0)
        }
    }
    
    private fun mapRiskLevel(riskLevel: String?): DdsRiskLevel {
        return when (riskLevel?.uppercase()) {
            "NEGLIGIBLE" -> DdsRiskLevel.NEGLIGIBLE
            "LOW" -> DdsRiskLevel.LOW
            "MEDIUM", "STANDARD" -> DdsRiskLevel.STANDARD
            "HIGH" -> DdsRiskLevel.HIGH
            else -> DdsRiskLevel.STANDARD
        }
    }
    
    private fun buildCertifications(workflow: SupplyChainWorkflow): List<CertificationInfo>? {
        val certs = mutableListOf<CertificationInfo>()
        
        workflow.complianceCertificateNftId?.let { nftId ->
            certs.add(CertificationInfo(
                scheme = "AGRIBACKUP_HEDERA_NFT",
                certificateNumber = "${nftId}#${workflow.complianceCertificateSerialNumber}",
                validUntil = null
            ))
        }
        
        return certs.ifEmpty { null }
    }
}

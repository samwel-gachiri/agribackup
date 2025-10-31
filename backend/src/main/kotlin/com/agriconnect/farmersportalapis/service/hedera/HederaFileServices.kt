package com.agriconnect.farmersportalapis.service.hedera

import com.agriconnect.farmersportalapis.domain.eudr.DocumentVisibility
import com.agriconnect.farmersportalapis.domain.eudr.EudrDocument
import com.agriconnect.farmersportalapis.domain.eudr.EudrDocumentType
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrDocumentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedera.hashgraph.sdk.FileCreateTransaction
import com.hedera.hashgraph.sdk.Hbar
import com.hedera.hashgraph.sdk.TransactionRecord
import com.hedera.hashgraph.sdk.TransactionResponse
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime


class HederaFileServices(
    private val hcs: HederaConsensusServices,
    private val eudrDocumentRepository: EudrDocumentRepository,
    private val ipfsDocumentService: IpfsDocumentService,
    private val hederaNetworkInitialization: HederaNetworkInitialization,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(HederaFileServices::class.java)
    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 50MB
    }

    private fun createHederaFile(file: MultipartFile): TransactionRecord? {
        if (file.isEmpty) { throw IllegalArgumentException("File is empty") }
        if (file.size > MAX_FILE_SIZE) { throw IllegalArgumentException("File size exceeds maximum allowed size of ${MAX_FILE_SIZE / 1024 / 1024}MB") }

        //Create the transaction
        val transaction: FileCreateTransaction = FileCreateTransaction()
            .setKeys(hederaNetworkInitialization.getOperatorPrivateKey())
            .setContents(file.bytes)

        //Change the default max transaction fee to 2 hbars
        val modifyMaxTransactionFee = transaction.setMaxTransactionFee(Hbar(2))

        //Prepare transaction for signing, sign with the key on the file, sign with the client operator key and submit to a Hedera network
        val txResponse: TransactionResponse = modifyMaxTransactionFee
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())
                .execute(hederaNetworkInitialization.getClient())

        //Request Record for full details
        return txResponse.getRecord(hederaNetworkInitialization.getClient())
    }

    fun uploadDocument(
        file: MultipartFile,
        documentType: EudrDocumentType,
        ownerEntityId: String,
        ownerEntityType: String,
        uploaderId: String,
        metadata: Map<String, String> = emptyMap()
    ): EudrDocument {

        //Get the file ID
        val record = createHederaFile(file)

        //let us get the fileID
        val fileId = record?.receipt?.fileId

        //get the transaction information
        val transactionId = record?.transactionId
        val transactionHash = record?.transactionHash

        // Create document entity
        val document = EudrDocument(
            id = fileId.toString(),
            ownerEntityId = ownerEntityId,
            ownerEntityType = ownerEntityType,
            batch = null,
            documentType = documentType,
            issuer = null,
            issueDate = null,
            expiryDate = null,
            s3Key = "",
            fileName = file.originalFilename ?: "unknown",
            mimeType = file.contentType ?: "application/octet-stream",
            checksumSha256 = ipfsDocumentService.calculateSHA256(file.bytes),
            fileSize = file.size,
            uploaderId = uploaderId,
            uploadedAt = LocalDateTime.now(),
            exifLatitude = null,
            exifLongitude = null,
            exifTimestamp = null,
            tags = objectMapper.writeValueAsString(metadata),
            retentionUntil = null,
            visibility = DocumentVisibility.PRIVATE,
            hederaTransactionId = transactionId.toString(),
            hederaHashRecord = transactionHash.toString()
        )

        // Save some transaction information to database
        val savedDocument = eudrDocumentRepository.save(document)

        // Record on Hedera DLT
        try {
            val hederaTransactionId = hcs.recordDocumentUpload(savedDocument)
            savedDocument.hederaTransactionId = hederaTransactionId
            eudrDocumentRepository.save(savedDocument)
        } catch (e: Exception) { logger.error(e.message, e)}

        logger.info("Document uploaded successfully: ${savedDocument.id}, Hedera: ${savedDocument.hederaTransactionId}")
        return savedDocument
    }
}
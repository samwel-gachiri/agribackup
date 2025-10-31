package com.agriconnect.farmersportalapis.service.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class S3Service(
    @Value("\${aws.accessKey}") private val accessKey: String,
    @Value("\${aws.secretKey}") private val secretKey: String,
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.bucketName}") private val bucketName: String
) {

    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build()

    fun uploadFile(file: MultipartFile): String {
        val key = "produce-images/${UUID.randomUUID()}-${file.originalFilename}"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.bytes))

        return "https://$bucketName.s3.$region.amazonaws.com/$key"
    }

    fun uploadLicenseDocument(file: MultipartFile): String {
        val key = "license-documents/${UUID.randomUUID()}-${file.originalFilename}"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.bytes))

        return "https://$bucketName.s3.$region.amazonaws.com/$key"
    }

    fun deleteFile(fileUrl: String) {
        val key = fileUrl.substringAfter("amazonaws.com/")

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }
}

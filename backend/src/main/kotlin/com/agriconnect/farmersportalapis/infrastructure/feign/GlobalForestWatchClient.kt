package com.agriconnect.farmersportalapis.infrastructure.feign

import feign.Client
import feign.Request
import feign.Response
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@FeignClient(
    name = "globalForestWatchClient",
    url = "\${gfw.api.base-url:https://data-api.globalforestwatch.org}",
    configuration = [GlobalForestWatchClient.GfwFeignConfig::class]
)
interface GlobalForestWatchClient {

    @PostMapping("/dataset/{datasetId}/{version}/query")
    fun queryDataset(
        @PathVariable datasetId: String,
        @PathVariable version: String,
        @RequestHeader("x-api-key") apiKey: String,
        @RequestBody request: GfwQueryRequest
    ): GfwQueryResponse

    @Configuration
    class GfwFeignConfig {

        @Bean
        fun feignClient(): Client {
            return RedirectFollowingClient()
        }
    }
}

class RedirectFollowingClient : Client {

    override fun execute(request: Request, options: Request.Options): Response {
        var currentUrl = request.url()
        var maxRedirects = 5
        var currentRequest = request

        while (maxRedirects > 0) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection

            try {
                // Configure connection to NOT automatically follow redirects
                connection.instanceFollowRedirects = false
                connection.connectTimeout = options.connectTimeoutMillis()
                connection.readTimeout = options.readTimeoutMillis()
                connection.requestMethod = request.httpMethod().name

                // Set headers
                request.headers().forEach { (name, values) ->
                    values.forEach { value ->
                        connection.setRequestProperty(name, value)
                    }
                }

                // Set request body for POST requests
                if (request.body() != null) {
                    connection.doOutput = true
                    connection.outputStream.use { outputStream ->
                        outputStream.write(request.body())
                    }
                }

                val statusCode = connection.responseCode

                // Check if response is a redirect (3xx status codes)
                if (statusCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (location != null) {
                        // Resolve relative URLs against the current URL
                        val resolvedUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                            location
                        } else {
                            // Handle relative URLs
                            URL(URL(currentUrl), location).toString()
                        }
                        
                        // Update URL for next iteration
                        currentUrl = resolvedUrl

                        // Create new request with the redirect location
                        currentRequest = Request.create(
                            request.httpMethod(),
                            resolvedUrl,
                            request.headers(),
                            request.body(),
                            request.charset(),
                            request.requestTemplate()
                        )

                        maxRedirects--
                        continue
                    }
                }

                // Not a redirect or no location header, return the response
                return convertResponse(connection, currentRequest)

            } catch (e: Exception) {
                throw IOException("Failed to execute request to $currentUrl", e)
            }
        }

        throw IOException("Too many redirects (>5) when calling $currentUrl")
    }

    private fun convertResponse(connection: HttpURLConnection, request: Request): Response {
        val status = connection.responseCode
        val reason = connection.responseMessage ?: ""

        val headers = mutableMapOf<String, Collection<String>>()
        var i = 0
        while (true) {
            val name = connection.getHeaderFieldKey(i) ?: break
            val value = connection.getHeaderField(i)
            if (name != null) {
                headers.computeIfAbsent(name) { mutableListOf() }.let {
                    (it as MutableList).add(value)
                }
            }
            i++
        }

        val body = try {
            if (status >= 400) {
                connection.errorStream?.readBytes()
            } else {
                connection.inputStream?.readBytes()
            }
        } catch (e: IOException) {
            ByteArray(0)
        }

        return Response.builder()
            .status(status)
            .reason(reason)
            .headers(headers)
            .body(body)
            .request(request)
            .build()
    }
}

data class GfwQueryRequest(
    val sql: String
)

data class GfwQueryResponse(
    val data: List<Map<String, Any>>?,
    val status: String? = null,
    val message: String? = null
)

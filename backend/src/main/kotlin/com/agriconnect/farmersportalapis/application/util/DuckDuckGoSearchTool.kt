package com.agriconnect.farmersportalapis.application.util

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class DuckDuckGoSearchTool {

    private val webClient = WebClient.create("https://api.duckduckgo.com")

    @Tool(name = "webSearch", description = "Perform a web search using DuckDuckGo")
    fun search(
        @ToolParam(description = "Search query") query: String,
        @ToolParam(description = "Number of results") limit: Int = 3
    ): Mono<String> {
        return webClient.get()
            .uri {
                it.queryParam("q", query)
                  .queryParam("format", "json")
                  .queryParam("no_html", "1")
                  .queryParam("no_redirect", "1")
                  .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
    }
}
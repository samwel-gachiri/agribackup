package com.agriconnect.farmersportalapis.buyers.controller

import com.agriconnect.farmersportalapis.buyers.application.dtos.AddOrderToRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.RequestResponseDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.requestAProduceRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.updateRequestRequestDto
import com.agriconnect.farmersportalapis.buyers.application.services.impl.RequestService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.domain.request.ProduceRequest
import com.agriconnect.farmersportalapis.buyers.domain.request.RequestOrder
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap


@RestController
@RequestMapping(path = ["/buyers-service/request"])
@Tag(name = "Requests Of Produce", description = "Manages request of produces")
class ProduceRequestController {
    @Autowired
    lateinit var requestService: RequestService

    @Operation(
        summary = "Gets all of the requests"
    )
    @GetMapping("/list")
    fun getRequests() = requestService.getRequests();

    @Operation(
        summary = "Gets out a specific request",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getRequest(
        @Parameter(description = "Id of request")
        @RequestParam("requestId", defaultValue = "") requestId: String,
    ): Result<RequestResponseDto> {
        return requestService.getRequest(requestId)
    }

    @Operation(
        summary = "Gets out requests for a specific buyer"
    )
    @GetMapping("/buyer")
    fun getRequestByBuyer(
        @Parameter(description = "Id of buyer")
        @RequestParam("buyerId", defaultValue = "") buyerId: String,
        pageable: Pageable
    ): Result<Page<ProduceRequest>> {
        return requestService.getBuyerRequests(buyerId, pageable)
    }
    @Operation(
        summary = "Lists a produce from a buyer"
    )
    @PostMapping
    fun requestAProduce(
        @RequestBody requestAProduceRequestDto: requestAProduceRequestDto
    ) = requestService.requestAProduce(requestAProduceRequestDto)

    @Operation(
        summary = "Updates a request"
    )
    @PutMapping
    fun updateRequest(
        @RequestBody updateRequestRequestDto: updateRequestRequestDto
    ) = requestService.updateRequest(updateRequestRequestDto)

    @Operation(
        summary = "Adds order to a request"
    )
    @PostMapping("/order")
    fun addOrderToRequest(
        @RequestBody addOrderToRequestDto: AddOrderToRequestDto
    ): Result<ProduceRequest> {
        val res = requestService.addOrderToRequest(addOrderToRequestDto)
        emitEvent(addOrderToRequestDto.requestId, "order-added-to-request")
        return res
    }

    @Operation(
        summary = "Accept order from buyer"
    )
    @PutMapping("/order/accept")
    fun acceptOrder(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @RequestParam requestId: String,
    ): Result<RequestOrder> {
        val res = requestService.acceptOrder(orderId)
        emitEvent(requestId = requestId, name = "order-accepted")
        return res
    }
    @Operation(
        summary = "Confirm produce supplied [buyer]"
    )
    @PutMapping("/order/confirm-supply")
    fun confirmSupply(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @RequestParam requestId: String,
    ): Result<RequestOrder> {
        val res = requestService.confirmSupply(orderId)
        emitEvent(requestId = requestId, name = "supply-confirmed")
        return res
    }
    @Operation(
        summary = "Confirm payment done to a buyer"
    )
    @PutMapping("/order/confirm-payment")
    fun confirmPayment(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @RequestParam requestId: String,
    ): Result<RequestOrder> {
        val res = requestService.confirmPayment(orderId)
        emitEvent(requestId = requestId, name = "payment-confirmed", orderId);
        return res
    }
    fun emitEvent(requestId: String, name: String, data: String = "") {
        val emitter = emitters[requestId]
        if (emitter != null) {
            val event = SseEmitter.event()
                .name(name)
                .data(data)
            try {
                emitter.send(event)
            } catch (e: Exception) {
                emitter.completeWithError(e)
            }
        }
    }

    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val logger = LoggerFactory.getLogger(ProduceRequestController::class.java)

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @RequestParam requestId: String
    ): SseEmitter {
        logger.info("Setting emitter for requestId: {}", requestId)
        val emitter = SseEmitter(Long.MAX_VALUE)

        // registering the event
        emitters[requestId] = emitter

        emitter.onCompletion {
            logger.info("Emitter for requestId {} completed", requestId)
            emitters.remove(requestId)
        }

        emitter.onTimeout {
            logger.info("Emitter for requestId {} timed out", requestId)
            emitters.remove(requestId)
        }

        logger.info("Emitter for requestId {} set!", requestId)
        return emitter
    }

}
package com.agriconnect.farmersportalapis.buyers.application.services

import com.agriconnect.farmersportalapis.buyers.application.dtos.AddOrderToRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.RequestResponseDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.requestAProduceRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.updateRequestRequestDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.domain.request.RequestOrder
import com.agriconnect.farmersportalapis.buyers.domain.request.ProduceRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IRequestService {
    fun getRequests(): Result<List<ProduceRequest>>

    fun requestAProduce(requestAProduceRequestDto: requestAProduceRequestDto): Result<ProduceRequest>

    fun updateRequest(updateRequestRequestDto: updateRequestRequestDto): Result<ProduceRequest>


    fun unrequest(produceRequestId: String): Result<String>
    fun getRequest(requestId: String): Result<RequestResponseDto>
    fun getBuyerRequests(buyerId: String, pageable: Pageable): Result<Page<ProduceRequest>>
    fun addOrderToRequest(addOrderToRequestDto: AddOrderToRequestDto): Result<ProduceRequest>
    fun acceptOrder(orderId: String): Result<RequestOrder>
    fun confirmSupply(orderId: String): Result<RequestOrder>
    fun confirmPayment(orderId: String): Result<RequestOrder>
}
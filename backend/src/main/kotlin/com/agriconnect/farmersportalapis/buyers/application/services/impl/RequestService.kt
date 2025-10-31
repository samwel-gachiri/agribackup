package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.AddOrderToRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.RequestResponseDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.requestAProduceRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.updateRequestRequestDto
import com.agriconnect.farmersportalapis.buyers.application.services.IRequestService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.BuyerProduceStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.ProduceRequestStatus
import com.agriconnect.farmersportalapis.buyers.domain.request.ProduceRequest
import com.agriconnect.farmersportalapis.buyers.domain.request.RequestOrder
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.PreferredProduceRepository
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.ProduceRequestRepository
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.RequestOrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class RequestService: IRequestService {
    @Autowired
    lateinit var produceRequestRepository: ProduceRequestRepository

    @Autowired
    lateinit var preferredProduceRepository: PreferredProduceRepository

    @Autowired
    lateinit var requestOrderRepository: RequestOrderRepository

    val orderNotFound = "Order not found"

    val produceRequestNotFound = "Request not found."
    override fun getRequests(): Result<List<ProduceRequest>> {
        return try {
            val produceRequests = produceRequestRepository.findAll()
            ResultFactory.getSuccessResult(produceRequests);
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun requestAProduce(requestAProduceRequestDto: requestAProduceRequestDto): Result<ProduceRequest> {
        return try {
            val buyerProduce = preferredProduceRepository.findByIdOrNull(requestAProduceRequestDto.buyerProduceId)
                ?: return ResultFactory.getFailResult(msg = "Buyer produce not found.")
            // println("The buyer produce is: "+buyerProduce.id+" buyer: "+buyerProduce.buyer.userProfile?.fullName)
            // println(buyerProduce)
            val savedProduceRequest = produceRequestRepository.saveAndFlush(
                ProduceRequest(
                    id = UUID.randomUUID().toString(),
                    preferredProduce = buyerProduce,
                    price = requestAProduceRequestDto.price,
                    quantity = requestAProduceRequestDto.quantity,
                    unit = requestAProduceRequestDto.unit,
                    status = ProduceRequestStatus.ACTIVE,
                    dateCreated = LocalDateTime.now(),
                    rating = 0.00,
                )
            )
            buyerProduce.status = BuyerProduceStatus.REQUESTING
            preferredProduceRepository.saveAndFlush(buyerProduce)
            ResultFactory.getSuccessResult(savedProduceRequest)
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun updateRequest(updateRequestRequestDto: updateRequestRequestDto): Result<ProduceRequest> {
        TODO("Not yet implemented")
    }

    override fun addOrderToRequest(addOrderToRequestDto: AddOrderToRequestDto): Result<ProduceRequest> {
        try {
            if (addOrderToRequestDto.quantity <= 0) {
                return ResultFactory.getFailResult(msg = "Please input the correct quantity")
            }

            val produceRequest = produceRequestRepository.findByIdOrNull(addOrderToRequestDto.requestId)
                ?: return ResultFactory.getFailResult(msg = produceRequestNotFound)

            if (produceRequest.quantity < addOrderToRequestDto.quantity) {
                return ResultFactory.getFailResult(msg = "You have submitted more quantity than requested")
            }

            // Ensure produceRequest is managed
            val managedRequest = produceRequestRepository.saveAndFlush(produceRequest)

            // Create and persist the new order
            val newOrder = requestOrderRepository.save(
                RequestOrder(
                    farmerId = addOrderToRequestDto.farmerId,
                    quantity = addOrderToRequestDto.quantity,
                    dateCreated = LocalDateTime.now(),
                    status = OrderStatus.PENDING_ACCEPTANCE,
                    produceRequest = managedRequest
                )
            )

            managedRequest.requestOrders.add(newOrder)

            val quantityOrdered = managedRequest.requestOrders.sumOf { it.quantity }
            if (managedRequest.quantity == quantityOrdered) {
                managedRequest.preferredProduce.status = BuyerProduceStatus.ACTIVE
                managedRequest.status = ProduceRequestStatus.CLOSED
                preferredProduceRepository.saveAndFlush(managedRequest.preferredProduce)
            }

            return ResultFactory.getSuccessResult(produceRequestRepository.saveAndFlush(managedRequest))
        } catch (e: Exception) {
            return ResultFactory.getFailResult(e.message)
        }
    }


    override fun acceptOrder(orderId: String): Result<RequestOrder> {
        return try {
            val order = requestOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.dateAccepted = LocalDateTime.now()
            order.status = OrderStatus.BOOKED_FOR_SUPPLY
            ResultFactory.getSuccessResult(requestOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }
    override fun confirmSupply(orderId: String): Result<RequestOrder> {
        return try {
            val order = requestOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.dateSupplied = LocalDateTime.now()
            order.status = OrderStatus.SUPPLIED
            ResultFactory.getSuccessResult(requestOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun confirmPayment(orderId: String): Result<RequestOrder> {
        return try {
            val order = requestOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.datePaid = LocalDateTime.now()
            order.status = OrderStatus.SUPPLIED_AND_PAID
            ResultFactory.getSuccessResult(requestOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun unrequest(produceRequestId: String): Result<String> {
        return try {
            val produceRequest = produceRequestRepository.findByIdOrNull(produceRequestId)
                ?: return ResultFactory.getFailResult(msg = produceRequestNotFound)
            produceRequest.status = ProduceRequestStatus.CANCELLED
            ResultFactory.getSuccessResult(data="Unrequested")
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun getRequest(requestId: String): Result<RequestResponseDto> {
        return try {
            val produceRequest = produceRequestRepository.findByIdOrNull(requestId)
                ?: return ResultFactory.getFailResult(msg = produceRequestNotFound)
            val requestResponseDto: RequestResponseDto =
                RequestResponseDto(
                    produceRequest = produceRequest,
                    quantityLeft = produceRequest.quantity,
                    quantitySold = 0.00,
                    earnings = 0.00,
                    noOfPurchases = produceRequest.requestOrders.size
                )

            produceRequest.requestOrders.forEach { requestOrder: RequestOrder ->
                run {
                    requestResponseDto.quantitySold += requestOrder.quantity
                    requestResponseDto.quantityLeft -= requestOrder.quantity
                    requestResponseDto.earnings += (requestOrder.quantity * produceRequest.price.price)
                }
            }
            ResultFactory.getSuccessResult(requestResponseDto)
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun getBuyerRequests(buyerId: String, pageable: Pageable): Result<Page<ProduceRequest>> {
        return try {
            ResultFactory.getSuccessResult(produceRequestRepository.getBuyerRequests(buyerId, pageable))
        }catch (e: Exception) {
            ResultFactory.getFailResult(msg = e.message)
        }
    }
}
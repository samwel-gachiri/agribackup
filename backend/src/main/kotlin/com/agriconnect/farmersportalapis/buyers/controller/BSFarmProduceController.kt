package com.agriconnect.farmersportalapis.buyers.controller

import com.agriconnect.farmersportalapis.buyers.application.dtos.createFarmProduceDto
import com.agriconnect.farmersportalapis.buyers.application.services.impl.BSFarmProduceService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/buyers-service/produce")
@Tag(name = "Produce management", description="APIS for creating, updating, and deleting farm produces")
class BSFarmProduceController {
    @Autowired
    lateinit var BSFarmProduceService: BSFarmProduceService

    @GetMapping
    fun getFarmProduces() = BSFarmProduceService.getFarmProduces()

    @PostMapping
    fun createFarmProduce(
        @Valid @RequestBody createFarmProduceDto: createFarmProduceDto,
    ) = BSFarmProduceService.createFarmProduce(createFarmProduceDto)

    @DeleteMapping(path = ["/{farmProduceId}"])
    fun deleteFarmProduce(
        @PathVariable farmProduceId: String,
    ) = BSFarmProduceService.deleteFarmProduce(farmProduceId)

}
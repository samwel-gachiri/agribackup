package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.ProduceSearchRequest
import com.agriconnect.farmersportalapis.application.dtos.ProduceSearchResponse
import com.agriconnect.farmersportalapis.application.dtos.createFarmProduceDto
import com.agriconnect.farmersportalapis.service.common.impl.FarmProduceService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/farmers-service/produce")
@Tag(name = "Produce management", description="APIS for creating, updating, and deleting farm produces")
class FarmProduceController {
    @Autowired
    lateinit var farmProduceService: FarmProduceService

    @GetMapping
    fun getFarmProduces() = farmProduceService.getFarmProduces()

    @PostMapping
    fun createFarmProduce(
        @Valid @RequestBody createFarmProduceDto: createFarmProduceDto,
    ) = farmProduceService.createFarmProduce(createFarmProduceDto)

    @DeleteMapping(path = ["/{farmProduceId}"])
    fun deleteFarmProduce(
        @PathVariable farmProduceId: String,
    ) = farmProduceService.deleteFarmProduce(farmProduceId)

    @PostMapping("/search")
    @Operation(
        summary = "Search for produce",
        description = """
        Search for produce using various criteria:
        - By name or description
        - By location (requires latitude, longitude, and optionally maxDistance in km)
        Returns a list of produces with their farmers and locations
        """
    )
    fun searchProduce(
        @RequestBody request: ProduceSearchRequest
    ): Result<List<ProduceSearchResponse>> {
        return ResultFactory.getSuccessResult(data=farmProduceService.searchProduce(request))
    }


}
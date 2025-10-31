package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.ConnectionService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.common.model.FarmerBuyerConnection
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/connection-service")
@Tag(name = "Connection Service", description = "APIs for controlling relationship between buyers and sellers")
class ConnectionController(
    @Autowired val connectionService: ConnectionService
) {
    @PostMapping("/connect")
    @Operation(summary = "Connects a farmer a buyer", description = "It needs only their ids")
    fun connect(@RequestParam farmerId: String, @RequestParam buyerId: String): Result<FarmerBuyerConnection> {
       return connectionService.connectFarmerToBuyer(farmerId, buyerId)
    }

    @DeleteMapping("/disconnect")
    @Operation(summary = "Removes the connection between a farmer and a buyer", description = "It needs only their IDs to perform the operation")
    fun disconnect(@RequestParam farmerId: String, @RequestParam buyerId: String): Result<String> {
        return connectionService.disconnectFarmerAndBuyer(farmerId, buyerId)
    }

    @GetMapping("/farmer-connections/{farmerId}")
    @Operation(summary = "Retrieves the buyers for a specific farmer", description = "Give in the farmer's Id that you want to get connected buyers")
    fun getBuyersForFarmer(@PathVariable farmerId: String): Result<List<FarmerBuyerConnection>> {
        return connectionService.getBuyersForFarmer(farmerId)
    }

    @GetMapping("/buyer-connections/{buyerId}")
    @Operation(summary = "Retrieves the farmers connected to a particular buyer")
    fun getFarmersForBuyer(@PathVariable buyerId: String): Result<List<FarmerBuyerConnection>> {
        return connectionService.getFarmersForBuyer(buyerId)
    }
}
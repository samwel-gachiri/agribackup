package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/farmers-service/user")
class UserController {
    @Autowired
    lateinit var userService: UserService

//    @Operation(
//            summary = "Check farmer or buyer with phone number"
//    )
//    @GetMapping("/check-existence")
//    fun getUserExistence(
//            @Parameter(description = "Phone number")
//            @RequestParam("phoneNumber", defaultValue = "123456789") phoneNumber: String,
//            @Parameter(description = "email")
//            @RequestParam("email", defaultValue = "asdfghgfdsa") email: String,
//    ): Result<ExistFarmerOrBuyerDTO> {
//        return userService.getExistence(phoneNumber, email)
//    }
}
package com.agriconnect.farmersportalapis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableAsync

@EnableFeignClients
@EnableAsync
@SpringBootApplication
class FarmersPortalApisApplication

fun main(args: Array<String>) {
	runApplication<FarmersPortalApisApplication>(*args)
}

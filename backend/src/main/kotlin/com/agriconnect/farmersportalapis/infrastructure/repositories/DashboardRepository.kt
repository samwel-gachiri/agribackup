package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DashboardListingRepository: JpaRepository<ProduceListing, String> {

    @Query(value = "SELECT count(*) FROM produce_listings pl " +
            "JOIN farmers_service_db.farmer_produces fp ON pl.farmer_produces_id = fp.id " +
            "WHERE pl.status IN :stages AND fp.farmer_id = :farmerId", nativeQuery = true)
    fun getProduceListingStageCount(@Param("stages") stages: Array<String>, @Param("farmerId") farmerId: String?): Int

}

@Repository
interface DashboardListingOrderRepository: JpaRepository<ListingOrder, String> {

    @Query(value = "SELECT count(*) FROM ListingOrder lo WHERE lo.produceListing.farmerProduce.farmer.id = :farmerId")
    fun getBuyersInteractions(@Param("farmerId") farmerId: String): Int

    @Query(value = "SELECT new com.agriconnect.farmersportalapis.domain.common.valueobject.Money(COALESCE(sum(lo.quantity * lo.produceListing.price.price), 0.0), lo.produceListing.price.currency) FROM ListingOrder lo WHERE lo.status = :status AND lo.produceListing.farmerProduce.farmer.id = :farmerId AND lo.dateCreated > :dateCreated group by lo.produceListing.price.currency")
    fun getRevenue(@Param("farmerId") farmerId: String, @Param("status") status: OrderStatus = OrderStatus.SUPPLIED_AND_PAID, @Param("dateCreated") dateCreated: LocalDateTime = LocalDateTime.MIN): Money

    @Query(
        """
        SELECT 
            DATE_FORMAT(lo.dateSupplied, '%Y-%m'),
            fp.farmProduce.name,
            SUM(lo.quantity),
            SUM(lo.quantity * pl.price.price)
        FROM ListingOrder lo
        JOIN lo.produceListing pl
        JOIN pl.farmerProduce fp
        WHERE lo.status = :orderStatus
        AND fp.farmer.id = :farmerId
        GROUP BY DATE_FORMAT(lo.dateSupplied, '%Y-%m'), fp.farmProduce.name
        ORDER BY DATE_FORMAT(lo.dateSupplied, '%Y-%m')
        """
    )
    fun getSalesReport(@Param("farmerId") farmerId: String, @Param("orderStatus") orderStatus: OrderStatus? = OrderStatus.SUPPLIED_AND_PAID): List<Any>
}

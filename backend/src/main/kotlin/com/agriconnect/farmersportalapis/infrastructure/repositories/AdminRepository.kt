package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce
import com.agriconnect.farmersportalapis.domain.profile.SystemAdmin
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminRepository : CrudRepository<FarmProduce, String> {
    @Query("""
        SELECT fp.name, COALESCE(SUM(lo.quantity * pl.price), 0) AS total_sales
        FROM farm_produces fp
        JOIN farmer_produces fpr ON fp.farm_produce_id = fpr.farm_produce_id
        JOIN produce_listings pl ON fpr.id = pl.farmer_produces_id
        LEFT JOIN listing_orders lo ON pl.produce_listing_id = lo.produce_listing_id
        GROUP BY fp.name
    """, nativeQuery = true)
    fun getProduceSales(): List<Array<Any>>

    @Query("""
        SELECT DATE(pl.created_at) AS listing_date, 
               COUNT(DISTINCT pl.produce_listing_id) AS total_listings, 
               COUNT(lo.order_id) AS total_orders
        FROM produce_listings pl
        LEFT JOIN listing_orders lo ON pl.produce_listing_id = lo.produce_listing_id
        GROUP BY DATE(pl.created_at)
        ORDER BY listing_date ASC
    """, nativeQuery = true)
    fun getDailyListingsAndOrders(): List<Array<Any>>

    @Query("""
        SELECT 
            COUNT(DISTINCT pl.produce_listing_id) AS totalListings,
            COUNT(lo.order_id) AS totalOrders,
            COUNT(CASE WHEN lo.status = 'PENDING_ACCEPTANCE' THEN 1 END) AS totalPendingOrders,
            COUNT(CASE WHEN lo.status = 'BOOKED_FOR_SUPPLY' THEN 1 END) AS totalBookedOrders,
            COUNT(CASE WHEN lo.status = 'DECLINED' THEN 1 END) AS totalDeclinedOrders,
            COUNT(CASE WHEN lo.status = 'SUPPLIED' THEN 1 END) AS totalSuppliedOrders,
            COUNT(CASE WHEN lo.status = 'SUPPLIED_AND_PAID' THEN 1 END) AS totalSuppliedAndPaidOrders,
            COALESCE(SUM(CASE WHEN lo.status = 'SUPPLIED_AND_PAID' THEN lo.quantity * pl.price END), 0) AS totalTransactionAmount
        FROM produce_listings pl
        LEFT JOIN listing_orders lo ON pl.produce_listing_id = lo.produce_listing_id
    """, nativeQuery = true)
    fun getOrderReport(): Array<Any>

    @Query("""
        SELECT DATE(combined.created_at) AS sign_in_date, 
               COUNT(DISTINCT CASE WHEN combined.user_type = 'farmer' THEN combined.id END) AS farmers_signed_in, 
               COUNT(DISTINCT CASE WHEN combined.user_type = 'buyer' THEN combined.id END) AS buyers_signed_in
        FROM (
            SELECT created_at, farmer_id AS id, 'farmer' AS user_type FROM farmers
            UNION ALL
            SELECT created_at, buyer_id AS id, 'buyer' AS user_type FROM buyers
        ) AS combined
        GROUP BY sign_in_date
        ORDER BY sign_in_date DESC
    """, nativeQuery = true)
    fun getDailySignIns(): List<Array<Any>>

    @Query("SELECT COUNT(*) FROM farmers", nativeQuery = true)
    fun getTotalFarmers(): Int

    @Query("SELECT COUNT(*) FROM buyers", nativeQuery = true)
    fun getTotalBuyers(): Int

    @Query("SELECT sa FROM Admin sa")
    fun findAllAdmins(): List<SystemAdmin>
}
package com.msnanda515.stockflow.repository

import com.msnanda515.stockflow.model.Pallet
import com.msnanda515.stockflow.model.Warehouse
import org.springframework.data.mongodb.repository.MongoRepository

interface WarehouseRepository : MongoRepository<Warehouse, String> {
//    fun findByWareNo(wareNo: Long): List<Warehouse>
    fun findByWareNo(wareNo: Long): List<Warehouse>

    /**
     * Finds the warehouse with the maximum wareNo
     */
    fun findTopByOrderByWareNoDesc(): Warehouse?


}
package com.msnanda515.stockflow.repository

import com.msnanda515.stockflow.model.Warehouse
import org.springframework.data.mongodb.repository.MongoRepository

interface WarehouseRepository : MongoRepository<Warehouse, String> {
}
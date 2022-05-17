package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.exception.AlreadyExistsException
import com.msnanda515.stockflow.model.Warehouse
import com.msnanda515.stockflow.model.WarehouseVM
import com.msnanda515.stockflow.repository.WarehouseRepository
import org.springframework.stereotype.Service

@Service
class WarehouseService(val warehouseRepository: WarehouseRepository) {
    /**
     * Creates and Persists a warehouse in the database
     * @param wareVm the view model of the warehouse to create
     */
    fun createWarehouse(wareVm: WarehouseVM) {
        // only store the warehouse if warehouse No is unique
        val wareExists = warehouseRepository.findByWareNo(wareVm.wareNo).isNotEmpty()
        if (wareExists) {
            throw AlreadyExistsException("Warehouse with id ${wareVm.wareNo} already exists")
        }
        val ware = Warehouse.createWarehouse(wareVm)
        warehouseRepository.save(ware)
    }

    /**
     * Gets all the warehouses
     */
    fun getAllWarehouses(): List<Warehouse> {
        return warehouseRepository.findAll()
    }

    fun getWarehouse(wareNo: Long) {

    }
}
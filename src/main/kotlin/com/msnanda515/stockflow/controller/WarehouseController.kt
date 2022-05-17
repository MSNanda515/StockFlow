package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.Warehouse
import com.msnanda515.stockflow.model.WarehouseVM
import com.msnanda515.stockflow.service.WarehouseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/warehouses")
class WarehouseController(
    val warehouseService: WarehouseService
) {

    /**
     * Gets all the warehouses
     */
    @GetMapping()
    fun getAllWarehouses(): ResponseEntity<List<Warehouse>> {
        return ResponseEntity.ok(warehouseService.getAllWarehouses())
    }

    /**
     * Create a warehouse
     */
    @PostMapping(
        consumes = ["application/json"],
        path = ["/create"]
    )
    fun createWarehouse(@RequestBody wareVm: WarehouseVM):
            ResponseEntity<WarehouseVM> {
        warehouseService.createWarehouse(wareVm)
        return ResponseEntity.ok(wareVm)
    }
}